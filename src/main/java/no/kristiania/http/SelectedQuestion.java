package no.kristiania.http;

import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;

public class SelectedQuestion implements HttpController {
    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        QuestionDao questionDao = new QuestionDao(SurveyManager.createDataSource());

        String responseTxt = "";
        if (SelectAnsweredQuestion.getQuestionId() != 0) {
            responseTxt += "<h3>" + (questionDao.retrieve(SelectAnsweredQuestion.getQuestionId()).getQuestionText()) + "</h3>";
        }

        return new HttpMessage("200 OK", responseTxt);
    }
}
