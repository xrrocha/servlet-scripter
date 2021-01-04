package net.xrrocha.scripter.commons.registry;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class MapBasedRegistryTest {

    private final Data john = new Data("1", "John Doe");
    private final Data janet = new Data("2", "Janet Doe");
    private final Data neo = new Data("3", "Neo Anderson");
    private final Data smith = new Data("4", "Agent Smith");

    @Test
    public void registersInstance() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        assertFalse(registry.lookup(john.id).isPresent());
        registry.register(john.id, john);
        assertTrue(registry.lookup(john.id).isPresent());
    }

    @Test
    public void returnsPreviousEntityOnRegister() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        assertFalse(registry.lookup(john.id).isPresent());
        registry.register(john.id, john);
        assertTrue(registry.lookup(john.id).isPresent());
        assertTrue(registry.register(john.id, janet).isPresent());
        assertTrue(registry.lookup(john.id).isPresent());
        assertEquals(janet, registry.lookup(john.id).get());
    }

    @Test
    public void deregistersInstance() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        assertFalse(registry.lookup(neo.id).isPresent());
        registry.register(neo.id, neo);
        assertTrue(registry.lookup(neo.id).isPresent());
        assertTrue(registry.deregister(neo.id).isPresent());
        assertFalse(registry.lookup(neo.id).isPresent());
    }

    @Test
    public void looksUpInstance() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        assertFalse(registry.lookup(neo.id).isPresent());
        registry.register(neo.id, neo);
        assertTrue(registry.lookup(neo.id).isPresent());
    }

    @Test
    public void listsAllInstances() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());

        registry.register(neo.id, neo);
        registry.register(john.id, john);
        registry.register(janet.id, janet);
        registry.register(smith.id, smith);

        Set<Entry<String, Data>> entrySet = ImmutableMap.of(
                neo.id, neo,
                john.id, john,
                janet.id, janet,
                smith.id, smith
        )
                .entrySet();

        Stream<Entry<String, Data>> stream =
                StreamSupport.stream(registry.list().spliterator(), false);
        assertTrue(stream.allMatch(entrySet::contains));
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullKeyOnRegister() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        registry.register(null, janet);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullInstanceOnRegister() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        registry.register(smith.id, null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullKeyOnDeregister() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        registry.deregister(null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullKeyOnLookup() {
        MapBasedRegistry<String, Data> registry =
                new MapBasedRegistry<>(new ConcurrentHashMap<>());
        registry.lookup(null);
    }

    class Data {

        String id;
        String name;

        public Data(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("name", name)
                    .toString();
        }
    }
}
