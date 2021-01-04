package net.xrrocha.scripter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The prepared object for scripts.
 */
public class PreparedScript extends PreparedObject {

    /**
     * The originating script object.
     */
    private final ScriptExecutor scriptExecutor;

    public PreparedScript(@NotNull Script script,
                          @NotNull String yamlString,
                          @NotNull ScriptExecutor scriptExecutor) {
        super(script, yamlString);
        checkNotNull(scriptExecutor, "Script executor cannot bee null");
        this.scriptExecutor = scriptExecutor;
    }

    public Object executeScript(@Null Map<String, Object> invocationVariables) {
        return scriptExecutor.executeScript(invocationVariables);
    }
}
