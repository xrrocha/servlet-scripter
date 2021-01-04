package net.xrrocha.service.email;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Email response data class.
 */
public class EmailResponse {

    private final String id;
    private final String message;

    @JsonCreator
    public EmailResponse(@NotNull @JsonProperty("id") String id,
                         @NotNull @JsonProperty("message") String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "id: " + id + ", message: " + message;
    }
}
