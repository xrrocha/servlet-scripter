package net.xrrocha.service.email;

import org.junit.Test;

import static net.xrrocha.scripter.commons.YamlUtils.YAML;
import static org.junit.Assert.assertEquals;

public class MailgunEmailServiceTest {

    private final String from = "emailer@xrrocha.net";
    private final String server =
            "https://api.mailgun.net/v3/sandbox025b86cabd724cdda4c651325d5aca00.mailgun";
    private final String key = "key-ac06b4789275c2c4f31138b21f202747";

    private static String buildYaml(String from, String server, String key) {
        return "--- !!net.xrrocha.service.email.MailgunEmailService\n" +
                "from: " + from + "\n" +
                "server: " + server + "\n" +
                "key: " + key + "\n";
    }

    @Test
    public void generatesProperStringRepresentation() {
        MailgunEmailService service = new MailgunEmailService(from, server, key);
        assertEquals(
                String.format("%s{from=%s, server=%s, key=%s}",
                        service.getClass().getSimpleName(),
                        from, server, key),
                service.toString());
    }

    @Test
    public void acceptsValidProperties() {
        String yamlString = buildYaml(from, server, key);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullFrom() {
        String yamlString = buildYaml(null, server, key);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankFrom() {
        String yamlString = buildYaml("' \t\r '", server, key);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullServer() {
        String yamlString = buildYaml(from, null, key);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankServer() {
        String yamlString = buildYaml(from, "' \t\r '", key);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void validatesNullKey() {
        String yamlString = buildYaml(from, server, null);
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatesBlankKey() {
        String yamlString = buildYaml(from, server, "' \t\r '");
        MailgunEmailService service = YAML.load(yamlString);
        service.initialize();
    }

    @Test
    public void acceptsValidFrom() {
        new MailgunEmailService(from, server, key);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullFrom() {
        new MailgunEmailService(null, server, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankFrom() {
        new MailgunEmailService(" \t\r\n ", server, key);
    }

    @Test
    public void acceptsValidServer() {
        new MailgunEmailService(from, server, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedServer() {
        new MailgunEmailService(from, "badUrl", key);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullServer() {
        new MailgunEmailService(from, null, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankServer() {
        new MailgunEmailService(from, " \t\r\n ", key);
    }

    @Test
    public void acceptsValidKey() {
        new MailgunEmailService(from, server, key);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullKey() {
        new MailgunEmailService(from, server, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankKey() {
        new MailgunEmailService(from, server, " \t\r\n ");
    }
}
