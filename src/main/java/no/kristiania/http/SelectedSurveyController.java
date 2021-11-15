package no.kristiania.http;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.HttpServer.mapSurvey;

public class SelectedSurveyController implements HttpController {

    public SelectedSurveyController() {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";
        messageBody += "<h1>" + UrlEncoding.decodeValue(mapSurvey.get(JoinSurveyController.getSurveyId())) + "</h1>";
        return new HttpMessage("200 OK", messageBody);
    }
}
