package net.xrrocha.scripter;

import javax.validation.constraints.Null;
import java.util.Map;

/**
 * Executor for both compiled and interpreted scripts.
 */
public interface ScriptExecutor {

    /**
     * Execute the given script with the given variables.
     *
     * @param variables The optional variables to pass for execution.
     * @return The script's execution return value.
     */
    Object executeScript(@Null Map<String, Object> variables);
}
