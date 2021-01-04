package net.xrrocha.scripter.transformer;

import com.google.common.io.Resources;
import net.xrrocha.scripter.Script;
import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import javax.validation.constraints.NotNull;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Transforms a script's body and language by transpiling to a target language using a transpiler
 * written in the target language itself.
 */
public abstract class TranspilingScriptTransformer implements ScriptTransformer, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(TranspilingScriptTransformer.class);
    private final String sourceLanguageName;
    private final String targetLanguageName;
    private final String transpilerName;
    private final String transpilerResourceName;
    private final Function<String, String> transpilerObjectRetrieval;
    private final Supplier<String> transpilerInvocation;
    private Function<String, String> transpiler = null;

    public TranspilingScriptTransformer(@NotNull String sourceLanguageName,
                                        @NotNull String targetLanguageName,
                                        @NotNull String transpilerName,
                                        @NotNull String transpilerResourceName,
                                        @NotNull Function<String, String> transpilerObjectRetrieval,
                                        @NotNull Supplier<String> transpilerInvocation) {
        this.sourceLanguageName = sourceLanguageName;
        this.targetLanguageName = targetLanguageName;
        this.transpilerName = transpilerName;
        this.transpilerResourceName = transpilerResourceName;
        this.transpilerObjectRetrieval = transpilerObjectRetrieval;
        this.transpilerInvocation = transpilerInvocation;

        initialize();
    }

    static Function<String, String>
    loadResource(@NotNull String resourceName,
                 @NotNull String targetLanguageName,
                 @NotNull String transpilerName,
                 @NotNull Function<String, String> transpilerObjectRetrieval,
                 @NotNull Supplier<String> transpilerInvocation) {

        try {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            ScriptEngine scriptEngine = engineManager.getEngineByName(targetLanguageName);

            String resourceContents = Resources.toString(getResource(resourceName), UTF_8);

            String transpilerSourceCode = transpilerObjectRetrieval.apply(resourceContents);

            if (scriptEngine instanceof Compilable) {

                CompiledScript compiledScript = ((Compilable) scriptEngine).compile(transpilerSourceCode);

                Object transpiler = compiledScript.eval();
                CompiledScript invocationScript =
                        ((Compilable) scriptEngine).compile(transpilerInvocation.get());

                return (scriptBody) -> {
                    try {
                        Bindings invocationBindings = scriptEngine.createBindings();
                        invocationBindings.put(transpilerName, transpiler);
                        invocationBindings.put("scriptBody", scriptBody);
                        return invocationScript.eval(invocationBindings).toString();
                    } catch (ScriptException e) {
                        throw new IllegalArgumentException(e);
                    }
                };

            } else {

                return (scriptBody) -> {
                    try {
                        Object transpiler = scriptEngine.eval(transpilerSourceCode);
                        Bindings transpilerBindings = scriptEngine.createBindings();
                        transpilerBindings.put(transpilerName, transpiler);
                        Bindings invocationBindings = scriptEngine.createBindings();
                        invocationBindings.put("scriptBody", scriptBody);

                        return scriptEngine.eval(transpilerSourceCode, invocationBindings).toString();


                    } catch (ScriptException e) {
                        throw new IllegalArgumentException(e);
                    }
                };
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Script transformScript(@NotNull Script script) {
        checkNotNull(script, "Script cannot be null");
        checkNotNull(script.getLanguage(), "Script language cannot be null");

        if (sourceLanguageName.trim().equalsIgnoreCase(script.getLanguage().trim())) {

            return new Script(
                    script.getId(),
                    targetLanguageName,
                    script.getUsage(),
                    script.getDescription().orElse(null),
                    script.getClassLoaderCreator().orElse(null),
                    script.getServices(),
                    script.getGlobalVariables(),
                    getTranspiler().apply(script.getScript())
            );
        }

        return script;
    }

    protected Function<String, String> getTranspiler() {
        if (transpiler == null) {
            synchronized (this) {
                if (transpiler == null) {
                    long startTime = System.currentTimeMillis();
                    transpiler = loadResource(transpilerResourceName,
                            targetLanguageName,
                            transpilerName,
                            transpilerObjectRetrieval,
                            transpilerInvocation);
                    long endTime = System.currentTimeMillis();
                    logger.debug("Transpiler compilation time: {}", (endTime - startTime) / 1000D);
                }
            }
        }

        return transpiler;
    }

    @Override
    public void initialize() {
        checkNotNull(sourceLanguageName, "Source language cannot be null");
        checkNotNull(targetLanguageName, "Target language cannot be null");
        checkNotNull(transpilerName, "Transpiler name cannot be null");
        checkNotNull(transpilerResourceName, "Transpiler resource name cannot be null");
        checkNotNull(transpilerObjectRetrieval, "Transpiler object retrieval cannot be null");
        checkNotNull(transpilerInvocation, "Transpiler invocation cannot be null");

    }
}
