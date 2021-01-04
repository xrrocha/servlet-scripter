package net.xrrocha.scripter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.xrrocha.scripter.transformer.ScriptTransformer;
import org.junit.Before;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static net.xrrocha.scripter.commons.YamlUtils.YAML;
import static net.xrrocha.scripter.commons.io.FileUtils.purge;
import static org.junit.Assert.*;

public class ScripterTest {

    private File registryDirectory;

    @Before
    public void purgeRegistryDirectory() {
        purge(getRegistryDirectory());
        assertFalse(getRegistryDirectory().exists());
    }

    @Test
    public void appliesConfigurers() {

    }

    @Test
    public void appliesTransformers() {

    }

    @Test
    public void addsAndExecutesNewScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId = "script";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "name.toUpperCase()\n" // script
        );
        scripter.addScript(YAML.dump(script));

        Object result = scripter.executeScript(script.getId(), emptyMap());
        assertNotNull(result);
        assertEquals("SCRIPTER", result);
    }

    @Test
    public void transformsScript() {

        ScriptTransformer scriptTransformer = script -> new Script(
                script.getId(),
                "nashorn", // language
                script.getUsage(),
                script.getDescription().orElse(null),
                script.getClassLoaderCreator().orElse(null),
                script.getServices(),
                script.getGlobalVariables(),
                script.getScript()
        );

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                ImmutableMap.of("javascript", scriptTransformer),
                emptyMap()
        );

        String scriptId = "script";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "name.toUpperCase()\n" // script
        );

        Script transformedScript = scripter.tryAndTransform(script);
        assertEquals("nashorn", transformedScript.getLanguage());
    }

    @Test
    public void registersAndExecutesInvocableScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        assertEquals(0, Iterables.size(scripter.listScriptIds()));

        String scriptId = "script";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "name.toUpperCase()\n" // script
        );

        scripter.addScript(YAML.dump(script));
        Iterator<String> iterator = scripter.listScriptIds().iterator();
        assertTrue(iterator.hasNext());
        assertEquals("SCRIPTER", scripter.executeScript(iterator.next(), null));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void registersAndProvidesServiceScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        assertEquals(0, Iterables.size(scripter.listScriptIds()));

        String serviceId = "normalizer";
        Script serviceScript = new Script(
                serviceId, // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "({normalize: function(s){return s.trim().replaceAll('\\\\s+', ' ')}})\n" // script
        );
        scripter.addScript(YAML.dump(serviceScript));
        Iterator<String> iterator = scripter.listScriptIds().iterator();
        assertTrue(iterator.hasNext());
        assertEquals(serviceId, iterator.next());
        assertFalse(iterator.hasNext());

        String scriptId = "scriptie";
        Script invocableScript = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                ImmutableSet.of(serviceId), // services
                null, // variables
                "normalizer.normalize(name);\n" // script
        );
        scripter.addScript(YAML.dump(invocableScript));

        String unnormalizedString = " \t\nScripter \t\rrocks! \t";
        Object result = scripter.executeScript(scriptId, ImmutableMap.of("name", unnormalizedString));
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("Scripter rocks!", result.toString());
    }

    @Test
    public void replacesScriptWithSameId() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        assertEquals(0, Iterables.size(scripter.listScriptIds()));

        String scriptId = "scriptie";
        Script script1 = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "'scripter rocks!'.toUpperCase()" // script
        );

        scripter.addScript(YAML.dump(script1));
        Iterator<String> scriptNames1 = scripter.listScriptIds().iterator();
        assertTrue(scriptNames1.hasNext());
        assertEquals(script1.getId(), scriptNames1.next());
        assertFalse(scriptNames1.hasNext());
        assertEquals("SCRIPTER ROCKS!", scripter.executeScript(script1.getId(), null));

        Script script2 = new Script(
                script1.getId(),
                "groovy", // language
                script1.getUsage(),
                script1.getDescription().orElse(null),
                script1.getClassLoaderCreator().orElse(null),
                script1.getServices(),
                script1.getGlobalVariables(),
                "'ACME SUCKS!'.toLowerCase()"
        );
        scripter.addScript(YAML.dump(script2), true);
        Iterator<String> scriptNames2 = scripter.listScriptIds().iterator();
        assertTrue(scriptNames2.hasNext());
        assertEquals(script1.getId(), scriptNames2.next());
        assertFalse(scriptNames2.hasNext());
        assertEquals("acme sucks!", scripter.executeScript(script1.getId(), null));
    }

    @Test
    public void removesScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        assertEquals(0, Iterables.size(scripter.listScriptIds()));

        String scriptId = "scriptie";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "'Scripter rocks!'.toUpperCase()" // script
        );
        scripter.addScript(YAML.dump(script));

        Iterator<String> scriptNames = scripter.listScriptIds().iterator();
        assertTrue(scriptNames.hasNext());
        assertEquals(script.getId(), scriptNames.next());
        assertFalse(scriptNames.hasNext());
        assertEquals("SCRIPTER ROCKS!", scripter.executeScript(script.getId(), null));

        scripter.removeScript(script.getId());
        assertEquals(0, Iterables.size(scripter.listScriptIds()));
    }

    @Test
    public void preparesInvocableScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        Script script = new Script(
                "script", // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "'Scripter rocks!'.toUpperCase()" // script
        );
        String scriptYamlString = YAML.dump(script);
        PreparedObject preparedObject = scripter.prepareObject(script, scriptYamlString);
        assertTrue(preparedObject instanceof PreparedScript);
    }

    @Test
    public void preparesServiceScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String serviceId = "normalizer";
        Script serviceScript = new Script(
                serviceId, // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "({normalize: function(s){return s.trim().replaceAll('\\\\s+', ' ')}})\n" // script
        );

        String scriptYamlString = YAML.dump(serviceScript);
        PreparedObject preparedObject = scripter.prepareObject(serviceScript, scriptYamlString);
        assertTrue(preparedObject instanceof PreparedService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectNonExistentScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        scripter.executeScript("nonExistent", null);
    }

    @Test
    public void acceptsNullValueReturnedByScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId = "maScript";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "null;\n" // script
        );
        scripter.addScript(YAML.dump(script));

        assertNull(scripter.executeScript(scriptId, null));
    }

    @Test
    public void replacesExistingScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId = "script";
        Script script1 = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "name.toUpperCase()\n" // script
        );
        scripter.addScript(YAML.dump(script1));
        assertEquals("SCRIPTER", scripter.executeScript(scriptId, emptyMap()));

        Script script2 = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "SCRIPTER"), // variables
                "name.toLowerCase()\n" // script
        );
        scripter.addScript(YAML.dump(script2), true);
        assertEquals("scripter", scripter.executeScript(scriptId, emptyMap()));
    }

    @Test
    public void populatesBindingsInExpectedOrder() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        Script serviceScript = new Script(
                "normalizer", // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "({normalize: function(s){return s.trim().replaceAll('\\\\s+', ' ')}})\n" // script
        );
        scripter.addScript(YAML.dump(serviceScript));

        Script sourceScript = new Script(
                "script", // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                ImmutableSet.of(serviceScript.getId()), // services
                ImmutableMap.of("name", "scripter"), // variables
                "normalizer.normalize(name);\n" // script
        );
        scripter.addScript(YAML.dump(sourceScript));
        Script registeredScript = scripter.getScript(sourceScript.getId()).get();

        Bindings bindings1 = new SimpleBindings();
        scripter.populateBindings(registeredScript, null, bindings1);
        assertTrue(bindings1.containsKey("normalizer"));
        assertNotNull(bindings1.get("normalizer"));
        assertTrue(bindings1.containsKey("name"));
        assertNotNull(bindings1.get("name"));
        assertEquals("scripter", bindings1.get("name").toString());

        Bindings bindings2 = new SimpleBindings();
        Map<String, Object> invocationVariables = ImmutableMap.of("name", "xrrocha.net");
        scripter.populateBindings(registeredScript, invocationVariables, bindings2);
        assertTrue(bindings2.containsKey("normalizer"));
        assertNotNull(bindings2.get("normalizer"));
        assertTrue(bindings2.containsKey("name"));
        assertNotNull(bindings2.get("name"));
        assertEquals("xrrocha.net", bindings2.get("name").toString());
    }

    @Test
    public void allowsServiceRemovalOnNoDependents() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );
        assertFalse(scripter.listScriptIds().iterator().hasNext());

        Script serviceScript = new Script(
                "normalizer", // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "({normalize: function(s){return s.trim().replaceAll('\\\\s+', ' ')}})\n" // script
        );
        scripter.addScript(YAML.dump(serviceScript));
        assertTrue(scripter.listScriptIds().iterator().hasNext());

        scripter.removeScript(serviceScript.getId());
        assertFalse(scripter.listScriptIds().iterator().hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void detectsOrphanedScriptsOnServiceRemoval() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        Script serviceScript = new Script(
                "normalizer", // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "({normalize: function(s){return s.trim().replaceAll('\\\\s+', ' ')}})\n" // script
        );
        scripter.addScript(YAML.dump(serviceScript));

        Script sourceScript = new Script(
                "script", // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                ImmutableSet.of(serviceScript.getId()), // services
                ImmutableMap.of("name", "scripter"), // variables
                "normalizer.normalize(name);\n" // script
        );
        scripter.addScript(YAML.dump(sourceScript));

        scripter.removeScript(serviceScript.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removesExistingScript() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId = "script1";
        Script script = new Script(
                scriptId, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "Alex"), // variables
                "name\n" // script
        );
        scripter.addScript(YAML.dump(script));

        Object result = scripter.executeScript(script.getId(), emptyMap());
        assertNotNull(result);
        assertEquals("Alex", result);

        scripter.removeScript(script.getId());

        scripter.executeScript(script.getId(), emptyMap());
    }

    @Test
    public void listsScriptsNames() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        assertFalse(scripter.listScriptIds().iterator().hasNext());

        String scriptId1 = "script1";
        Script script1 = new Script(
                scriptId1, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "print(name)\n" // script
        );
        scripter.addScript(YAML.dump(script1));
        assertEquals(1, Iterables.size(scripter.listScriptIds()));

        String scriptId2 = "script2";
        Script script2 = new Script(
                scriptId2, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "print(name)\n" // script
        );
        scripter.addScript(YAML.dump(script2));
        assertEquals(2, Iterables.size(scripter.listScriptIds()));

        scripter.removeScript(script2.getId());
        assertEquals(1, Iterables.size(scripter.listScriptIds()));
    }

    @Test
    public void addsAndExposesService() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId1 = "script1";
        Script script1 = new Script(
                scriptId1, // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "({\"name\": name})\n" // script
        );
        scripter.addScript(YAML.dump(script1));

        String scriptId2 = "script2";
        Script script2 = new Script(
                scriptId2, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                ImmutableSet.of(scriptId1), // services
                null, // variables
                scriptId1 + ".name.toUpperCase();\n" // script
        );
        scripter.addScript(YAML.dump(script2));

        Object result =
                scripter.executeScript(script2.getId(), emptyMap());
        assertNotNull(result);
        assertEquals("SCRIPTER", result);
    }

    @Test
    public void removesServiceRemovalWithoutDependants() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId1 = "script1";
        Script script1 = new Script(
                scriptId1, // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "scripter"), // variables
                "({\"name\": name})\n" // script
        );
        scripter.addScript(YAML.dump(script1));

        String scriptId2 = "script2";
        Script script2 = new Script(
                scriptId2, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                null, // variables
                "print(" + scriptId1 + ".name)\n" // script
        );
        scripter.addScript(YAML.dump(script2));

        scripter.removeScript(script1.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsServiceRemovalWithServiceDependants() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );

        String scriptId1 = "script1";
        Script script1 = new Script(
                scriptId1, // id,
                "javascript", // language
                ScriptUsage.REUSABLE_SERVICE, // usage
                null, // description
                null, // classLoaderCreator
                null, // services
                ImmutableMap.of("name", "alex"), // variables
                "({\"name\": \" + name + \"})\n" // script
        );
        scripter.addScript(YAML.dump(script1));

        String scriptId2 = "script2";
        Script script2 = new Script(
                scriptId2, // id,
                "javascript", // language
                ScriptUsage.INVOCABLE_SCRIPT, // usage
                null, // description
                null, // classLoaderCreator
                ImmutableSet.of(scriptId1), // services
                null, // variables
                "print(" + scriptId1 + ".name)\n" // script
        );
        scripter.addScript(YAML.dump(script2));

        scripter.removeScript(scriptId1);
    }

    @Test
    public void ignoresNonExistentScriptRemoval() {

        Scripter scripter = new Scripter(
                getRegistryDirectory(),
                emptyMap(),
                emptyMap()
        );
        scripter.removeScript("nonExistent");
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullYamlScriptRegistry() {
        String yamlString =
                "--- !scripter\n" +
                        "registryDirectory: null\n";
        YAML.load(yamlString);
    }

    @Test
    public void acceptsNullTransformers() {

        new Scripter(
                getRegistryDirectory(),
                null,
                emptyMap()
        );
    }

    private File getRegistryDirectory() {
        if (registryDirectory == null) {
            registryDirectory = new File(
                    System.getProperty("java.io.tmpdir") +
                            File.separator + "registry-" + System.currentTimeMillis()
            );
            registryDirectory.mkdir();
            registryDirectory.deleteOnExit();
        }
        return registryDirectory;
    }
}
