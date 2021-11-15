package no.kristiania.http;

import no.kristiania.jdbc.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class DeleteQuestionController implements HttpController {
    private final QuestionDao questionDao;
    private final AlternativeDao alternativeDao;

    public DeleteQuestionController(QuestionDao questionDao, AlternativeDao alternativeDao) {
        this.questionDao = questionDao;
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String inputId = queryMap.get("questionInput");

        for (Alternative alternative: alternativeDao.listAlternativesByQuestionId(Long.parseLong(inputId))){
            alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
        }
        questionDao.delete(Integer.parseInt(inputId));

        return new HttpMessage("303 See Other", "/editSurvey.html", "Question deleted!");
    }
}
