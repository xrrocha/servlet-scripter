package net.xrrocha.scripter.commons.classloader;

import com.google.common.collect.ImmutableSet;
import net.xrrocha.scripter.commons.io.FileInputStreamOpener;
import net.xrrocha.scripter.commons.io.InputStreamFileCreator;
import net.xrrocha.scripter.commons.io.InputStreamOpener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static java.util.Collections.emptySet;
import static junit.framework.TestCase.assertTrue;
import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;
import static net.xrrocha.scripter.commons.io.FileUtils.purge;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ParentLastClassLoaderCreatorIT {

    private final File jarDirectory = new File(TEMP_DIRECTORY,
            "class-loader-" + System.currentTimeMillis());

    private final File resourceDirectory = new File(jarDirectory, "resources");

    private final InputStreamOpener opener = new FileInputStreamOpener();

    private final File classLoaderDirectory = new File("src/test/resources/classLoader");

    @Before
    public void setUpDirectories() {
        assertFalse(jarDirectory.exists());
        assertTrue(jarDirectory.mkdir());
        assertTrue(resourceDirectory.mkdir());
    }

    @After
    public void TearDownDirectories() {
        purge(jarDirectory);
        jarDirectory.delete();
    }

    @Test
    public void createsProperClassLoader() throws Exception {
        ParentLastClassLoaderCreator classLoaderCreator =
                new ParentLastClassLoaderCreator(
                        opener,
                        ImmutableSet.of(new InputStreamFileCreator(
                                "class-loader.jar",
                                "src/test/resources/classLoader/hello-world.jar"
                        )),
                        ImmutableSet.of(new InputStreamFileCreator(
                                "a/b/c/some-resource.txt",
                                "src/test/resources/classLoader/resource.txt"
                        ))
                );
        ClassLoader classLoader =
                classLoaderCreator.createClassLoader(
                        getClass().getClassLoader(),
                        jarDirectory,
                        resourceDirectory
                );
        classLoader.loadClass("HelloWorld");
        assertNotNull(classLoader.getResource("a/b/c/some-resource.txt"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyClassLoader() {
        ParentLastClassLoaderCreator classLoaderCreator =
                new ParentLastClassLoaderCreator(
                        opener,
                        emptySet(),
                        emptySet()
                );
        classLoaderCreator.createClassLoader(
                getClass().getClassLoader(),
                jarDirectory,
                resourceDirectory
        );
    }

}
