package no.kristiania.http;

import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;

public class DeleteQuestionController implements HttpController {
    public DeleteQuestionController(QuestionDao questionDao) {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        return null;
    }
}
