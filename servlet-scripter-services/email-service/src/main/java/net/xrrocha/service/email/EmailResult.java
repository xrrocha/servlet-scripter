package net.xrrocha.service.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Email result data class.
 */
public class EmailResult {

    private final int responseCode;
    private final EmailResponse response;

    @JsonCreator
    public EmailResult(@NotNull @JsonProperty("responseCode") int responseCode,
                       @NotNull @JsonProperty("response") EmailResponse response) {
        this.responseCode = responseCode;
        this.response = response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public EmailResponse getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "responseCode: " + responseCode + ", response: " + response;
    }
}
