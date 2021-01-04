package net.xrrocha.scripter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.xrrocha.scripter.commons.classloader.ParentLastClassLoaderCreator;
import net.xrrocha.scripter.commons.io.InputStreamFileCreator;
import net.xrrocha.scripter.commons.io.StringInputStreamOpener;
import org.junit.Test;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static net.xrrocha.scripter.ScriptUtils.scriptFromYaml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScriptTest {

    private final String id = "emailerTest";

    private final String language = "javascript";

    private final ScriptUsage usage = ScriptUsage.INVOCABLE_SCRIPT;

    private final String description = "Send silly email message";

    private final Set<String> services = ImmutableSet.of("emailer");

    private final Map<String, Object> globalVariables = ImmutableMap.of(
            "title", "George Bernard Shaw's Quotable Quote",
            "text", "The reasonable man adapts himself to the world.\n" +
                    "The unreasonable one persists in trying to adapt the world to himself.\n" +
                    "Therefore all progress depends on the unreasonable man.\n"
    );

    private final ParentLastClassLoaderCreator
            parentLastClassLoaderCreator =
            new ParentLastClassLoaderCreator(
                    new StringInputStreamOpener(),
                    ImmutableSet.of(
                            new InputStreamFileCreator("service.jar", "http://localhost:1234/service.jar")
                    ),
                    ImmutableSet.of(
                            new InputStreamFileCreator("resource.txt", "http://localhost:1234/respurce.txt")
                    )
            );

    private final String scriptBody =
            "// Send a silly email\n" +
                    "var recipient = 'noone@xrrocha.net';\n" +
                    "var emailResult = emailer.send(recipient, title, text);\n" +
                    "if (emailResult.responseCode == 200) {\n" +
                    "  print('Email message accepted. Message: ' + emailResult.response.message);\n" +
                    "} else {\n" +
                    "  System.err.println('Error sending email: ' + emailResult.responseCode);\n" +
                    "}\n" +
                    "emailResult;";

    @Test
    public void generatesProperStringRepresentation() {

        Script script = new Script(
                id, language, usage, description, parentLastClassLoaderCreator, services, globalVariables,
                scriptBody
        );

        Map<String, Object> properties = new LinkedHashMap<String, Object>() {
            {
                put("id", id);
                put("language", language);
                put("usage", usage);
                put("description", description);
                put("parentLastClassLoaderCreator", parentLastClassLoaderCreator);
                put("services", services);
                put("globalVariables", globalVariables);
                put("script", scriptBody);
            }
        };

        String propertyFormat =
                properties.entrySet().stream()
                        .map(entry -> entry.getKey() + "=%s")
                        .collect(joining(", "));

        String propertyString = String.format(
                "%s{" + propertyFormat + "}",
                script.getClass().getSimpleName(),
                id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                scriptBody);

        assertEquals(propertyString, script.toString());
    }

    @Test
    public void acceptsValidYamlProperties() {
        scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                scriptBody);
    }

    @Test
    public void acceptsNullYamlDescription() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                null,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankYamlDescription() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                " \t\r", // descriptions
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullYamlScript() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                null); // scriptBody
        script.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankYamlScript() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                globalVariables,
                " \t\r"); // scriptBody
        script.initialize();
    }

    @Test
    public void acceptsNullYamlServices() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                null, // services
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullYamlServices() {
        Set<String> myServices = ImmutableSet.of(null, "service");
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                myServices,
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankYamlServices() {
        Set<String> myServices = ImmutableSet.of(" \t\r\n", "service");
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                myServices,
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test
    public void acceptsNullYamlClassLoaderCreator() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                null, // parentLastClassLoaderCreator
                services,
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankYamlClassLoaderCreator() {
        Set<String> myServices = ImmutableSet.of(" \t\r\n", "service");
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                myServices, // services
                globalVariables,
                scriptBody);
        script.initialize();
    }

    @Test
    public void acceptsNullYamlVariables() {
        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                null, // globalVariables
                scriptBody);
        script.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullYamlVariables() {
        Map<String, Object> myVariables = ImmutableMap.of(
                null, "nuttin'",
                "notNull", "somethin'"
        );

        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                myVariables,
                scriptBody);
        script.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankYamlVariables() {
        Map<String, Object> myVariables = ImmutableMap.of(
                " ", "wot?",
                "notEmpty", "ok"
        );

        Script script = scriptFromYaml(id,
                language,
                usage,
                description,
                parentLastClassLoaderCreator,
                services,
                myVariables, // globalVariables
                scriptBody);
        script.initialize();
    }

    @Test
    public void acceptsNullId() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertTrue(script.getId() != null);
        UUID.fromString(script.getId());
    }

    @Test
    public void acceptsValidId() {
        Script script = new Script(
                "someId", // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertEquals("someId", script.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyId() {
        new Script(
                "", // id
                null, // usage
                null, // language
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankId() {
        new Script(
                "\t \n", // id
                null, // usage
                null, // language
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test
    public void defaultsNullLanguage() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertEquals("javascript", script.getLanguage());
    }

    @Test
    public void acceptsValidLanguage() {
        Script script = new Script(
                null, // id
                "groovy", // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "println 'Hello world!'" // script
        );
        assertEquals("groovy", script.getLanguage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyLanguage() {
        new Script(
                null, // id
                "", // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankLanguage() {
        new Script(
                null, // id
                " \t\r\n ", // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullScript() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                null // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyScript() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankScript() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                " \t\r\n" // script
        );
    }

    @Test
    public void acceptsNullDescription() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertFalse(script.getDescription().isPresent());
    }

    @Test
    public void acceptsNonEmptyDescription() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                "Voici un script", // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertTrue(script.getDescription().isPresent());
        assertEquals("Voici un script", script.getDescription().get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyDescription() {
        new Script(
                null, // id
                null, // usage
                null, // language
                "", // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankDescription() {
        new Script(
                null, // id
                null, // language
                null, // usage
                " \t\r\n ", // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
    }

    @Test
    public void acceptsNullClassLoaderCreator() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertFalse(script.getClassLoaderCreator().isPresent());
    }

    @Test
    public void acceptsValidClassLoaderCreator() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                parentLastClassLoaderCreator, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!');" // script
        );
        assertTrue(script.getClassLoaderCreator().isPresent());
    }

    @Test
    public void acceptsNullServices() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
        assertTrue(script.getServices().isEmpty());
    }

    @Test
    public void acceptsEmptyServices() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                Collections.emptySet(), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test
    public void acceptsValidServiceNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of("emailer", "host", "application"), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test
    public void acceptsAndTrimsValidServiceNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of(" emailer ", "\thost\n", "application \r\n"), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullServiceNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of(null, "host", "application"), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyServiceNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of("emailer", "", "application"), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankServiceNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of("emailer", "host", " \t\r\n"), // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test
    public void acceptsNullVariables() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                null, // globalVariables
                "print('Hello world!')" // script
        );
        assertTrue(script.getGlobalVariables().isEmpty());
    }

    @Test
    public void acceptsEmptyVariables() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                Collections.emptyMap(), // globalVariables
                "print('Hello world!')" // script
        );
        assertTrue(script.getGlobalVariables().isEmpty());
    }

    @Test
    public void acceptsValidVariables() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                ImmutableMap.of( // globalVariables
                        "firstName", "Neo",
                        "lastName", "Anderson"
                ), // globalVariables
                "print('Hello world!')" // script
        );
        assertFalse(script.getGlobalVariables().isEmpty());
        assertEquals(2, script.getGlobalVariables().size());
        assertTrue(script.getGlobalVariables().containsKey("firstName"));
        assertEquals("Neo", script.getGlobalVariables().get("firstName"));
        assertTrue(script.getGlobalVariables().containsKey("lastName"));
        assertEquals("Anderson", script.getGlobalVariables().get("lastName"));
    }

    @Test
    public void acceptsNullVariableValues() {
        Script script = new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                new HashMap<String, Object>() {{ // globalVariables
                    put("firstName", "Neo");
                    put("middleName", null);
                    put("lastName", "Anderson");
                }}, // globalVariables;
                "print('Hello world!')"); // script
        assertFalse(script.getGlobalVariables().isEmpty());
        assertEquals(3, script.getGlobalVariables().size());
        assertTrue(script.getGlobalVariables().containsKey("firstName"));
        assertEquals("Neo", script.getGlobalVariables().get("firstName"));
        assertTrue(script.getGlobalVariables().containsKey("middleName"));
        assertNull(script.getGlobalVariables().get("middleName"));
        assertTrue(script.getGlobalVariables().containsKey("lastName"));
        assertEquals("Anderson", script.getGlobalVariables().get("lastName"));
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullVariableNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                ImmutableMap.of(
                        "firstName", "Neo",
                        null, "wot?",
                        "lastName", "Anderson"
                ), // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyVariableNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                ImmutableMap.of( // globalVariables
                        "firstName", "Neo",
                        "", "wot?",
                        "lastName", "Anderson"
                ), // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankVariableNames() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                null, // services
                ImmutableMap.of( // globalVariables
                        "firstName", "Neo",
                        " \t\r\n", "wot?",
                        "lastName", "Anderson"
                ), // globalVariables
                "print('Hello world!')" // script
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsVariableAndServiceNameClash() {
        new Script(
                null, // id
                null, // language
                null, // usage
                null, // description
                null, // parentLastClassLoaderCreator
                ImmutableSet.of("firstName", "someOtherVariable"), // services
                ImmutableMap.of( // globalVariables
                        "firstName", "Neo",
                        "lastName", "Anderson"
                ), // globalVariables
                "print('Hello world!')" // script
        );
    }
}
