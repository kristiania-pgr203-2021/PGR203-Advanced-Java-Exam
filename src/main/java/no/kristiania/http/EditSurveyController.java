package no.kristiania.http;

import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class EditSurveyController implements HttpController {
    private final SurveyDao surveyDao;
    private Survey survey;

    public EditSurveyController(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        Long id = Long.valueOf(queryMap.get("surveyIdInput"));
        String surveyName = queryMap.get("surveyNameInput");
        surveyDao.update(id, surveyName);
        return new HttpMessage("303 See Other", "/editSurvey.html", "Survey title edited!");
    }
}
