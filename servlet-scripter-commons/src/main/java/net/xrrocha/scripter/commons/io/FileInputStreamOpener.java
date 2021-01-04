package net.xrrocha.scripter.commons.io;

import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Opens a file input stream.
 */
public class FileInputStreamOpener implements InputStreamOpener {

    /**
     * Opens a file input stream.
     */
    @Override
    public InputStream openInputStream(@NotNull String location) {
        try {
            return new FileInputStream(location);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
