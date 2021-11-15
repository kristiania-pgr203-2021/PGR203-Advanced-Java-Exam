package no.kristiania.http;

import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;

public class ListSurveysController implements HttpController {

    private final SurveyDao surveyDao;

    public ListSurveysController(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String responseText = "";

        for (Survey survey : surveyDao.listAll()) {
            responseText += "<p>" + "ID: " + survey.getId() + " " + "Name: " + UrlEncoding.decodeValue(survey.getSurveyName()) + "</p>";

        }

        return new HttpMessage("200 OK", responseText);
    }
}
