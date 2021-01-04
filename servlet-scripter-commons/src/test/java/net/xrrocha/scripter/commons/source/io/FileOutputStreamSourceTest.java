package net.xrrocha.scripter.commons.source.io;

import org.junit.Test;

import java.io.File;

import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;

public class FileOutputStreamSourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void failsOnNonBadFilename() {
        String filename = "non-existent-dir-" + System.currentTimeMillis() + "/file.txt";
        File file = new File(TEMP_DIRECTORY, filename);
        FileOutputStreamSource source = new FileOutputStreamSource(file.getAbsolutePath());
        source.getObject();
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilename() {
        new FileOutputStreamSource(null);
    }

}
