package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.utf8Value;

public class ListAlternativesByQuestionIdController implements HttpController {
    private final AlternativeDao alternativeDao;
    private long questionId = -1;


    public ListAlternativesByQuestionIdController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {

        if (questionId == -1) {
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
            long questionId = Long.parseLong(queryMap.get("questionInput"));

            if (questionId != -1) {
                this.questionId = questionId;
            }
            return new HttpMessage("303 See Other", "/createSurvey.html" , "Its done");
        }

        String responseTxt = "";
        for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(GetQuestionIdController.getQuestionId())) {
            responseTxt += "<li>" + utf8Value(alternative.getAlternative()) + "</li>";

        }

        return new HttpMessage("200 OK", responseTxt);
    }
}
