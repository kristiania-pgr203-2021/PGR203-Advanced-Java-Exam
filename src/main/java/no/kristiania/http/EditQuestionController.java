package no.kristiania.http;

import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class EditQuestionController implements HttpController {
    private final QuestionDao questionDao;

    public EditQuestionController(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {

        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        Long id = Long.valueOf(queryMap.get("questionIdInput"));
        String question = queryMap.get("questionNameInput");
        questionDao.update(id, decodeValue(question));
        return new HttpMessage("303 See Other", "/editSurvey.html", "Question edited!");
    }
}
