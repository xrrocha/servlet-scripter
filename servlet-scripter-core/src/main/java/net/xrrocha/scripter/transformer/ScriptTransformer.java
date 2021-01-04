package net.xrrocha.scripter.transformer;

import net.xrrocha.scripter.Script;

import javax.validation.constraints.NotNull;

/**
 * Transforms a script's body and language. Meant primarily for transpilers.
 */
public interface ScriptTransformer {

    /**
     * Transforms the script's body and language.
     *
     * @param script The immutable script to be transformed.
     * @return The newly created script.
     */
    Script transformScript(@NotNull Script script);
}
