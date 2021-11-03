package no.kristiania.survey;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpServerTest {

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpServer server = new HttpServer(10001);
        HttpClient client = new HttpClient(
                "localhost", server.getPort(), "/ghfhgdgthl");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldResponseWithRequestTargetIn404() throws IOException {
        HttpServer server = new HttpServer(
                10002);
        HttpClient client = new HttpClient(
                "localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }
}
