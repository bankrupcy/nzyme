package app.nzyme.core.dot11.monitoring.disco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DiscoMonitorAnomaly {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("frame_count")
    public abstract long frameCount();

    public static DiscoMonitorAnomaly create(DateTime timestamp, long frameCount) {
        return builder()
                .timestamp(timestamp)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DiscoMonitorAnomaly.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder frameCount(long frameCount);

        public abstract DiscoMonitorAnomaly build();
    }
}
