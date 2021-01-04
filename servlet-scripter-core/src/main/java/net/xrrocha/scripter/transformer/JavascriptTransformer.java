package net.xrrocha.scripter.transformer;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

/**
 * Targets Javascript for transpilation of script's body and language.
 */
public class JavascriptTransformer extends TranspilingScriptTransformer {

    public JavascriptTransformer(@NotNull String transpilerName,
                                 @NotNull String transpilerResourceName,
                                 @NotNull Supplier<String> transpilerInvocation) {
        super("ecmascript7",
                "javascript",
                transpilerName,
                transpilerResourceName,
                resourceContents -> resourceContents + "\n" + transpilerName,
                transpilerInvocation);
    }
}
