package net.xrrocha.scripter.commons.io;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResourceInputStreamOpenerIT {

    @Test
    public void opensExistingResource() throws Exception {
        InputStreamOpener
                inputStreamOpener =
                new ResourceInputStreamOpener(ResourceInputStreamOpenerIT.class.getClassLoader());
        InputStream is = inputStreamOpener.openInputStream("resource.txt");
        assertNotNull(is);
        String contents = new String(ByteStreams.toByteArray(is));
        assertEquals("Scripter rocks!", contents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonExistingResource() {
        InputStreamOpener
                inputStreamOpener =
                new ResourceInputStreamOpener(ResourceInputStreamOpenerIT.class.getClassLoader());
        inputStreamOpener.openInputStream("non-existent.txt");
    }
}
