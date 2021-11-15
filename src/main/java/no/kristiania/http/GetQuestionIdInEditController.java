package no.kristiania.http;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class GetQuestionIdInEditController implements HttpController {

    private static int questionId;

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        int id = Integer.parseInt(queryMap.get("questionInput"));
        this.questionId = id;

        return new HttpMessage("303 See Other", "/editSurvey.html", "Fetch question ID");
    }

    public static int getQuestionId() {
        return questionId;
    }
}
