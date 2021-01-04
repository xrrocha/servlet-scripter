package net.xrrocha.scripter;

import com.google.common.base.Converter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.xrrocha.scripter.commons.Initializable;
import net.xrrocha.scripter.commons.registry.CachingRegistry;
import net.xrrocha.scripter.commons.registry.ConvertingRegistry;
import net.xrrocha.scripter.commons.registry.FileBasedRegistry;
import net.xrrocha.scripter.commons.registry.Registry;
import net.xrrocha.scripter.configurer.ScriptEngineConfigurer;
import net.xrrocha.scripter.transformer.ScriptTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;
import static net.xrrocha.scripter.commons.YamlUtils.YAML;
import static net.xrrocha.scripter.commons.io.FileUtils.HOME_DIRECTORY;
import static net.xrrocha.scripter.commons.io.FileUtils.isValidDirectory;

/**
 * The scripting support workhorse.
 */
public class Scripter implements Initializable, Serializable {

    public static final String SCRIPT_DIRECTORY_NAME = "scripts";
    public static final String CLASS_LOADER_DIRECTORY_NAME = "class-loaders";
    public static final String SCRIPTER_DIRECTORY_NAME = "servlet-scripter";
    private static final File DEFAULT_REGISTRY_DIRECTORY =
            new File(HOME_DIRECTORY, SCRIPTER_DIRECTORY_NAME);
    private static final Map<String, ScriptEngineFactory> scriptEngineFactories =
            new ScriptEngineManager().getEngineFactories().stream()
                    .flatMap(factory -> factory.getNames().stream().map(name -> new SimpleEntry<>(name, factory)))
                    .collect(toMap(Entry::getKey, Entry::getValue));
    private static final Logger logger = LoggerFactory.getLogger(Scripter.class);
    private final File registryDirectory;
    private final Map<String, ScriptTransformer> transformers;
    private final Map<String, ScriptEngineConfigurer> configurers;
    private Registry<String, PreparedObject> scriptRegistry;
    private File baseClassLoaderDirectory;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private Scripter() {
        registryDirectory = DEFAULT_REGISTRY_DIRECTORY;
        transformers = emptyMap();
        configurers = emptyMap();
    }

    public Scripter(@NotNull File registryDirectory,
                    @NotNull Map<String, ScriptTransformer> transformers,
                    @NotNull Map<String, ScriptEngineConfigurer> configurers) {

        if (registryDirectory != null) {
            this.registryDirectory = registryDirectory;
        } else {
            this.registryDirectory = DEFAULT_REGISTRY_DIRECTORY;
        }

        if (transformers != null) {
            this.transformers = transformers;
        } else {
            this.transformers = emptyMap();
        }

        if (configurers != null) {
            this.configurers = configurers;
        } else {
            this.configurers = emptyMap();
        }

        initialize();
    }

    public Optional<String> addScript(@NotNull String scriptYaml) {
        return addScript(scriptYaml, false);
    }

