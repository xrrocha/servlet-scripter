package net.xrrocha.scripter.commons.registry;

import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static net.xrrocha.scripter.commons.io.FileUtils.CURRENT_DIRECTORY;


/**
 * Filesystem-based <code>String</code> key/value store.
 */
public class FileBasedRegistry
        implements Registry<String, String>, Initializable, Serializable {

    public static final String DEFAULT_FILENAME_REGEX =
            "^[a-zA-Z][-_a-zA-Z0-9]*(\\.[_a-zA-Z0-9]+)?$";
    private static final Pattern defaultFilenamePattern = Pattern.compile(DEFAULT_FILENAME_REGEX);
    private final File directory;
    private final String filenameRegex;
    private final Logger logger = LoggerFactory.getLogger(FileBasedRegistry.class);
    private Pattern filenamePattern;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private FileBasedRegistry() {
        directory = CURRENT_DIRECTORY;
        filenameRegex = DEFAULT_FILENAME_REGEX;
    }

    public FileBasedRegistry(@NotNull File directory) {
        this(directory, null);
    }

    public FileBasedRegistry(@NotNull File directory,
                             String filenameRegex) {

        if (directory == null) {
            this.directory = CURRENT_DIRECTORY;
        } else {
            this.directory = directory;
        }

        this.filenameRegex = filenameRegex;

        initialize();
    }

    @Override
    public Optional<String> register(@NotNull String filename,
                                     @NotNull String contents) {
        checkNotNull(filename, "Filename cannot be null");
        checkNotNull(contents, "Contents cannot be null");
        checkArgument(filenamePattern.matcher(filename).matches(),
                "Invalid filename: " + filename);

        logger.debug("Registering '" + filename + "' with '" + contents + "'");

        File file = new File(directory, filename);

        try {

            final String previousContents;
            if (file.exists()) {
                previousContents = new String(Files.readAllBytes(file.toPath()));
            } else {
                previousContents = null;
            }

            Files.write(file.toPath(), contents.getBytes());

            return Optional.ofNullable(previousContents);
        } catch (Exception e) {
            String errorMessage = "Error creating file '" + filename + "': " + e;
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public Optional<String> deregister(@NotNull String filename) {
        checkNotNull(filename, "Filename cannot be null");
        checkArgument(defaultFilenamePattern.matcher(filename).matches(),
                "Invalid filename: " + filename);

        logger.debug("Deregistering '" + filename + "'");

        File file = new File(directory, filename);

        final String previousContents;
        try {
            if (file.exists()) {
                previousContents = new String(Files.readAllBytes(file.toPath()));
                if (!file.delete()) {
                    String errorMessage = "Can't delete file '" + filename + "'";
                    logger.error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            } else {
                previousContents = null;
            }

            return Optional.ofNullable(previousContents);
        } catch (IOException e) {
            String errorMessage = "Error reading file '" + filename + "': " + e;
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public Optional<String> lookup(@NotNull String filename) {
        checkNotNull(filename, "Filename cannot be null");
        checkArgument(defaultFilenamePattern.matcher(filename).matches(),
                "Invalid filename: " + filename);

        File file = new File(directory, filename);

        final String contents;
        try {
            if (file.exists()) {
                contents = new String(Files.readAllBytes(file.toPath()));
            } else {
                contents = null;
            }

            logger.debug("Looking up '" + filename + "' with " + contents);

            return Optional.ofNullable(contents);
        } catch (IOException e) {
            String errorMessage = "Error reading file '" + filename + "': " + e;
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public Iterable<Entry<String, String>> list() {

        File[] files = directory.listFiles(File::isFile);
        if (files == null) {
            String errorMessage = "Error listing files under '" + directory + "'";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        logger.debug("Listing " + files.length + " files");

        return Arrays.stream(files)
                .map(file -> {
                    try {
                        String contents = new String(Files.readAllBytes(file.toPath()));
                        return new SimpleImmutableEntry<>(file.getName(), contents);
                    } catch (IOException e) {
                        String errorMessage = "Error reading file '" + file + "': " + e;
                        logger.error(errorMessage, e);
                        throw new IllegalArgumentException(errorMessage, e);
                    }
                })
                .collect(toList());
    }

    @Override
    public void initialize() {
        checkNotNull(directory, "Directory cannot be null");
        checkArgument(!directory.exists() ||
                        (directory.isDirectory() && directory.canRead() && directory.canWrite()),
                "Unreadable/unwritable directory: " + directory);

        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Existing file '" + directory + "' is not a directory");
        }

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalArgumentException("Can't create directory: " + directory);
        }

        if (filenameRegex != null) {
            filenamePattern = Pattern.compile(filenameRegex);
        } else {
            filenamePattern = defaultFilenamePattern;
        }
    }

    public File getDirectory() {
        return directory;
    }
}
