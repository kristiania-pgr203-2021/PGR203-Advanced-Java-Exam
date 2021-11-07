package no.kristiania.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    HttpServerTest() throws IOException {

    }

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }

    @Test
    void shouldRespondWithRequestTarget200() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/index.html");
        assertEquals(200, client.getStatusCode());
    }

    /*
    @Test
    void shouldCreateNewProduct() throws IOException {
        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/newProduct",
                "category=1&productName=Kylling");
        assertEquals(200, postClient.getStatusCode());
        Products products = server.getProductsList().get(0);
        assertEquals("Kylling", products.getProduct());
    }

      */
}
