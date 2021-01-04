package net.xrrocha.scripter.commons.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import static net.xrrocha.scripter.commons.io.FileUtils.TEMP_DIRECTORY;
import static net.xrrocha.scripter.commons.io.FileUtils.isValidDirectory;
import static org.junit.Assert.*;

public class FileBasedRegistryIT {

    @Before
    public void waitALittle() throws Exception {
        Thread.sleep(10L);
    }

    @Test
    public void acceptsValidDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertTrue(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void rejectsFileAsDirectory() throws Exception {
        File file = new File(TEMP_DIRECTORY, "file-" + System.currentTimeMillis());
        assertTrue(file.createNewFile());
        assertFalse(isValidDirectory(file));
        assertTrue(file.delete());
    }

    @Test
    public void rejectsUnreadableDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertTrue(directory.mkdir());
        assertTrue(directory.setReadable(false));
        assertFalse(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void rejectsUnwritableDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertTrue(directory.mkdir());
        assertTrue(directory.setWritable(false));
        assertFalse(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void registersValidFile() throws Exception {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        String contents = "Scripter rocks!";
        assertFalse(registry.register(filename, contents).isPresent());
        File file = new File(directory, filename);
        file.deleteOnExit();
        assertEquals(contents, new String(Files.readAllBytes(file.toPath())));
    }

    @Test
    public void registersExistingFile() throws Exception {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        String contents1 = "Scripter rocks!";
        assertFalse(registry.register(filename, contents1).isPresent());
        File file = new File(directory, filename);
        file.deleteOnExit();
        assertEquals(contents1, new String(Files.readAllBytes(file.toPath())));
        String contents2 = "Acme sucks!";
        assertTrue(registry.register(filename, contents2).isPresent());
        assertEquals(contents2, new String(Files.readAllBytes(file.toPath())));
    }

    @Test
    public void deregistersExistingFile() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        String contents = "Scripter rocks!";
        assertFalse(registry.register(filename, contents).isPresent());
        File file = new File(directory, filename);
        file.deleteOnExit();
        Optional<String> result = registry.deregister(filename);
        assertTrue(result.isPresent());
        assertEquals(contents, result.get());
        assertFalse(file.exists());
    }

    @Test
    public void acceptsNonExistentFileOnDeregister() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        Optional<String> result = registry.deregister(filename);
        assertFalse(result.isPresent());
        assertFalse(file.exists());
    }

    @Test
    public void looksUpExistingFile() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        String contents = "Scripter rocks!";
        assertFalse(registry.register(filename, contents).isPresent());
        File file = new File(directory, filename);
        file.deleteOnExit();
        Optional<String> result = registry.lookup(filename);
        assertTrue(result.isPresent());
        assertEquals(contents, result.get());
        assertTrue(file.exists());
    }

    @Test
    public void returnEmptyOnNonExistingLookup() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        String filename = "motto";
        File file = new File(directory, filename);
        assertFalse(file.exists());
        Optional<String> result = registry.lookup(filename);
        assertFalse(result.isPresent());
        assertFalse(file.exists());
    }

    @Test
    public void ListsAllFiles() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);

        String filename1 = "motto";
        String contents1 = "Scripter rocks!";
        File file1 = new File(directory, filename1);
        assertFalse(file1.exists());
        assertFalse(registry.register(filename1, contents1).isPresent());
        assertTrue(file1.exists());

        String filename2 = "insult";
        String contents2 = "Acme sucks!";
        File file2 = new File(directory, filename2);
        assertFalse(file2.exists());
        assertFalse(registry.register(filename2, contents2).isPresent());
        assertTrue(file2.exists());

        Iterable<Entry<String, String>> list1 = registry.list();
        assertNotNull(list1);
        assertEquals(2, Iterables.size(list1));
        Map<String, String> map = ImmutableMap.of(
                filename1, contents1,
                filename2, contents2
        );
        Set<Entry<String, String>> entries = new HashSet<>();
        Iterable<Entry<String, String>> list2 = registry.list();
        list2.forEach(entries::add);
        assertEquals(map.entrySet(), entries);

        assertTrue(registry.deregister(filename1).isPresent());
        Iterator<Entry<String, String>> iterator = registry.list().iterator();
        assertTrue(iterator.hasNext());
        Entry<String, String> entry = iterator.next();
        assertEquals(filename2, entry.getKey());
        assertEquals(contents2, entry.getValue());
        assertFalse(iterator.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExistingFile() throws Exception {
        File file = File.createTempFile("existing", "file");
        new FileBasedRegistry(file);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonCreatableDirectory() {
        File baseDirectory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        baseDirectory.deleteOnExit();
        assertTrue(baseDirectory.mkdir());
        assertTrue(baseDirectory.exists());
        assertTrue(baseDirectory.setWritable(false));
        File subdirectory = new File(baseDirectory, "subdir");
        subdirectory.deleteOnExit();
        new FileBasedRegistry(subdirectory);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilenameOnRegister() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        registry.register(null, "someContents");
    }

    @Test(expected = RuntimeException.class)
    public void detectsWriteErrorOnRegister() throws Exception {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        File file = new File(directory, "file");
        file.deleteOnExit();
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
        assertTrue(file.setWritable(false));
        registry.register("file", "contents");
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilenameOnDeregister() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        registry.deregister(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidFilenameOnDeregister() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        File file = new File(directory, "!@#$%^&*()_+");
        assertFalse(file.exists());
        registry.deregister(file.getName());
    }

    @Test(expected = RuntimeException.class)
    public void detectsUnwritableFilenameOnDeregister() throws Exception {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        assertTrue(directory.setWritable(false));
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        File file = new File(directory, "file");
        file.deleteOnExit();
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
        registry.deregister(file.getName());
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFilenameOnLookup() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        registry.lookup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidFilenameOnLookup() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        File file = new File(directory, "!@#$%^&*()_+");
        assertFalse(file.exists());
        registry.lookup(file.getName());
    }

    @Test(expected = RuntimeException.class)
    public void detectsReadErrorOnLookup() throws Exception {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        File file = new File(directory, "file");
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
        assertTrue(file.setReadable(false));
        registry.lookup(file.getName());
    }

    @Test(expected = RuntimeException.class)
    public void detectsListErrorOnList() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        directory.deleteOnExit();
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());
        assertTrue(directory.setReadable(false));
        FileBasedRegistry registry = new FileBasedRegistry(directory);
        registry.list();
    }
}
