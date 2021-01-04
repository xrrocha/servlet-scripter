package net.xrrocha.scripter.commons.registry;

import javax.validation.constraints.NotNull;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Key/value store.
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public interface Registry<K, V> {

    /**
     * Register an value.
     *
     * @param key   The key to be registered
     * @param value The value to be registered
     * @throws NullPointerException if the key or value are <code>null</code>
     */
    Optional<V> register(@NotNull K key, @NotNull V value);

    /**
     * Deregister a non-null value.
     *
     * @param key The value key to be deregistered
     * @return The deregistered value if one existed
     */
    Optional<V> deregister(@NotNull K key);

    /**
     * Retrieve an value given its key.
     *
     * @param key The non-null value key
     * @return The retrieved value, if any
     */
    Optional<V> lookup(@NotNull K key);

    /**
     * Retrieve all key/value pairs.
     *
     * @return All key/value pairs
     */
    Iterable<Entry<K, V>> list();

}

