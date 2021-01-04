package net.xrrocha.scripter.commons.registry;

import net.xrrocha.scripter.commons.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * In-memory caching implementation of @see{Registry}.
 *
 * @param <K> The key type
 * @param <V> The value type
 */
public class CachingRegistry<K, V> implements Registry<K, V>, Initializable, Serializable {

    private static final int INITIAL_CAPACITY = 1024;
    private static final float LOAD_FACTOR = 0.75F;
    private final Registry<K, V> delegate;
    private final Map<K, V> cache =
            new LinkedHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, true);

    private final Logger logger = LoggerFactory.getLogger(CachingRegistry.class);

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private CachingRegistry() {
        delegate = null;
    }

    public CachingRegistry(@NotNull Registry<K, V> delegate) {
        this.delegate = delegate;
        initialize();
    }

    @Override
    public Optional<V> register(@NotNull K key, @NotNull V value) {

        logger.debug("Registering '" + key + "' with '" + value + "'");

        if (cache.containsKey(key) && value.equals(cache.get(key))) {
            return Optional.of(value);
        }

        Optional<V> previousValue = delegate.register(key, value);
        cache.put(key, value);

        return previousValue;
    }

    @Override
    public Optional<V> deregister(@NotNull K key) {

        logger.debug("Deregistering '" + key + "'");

        cache.remove(key);
        return delegate.deregister(key);
    }

    @Override
    public Optional<V> lookup(@NotNull K key) {

        V value = cache.get(key);
        logger.debug("Looking up '" + key + "': " + value);

        return Optional.ofNullable(value);
    }

    @Override
    public Iterable<Entry<K, V>> list() {

        logger.debug("Listing " + cache.size() + " entries");
        return cache.entrySet();
    }

    @Override
    public void initialize() {
        checkNotNull(delegate, "Delegate can't be null");

        // Pre-populate cache from delegate registry
        delegate.list().forEach(entry -> cache.put(entry.getKey(), entry.getValue()));
    }
}
