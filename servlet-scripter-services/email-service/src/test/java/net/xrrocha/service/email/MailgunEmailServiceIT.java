package net.xrrocha.service.email;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class MailgunEmailServiceIT {

    private final static int PORT = 4269;
    private final static String API_PATH = "/v3/mail.xrrocha.net/messages";
    private final MailgunEmailService service = new MailgunEmailService(
            "emailer@xrrocha.net",
            "http://localhost:" + PORT + API_PATH,
            "someKey"
    );
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Test
    public void sendsEmail() {

        stubFor(post(urlEqualTo(API_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": \"123\", \"message\": \"hey!\"}")));

        EmailResult emailResult =
                service.send("noone@xrrocha.net", "Hey there!", "What's up?");

        assertEquals(200, emailResult.getResponseCode());
        assertEquals("123", emailResult.getResponse().getId());
        assertEquals("hey!", emailResult.getResponse().getMessage());

        verify(postRequestedFor(urlMatching(API_PATH))
                .withRequestBody(matching(".*name=\"from\".*"))
                .withHeader("Content-Type", notMatching("application/json")));
    }
}
