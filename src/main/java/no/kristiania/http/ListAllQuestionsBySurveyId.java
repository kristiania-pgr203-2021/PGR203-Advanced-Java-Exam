package no.kristiania.http;

import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;

public class ListAllQuestionsBySurveyId implements HttpController {
    private final QuestionDao questionDao;

    public ListAllQuestionsBySurveyId(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";

        for (Question question : questionDao.listQuestionsBySurveyId(1)) {
            int value = Math.toIntExact(question.getId());
            System.out.println("QuestionID: " + value);
            messageBody += "<option value=" + (value) + ">" + "ID: " + question.getId() + " " + question.getQuestionText() + "</option>";
        }

        System.out.println("This is messagebody (HTML): " + messageBody);

        return new HttpMessage("200, OK", messageBody);
    }
}
