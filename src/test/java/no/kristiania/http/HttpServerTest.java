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
    @Test
    void shouldCreateNewSurvey() throws IOException {
        server.addController("/api/newSurvey", new newSurveyController());
        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/newSurvey",
                "survey_text=New+Survey");
        assertEquals(303, postClient.getStatusCode());
        assertEquals("New Survey", server.getSurvey().getSurveyName());
    }
}
