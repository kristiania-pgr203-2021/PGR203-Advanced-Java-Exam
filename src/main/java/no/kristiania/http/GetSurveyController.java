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
        String tmpRes = "";
        String responseTxt = "";
        long tmpId = -1;

        for (Survey survey : surveyDao.listAll()) {
            tmpRes = UrlEncoding.utf8Value(survey.getSurveyName());
            tmpId = survey.getId();
        }

        responseTxt += "Newly added survey: " + tmpRes;
        this.surveyId = tmpId;

        return new HttpMessage("200 OK", responseTxt);
    }

    public static long getSurveyId() {
        return surveyId;
    }
}
