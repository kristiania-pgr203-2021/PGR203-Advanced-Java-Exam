package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.Answer;
import no.kristiania.jdbc.AnswerDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.HttpServer.mapUser;

public class AnswerController implements HttpController {
    private AlternativeDao alternativeDao;
    private AnswerDao answerDao;

    public AnswerController(AlternativeDao alternativeDao, AnswerDao answerDao) {
        this.alternativeDao = alternativeDao;
        this.answerDao = answerDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        int alternativeId = Integer.parseInt(queryMap.get("answerInput"));
        System.out.println("SVAR: Alternativ ID: " + alternativeId);
        String[] alternativeArray = String.valueOf(alternativeDao.retrieve(alternativeId)).split("'");
        System.out.println("Alternative retrieve: " + alternativeDao.retrieve(alternativeId));
        System.out.println(alternativeArray[3]);

        Answer answer = new Answer();
        answer.setAlternativeId(Long.valueOf(alternativeId));
        System.out.println("Alternativ ID satt: " + Long.valueOf(alternativeId));
        answer.setQuestionId(Long.valueOf(alternativeArray[3]));
        System.out.println("Question ID satt til: " + Long.valueOf(alternativeArray[3]));
        System.out.println("Bruker ID satt: " + Long.valueOf(UserFormController.getUserId()));
        answer.setUserId(Long.valueOf(UserFormController.getUserId()));
        AnswerDao answerDao = new AnswerDao(SurveyManager.createDataSource());
        answerDao.save(answer);

        return new HttpMessage("303 See Other", "/answerSurvey.html", "Answer submitted!");
    }
}
