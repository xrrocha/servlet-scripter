package net.xrrocha.scripter.configurer;

import scala.tools.nsc.interpreter.IMain;

import javax.script.ScriptEngine;
import javax.validation.constraints.NotNull;

/**
 * Preprocess a newly created Scala <code>ScriptEngine</code> to use Java classpath.
 */
public class ScalaScriptEngineConfigurer implements ScriptEngineConfigurer {

    @Override
    public void configureScriptEngine(@NotNull ScriptEngine scriptEngine) {
        ((IMain) scriptEngine).settings().usejavacp().value_$eq(true);
    }
}
