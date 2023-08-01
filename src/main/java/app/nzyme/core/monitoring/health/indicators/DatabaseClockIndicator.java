package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.plugin.Database;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseClockIndicator extends Indicator {

    private static final Logger LOG = LogManager.getLogger(DatabaseClockIndicator.class);

    private final Database database;
    private final String ntpHost;

    /*
     * Used to count how many errors were not logged to avoid logging every timeout error, which can occur fairly
     * frequently. Is reset to 0 after a log message was written.
     */
    private final AtomicInteger tempErrorCount;

    public DatabaseClockIndicator(Database database, String ntpHost) {
        this.database = database;
        this.ntpHost = ntpHost;

        this.tempErrorCount = new AtomicInteger(0);
    }

    @Override
    protected IndicatorStatus doRun() {
        NTPUDPClient c = new NTPUDPClient();
        c.setDefaultTimeout(5000);

        try {
            c.open();

            TimeInfo info = c.getTime(InetAddress.getByName(ntpHost));
            info.computeDetails();

            /*
             * Instead of messing around with hyper-accurate offsets, we just take the time the NTP server reported as
             * receive time. Good enough. (tm)
             */
            DateTime worldTime = new DateTime(info.getMessage().getReceiveTimeStamp().getDate());
            DateTime dbTime = database.getDatabaseClock();

            if (dbTime.isBefore(worldTime.minusSeconds(5)) || dbTime.isAfter(worldTime.plusSeconds(5))) {
                LOG.warn("Database time is not synchronized with world time. World time is [{}], database time is [{}]",
                        worldTime.withZone(DateTimeZone.UTC), dbTime.withZone(DateTimeZone.UTC));
                return IndicatorStatus.red(this);
            } else {
                return IndicatorStatus.green(this);
            }
        } catch(Exception e) {
            if (this.tempErrorCount.get() < 3) {
                this.tempErrorCount.incrementAndGet();
            } else {
                LOG.warn("Error running [{}] indicator. Marking as unavailable.", getId(), e);
                this.tempErrorCount.set(0);
            }

            return IndicatorStatus.unavailable(this);
        } finally {
            c.close();
        }
    }

    @Override
    public String getId() {
        return "db_clock";
    }

    @Override
    public String getName() {
        return "Database Clock";
    }

}
