package net.xrrocha.scripter.commons.io;

import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Classpath-based @see{UrlOpener} implementation.
 */
public class ResourceInputStreamOpener implements InputStreamOpener, Initializable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ResourceInputStreamOpener.class);
    private final ClassLoader classLoader;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    public ResourceInputStreamOpener() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ResourceInputStreamOpener(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
        initialize();
    }

    /**
     * Open a classpath URL returning its <code>InputStream</code>.
     *
     * @param location The URL string representation
     * @return The <code>InputStream</code> containing the URL's contents
     */
    @Override
    public InputStream openInputStream(@NotNull String location) {
        URL resourceUrl = classLoader.getResource(location);
        checkArgument(resourceUrl != null, "Non-existent resource: " + location);
        try {
            return resourceUrl.openStream();
        } catch (IOException e) {
            String errorMessage = "Error opening resource: " + location + ": " + e;
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public void initialize() {
        checkNotNull(classLoader, "Class loader cannot be null");
    }
}
