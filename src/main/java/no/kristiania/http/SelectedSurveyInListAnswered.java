package no.kristiania.http;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.HttpServer.mapInAnswered;

public class SelectedSurveyInListAnswered implements HttpController {

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";
        messageBody += "<h1>" + UrlEncoding.decodeValue(mapInAnswered.get(SelectAnsweredSurveys.getSurveyId())) + "</h1>";
        return new HttpMessage("200 OK", messageBody);
    }
}
