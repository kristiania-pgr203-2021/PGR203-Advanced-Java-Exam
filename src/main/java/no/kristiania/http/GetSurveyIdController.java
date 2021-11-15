package no.kristiania.http;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class GetSurveyIdController implements HttpController {

    private static int surveyId;
  
    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        this.surveyId = Integer.parseInt(queryMap.get("surveyInput"));
        return new HttpMessage("303 See Other", "/editSurvey.html", "Its done");
    }

    public static int getSurveyId() {
        return surveyId;
    }
}
