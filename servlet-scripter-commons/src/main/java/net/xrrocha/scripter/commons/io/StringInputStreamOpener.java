package net.xrrocha.scripter.commons.io;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Opens an input stream from a <code>String</code>.
 */
public class StringInputStreamOpener implements InputStreamOpener {

    /**
     * Opens an input stream from a <code>String</code>.
     */
    @Override
    public InputStream openInputStream(@NotNull String location) {
        checkNotNull(location, "String location cannot be null");
        return new ByteArrayInputStream(location.getBytes());
    }
}
