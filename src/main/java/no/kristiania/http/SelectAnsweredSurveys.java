package no.kristiania.http;

import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.HttpServer.mapInAnswered;

public class SelectAnsweredSurveys implements HttpController {
    private final SurveyDao surveyDao;
    private static int surveyId = 1;

    public SelectAnsweredSurveys(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String surveyId = queryMap.get("surveyName");

        String[] name = String.valueOf(surveyDao.retrieve(Long.parseLong(surveyId))).split("'");
        mapInAnswered.put(Integer.valueOf(surveyId), name[1]);
        SelectAnsweredSurveys.surveyId = Integer.valueOf(surveyId);

        return new HttpMessage("303 See Other","/listAnsweredQuestions.html", "surveyId");
    }

    public static Integer getSurveyId() {
        return surveyId;
    }
}
