/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.dot11.networks;

import app.nzyme.core.NzymeNode;
import com.codahale.metrics.Gauge;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.Dot11TaggedParameters;
import app.nzyme.core.dot11.frames.*;
import app.nzyme.core.dot11.networks.beaconrate.BeaconRateManager;
import app.nzyme.core.dot11.networks.signalstrength.SignalStrengthTable;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Networks {

    private static final Logger LOG = LogManager.getLogger(Networks.class);

    private final Map<String, BSSID> bssids;

    private final NzymeNode nzyme;

    private final BeaconRateManager beaconRateManager;

    public Networks(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.bssids = Maps.newConcurrentMap();
        this.beaconRateManager = new BeaconRateManager(nzyme);
    }

    public void registerBeaconFrame(Dot11BeaconFrame frame) {
        if (nzyme.getIgnoredFingerprints().contains(frame.transmitterFingerprint())) {
            LOG.trace("Not registering ignored fingerprint [{}]", frame.transmitterFingerprint());
            return;
        }

        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(Dot11FrameSubtype.BEACON, frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getAntennaSignal());
        }
    }

    public void registerProbeResponseFrame(Dot11ProbeResponseFrame frame) {
        if (nzyme.getIgnoredFingerprints().contains(frame.transmitterFingerprint())) {
            LOG.trace("Not registering ignored fingerprint [{}]", frame.transmitterFingerprint());
            return;
        }

        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(Dot11FrameSubtype.PROBE_RESPONSE, frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getAntennaSignal());
        }
    }

    private void register(byte subtype,
                          String transmitter,
                          String transmitterFingerprint,
                          Dot11TaggedParameters taggedParameters,
                          String ssidName,
                          int channelNumber,
                          int antennaSignal) {
        // Ensure that the BSSID exists in the map.
        BSSID bssid;
        if (bssids.containsKey(transmitter)) {
            bssid = bssids.get(transmitter);

            // Ensure that the SSID has been recorded for this BSSID.
            if (!bssid.ssids().containsKey(ssidName)) {
                bssid.ssids().put(ssidName, SSID.create(ssidName, bssid.bssid(), beaconRateManager));
            }
        } else {
            // First time we are seeing this BSSID.
            String oui = nzyme.getOUIManager().lookupBSSID(transmitter);

            if (oui == null) {
                oui = "unknown";
            }

            SSID ssid = SSID.create(ssidName, transmitter, beaconRateManager);
            bssid = BSSID.create(new HashMap<String, SSID>() {{
                put(ssidName, ssid);
            }}, oui, transmitter);

            bssids.put(transmitter, bssid);
        }

        // Update 'last seen'.
        bssid.updateLastSeen();

        // Update properties that could change during the lifetime of this BSSID.
        bssid.updateIsWPS(taggedParameters.isWPS());

        // Find our SSID.
        SSID ssid = bssid.ssids().get(ssidName);
        ssid.updateSecurity(taggedParameters.getSecurityConfiguration());

        // Update beacon counter.
        if (subtype == Dot11FrameSubtype.BEACON) {
            // Used for beacon rate calculation.
            ssid.beaconCount.incrementAndGet();
        }

        DateTime now = DateTime.now();
        try {
            // Create or update channel.
            if (ssid.channels().containsKey(channelNumber)) {
                // Update channel statistics.
                Channel channel = ssid.channels().get(channelNumber);
                channel.totalFrames().incrementAndGet();
                channel.totalFramesRecent().incrementAndGet();

                // Add fingerprint.
                if (transmitterFingerprint != null) {
                    channel.registerFingerprint(transmitterFingerprint);
                }

                // Record signal strength.
                channel.signalStrengthTable().recordSignalStrength(
                        SignalStrengthTable.SignalStrength.create(
                                now,
                                antennaSignal
                        )
                );

                ssid.channels().replace(channelNumber, channel);
            } else {
                // Create new channel.
                Channel channel = Channel.create(
                        nzyme,
                        channelNumber,
                        bssid.bssid(),
                        ssid.name(),
                        new AtomicLong(1),
                        new AtomicLong(1),
                        transmitterFingerprint
                );

                // Record signal strength.
                channel.signalStrengthTable().recordSignalStrength(
                        SignalStrengthTable.SignalStrength.create(now, antennaSignal)
                );

                ssid.channels().put(channelNumber, channel);
            }
        } catch (NullPointerException e) {
            LOG.error(ssid);
            throw e;
        }
    }

    public Map<String, BSSID> getBSSIDs() {
        return new ImmutableMap.Builder<String, BSSID>().putAll(bssids).build();
    }

    // NOTE: This is just a list of the SSIDs and is not to be confused with SSIDs per BSSID. Multiple SSIDs are swallowed.
    public Set<String> getSSIDs() {
        Set<String> ssids = Sets.newHashSet();

        for (BSSID bssid : bssids.values()) {
            for (String ssid : bssid.ssids().keySet()) {
                if (!ssids.contains(ssid)) {
                    ssids.add(ssid);
                }
            }
        }

        return new ImmutableSet.Builder<String>().addAll(ssids).build();
    }

    @Nullable
    public Channel findChannel(String bssidMac, String ssidName, int channelNumber) {
        for (BSSID bssid : bssids.values()) {
            if (!bssid.bssid().equals(bssidMac)) {
                continue;
            }

            for (SSID ssid : bssid.ssids().values()) {
                if (!ssid.name().equals(ssidName)) {
                    continue;
                }

                for (Channel channel : ssid.channels().values()) {
                    if (channel.channelNumber() == channelNumber) {
                        return channel;
                    }
                }
            }
        }

        return null;
    }

    public void retentionClean(int seconds) {
        try {
            for (Map.Entry<String, BSSID> entry : Lists.newArrayList(bssids.entrySet())) {
                BSSID bssid = entry.getValue();

                if (bssid.getLastSeen().isBefore(DateTime.now().minusSeconds(seconds))) {
                    LOG.debug("Retention cleaning expired BSSID [{}] from internal networks list.", bssid.bssid());
                    bssids.remove(entry.getKey());
                }
            }
        } catch(Exception e) {
            LOG.error("Error when trying to clean expired BSSIDs.", e);
        }
    }

    public void reset() {
        this.bssids.clear();
    }

    public static class NoSuchNetworkException extends Exception {
    }

    public static class NoSuchChannelException extends Exception {
    }
}
