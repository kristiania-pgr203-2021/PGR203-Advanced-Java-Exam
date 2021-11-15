package no.kristiania.http;

import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;

import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class AddSurveyController implements HttpController {

    private final SurveyDao surveyDao;

    public AddSurveyController(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        Survey survey = new Survey();
        String decodedValue = decodeValue(queryMap.get("surveyInput"));
        survey.setSurveyName(decodedValue);
        surveyDao.save(survey);
        return new HttpMessage("303 See Other", "/createSurvey.html" , "Survey created!");
    }

}
