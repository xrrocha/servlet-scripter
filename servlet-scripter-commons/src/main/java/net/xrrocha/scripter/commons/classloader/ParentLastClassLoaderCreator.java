package net.xrrocha.scripter.commons.classloader;

import net.xrrocha.scripter.commons.Initializable;
import net.xrrocha.scripter.commons.io.InputStreamFileCreator;
import net.xrrocha.scripter.commons.io.InputStreamOpener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.xrrocha.scripter.commons.io.FileUtils.isValidDirectory;

/**
 * Base class to create a new URL class loader under the specified jar and resource directories.
 * This abstract implementation defers actual class loader creations so as to enable different
 * strategies such as parent-last or child-last class/resource loading delegation models.
 */
public class ParentLastClassLoaderCreator implements Initializable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ParentLastClassLoaderCreator.class);
    private final Set<InputStreamFileCreator> jarUrls;
    private final Set<InputStreamFileCreator> resourceUrls;
    private final InputStreamOpener opener;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    protected ParentLastClassLoaderCreator() {
        opener = null;
        jarUrls = null;
        resourceUrls = null;
    }

    public ParentLastClassLoaderCreator(@NotNull InputStreamOpener opener,
                                        @NotNull Set<InputStreamFileCreator> jarUrls,
                                        @NotNull Set<InputStreamFileCreator> resourceUrls) {

        this.opener = opener;
        this.jarUrls = jarUrls;
        this.resourceUrls = resourceUrls;

        initialize();
    }

    /**
     * Creates a new URL class loader under the specified jar and resource directories.
     *
     * @param parentClassLoader The (optional) class loader to delegate to
     * @param jarDirectory      The directory to store jar files copied from URL
     * @param resourceDirectory The directory to store resource files copied from URL
     */
    public ClassLoader createClassLoader(@NotNull ClassLoader parentClassLoader,
                                         @NotNull File jarDirectory,
                                         @NotNull File resourceDirectory) {
        List<URL> outputUrls = new ArrayList<>();

        if (jarUrls != null && !jarUrls.isEmpty()) {
            checkNotNull(jarDirectory, "Jar directory cannot be null");
            populateFiles(jarUrls, jarDirectory, outputUrls);
        }

        if (resourceUrls != null && !resourceUrls.isEmpty()) {
            checkArgument(isValidDirectory(resourceDirectory), "Resource directory cannot be null");

            populateFiles(resourceUrls, resourceDirectory, null);
            try {
                outputUrls.add(resourceDirectory.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        if (outputUrls.isEmpty()) {
            throw new IllegalArgumentException("No jar or directories given for class loader");
        }
        logger.debug("outputUrls: {}}", outputUrls);

        URL[] classpathUrls = outputUrls.toArray(new URL[outputUrls.size()]);

        return new ParentLastUrlClassLoader(classpathUrls, parentClassLoader);
    }

    void populateFiles(@NotNull Set<InputStreamFileCreator> fileCreators,
                       @NotNull File directory,
                       @NotNull List<URL> outputUrls) {

        directory.mkdirs();
        if (!isValidDirectory(directory)) {
            String errorMessage = "Can't write directory: " + directory;
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        fileCreators.forEach(fileCreator -> {
            File file = fileCreator.createFile(directory, opener);
            if (outputUrls != null) {
                try {
                    outputUrls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    @Override
    public void initialize() {
        checkArgument(jarUrls != null || resourceUrls != null,
                "Both jar urls and resource urls are null");
        if (jarUrls != null) {
            checkArgument(jarUrls.stream().allMatch(Objects::nonNull), "One or more jar urls is null");
        }
        if (resourceUrls != null) {
            checkArgument(resourceUrls.stream().allMatch(Objects::nonNull),
                    "One or more resource urls is null");
        }
    }
}
