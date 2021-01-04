package net.xrrocha.scripter.commons.io;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Opens a URL location returning its <code>InputStream</code>.
 */
public class UrlInputStreamOpener implements InputStreamOpener {

    /**
     * Open a URL resource as a <code>InputStream</code>.
     *
     * @param location The URL location
     * @return The <code>InputStream</code> containing the URL contents
     */
    @Override
    public InputStream openInputStream(@NotNull String location) {
        checkNotNull(location, "URL location cannot be null");
        try {
            return new URL(location).openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
