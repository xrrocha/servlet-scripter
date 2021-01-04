package net.xrrocha.scripter.commons.source.io;

import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.File;

import static net.xrrocha.scripter.commons.YamlUtils.YAML;

public class IOStreamSourceTest {

    private final IOStreamSource<String> source = new IOStreamSource<String>() {
        @Override
        protected String createFromFilename(String filename) {
            return "scripter";
        }
    };

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilename() {
        new TestIOStreamSource(null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilenameYaml() throws Exception {
        File file = File.createTempFile("source", ".dat");
        file.deleteOnExit();
        String yamlString =
                "!!net.xrrocha.scripter.commons.source.io.IOStreamSourceTest$TestIOStreamSource\n" +
                        "filename: null\n";
        TestIOStreamSource source = YAML.load(yamlString);
        source.getObject();
    }

    @Test(expected = NullPointerException.class)
    public void rejectsAbsentFilenameYaml() throws Exception {
        File file = File.createTempFile("source", ".dat");
        file.deleteOnExit();
        String yamlString =
                "--- !!net.xrrocha.scripter.commons.source.io.IOStreamSourceTest$TestIOStreamSource []\n";
        TestIOStreamSource source = YAML.load(yamlString);
        source.getObject();
    }

    public static class TestIOStreamSource extends IOStreamSource<String> {

        private TestIOStreamSource() {
            super();
        }

        public TestIOStreamSource(@NotNull String filename) {
            super(filename);
        }

        @Override
        protected String createFromFilename(@NotNull String filename) {
            return filename;
        }
    }

}
