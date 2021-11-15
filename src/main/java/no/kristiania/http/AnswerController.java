package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.Answer;
import no.kristiania.jdbc.AnswerDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class AnswerController implements HttpController {
    private final AlternativeDao alternativeDao;
    private final AnswerDao answerDao;

    public AnswerController(AlternativeDao alternativeDao, AnswerDao answerDao) {
        this.alternativeDao = alternativeDao;
        this.answerDao = answerDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        int alternativeId = Integer.parseInt(queryMap.get("answerInput"));
        String[] alternativeArray = String.valueOf(alternativeDao.retrieve(alternativeId)).split("'");

        Answer answer = new Answer();
        answer.setAlternativeId(Long.valueOf(alternativeId));
        answer.setQuestionId(Long.valueOf(alternativeArray[3]));
        answer.setUserId(Long.valueOf(UserFormController.getUserId()));
        answerDao.save(answer);

        return new HttpMessage("303 See Other", "/answerSurvey.html", "Answer submitted!");
    }
}
