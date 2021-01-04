package net.xrrocha.scripter.configurer;

import javax.script.ScriptEngine;
import javax.validation.constraints.NotNull;

/**
 * Preprocess a newly created <code>ScriptEngine</code> in a language-specific fashion.
 */
public interface ScriptEngineConfigurer {

    /**
     * Configure a newly created <code>ScriptEngine</code> in a language-specific fashion.
     */
    void configureScriptEngine(@NotNull ScriptEngine scriptEngine);
}
