package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SSIDChannelDetailsResponse {

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("frequency")
    public abstract long frequency();

    @JsonProperty("channel")
    public abstract long channel();

    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("total_bytes")
    public abstract long totalBytes();

    @JsonProperty("is_main_active")
    public abstract boolean isMainActive();

    @JsonProperty("security_protocols")
    public abstract List<String> securityProtocols();

    @JsonProperty("infrastructure_types")
    public abstract List<String> infrastructureTypes();

    @JsonProperty("is_wps")
    public abstract List<Boolean> isWps();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("is_monitored")
    public abstract boolean isMonitored();

    public static SSIDChannelDetailsResponse create(String ssid, long frequency, long channel, float signalStrengthAverage, long totalFrames, long totalBytes, boolean isMainActive, List<String> securityProtocols, List<String> infrastructureTypes, List<Boolean> isWps, DateTime lastSeen, boolean isMonitored) {
        return builder()
                .ssid(ssid)
                .frequency(frequency)
                .channel(channel)
                .signalStrengthAverage(signalStrengthAverage)
                .totalFrames(totalFrames)
                .totalBytes(totalBytes)
                .isMainActive(isMainActive)
                .securityProtocols(securityProtocols)
                .infrastructureTypes(infrastructureTypes)
                .isWps(isWps)
                .lastSeen(lastSeen)
                .isMonitored(isMonitored)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDChannelDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder frequency(long frequency);

        public abstract Builder channel(long channel);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder totalBytes(long totalBytes);

        public abstract Builder isMainActive(boolean isMainActive);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder isMonitored(boolean isMonitored);

        public abstract SSIDChannelDetailsResponse build();
    }
}
