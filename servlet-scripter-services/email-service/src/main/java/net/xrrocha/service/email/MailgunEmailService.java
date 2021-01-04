package net.xrrocha.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import net.xrrocha.scripter.commons.Initializable;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Email service implementation.
 */
public class MailgunEmailService implements EmailSender, Initializable, Serializable {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    private static final String LINE_FEED = "\r\n";
    private static final String CHARSET = UTF_8.toString();
    private static final Logger logger = LoggerFactory.getLogger(MailgunEmailService.class);
    private final String from;
    private final String server;
    private final String key;
    private String basicCredentials;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private MailgunEmailService() {
        from = null;
        server = null;
        key = null;
    }

    public MailgunEmailService(@NotNull String from, @NotNull String server, @NotNull String key) {
        this.from = from;
        this.server = server;
        this.key = key;
        initialize();
    }

    @Override
    public EmailResult send(@NotNull String to, @NotNull String subject, @NotNull String text) {

        try {

            URL url = getServerUrl();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.addRequestProperty("Authorization", getBasicCredentials());

            String boundary = "===" + System.currentTimeMillis() + "===";
            connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            PrintWriter out =
                    new PrintWriter(new OutputStreamWriter(connection.getOutputStream()), true);

            ImmutableMap.of(
                    "from", from,
                    "to", to,
                    "subject", subject,
                    "text", text
            )
                    .forEach((name, value) -> {
                        try {
                            out
                                    .append("--").append(boundary).append(LINE_FEED)
                                    .append("Content-Disposition: form-data; ")
                                    .append("name=\"").append(name).append("\"").append(LINE_FEED)
                                    .append("Content-Type: text/plain; charset=").append(CHARSET)
                                    .append(LINE_FEED).append(LINE_FEED)
                                    .append(value).append(LINE_FEED);
                        } catch (Exception e) {
                            throw new IllegalArgumentException(e);
                        }
                    });

            out
                    .append(LINE_FEED)
                    .append("--").append(boundary).append("--")
                    .append(LINE_FEED)
                    .close();

            int responseCode = connection.getResponseCode();
            logger.debug("Response code: " + responseCode);

            String responseBody =
                    CharStreams.toString(new InputStreamReader(connection.getInputStream()));
            logger.debug("Response body:" +
                    ("\n" + responseBody.trim())
                            .replaceAll("(\\r?\\n)", "$1  "));

            EmailResponse emailResponse = objectMapper.readValue(responseBody, EmailResponse.class);

            return new EmailResult(responseCode, emailResponse);

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    String getBasicCredentials() {
        if (basicCredentials == null) {
            try {
                basicCredentials =
                        "Basic " + Base64.getEncoder().encodeToString(("api:" + key).getBytes(CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return basicCredentials;
    }

    @Override
    public void initialize() {

        checkNotNull(from, "From cannot be null");
        checkNotNull(server, "Server cannot be null");
        checkNotNull(key, "Key cannot be null");

        checkArgument(!server.trim().isEmpty(),
                "Server cannot be blank");
        getServerUrl();

        checkArgument(!key.trim().isEmpty(),
                "Key cannot be blank");

        checkArgument(EmailValidator.getInstance().isValid(from.trim()),
                "Invalid 'from' email: " + from);
    }

    URL getServerUrl() {
        try {
            return new URL(server);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed server URL: " + server, e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("server", server)
                .add("key", key)
                .toString();
    }
}
