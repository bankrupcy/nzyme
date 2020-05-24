/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.periodicals.alerting.beaconrate;

import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.networks.BSSID;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.dot11.networks.SSID;
import horse.wtf.nzyme.periodicals.Periodical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeaconRateWriter extends Periodical {

    private static final Logger LOG = LogManager.getLogger(BeaconRateWriter.class);

    private final Networks networks;
    private final Database database;

    public BeaconRateWriter(NzymeLeader nzyme) {
        this.networks = nzyme.getNetworks();
        this.database = nzyme.getDatabase();
    }

    @Override
    protected void execute() {
        try {
            for (BSSID bssid : networks.getBSSIDs().values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    if (!ssid.isHumanReadable()) {
                        continue;
                    }

                    database.useHandle(handle -> handle.execute("INSERT INTO beacon_rate_history(bssid, ssid, beacon_rate, created_at) " +
                            "VALUES(?, ?, ?, current_timestamp at time zone 'UTC')",
                            bssid.bssid().toLowerCase(),
                            ssid.name(),
                            ssid.beaconCount.get()
                    ));

                    // Reset internal counter.
                    ssid.beaconCount.set(0);
                }
            }
        } catch(Exception e) {
            LOG.error("Could not write beacon rate information.", e);
        }
    }

    @Override
    public String getName() {
        return "BeaconRateWriter";
    }

}
