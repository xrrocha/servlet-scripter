package net.xrrocha.scripter;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
// import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

import static net.xrrocha.scripter.commons.YamlUtils.YAML;
import static net.xrrocha.scripter.commons.io.FileUtils.purge;
import static org.junit.Assert.*;

public class ScripterIT {

    @Test
    // @Ignore
    public void restoresExistingRegistry() throws Exception {

        purge(new File("target/servlet-scripter")); // see resource "scripter.yaml"

        URL scripterUrl = Resources.getResource("scripter.yaml");
        assertNotNull(scripterUrl);
        String scripterYaml = Resources.toString(scripterUrl, Charset.defaultCharset());
        Scripter scripter1 = YAML.loadAs(scripterYaml, Scripter.class);

        URL scriptUrl = Resources.getResource("script.yaml");
        assertNotNull(scriptUrl);
        String scriptYaml = Resources.toString(scriptUrl, Charset.defaultCharset());
        scripter1.addScript(scriptYaml);
        Object result1 = scripter1.executeScript("scriptTest", ImmutableMap.of());
        assertTrue(result1 instanceof String);

        Scripter scripter2 = YAML.loadAs(scripterYaml, Scripter.class);
        scripter2.addScript(scriptYaml, true);
        Object result2 = scripter1.executeScript("scriptTest", ImmutableMap.of());
        assertTrue(result2 instanceof String);
        assertEquals(result1, result2);
    }
}
