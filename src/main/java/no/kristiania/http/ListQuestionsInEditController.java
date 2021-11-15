package no.kristiania.http;

import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.UrlEncoding.utf8Value;

public class ListQuestionsInEditController implements HttpController {
    private final QuestionDao questionDao;

    public ListQuestionsInEditController(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String Test = "";

        for (Question question : questionDao.listQuestionsBySurveyId(GetSurveyIdController.getSurveyId())) {
            Test += "<p>" + "ID: " + question.getId() + " " + "Question text: " + utf8Value(question.getQuestionText()) + "</p>";
        }
        return new HttpMessage("200 OK", Test);
    }
}
