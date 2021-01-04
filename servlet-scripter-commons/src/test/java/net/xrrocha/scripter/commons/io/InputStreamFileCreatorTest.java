package net.xrrocha.scripter.commons.io;

import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;

import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO(rrocha) Add unit tests for FileFromUrlCreator. recreate = true. Invalid dirs
public class InputStreamFileCreatorTest {


    @Test(expected = NullPointerException.class)
    public void rejectsNullFilePath() {
        new InputStreamFileCreator(null, "urlLocation");
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullUrlLocation() {
        new InputStreamFileCreator("filePath", null);
    }

    @Test
    public void createsFile() throws Exception {

        String filePath = "filePath";
        String urlLocation = "urlLocation";
        String urlContents = "someUrlContents";

        InputStreamFileCreator creator =
                new InputStreamFileCreator(filePath, urlLocation);

        File file = creator.createFile(
                TEMP_DIRECTORY,
                location -> new ByteArrayInputStream(urlContents.getBytes()));
        file.deleteOnExit();

        assertTrue(file.isFile());
        assertEquals(urlContents, CharStreams.toString(new FileReader(file)));
    }
}
