package app.nzyme.core.dot11.monitoring;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.detection.alerts.DetectionType;
import app.nzyme.core.detection.alerts.Subsystem;
import app.nzyme.core.dot11.db.ChannelHistogramEntry;
import app.nzyme.core.dot11.db.monitoring.MonitoredBSSID;
import app.nzyme.core.dot11.db.monitoring.MonitoredChannel;
import app.nzyme.core.dot11.db.monitoring.MonitoredSSID;
import app.nzyme.core.dot11.tracks.Track;
import app.nzyme.core.dot11.tracks.TrackDetector;
import app.nzyme.core.periodicals.Periodical;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Dot11SignalTrackMonitor extends Periodical {

    private static final Logger LOG = LogManager.getLogger(Dot11SignalTrackMonitor.class);

    private final NzymeNode nzyme;

    public Dot11SignalTrackMonitor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    protected void execute() {
        LOG.debug("Starting 802.11 signal track monitor run.");

        for (MonitoredSSID monitoredSSID : nzyme.getDot11().findAllMonitoredSSIDs(null, null)) {
            if (!monitoredSSID.isEnabled()) {
                continue;
            }
            
            List<UUID> tapUUIDs = nzyme.getTapManager()
                    .allTapUUIDsAccessibleByScope(monitoredSSID.organizationId(), monitoredSSID.tenantId());

            for (MonitoredBSSID monitoredBSSID : nzyme.getDot11().findMonitoredBSSIDsOfSSID(monitoredSSID.id())) {
                for (MonitoredChannel frequency : nzyme.getDot11().findMonitoredChannelsOfMonitoredNetwork(monitoredSSID.id())) {
                    List<ChannelHistogramEntry> signals = nzyme.getDot11().getSSIDSignalStrengthWaterfall(
                            monitoredBSSID.bssid(), monitoredSSID.ssid(), (int) frequency.frequency(), 8*60, tapUUIDs);

                    TrackDetector.TrackDetectorHeatmapData heatmap = TrackDetector.toChartAxisMaps(signals);
                    TrackDetector td = new TrackDetector();
                    List<Track> tracks = td.detect(heatmap.z(), heatmap.y(), TrackDetector.DEFAULT_CONFIG);

                    if (tracks.size() > 1) {
                        // Multiple tracks detected.
                        Map<String, String> attributes = Maps.newHashMap();
                        attributes.put("bssid", monitoredBSSID.bssid());
                        attributes.put("channel", String.valueOf(frequency.frequency()));

                        nzyme.getDetectionAlertService().raiseAlert(
                                monitoredSSID.organizationId(),
                                monitoredSSID.tenantId(),
                                monitoredSSID.uuid(),
                                null,
                                DetectionType.DOT11_MONITOR_FINGERPRINT,
                                Subsystem.DOT11,
                                "Monitored network \"" + monitoredSSID.ssid() + "\" advertised " +
                                        "with multiple signal tracks on channel \"" + frequency.frequency() + "\".",
                                attributes,
                                new String[]{"bssid", "channel"},
                                null
                        );
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "802.11 Signal Track Monitor";
    }

}
