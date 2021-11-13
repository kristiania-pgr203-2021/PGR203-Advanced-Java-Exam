package no.kristiania.http;

import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;
import java.sql.SQLException;

public class GetSurveyController implements HttpController {
    private static long surveyId;
    private final SurveyDao surveyDao;

    public GetSurveyController(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String messageBody = "";
        String tmpName = "";
        long tmpId = -1;

        for (Survey survey : surveyDao.listAll()) {
            tmpName = UrlEncoding.utf8Value(survey.getSurveyName());
            tmpId = survey.getId();
        }

        messageBody += "Newly added survey: " + tmpName;
        this.surveyId = tmpId;

        return new HttpMessage("200 OK", messageBody);
    }

    public static long getSurveyId() {
        return surveyId;
    }
}
