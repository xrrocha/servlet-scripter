package net.xrrocha.scripter.commons.registry;

import org.junit.Test;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class CachingRegistryTest {

    @Test
    public void registers() {
        Registry<String, String> delegate = (Registry<String, String>) mock(Registry.class);
        CachingRegistry registry = new CachingRegistry<>(delegate);
        String key = "name";
        String value = "scripter";
        registry.register(key, value);
        verify(delegate, times(1)).register(key, value);
        reset(delegate);
        registry.register(key, value);
        verify(delegate, times(0)).register(key, value);
    }

    @Test
    public void deregisters() {
        Registry<String, String> delegate = (Registry<String, String>) mock(Registry.class);
        CachingRegistry registry = new CachingRegistry<>(delegate);
        String key = "name";
        String value = "scripter";
        registry.register(key, value);
        verify(delegate, times(1)).register(key, value);
        registry.list();
        registry.deregister(key);
        verify(delegate, times(1)).deregister(key);
    }

    @Test
    public void looksUp() {
        Registry<String, String> delegate = (Registry<String, String>) mock(Registry.class);
        CachingRegistry registry = new CachingRegistry<>(delegate);
        String key = "name";
        String value = "scripter";
        registry.register(key, value);
        verify(delegate, times(1)).register(key, value);
        registry.lookup(key);
        verify(delegate, times(0)).lookup(key);
    }

    @Test
    public void lists() {
        Registry<String, String> delegate = (Registry<String, String>) mock(Registry.class);
        CachingRegistry registry = new CachingRegistry<>(delegate);
        String key = "name";
        String value = "scripter";
        registry.register(key, value);
        verify(delegate, times(1)).register(key, value);
        reset(delegate);
        registry.list();
        verify(delegate, times(0)).list();
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullDelegate() {
        new CachingRegistry(null);
    }
}
