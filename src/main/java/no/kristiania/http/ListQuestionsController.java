package no.kristiania.http;

import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;
import java.sql.SQLException;
import static no.kristiania.http.UrlEncoding.utf8Value;

public class ListQuestionsController implements HttpController {
    private final QuestionDao questionDao;
    private static Long questionId;

    public ListQuestionsController(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String responseTxt = "";
        long tmp = 0;

        for (Question question : questionDao.listQuestionsBySurveyId(GetSurveyController.getSurveyId())) {
            responseTxt += "<p>" + "ID: " + question.getId() + " " + "Text: " + utf8Value(question.getQuestionText()) + "</p>";
            tmp = question.getId();
        }
        this.questionId = tmp;

        return new HttpMessage("200 OK", responseTxt);
    }

    public static Long getQuestionId() {
        return questionId;
    }
}
