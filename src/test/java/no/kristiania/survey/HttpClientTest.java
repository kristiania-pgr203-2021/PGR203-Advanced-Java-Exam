package no.kristiania.survey;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class HttpClientTest {

    @Test
    void shouldReturnStatusCode200() throws IOException {
        assertEquals(200,
                new HttpClient("httpbin.org", 80, "/html")
                        .getStatusCode());
    }
}
