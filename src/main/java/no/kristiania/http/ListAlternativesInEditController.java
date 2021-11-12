package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;

import java.io.IOException;
import java.sql.SQLException;

public class ListAlternativesInEditController implements HttpController {
    public ListAlternativesInEditController(AlternativeDao alternativeDao) {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        return null;
    }
}
