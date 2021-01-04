package net.xrrocha.scripter.commons.classloader;

import com.google.common.collect.ImmutableSet;
import net.xrrocha.scripter.commons.io.InputStreamFileCreator;
import net.xrrocha.scripter.commons.io.StringInputStreamOpener;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;

public class ParentLastClassLoaderCreatorTest {

    private final InputStreamFileCreator creator =
            new InputStreamFileCreator("a/b/c/scripter.txt", TEMP_DIRECTORY.toURI().toString());

    @Test
    public void acceptsNullJarUrls() {
        new ParentLastClassLoaderCreator(
                new StringInputStreamOpener(), // opener
                null, // jarUrls
                ImmutableSet.of(creator) // resourceUrls
        );
    }

    @Test
    public void acceptsNullResourceUrls() {
        new ParentLastClassLoaderCreator(
                new StringInputStreamOpener(), // opener
                ImmutableSet.of(creator), // jarUrls
                null // resourceUrls
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullJarAndResourceUrls() {
        new ParentLastClassLoaderCreator(
                new StringInputStreamOpener(), // opener
                null, // jarUrls
                null // resourceUrls
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullJarUrl() {
        new ParentLastClassLoaderCreator(
                new StringInputStreamOpener(), // opener
                new HashSet<InputStreamFileCreator>() {{
                    add(null);
                }}, // jarUrls
                null // resourceUrls
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullResourceUrl() {
        new ParentLastClassLoaderCreator(
                new StringInputStreamOpener(), // opener
                null, // jarUrls
                new HashSet<InputStreamFileCreator>() {{
                    add(null);
                }} // resourceUrls
        );
    }
}
