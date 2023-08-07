package app.nzyme.core.events.types;

import app.nzyme.core.detection.alerts.DetectionType;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class DetectionEvent {

    public abstract UUID alertId();
    public abstract DetectionType detectionType();
    public abstract DateTime timestamp();

    public static DetectionEvent create(UUID alertId, DetectionType detectionType, DateTime timestamp) {
        return builder()
                .alertId(alertId)
                .detectionType(detectionType)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionEvent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder alertId(UUID alertId);

        public abstract Builder detectionType(DetectionType detectionType);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract DetectionEvent build();
    }
}
