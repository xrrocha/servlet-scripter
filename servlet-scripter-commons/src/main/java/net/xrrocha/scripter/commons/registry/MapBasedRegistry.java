package net.xrrocha.scripter.commons.registry;

import net.xrrocha.scripter.commons.Initializable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * In-memory, map-based key/value store.
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class MapBasedRegistry<K, V> implements Registry<K, V>, Initializable, Serializable {

    /**
     * The backing map source.
     */
    private final Map<K, V> registry;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private MapBasedRegistry() {
        registry = null;
    }

    public MapBasedRegistry(@NotNull Map<K, V> registry) {
        this.registry = registry;
        initialize();
    }

    @Override
    public void initialize() {
        checkNotNull(registry, "Registry map cannot be null");
    }

    /**
     * Register a value.
     *
     * @param key   The key to be registered
     * @param value The value to be registered
     * @throws NullPointerException if the key or value are <code>null</code>
     */
    @Override
    public Optional<V> register(@NotNull K key, @NotNull V value) {
        checkNotNull(key, "Key cannot be null");
        checkNotNull(value, "Value cannot be null");
        return Optional.ofNullable(registry.put(key, value));
    }

    /**
     * Deregister a non-null value.
     *
     * @param key The value key to be deregistered
     * @return The deregistered value if one existed
     */
    @Override
    public Optional<V> deregister(@NotNull K key) {
        checkNotNull(key, "Key cannot be null");
        return Optional.ofNullable(registry.remove(key));
    }

    /**
     * Retrieve an value given its key.
     *
     * @param key The non-null value key
     * @return The retrieved value, if any
     */
    @Override
    public Optional<V> lookup(@NotNull K key) {
        checkNotNull(key, "Key cannot be null");
        return Optional.ofNullable(registry.get(key));
    }

    /**
     * Retrieve all key/value pairs.
     */
    @Override
    public Iterable<Entry<K, V>> list() {
        return registry.entrySet();
    }
}
