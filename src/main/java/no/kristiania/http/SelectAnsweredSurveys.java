package no.kristiania.http;

import no.kristiania.jdbc.SurveyDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SelectAnsweredSurveys implements HttpController {
    private final SurveyDao surveyDao;
    private HashMap<Integer, String> mapSurvey = new HashMap();
    private static int surveyId = 1;

    public static int getSurveyId() {
        return surveyId;
    }

    public SelectAnsweredSurveys(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String surveyId = queryMap.get("surveyName");

        System.out.println("Valgte survey sin ID: " + surveyId);
        System.out.println("Valgte survey sin tittel (retrieve): " + surveyDao.retrieve(Long.parseLong(surveyId)));
        String[] name = String.valueOf(surveyDao.retrieve(Long.parseLong(surveyId))).split("'");
        System.out.println("Survey etter split: " + name[1]);

        mapSurvey.put(Integer.valueOf(surveyId), name[1]);
        this.surveyId = Integer.valueOf(surveyId);


        return new HttpMessage("303, See Other","/api/selectAnsweredSurveys", surveyId);

    }
}
