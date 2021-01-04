package net.xrrocha.scripter.commons;

import com.google.common.io.ByteStreams;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static java.util.stream.Collectors.toList;
import static net.xrrocha.scripter.commons.io.FileUtils.collectFiles;

public class JavaSourceDirectory {

    private final File sourceDirectory;
    private final String baseFilename;

    public JavaSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        sourceDirectory.mkdirs();

        baseFilename = sourceDirectory.getAbsolutePath();
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void addJavaClass(String packageName, String className, String sourceCode)
            throws IOException {

        final File packageDirectory;
        if (packageName.isEmpty()) {
            packageDirectory = sourceDirectory;
        } else {
            packageDirectory = createDirectory(packageName.replace(".", "/"));
        }

        File sourceFile = new File(packageDirectory, className + ".java");

        try (PrintWriter out = new PrintWriter(new FileWriter(sourceFile), true)) {
            out.println(String.format("package %s;", packageName));
            out.println(String.format("public class %s {", className));
            out.println(sourceCode);
            out.println("}");
        }
    }

    public void addFile(String packageName, String filename, InputStream in) throws IOException {

        final File packageDirectory;
        if (packageName.isEmpty()) {
            packageDirectory = sourceDirectory;
        } else {
            packageDirectory = createDirectory(packageName.replace(".", "/"));
        }

        File file = new File(packageDirectory, filename);
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            copy(in, fos);
        }
    }

    public boolean compile() {

        List<File> sourceFiles = collectFiles(
                sourceDirectory,
                file -> file.isDirectory() || file.getName().endsWith(".java"))
                .stream()
                .filter(File::isFile)
                .collect(toList());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                null, // diagnosticListener
                null, // locale
                null); // charset

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(sourceFiles);

        return compiler.getTask(
                null, // out
                fileManager, // fileManager
                null, // diagnosticListener
                null,  // options
                null,  // classes
                compilationUnits
        )
                .call();
    }

    public void buildJarLibrary(OutputStream destination) throws IOException {

        compile();

        List<File> resourceFiles =
                collectFiles(sourceDirectory, file -> !file.getName().endsWith(".java"))
                        .stream()
                        .filter(File::isFile)
                        .collect(toList());

        try (JarOutputStream jos = new JarOutputStream(destination)) {

            for (File resourceFile : resourceFiles) {

                String absolutePath = resourceFile.getAbsolutePath().replace("\\", "/");

                String filename = absolutePath.substring(baseFilename.length() + 1);

                JarEntry jarEntry = new JarEntry(filename);
                jarEntry.setTime(resourceFile.lastModified());
                jos.putNextEntry(jarEntry);

                try (InputStream is = new FileInputStream(resourceFile)) {
                    copy(is, jos);
                }

                jos.closeEntry();
            }
            jos.finish();
        }
    }

    private File createDirectory(String name) {
        File subdir = new File(sourceDirectory, name);
        subdir.mkdirs();
        return subdir;
    }

    private void copy(InputStream is, OutputStream os) throws IOException {
        ByteStreams.copy(is, os);
        os.flush();
        is.close();
    }
}