    public Optional<String> addScript(@NotNull String scriptYaml, boolean replace) {

        Script script = YAML.loadAs(scriptYaml, Script.class);

        Optional<PreparedObject> previousScript = scriptRegistry.lookup(script.getId());

        if (previousScript.isPresent() && !replace) {
            String errorMessage = "Can't replace script: " + script.getId();
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        PreparedObject preparedObject = addScript(script, scriptYaml);

        scriptRegistry.register(preparedObject.getScript().getId(), preparedObject);

        return previousScript.map(PreparedObject::getYamlString);
    }

    public Optional<String> getScriptText(@NotNull String scriptId) {
        return getScript(scriptId).map(Script::getScript);
    }

    public Object executeScript(@NotNull String scriptId, @Null Map<String, Object> variables) {

        Optional<PreparedObject> optPreparedObject = scriptRegistry.lookup(scriptId);
        if (!optPreparedObject.isPresent()) {
            String errorMessage = "Ignoring non-existent script '" + scriptId + "'";
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return optPreparedObject
                .filter(preparedObject -> preparedObject instanceof PreparedScript)
                .map(preparedObject -> (PreparedScript) preparedObject)
                .map(preparedScript ->
                        preparedScript.executeScript(variables == null ? emptyMap() : variables))
                .orElse(null);
    }

    public void removeScript(@NotNull String scriptId) {

        scriptRegistry.lookup(scriptId)
                .filter(preparedObject -> preparedObject instanceof PreparedService)
                .ifPresent(preparedObject -> checkForOrphanedDependants(scriptId));

        scriptRegistry.deregister(scriptId);
    }

    public Iterable<String> listScriptIds() {

        Set<String> set = new HashSet<>();
        scriptRegistry.list().iterator().forEachRemaining(entry -> set.add(entry.getKey()));
        return set;
    }

    private PreparedObject addScript(@NotNull Script sourceScript, @NotNull String scriptYaml) {
        Script script = tryAndTransform(sourceScript);
        return prepareObject(script, scriptYaml);
    }

    Optional<Script> getScript(@NotNull String scriptId) {
        return scriptRegistry.lookup(scriptId).map(PreparedObject::getScript);
    }

    Script tryAndTransform(@NotNull Script script) {
        if (transformers == null) {
            return script;
        }

        return Optional.ofNullable(transformers.get(script.getLanguage()))
                .map(transformer -> transformer.transformScript(script))
                .orElse(script);
    }

    PreparedObject prepareObject(@NotNull Script script, @NotNull String yamlString) {

        final ClassLoader classLoader =
                script.getClassLoaderCreator()
                        .map(creator -> {
                            File classLoaderDirectory = new File(baseClassLoaderDirectory, script.getId());
                            File resourceDirectory = new File(classLoaderDirectory, "resources");
                            return creator.createClassLoader(
                                    Thread.currentThread().getContextClassLoader(),
                                    classLoaderDirectory,
                                    resourceDirectory);
                        })
                        .orElse(Thread.currentThread().getContextClassLoader());

        ScriptExecutor scriptExecutor = buildScriptExecutor(script, classLoader);

        final PreparedObject preparedObject;
        switch (script.getUsage()) {
            case INVOCABLE_SCRIPT:
                preparedObject = new PreparedScript(script, yamlString, scriptExecutor);
                break;
            case REUSABLE_SERVICE:
                Object service = scriptExecutor.executeScript(emptyMap());
                preparedObject = new PreparedService(script, yamlString, service);
                break;
            default:
                throw new IllegalStateException("No such usage: " + script.getUsage());
        }

        return preparedObject;
    }

    ScriptExecutor buildScriptExecutor(@NotNull Script script,
                                       @NotNull ClassLoader scriptClassLoader) {

        ScriptEngineFactory factory = scriptEngineFactories.get(script.getLanguage());
        if (factory == null) {
            String errorMessage = "No such language: " + script.getLanguage();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        final ScriptEngine scriptEngine;
        if (factory instanceof NashornScriptEngineFactory) {
            scriptEngine = ((NashornScriptEngineFactory) factory).getScriptEngine(scriptClassLoader);
        } else {
            scriptEngine = factory.getScriptEngine();
        }

        if (configurers != null && configurers.containsKey(script.getLanguage())) {
            configurers.get(script.getLanguage()).configureScriptEngine(scriptEngine);
        }

        try {

            if (scriptEngine instanceof Compilable) {

                CompiledScript compiledScript = ((Compilable) scriptEngine).compile(script.getScript());

                return (invocationVariables) -> {
                    Bindings invocationBindings = scriptEngine.createBindings();
                    populateBindings(script, invocationVariables, invocationBindings);
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(scriptClassLoader);
                        return compiledScript.eval(invocationBindings);
                    } catch (ScriptException e) {
                        throw new IllegalArgumentException(e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(contextClassLoader);
                    }
                };

            } else { // Not compilable

                return (invocationVariables) -> {
                    Bindings invocationBindings = scriptEngine.createBindings();
                    populateBindings(script, invocationVariables, invocationBindings);
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(scriptClassLoader);
                        return scriptEngine.eval(script.getScript(), invocationBindings);
                    } catch (ScriptException e) {
                        throw new IllegalArgumentException(e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(contextClassLoader);
                    }
                };
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Populates bindings in priority order. <ol> <li>Services</li> <li>Invocation Variables</li>
     * <li>Global Variables</li> </ol>
     */
    void populateBindings(@NotNull Script script,
                          @NotNull Map<String, Object> invocationVariables,
                          @NotNull Bindings bindings) {

        // Start with script's own, compile-time properties
        bindings.putAll(script.getGlobalVariables());
        logger.debug("globalVariables: " + script.getGlobalVariables());

        // Per-invocation variables may trump script's own variables
        if (invocationVariables != null) {
            bindings.putAll(invocationVariables);
            logger.debug("invocationVariables: " + invocationVariables);
        }

        // Service references trump any clashing script variables
        Map<String, Object> serviceDependencies = collectServiceDependencies(script);
        logger.debug("serviceDependencies: " + serviceDependencies);
        bindings.putAll(serviceDependencies);
        logger.debug("Resulting bindings: " + bindings);

    }

    Map<String, Object> collectServiceDependencies(@NotNull Script script) {

        Map<Boolean, List<Entry<String, Optional<PreparedObject>>>> partition =
                script.getServices().stream()
                        .map(serviceName -> new SimpleEntry<>(serviceName, scriptRegistry.lookup(serviceName)))
                        .collect(partitioningBy(entry -> entry.getValue().isPresent()));

        final List<String> nonExistentServicesNames = partition.get(false).stream()
                .map(Entry::getKey).collect(toList());

        final List<String> nonServiceNameList = partition.get(true).stream()
                .filter(entry -> !(scriptRegistry.lookup(entry.getKey()).get() instanceof PreparedService))
                .map(Entry::getKey)
                .collect(toList());

        if (!(nonExistentServicesNames.isEmpty() && nonServiceNameList.isEmpty())) {

            StringBuilder sb = new StringBuilder();
            sb.append("Service errors in script '");
            sb.append(script.getId());
            sb.append("' => ");

            if (!nonExistentServicesNames.isEmpty()) {
                Collections.sort(nonExistentServicesNames);
                sb.append("References non-existent services ");
                sb.append(nonExistentServicesNames);
                sb.append(". ");
            }

            if (!nonServiceNameList.isEmpty()) {
                Collections.sort(nonServiceNameList);
                sb.append("References non-service scripts ");
                sb.append(nonServiceNameList);
                sb.append(". ");
            }

            String errorMessage = sb.toString();
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return partition.get(true).stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(),
                        ((PreparedService) entry.getValue().get()).getService()))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    void checkForOrphanedDependants(@NotNull String serviceId) {

        Set<Entry<String, PreparedObject>> set = new HashSet<>();
        for (Entry<String, PreparedObject> entry : scriptRegistry.list()) {
            if (!entry.getKey().equals(serviceId)) {
                set.add(entry);
            }
        }

        for (Entry<String, PreparedObject> entry : set) {
            entry.getValue().getScript().getServices().forEach(services -> {
                if (services.contains(serviceId)) {
                    String errorMessage = serviceId + " has dependants";
                    logger.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            });
        }

    }

    @Override
    public void initialize() {

        checkNotNull(registryDirectory, "Registry directory cannot be null");

        checkArgument(isValidDirectory(registryDirectory),
                "Invalid registry directory: " + registryDirectory);

        // Transformers
        if (transformers != null) {
            boolean validTransformers = transformers.entrySet().stream()
                    .allMatch(entry -> entry.getKey() != null && entry.getValue() != null);
            if (!validTransformers) {
                String errorMessage = "One or more names or transformers are null";
                logger.error(errorMessage);
                throw new NullPointerException(errorMessage);
            }
        }

        // Configurers
        if (configurers != null) {
            configurers.forEach((languageName, configurer) -> {
                checkNotNull(languageName, "Configurer language name cannot be null");
                checkNotNull(configurer, "Configurer name cannot be null");
            });
        }

        // Create subdirectories
        File scriptDirectory = new File(registryDirectory, SCRIPT_DIRECTORY_NAME);
        checkArgument(isValidDirectory(scriptDirectory),
                "Invalid script directory: " + scriptDirectory);
        baseClassLoaderDirectory = new File(registryDirectory, CLASS_LOADER_DIRECTORY_NAME);
        checkArgument(isValidDirectory(baseClassLoaderDirectory),
                "Invalid class loader directory: " + baseClassLoaderDirectory);

        // Populate initial prepared script registry
        Registry<String, String> fileRegistry =
                new FileBasedRegistry(scriptDirectory, "^[a-zA-Z][-_a-zA-Z0-9]*\\.yaml$");

        Registry<String, PreparedObject> convertingRegistry = new ConvertingRegistry<>(
                fileRegistry,
                new Converter<String, String>() {
                    @Override
                    protected String doForward(@NotNull String string) {
                        return string + ".yaml";
                    }

                    @Override
                    protected String doBackward(@NotNull String string) {
                        return string.substring(0, string.length() - 5);
                    }
                },
                new Converter<PreparedObject, String>() {
                    @Override
                    protected String doForward(@NotNull PreparedObject preparedObject) {
                        return preparedObject.getYamlString();
                    }

                    @Override
                    protected PreparedObject doBackward(@NotNull String yamlString) {
                        Script script = YAML.loadAs(yamlString, Script.class);
                        return addScript(script, yamlString);
                    }
                }
        );

        scriptRegistry = new CachingRegistry<>(convertingRegistry);
    }
}
