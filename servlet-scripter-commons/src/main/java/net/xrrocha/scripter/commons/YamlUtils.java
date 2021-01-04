package net.xrrocha.scripter.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.*;

/**
 * Mix-in for Yaml-aware tests. <p> In its current form (field-based bean access for immutable
 * classes) this mix-in is most beneficial when user classes implement @see{Initializable}.
 */
public class YamlUtils {

    public static final String YAMLTAG_RESOURCE_NAME = "yamltag.yaml";
    // Logger creation must precede YAML global instance creation
    public static final Logger YAML_LOGGER = LoggerFactory.getLogger(YamlUtils.class);

    /**
     * The yaml instance.
     */
    public static final Yaml YAML = doCreateYaml(Thread.currentThread().getContextClassLoader());

    public static Yaml createYaml(ClassLoader classLoader) {
        return doCreateYaml(classLoader);
    }

    /**
     * Create a yaml configured to write to fields rather than bean properties.
     *
     * @param classLoader The class loader to retrieve yamltag resource files
     * @return The new yaml instance
     */
    static Yaml doCreateYaml(ClassLoader classLoader) {

        List<TypeDescription> typeDescriptions = collectTags(classLoader);
        validateTagUniqueness(typeDescriptions);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setCanonical(true);

        Yaml yaml = new Yaml(new ClassConstructor(classLoader));
        yaml.setBeanAccess(BeanAccess.FIELD);
        typeDescriptions.forEach(yaml::addTypeDescription);
        return yaml;
    }

    static List<TypeDescription> collectTags(ClassLoader classLoader) {

        Yaml bootstrapYaml = new Yaml();

        final Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(YAMLTAG_RESOURCE_NAME);
        } catch (IOException e) {
            String errorMessage = "Error getting resources for " + YAMLTAG_RESOURCE_NAME + ": " + e;
            YAML_LOGGER.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }

        return Collections.list(resources).stream()
                .flatMap(url -> {
                    try {
                        Map<String, String> mappings = bootstrapYaml.load(url.openStream());
                        return mappings.entrySet().stream();
                    } catch (IOException e) {
                        YAML_LOGGER.warn("Ignoring loading error for resource '{}'" + url.toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(entry -> {
                    try {
                        checkNotNull(entry.getKey(), "Tag name cannot be null");
                        checkArgument(!entry.getKey().trim().isEmpty(), "Tag name cannot be blank");
                        checkNotNull(entry.getValue(), "Class name cannot be null");
                        checkArgument(!entry.getKey().trim().isEmpty(), "Class name cannot be blank");
                        Tag tag = new Tag("!" + entry.getKey());
                        Class<?> clazz = Class.forName(entry.getValue());
                        YAML_LOGGER.debug("Adding tag: '{}' for class {}",
                                entry.getKey(), entry.getValue());
                        return new TypeDescription(clazz, tag);
                    } catch (ClassNotFoundException e) {
                        YAML_LOGGER.warn("Ignoring loading error for class '{}' in tag '{}': {}",
                                entry.getValue(), entry.getKey(), e.toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    static void validateTagUniqueness(List<TypeDescription> typeDescriptions) {

        Map<Boolean, List<Entry<Tag, List<TypeDescription>>>> partitions =
                typeDescriptions.stream()
                        .collect(groupingBy(TypeDescription::getTag))
                        .entrySet().stream()
                        .collect(partitioningBy(entry -> entry.getValue().size() == 1));

        YAML_LOGGER.debug("unique tags: {}", partitions.get(true));
        YAML_LOGGER.debug("non-unique tags: {}", partitions.get(false));
        checkArgument(partitions.get(false).isEmpty(),
                "Tags with multiple occurrences: " + partitions.get(false));
    }


    /**
     * Class constructor to process <code>!!class</code> tag. This tag allows for class literals
     * inside Yaml files.
     */
    public static class ClassConstructor extends Constructor {

        public static final String CLASS_TAG_NAME = "!class";
        private final ClassLoader classLoader;

        public ClassConstructor(Class<?> type) {
            super(type);
            this.classLoader = type.getClassLoader();
            populateConstructors();
        }

        public ClassConstructor(ClassLoader classLoader) {
            this.classLoader = classLoader;
            populateConstructors();
        }

        public static Class<?> classFromName(String className, ClassLoader classLoader) {
            try {
                return classLoader.loadClass(className);
            } catch (Exception e) {
                String errorMessage = "Error constructing class " + className + ": " + e;
                YAML_LOGGER.error(errorMessage, e);
                throw new IllegalArgumentException(errorMessage, e);
            }
        }

        @Override
        protected Object constructObjectNoCheck(Node node) {
            Object object = super.constructObjectNoCheck(node);
            if (object instanceof Initializable) {
                ((Initializable) object).initialize();
            }
            return object;
        }

        protected void populateConstructors() {
            yamlConstructors.put(new Tag(CLASS_TAG_NAME), new ConstructClass());
        }

        /**
         * Constructor for class literals.
         */
        protected class ConstructClass extends AbstractConstruct {

            public Object construct(Node node) {
                String className = (String) constructScalar((ScalarNode) node);
                return classFromName(className, classLoader);
            }
        }
    }
}
