package net.xrrocha.scripter.commons.source;

/**
 * Repeatedly produce instances of a given generic type.
 *
 * @param <T> The type of produced instances.
 */
public interface Source<T> {

    /**
     * Create a fresh instance of the given type.
     *
     * @return A newly created instance
     */
    T getObject();
}
