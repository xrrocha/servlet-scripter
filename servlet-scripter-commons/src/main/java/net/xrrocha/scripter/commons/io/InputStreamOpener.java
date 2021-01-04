package net.xrrocha.scripter.commons.io;

import javax.validation.constraints.NotNull;
import java.io.InputStream;

/**
 * Opens a location returning its <code>InputStream</code>. This interface encapsulates
 * implementation details such as server credentials, path prefixes, etc.
 */
public interface InputStreamOpener {

    /**
     * Open an I/O resource as a <code>InputStream</code>.
     *
     * @param location The resource location
     * @return The <code>InputStream</code> containing the resource contents
     */
    InputStream openInputStream(@NotNull String location);
}
