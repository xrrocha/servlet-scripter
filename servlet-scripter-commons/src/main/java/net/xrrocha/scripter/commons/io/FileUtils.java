package net.xrrocha.scripter.commons.io;

import com.google.common.io.ByteStreams;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mix-in for file-aware.
 */
public class FileUtils {

    /**
     * The ready-made, os-dependent temporary directory.
     */
    public static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    /**
     * The ready-made, os-dependent temporary directory.
     */
    public static final File HOME_DIRECTORY = new File(System.getProperty("user.home"));

    /**
     * The ready-made, os-dependent temporary directory.
     */
    public static final File CURRENT_DIRECTORY = new File(System.getProperty("user.dir"));

    public static boolean isValidDirectory(@NotNull File directory) {

        if (directory == null) {
            return false;
        }

        directory.mkdirs();
        return directory.isDirectory() && directory.canRead() && directory.canWrite();
    }

    public static void copyToFile(@NotNull InputStream in, @NotNull File destination) {
        destination.getParentFile().mkdirs();
        checkArgument(!destination.exists() || (destination.isFile() && destination.canWrite()));
        try {
            ByteStreams.copy(in, new FileOutputStream(destination));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void purge(@NotNull File file) {
        purge(file, true);
    }

    public static void purge(@NotNull File file, boolean deleteFile) {
        if (file.exists()) {
            List<File> files = collectFiles(file, aFile -> true);
            files.forEach(File::delete);
            if (deleteFile) {
                file.delete();
            }
        }
    }

    public static List<File> collectFiles(@NotNull File file, @NotNull FileFilter filter) {
        List<File> files = new ArrayList<>();
        collectFiles(file, filter, files);
        files.sort((first, second) -> (int) (first.length() - second.length()));
        return files;
    }

    public static void collectFiles(@NotNull File base,
                                    @NotNull FileFilter filter,
                                    @NotNull List<File> fileList) {
        File[] children = base.listFiles(filter);
        checkNotNull(children, "Null children returned from listFiles()!");
        Arrays.sort(children, File::compareTo);
        for (File child : children) {
            if (child.isDirectory()) {
                collectFiles(child, filter, fileList);
            } else {
                fileList.add(child);
            }
        }
        fileList.add(base);
    }
}
