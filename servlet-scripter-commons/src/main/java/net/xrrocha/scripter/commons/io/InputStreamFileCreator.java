package net.xrrocha.scripter.commons.io;

import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.xrrocha.scripter.commons.io.FileUtils.copyToFile;
import static net.xrrocha.scripter.commons.io.FileUtils.isValidDirectory;

/**
 * Creates a local file from URL contents.
 */
public class InputStreamFileCreator implements Initializable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(InputStreamFileCreator.class);
    private final String filePath;
    private final String urlLocation;
    private final boolean recreate;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private InputStreamFileCreator() {
        filePath = null;
        urlLocation = null;
        recreate = false;
    }

    public InputStreamFileCreator(@NotNull String filePath,
                                  @NotNull String urlLocation) {
        this(filePath, urlLocation, false);
    }

    public InputStreamFileCreator(@NotNull String filePath,
                                  @NotNull String urlLocation,
                                  boolean recreate) {

        this.filePath = filePath;
        this.urlLocation = urlLocation;
        this.recreate = recreate;

        initialize();
    }

    /**
     * Creates a local file from <code>InputStream</code> contents.
     *
     * @param directory The directory under which to create files
     * @param opener    The input stream opener
     * @return The newly created file populated from the input stream
     */
    public File createFile(@NotNull File directory, @NotNull InputStreamOpener opener) {

        checkArgument(isValidDirectory(directory),
                "Bad base directory: null, non-existent or not a directory");

        File targetFile = new File(directory, filePath);

        if (targetFile.isFile() && targetFile.canRead() && !recreate) {
            logger.debug("Skipping existing file: {}", targetFile);
        } else {

            targetFile.getParentFile().mkdirs();
            if (targetFile.isDirectory() || !targetFile.getParentFile().canWrite()) {
                String errorMessage = "Unusable file '" + filePath + "'under '" + directory + "'";
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            targetFile.getParentFile().mkdirs();
            if (!isValidDirectory(targetFile.getParentFile())) {
                String errorMessage =
                        "Cannot create resource file '" + filePath + "'under '" + directory + "'";
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            try (InputStream is = opener.openInputStream(urlLocation)) {
                logger.debug("Creating new file: '{}' from '{}'", targetFile, urlLocation);
                copyToFile(is, targetFile);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        return targetFile;
    }

    @Override
    public void initialize() {
        checkNotNull(filePath, "Path cannot be null");
        checkNotNull(urlLocation, "Url cannot be null");
    }
}
