package net.xrrocha.scripter.commons.source.io;

import org.junit.Test;

import java.io.File;

import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;

public class FileInputStreamSourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void failsOnNonExistentFilename() {
        String filename = "non-existent-" + System.currentTimeMillis();
        File file = new File(TEMP_DIRECTORY, filename);
        FileInputStreamSource source = new FileInputStreamSource(file.getAbsolutePath());
        source.getObject();
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilename() {
        FileInputStreamSource source = new FileInputStreamSource(null);
    }

}
