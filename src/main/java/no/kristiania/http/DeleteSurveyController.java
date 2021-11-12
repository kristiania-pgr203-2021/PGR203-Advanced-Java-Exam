package no.kristiania.http;

import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;

public class DeleteSurveyController implements HttpController {
    public DeleteSurveyController(SurveyDao surveyDao) {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        return null;
    }
}
