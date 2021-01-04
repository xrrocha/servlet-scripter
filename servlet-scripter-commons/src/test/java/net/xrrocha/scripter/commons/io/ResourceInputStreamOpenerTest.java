package net.xrrocha.scripter.commons.io;

import org.junit.Test;

import static net.xrrocha.scripter.commons.YamlUtils.YAML;

public class ResourceInputStreamOpenerTest {

    @Test(expected = NullPointerException.class)
    public void rejectNullClassLoader() {
        new ResourceInputStreamOpener(null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectNullClassLoaderFromYaml() {
        String yamlString = "--- !resourceOpener\n" +
                "classLoader: null\n";
        YAML.load(yamlString);
    }
}
