package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MFAVerificationRequest {

    public abstract String code();

    @JsonCreator
    public static MFAVerificationRequest create(@JsonProperty("code") String code) {
        return builder()
                .code(code)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MFAVerificationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder code(String code);

        public abstract MFAVerificationRequest build();
    }
}
