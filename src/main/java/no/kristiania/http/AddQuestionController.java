package no.kristiania.http;

import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;

import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class AddQuestionController implements HttpController {
    private final QuestionDao questionDao;
    private Question question;

    public AddQuestionController(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        this.question = new Question();
        String decodedValue = decodeValue(queryMap.get("questionInput"));
        question.setQuestionText(decodedValue);
        question.setSurveyId(GetSurveyController.getSurveyId());
        questionDao.save(question);


        return new HttpMessage("303 See Other", "/createSurvey.html", "Question added!");
    }
}
