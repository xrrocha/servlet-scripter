package net.xrrocha.scripter.commons;

import com.google.common.io.Resources;
import net.xrrocha.scripter.commons.io.ResourceInputStreamOpener;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static net.xrrocha.scripter.commons.YamlUtils.*;
import static org.junit.Assert.*;

public class YamlUtilsTest {

    private static final Pattern TAG_PATTERN = Pattern.compile("^[A-Za-z_][0-9A-Za-z_]*$");

    private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    @Test
    public void loadsYamlTags() throws Exception {
        InputStream is =
                Resources.getResource("yamltag.yaml").openStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        List<String> lines = in.lines().collect(toList());
        boolean allTagsValid = lines.stream().allMatch(line -> {
            String[] fields = line.split("\\s*:\\s*");
            assertEquals(2, fields.length);
            assertTrue(TAG_PATTERN.matcher(fields[0]).matches());
            try {
                Class.forName(fields[1]);
            } catch (ClassNotFoundException e) {
                return false;
            }
            return true;
        });
        assertTrue(allTagsValid);
    }

    @Test
    public void includesMainResources() {
        // Relies on InMemoryRepository being present in main yamltag.yaml
        // resource file with tag equal to "inMemoryRegistry"
        String yamlString = "--- !resourceOpener []";
        Object object = YAML.load(yamlString);
        assertNotNull(object);
        YAML_LOGGER.debug(object.getClass().getName());
        assertTrue(object instanceof ResourceInputStreamOpener);
    }

    @Test
    public void includesTestResources() {
        // Relies on SpaceNormalizer being present in test yamltag.yaml
        // resource file with tag equal to "spaceNormalizer"
        String yamlString = "--- !stringNormalizer []";
        Object object = YAML.load(yamlString);
        assertNotNull(object);
        YAML_LOGGER.debug(object.getClass().getName());
        assertTrue(object instanceof SpaceNormalizer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateTags() throws Exception {
        File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        File classpathDirectory = new File(tempDirectory, "yamltag-" + System.currentTimeMillis());
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "stringNormalizer: " + ResourceInputStreamOpener.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        createYaml(classLoader);
    }

    @Test
    public void ignoresNonExistentClasses() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "tag: com.acme.NonExistent\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        createYaml(classLoader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadString() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.load(yamlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadInputStream() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.load(new ByteArrayInputStream(yamlString.getBytes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadReader() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.load(new StringReader(yamlString));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAsString() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAs(yamlString, DateParser.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAsInputStream() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAs(new ByteArrayInputStream(yamlString.getBytes()), DateParser.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAsReader() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAs(new StringReader(yamlString), DateParser.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAllString() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAll(yamlString).iterator().next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAllInputStream() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes());
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAll(new ByteArrayInputStream(yamlString.getBytes())).iterator().next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesOnLoadAllReader() throws Exception {
        File classpathDirectory = new File(TEMP_DIRECTORY, "yamltag-" + System.currentTimeMillis());
        classpathDirectory.deleteOnExit();
        assertFalse(classpathDirectory.exists());
        assertTrue(classpathDirectory.mkdir());

        File resourceFile = new File(classpathDirectory, YAMLTAG_RESOURCE_NAME);
        String yamltagString = "dateParser: " + DateParser.class.getName() + "\n";
        Files.write(resourceFile.toPath(), yamltagString.getBytes()).iterator().next();
        resourceFile.deleteOnExit();

        ClassLoader classLoader = new URLClassLoader(new URL[]{classpathDirectory.toURI().toURL()},
                YamlUtils.class.getClassLoader());

        Yaml yaml = createYaml(classLoader);
        String yamlString = "--- !dateParser\n" +
                "pattern: nonSensicalPattern\n";
        yaml.loadAll(new StringReader(yamlString)).iterator().next();
    }

    public static class SpaceNormalizer {

        public String normalizeString(String string) {
            if (string == null) {
                return "";
            }
            return string.trim().replaceAll("\\s+", " ");
        }
    }

    public static class DateParser implements Initializable {

        private final String pattern;
        private DateTimeFormatter dateTimeFormatter;

        private DateParser() {
            pattern = null;
        }

        public DateParser(String pattern) {
            this.pattern = pattern;
            initialize();
        }

        @Override
        public void initialize() {
            dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        }

        @SuppressWarnings("unchecked")
        public <T extends TemporalAccessor> T parse(String dateTimeString) {
            return (T) dateTimeFormatter.parse(dateTimeString);
        }
    }
}
