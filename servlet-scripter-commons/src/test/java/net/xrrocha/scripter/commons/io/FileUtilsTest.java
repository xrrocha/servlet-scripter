package net.xrrocha.scripter.commons.io;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static net.xrrocha.scripter.commons.io.FileUtils.*;
import static org.junit.Assert.*;

public class FileUtilsTest {

    @Test
    public void acceptsValidDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
        assertTrue(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void rejectsUnreadableDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
        assertTrue(directory.setReadable(false));
        assertFalse(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void rejectsUnwritableDirectory() {
        File directory = new File(TEMP_DIRECTORY, "dir-" + System.currentTimeMillis());
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
        assertTrue(directory.setWritable(false));
        assertFalse(isValidDirectory(directory));
        assertTrue(directory.delete());
    }

    @Test
    public void copiesToNewFileOnRoot() throws Exception {
        String filename = System.currentTimeMillis() + ".txt";
        File destination = new File(TEMP_DIRECTORY, filename);
        destination.deleteOnExit();
        assertFalse(destination.exists());
        String contents = "Scripter rocks!";
        InputStream is = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
        copyToFile(is, destination);
        String actualContents = CharStreams.toString(new FileReader(destination));
        assertEquals(contents, actualContents);
    }

    @Test
    public void copiesToNewFileBelowRoot() throws Exception {
        String filename = "subdir" + File.separator + System.currentTimeMillis() + ".txt";
        File destination = new File(TEMP_DIRECTORY, filename);
        destination.deleteOnExit();
        assertFalse(destination.exists());
        String contents = "Scripter rocks!";
        InputStream is = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
        copyToFile(is, destination);
        String actualContents = CharStreams.toString(new FileReader(destination));
        assertEquals(contents, actualContents);
    }

    @Test
    public void copiesToExistingFileOnRoot() throws Exception {
        String filename = System.currentTimeMillis() + ".txt";
        File destination = new File(TEMP_DIRECTORY, filename);
        destination.deleteOnExit();
        destination.createNewFile();
        assertTrue(destination.exists());
        String contents = "Scripter rocks!";
        InputStream is = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
        copyToFile(is, destination);
        String actualContents = CharStreams.toString(new FileReader(destination));
        assertEquals(contents, actualContents);
    }

    @Test
    public void copiesToExistingFileBelowRoot() throws Exception {
        String filename = "subdir" + File.separator + System.currentTimeMillis() + ".txt";
        File destination = new File(TEMP_DIRECTORY, filename);
        destination.deleteOnExit();
        destination.createNewFile();
        assertTrue(destination.exists());
        String contents = "Scripter rocks!";
        InputStream is = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
        copyToFile(is, destination);
        String actualContents = CharStreams.toString(new FileReader(destination));
        assertEquals(contents, actualContents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsOnNonWritableExistingFile() throws Exception {
        String filename = "subdir" + File.separator + System.currentTimeMillis() + ".txt";
        File destination = new File(TEMP_DIRECTORY, filename);
        destination.deleteOnExit();
        destination.createNewFile();
        destination.setWritable(false);
        assertTrue(destination.exists());
        String contents = "Scripter rocks!";
        InputStream is = new ByteArrayInputStream(contents.getBytes(Charset.defaultCharset()));
        copyToFile(is, destination);
        String actualContents = CharStreams.toString(new FileReader(destination));
        assertEquals(contents, actualContents);
    }

    @Test
    public void collectsFilesOnAndBelowRoot() throws Exception {

        String directoryName = System.currentTimeMillis() + "-dir";
        File directory = new File(TEMP_DIRECTORY, directoryName);
        directory.deleteOnExit();
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());

        String filename1 = System.currentTimeMillis() + ".txt";
        File file1 = new File(directory, filename1);
        assertFalse(file1.exists());
        assertTrue(file1.createNewFile() && file1.isFile());
        directory.deleteOnExit();
        assertTrue(file1.exists());

        String filename2 = "subdir" + File.separator + System.currentTimeMillis() + ".txt";
        File file2 = new File(directory, filename2);
        assertFalse(file2.exists());
        assertTrue(file2.getParentFile().mkdirs());
        assertTrue(file2.getParentFile().exists() && file2.getParentFile().isDirectory());
        assertTrue(file2.createNewFile() && file2.isFile());
        file2.deleteOnExit();
        file2.getParentFile().deleteOnExit();

        List<File> files = collectFiles(directory, file -> true);
        assertEquals(ImmutableList.of(file1, file2, file2.getParentFile(), directory), files);
    }

    @Test
    public void purgesAllFiles() throws Exception {

        String directoryName = System.currentTimeMillis() + "-dir";
        File directory = new File(TEMP_DIRECTORY, directoryName);
        directory.deleteOnExit();
        assertFalse(directory.exists());
        assertTrue(directory.mkdir());
        assertTrue(directory.exists());

        String filename1 = System.currentTimeMillis() + ".txt";
        File file1 = new File(directory, filename1);
        assertFalse(file1.exists());
        assertTrue(file1.createNewFile() && file1.isFile());
        directory.deleteOnExit();
        assertTrue(file1.exists());

        String filename2 = "subdir" + File.separator + System.currentTimeMillis() + ".txt";
        File file2 = new File(directory, filename2);
        assertFalse(file2.exists());
        assertTrue(file2.getParentFile().mkdirs());
        assertTrue(file2.getParentFile().exists() && file2.getParentFile().isDirectory());
        assertTrue(file2.createNewFile() && file2.isFile());
        file2.deleteOnExit();
        file2.getParentFile().deleteOnExit();

        purge(directory);
        assertFalse(directory.exists());
    }

    @Test
    public void exerciseReadyMadeDirectories() {
        System.out.println(HOME_DIRECTORY);
        System.out.println(CURRENT_DIRECTORY);
    }
}
