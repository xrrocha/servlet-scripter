package net.xrrocha.scripter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for prepared scripts and services.
 */
public abstract class PreparedObject implements Serializable {

    /**
     * The originating script.
     */
    private final Script script;

    /**
     * The originating yaml string.
     */
    private final String yamlString;

    public PreparedObject(@NotNull Script script, @NotNull String yamlString) {
        checkNotNull(script, "Script cannot be null");
        checkNotNull(yamlString, "Yaml string cannot be null");
        this.script = script;
        this.yamlString = yamlString;
    }

    @Override
    public String toString() {
        return script.toString();
    }

    public Script getScript() {
        return script;
    }

    public String getYamlString() {
        return yamlString;
    }
}
