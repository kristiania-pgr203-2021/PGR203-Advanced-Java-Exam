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
        String idInput = queryMap.get("questionInput");

        for (Alternative alternative: alternativeDao.listAlternativesByQuestionId(Long.parseLong(idInput))){
            alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
        }
        questionDao.delete(Integer.parseInt(idInput));

        return new HttpMessage("303 See Other", "/editSurvey.html", "Its done");
    }
}
