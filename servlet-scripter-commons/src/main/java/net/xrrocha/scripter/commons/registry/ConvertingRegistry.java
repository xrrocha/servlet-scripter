package net.xrrocha.scripter.commons.registry;

import com.google.common.base.Converter;
import net.xrrocha.scripter.commons.Initializable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converter-based implementation of @see{Registry}. This class converts keys and values
 * to allow for a custom view over a wrapped registry.
 *
 * @param <K1> The exposed key type
 * @param <V1> The exposed value type
 * @param <K2> The actual key type
 * @param <V2> The actual value type
 */
@SuppressWarnings("unchecked")
public class ConvertingRegistry<K1, V1, K2, V2>
        implements Registry<K1, V1>, Initializable, Serializable {

    private final Registry<K2, V2> delegate;

    private final Converter<K1, K2> keyConverter;
    private final Converter<V1, V2> valueConverter;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private ConvertingRegistry() {
        delegate = null;
        keyConverter = null;
        valueConverter = null;
    }

    public ConvertingRegistry(@NotNull Registry<K2, V2> delegate,
                              @NotNull Converter<K1, K2> keyConverter,
                              @NotNull Converter<V1, V2> valueConverter) {
        this.delegate = delegate;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
        initialize();
    }

    public Optional<V1> register(@NotNull K1 key, @NotNull V1 value) {
        return delegate.register(keyConverter.convert(key),
                valueConverter.convert(value))
                .map(valueConverter.reverse()::convert);
    }

    public Optional<V1> deregister(@NotNull K1 key) {
        return delegate.deregister(keyConverter.convert(key))
                .map(valueConverter.reverse()::convert);
    }

    public Optional<V1> lookup(@NotNull K1 key) {
        return delegate.lookup(keyConverter.convert(key))
                .map(valueConverter.reverse()::convert);
    }

    @Override
    public Iterable<Entry<K1, V1>> list() {

        Iterator<Entry<K2, V2>> list = delegate.list().iterator();

        return () -> new Iterator<Entry<K1, V1>>() {
            @Override
            public boolean hasNext() {
                return list.hasNext();
            }

            @Override
            public Entry<K1, V1> next() {
                Entry<K2, V2> entry = list.next();
                return new SimpleImmutableEntry<>(keyConverter.reverse().convert(entry.getKey()),
                        valueConverter.reverse().convert(entry.getValue()));
            }
        };
    }

    @Override
    public void initialize() {
        checkNotNull(delegate, "Delegate cannot be null");
        checkNotNull(keyConverter, "Key converter 1 cannot be null");
        checkNotNull(valueConverter, "Value converter 1 cannot be null");
        checkNotNull(keyConverter.reverse(), "Key converter 2 cannot be null");
        checkNotNull(valueConverter.reverse(), "Value converter 2 cannot be null");
    }
}
