package net.xrrocha.scripter.commons.classloader;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Class loader with inverse delegation policy (Child-first).
 * <p>
 * This class exists despite Java 9's module support so as to enable Java 8 (and back) libraries to
 * be used by scripts.
 */
public class ParentLastUrlClassLoader extends URLClassLoader {

    public ParentLastUrlClassLoader(@NotNull URL[] urls, @NotNull ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Always throws {@link ClassNotFoundException}. Is called if parent class loader did not find
     * class.
     */
    @Override
    protected final Class findClass(@NotNull String name)
            throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    @Override
    protected Class loadClass(@NotNull String name, boolean resolve)
            throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            /*
             * Check if we have already loaded this class.
             */
            Class c = findLoadedClass(name);

            if (c == null) {
                try {
                    /*
                     * We haven't previously loaded this class, try load it now
                     * from SUPER.findClass()
                     */
                    c = super.findClass(name);
                } catch (ClassNotFoundException ignore) {
                    /*
                     * Child did not find class, try parent.
                     */
                    return super.loadClass(name, resolve);
                }
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    @Override
    public URL getResource(@NotNull String name) {

        URL url = findResource(name);

        if (url == null) {
            return super.getResource(name);
        }

        return url;
    }

    @Override
    public Enumeration<URL> getResources(@NotNull String name) throws IOException {

        Enumeration<URL> childResources = findResources(name);
        List<URL> localUrlList = Collections.list(childResources);

        if (getParent() != null) {
            localUrlList.addAll(Collections.list(getParent().getResources(name)));
        }

        return Collections.enumeration(localUrlList);
    }
}
