package net.xrrocha.scripter.commons.classloader;

import com.google.common.io.CharStreams;
import net.xrrocha.scripter.commons.JavaSourceDirectory;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParentLastUrlClassLoaderTest {

    private final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

    private final String packageName = "net.xrrocha";
    private final String className = "Test";
    private final String codeTemplate = "public String toString() { return \"%s\"; }";
    private final String resourceName = "a/b/c/resource.txt";

    @Test
    public void loadsJarsFromChildFirst() throws Exception {

        String parentName = "père";
        String parentResourceContents = "Voici le " + parentName;
        ClassLoader parentClassLoader =
                createJarClassLoader(parentName, // classLoaderName
                        parentResourceContents, // resourceContents
                        Thread.currentThread().getContextClassLoader()); // parentClassLoader
        assertEquals(parentName, executeGeneratedToString(parentClassLoader));
        assertEquals(parentResourceContents, readResourceFile(parentClassLoader, resourceName));

        String childName = "fils";
        String childResourceContents = "Voici le " + childName;
        ClassLoader childClassLoader = createJarClassLoader(childName, // classLoaderName
                childResourceContents, // resourceContents
                parentClassLoader); // parentClassLoader
        assertEquals(childName, executeGeneratedToString(childClassLoader));
        assertEquals(childResourceContents, readResourceFile(childClassLoader, resourceName));
    }

    @Test
    public void loadsDirectoryJarFromChildFirst() throws Exception {

        String parentName = "padre";
        String parentResourceContents = "He aquí el " + parentName;
        ClassLoader parentClassLoader =
                createDirectoryClassLoader(parentName, // classLoaderName
                        parentResourceContents, // resourceContents
                        Thread.currentThread()
                                .getContextClassLoader()); // parentClassLoader
        assertEquals(parentName, executeGeneratedToString(parentClassLoader));
        assertEquals(parentResourceContents, readResourceFile(parentClassLoader, resourceName));

        String childName = "hijo";
        String childResourceContents = "He aquí el " + childName;
        ClassLoader childClassLoader = createJarClassLoader(childName, // classLoaderName
                childResourceContents, // resourceContents
                parentClassLoader); // parentClassLoader
        assertEquals(childName, executeGeneratedToString(childClassLoader));
        assertEquals(childResourceContents, readResourceFile(childClassLoader, resourceName));
    }

    @Test
    public void findsAllResources() throws Exception {
        String parentName = "père";
        String parentResourceContents = "Voici le " + parentName;
        ClassLoader parentClassLoader =
                createJarClassLoader(parentName, // classLoaderName
                        parentResourceContents, // resourceContents
                        Thread.currentThread().getContextClassLoader()); // parentClassLoader
        List<URL> parentResources = Collections.list(parentClassLoader.getResources(resourceName));
        assertNotNull(parentResources);
        assertEquals(1, parentResources.size());

        String childName = "fils";
        String childResourceContents = "Voici le " + childName;
        ClassLoader childClassLoader = createJarClassLoader(childName, // classLoaderName
                childResourceContents, // resourceContents
                parentClassLoader); // parentClassLoader
        List<URL> childResources = Collections.list(childClassLoader.getResources(resourceName));
        assertNotNull(childResources);
        assertEquals(2, childResources.size());
    }

    @Test(expected = ClassNotFoundException.class)
    public void findClassThrowsClassNotFoundException() throws Exception {
        new ParentLastUrlClassLoader(new URL[]{new File(".").toURI().toURL()},
                Thread.currentThread().getContextClassLoader())
                .findClass("java.lang.String");
    }

    private ClassLoader createJarClassLoader(String classLoaderName,
                                             String resourceContents,
                                             ClassLoader parentClassLoader)
            throws IOException {

        JavaSourceDirectory javaSourceDirectory = new JavaSourceDirectory(newTemporaryDirectory());
        createSourceFile(javaSourceDirectory, codeTemplate, classLoaderName);
        createResourceFile(javaSourceDirectory, resourceName, resourceContents);
        File jarFile = File.createTempFile("plcl", ".jar");
        javaSourceDirectory.buildJarLibrary(new FileOutputStream(jarFile));
        URL[] classpath = new URL[]{jarFile.toURI().toURL()};
        return new ParentLastUrlClassLoader(classpath, parentClassLoader);
    }

    private ClassLoader createDirectoryClassLoader(String classLoaderName,
                                                   String resourceContents,
                                                   ClassLoader parentClassLoader)
            throws IOException {

        JavaSourceDirectory javaSourceDirectory = new JavaSourceDirectory(newTemporaryDirectory());
        createSourceFile(javaSourceDirectory, codeTemplate, classLoaderName);
        createResourceFile(javaSourceDirectory, resourceName, resourceContents);
        URL[] classpath = new URL[]{javaSourceDirectory.getSourceDirectory().toURI().toURL()};
        return new ParentLastUrlClassLoader(classpath, parentClassLoader);
    }

    private String executeGeneratedToString(ClassLoader classLoader) throws Exception {
        return classLoader
                .loadClass(packageName + "." + className)
                .getDeclaredConstructor()
                .newInstance()
                .toString();
    }

    private void createSourceFile(JavaSourceDirectory sourceDirectory,
                                  String codeTemplate,
                                  String value)
            throws IOException {

        String javaCode = String.format(codeTemplate, value);
        sourceDirectory.addJavaClass(packageName, className, javaCode);
        if (!sourceDirectory.compile()) {
            throw new IllegalArgumentException("Compilation error");
        }
    }

    private void createResourceFile(JavaSourceDirectory sourceDirectory,
                                    String resourceName,
                                    String resourceContents)
            throws IOException {

        sourceDirectory.addFile("", // packageName
                resourceName,
                new ByteArrayInputStream(resourceContents.getBytes(UTF_8)));
    }

    private String readResourceFile(ClassLoader classLoader, String resourceName) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(resourceName);
        assertNotNull("No such resource: " + resourceName, inputStream);
        Reader reader = new InputStreamReader(inputStream, UTF_8);
        return CharStreams.toString(reader);
    }

    public File newTemporaryDirectory() {
        String directoryName = "res_" + System.currentTimeMillis();
        File sourceDirectory = new File(tempDirectory, directoryName);
        sourceDirectory.mkdir();
        sourceDirectory.deleteOnExit();
        return sourceDirectory;
    }

}
