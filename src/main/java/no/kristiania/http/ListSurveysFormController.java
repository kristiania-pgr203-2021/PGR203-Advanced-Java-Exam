package no.kristiania.http;

import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;

public class ListSurveysFormController implements HttpController {

    private final SurveyDao surveyDao;

    public ListSurveysFormController(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";

        for (Survey survey : surveyDao.listAll()) {
            int value = Math.toIntExact(survey.getId());
            messageBody += "<option value=" + (value) + ">" + "ID: " + survey.getId() + " " + UrlEncoding.decodeValue(survey.getSurveyName()) + "</option>";
        }

        return new HttpMessage("200 OK", messageBody);
    }
}
