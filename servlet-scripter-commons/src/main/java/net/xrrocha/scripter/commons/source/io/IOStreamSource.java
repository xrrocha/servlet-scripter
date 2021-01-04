package net.xrrocha.scripter.commons.source.io;

import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for filename-based sources.
 *
 * @param <T> The type of the i/o stream
 */
public abstract class IOStreamSource<T> implements Initializable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(FileOutputStreamSource.class);
    /**
     * The filename to create the stream from.
     */
    private final String filename;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    protected IOStreamSource() {
        this.filename = null;
    }

    /**
     * The standard constructor.
     *
     * @param filename The filename to create the stream from
     * @throws NullPointerException if the filename is null
     */
    public IOStreamSource(@NotNull String filename) {
        this.filename = filename;
        initialize();
    }

    /**
     * The actual workhorse in change of stream instance creation.
     *
     * @param filename The filename to create the stream from
     * @return The newly created stream
     * @throws Exception if any error occurs during stream creation
     */
    protected abstract T createFromFilename(String filename) throws Exception;

    /**
     * Create a fresh file stream instance.
     *
     * @return A newly created file stream instance
     * @throws NullPointerException if the filename is null
     */
    public T getObject() {
        try {
            T instance = createFromFilename(filename);
            checkNotNull(instance, "Newly created instance cannot be null");
            return instance;
        } catch (Exception e) {
            String errorMessage = "Error opening file: " + e;
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public void initialize() {
        checkNotNull(filename, "File name cannot be null");
        checkArgument(!filename.trim().isEmpty(), "File name cannot be blank");
    }
}
