package net.xrrocha.scripter.commons.registry;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static net.xrrocha.scripter.commons.io.FileUtils.*;
import static org.junit.Assert.*;

public class FileBasedRegistryTest {

    private static final File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
    private final FileBasedRegistry registry = new FileBasedRegistry(directory);

    @Before
    public void purgeRegistryDirectory() {
        purge(directory);
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
    }

    @Test
    public void acceptsNullDirectoryName() {
        FileBasedRegistry registry = new FileBasedRegistry(null);
        assertNotNull(registry.getDirectory());
        assertTrue(isValidDirectory((registry.getDirectory())));
    }

    @Test
    public void registersNewEntry() throws Exception {
        String filename = "content.txt";
        String fileContents = "This is the content";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        registry.register(filename, fileContents);
        assertTrue(file.isFile());
        assertEquals(fileContents, CharStreams.toString(new FileReader(file)));
    }

    @Test
    public void replacesExistingEntry() throws Exception {
        String filename = "content.txt";
        String fileContents1 = "This is content #1";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        assertFalse(registry.register(filename, fileContents1).isPresent());
        assertTrue(file.isFile());
        assertEquals(fileContents1, CharStreams.toString(new FileReader(file)));
        String fileContents2 = "This is content #2";
        assertEquals(Optional.of(fileContents1), registry.register(filename, fileContents2));
        assertTrue(file.isFile());
        assertEquals(fileContents2, CharStreams.toString(new FileReader(file)));
    }

    @Test
    public void looksUpExistingEntry() throws Exception {
        String filename = "content.txt";
        String fileContents1 = "This is content #1";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        assertFalse(registry.register(filename, fileContents1).isPresent());
        assertTrue(file.isFile());
        assertEquals(Optional.of(fileContents1), registry.lookup(filename));
        assertEquals(fileContents1, CharStreams.toString(new FileReader(file)));
    }

    @Test
    public void returnsEmptyOnNonExistingEntry() {
        String filename = "content.txt";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        assertFalse(registry.lookup(filename).isPresent());
        assertFalse(file.exists());
    }

    @Test
    public void deregistersEntry() throws Exception {
        String filename = "content.txt";
        String fileContents1 = "This is content #1";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        assertFalse(registry.register(filename, fileContents1).isPresent());
        assertTrue(file.isFile());
        assertEquals(fileContents1, CharStreams.toString(new FileReader(file)));
        assertEquals(Optional.of(fileContents1), registry.deregister(filename));
        assertFalse(file.exists());
    }

    @Test
    public void listsAllEntries() throws Exception {

        assertEquals(0, Iterables.size(registry.list()));

        String filename1 = "content1.txt";
        String fileContents1 = "This is content #1";
        File file1 = new File(directory, filename1);
        assertFalse(file1.exists());
        assertFalse(registry.register(filename1, fileContents1).isPresent());
        assertTrue(file1.isFile());
        assertEquals(fileContents1, CharStreams.toString(new FileReader(file1)));
        Set<String> filenames1 =
                Lists.newArrayList(registry.list()).stream().map(Entry::getKey).collect(toSet());
        assertTrue(filenames1.size() == 1 && filenames1.contains(filename1));

        String filename2 = "content2.txt";
        String fileContents2 = "This is content #2";
        File file2 = new File(directory, filename2);
        assertFalse(registry.register(filename2, fileContents2).isPresent());
        assertTrue(file2.isFile());
        assertEquals(fileContents1, CharStreams.toString(new FileReader(file1)));
        assertEquals(fileContents2, CharStreams.toString(new FileReader(file2)));
        Set<String> filenames2 =
                Lists.newArrayList(registry.list()).stream().map(Entry::getKey).collect(toSet());
        assertTrue(filenames2.size() == 2 &&
                filenames2.contains(filename1) &&
                filenames2.contains(filename2));

        assertEquals(Optional.of(fileContents1), registry.deregister(filename1));
        assertFalse(file1.exists());
        assertTrue(file2.isFile());
        assertEquals(fileContents2, CharStreams.toString(new FileReader(file2)));
        Set<String> filenames3 =
                Lists.newArrayList(registry.list()).stream().map(Entry::getKey).collect(toSet());
        assertTrue(filenames3.size() == 1 &&
                filenames3.contains(filename2) &&
                !filenames3.contains(filename1));
    }
}
