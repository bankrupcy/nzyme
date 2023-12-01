package app.nzyme.core.rest.responses.dot11.clients;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ConnectedClientDetailsResponse {

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("connected_bssid")
    public abstract Dot11MacAddressResponse connectedBSSID();

    @JsonProperty("probe_request_ssids")
    public abstract List<String> probeRequests();

    @JsonProperty("bssid_history")
    public abstract List<ConnectedBSSID> bssidHistory();

    public static ConnectedClientDetailsResponse create(Dot11MacAddressResponse mac, DateTime lastSeen, Dot11MacAddressResponse connectedBSSID, List<String> probeRequests, List<ConnectedBSSID> bssidHistory) {
        return builder()
                .mac(mac)
                .lastSeen(lastSeen)
                .connectedBSSID(connectedBSSID)
                .probeRequests(probeRequests)
                .bssidHistory(bssidHistory)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder connectedBSSID(Dot11MacAddressResponse connectedBSSID);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder bssidHistory(List<ConnectedBSSID> bssidHistory);

        public abstract ConnectedClientDetailsResponse build();
    }
}
