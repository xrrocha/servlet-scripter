package net.xrrocha.scripter;

import net.xrrocha.scripter.commons.classloader.ParentLastClassLoaderCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static net.xrrocha.scripter.commons.YamlUtils.YAML;

public class ScriptUtils {

    private static final Logger logger = LoggerFactory.getLogger(ScriptUtils.class);

    public static Script scriptFromYaml(String id,
                                        String language,
                                        ScriptUsage usage,
                                        String description,
                                        ParentLastClassLoaderCreator parentLastClassLoaderCreator,
                                        Set<String> services,
                                        Map<String, Object> globalVariables,
                                        String scriptBody) {
        String yamlString = buildYamlString(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                scriptBody);
        logger.debug("yamlString: {}", yamlString);
        return YAML.load(yamlString);
    }

    public static String buildYamlString(String id,
                                         String language,
                                         ScriptUsage usage,
                                         String description,
                                         ParentLastClassLoaderCreator parentLastClassLoaderCreator,
                                         Set<String> services,
                                         Map<String, Object> globalVariables,
                                         String scriptBody) {
        Script script = new Script(
                id, language, usage, description, parentLastClassLoaderCreator, services, globalVariables,
                scriptBody
        );
        return YAML.dump(script);
    }

}
