package app.nzyme.core.monitoring.health.indicators;

import app.nzyme.core.monitoring.health.Indicator;
import app.nzyme.core.monitoring.health.db.IndicatorStatus;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.TapManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

public class TapClockIndicator extends Indicator {

    private static final Logger LOG = LogManager.getLogger(TapClockIndicator.class);

    private final TapManager tapManager;

    public TapClockIndicator(TapManager tapManager) {
        this.tapManager = tapManager;
    }

    @Override
    protected IndicatorStatus doRun() {
        Optional<List<Tap>> taps = tapManager.findTaps();
        if (taps.isEmpty()) {
            // No recently active taps.
            return IndicatorStatus.green(this);
        }

        for (Tap tap : taps.get()) {
            // We only want to check very recently active taps.
            if (tap.updatedAt().isBefore(DateTime.now().minusMinutes(2))) {
                LOG.debug("Skipping inactive tap [{}].", tap.name());
                continue;
            }

            if (tap.clockDriftMs() < -5000 || tap.clockDriftMs() > 5000) {
                return IndicatorStatus.red(this);
            }
        }

        return IndicatorStatus.green(this);
    }

    @Override
    public String getId() {
        return "tap_clock";
    }

    @Override
    public String getName() {
        return "Tap Clock";
    }

}
