package net.xrrocha.scripter;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The prepared object for services.
 */
public class PreparedService extends PreparedObject {

    /**
     * The originating service object.
     */
    private final Object service;

    public PreparedService(@NotNull Script script,
                           @NotNull String yamlString,
                           @NotNull Object service) {
        super(script, yamlString);
        checkNotNull(service, "Service cannot bee null");
        this.service = service;
    }

    public Object getService() {
        return service;
    }
}
