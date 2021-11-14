package no.kristiania.http;

import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SelectAnsweredQuestion implements HttpController {
    private final QuestionDao questionDao;
    private HashMap<Integer, String> mapSurvey = new HashMap();
    private static Integer questionId = 1;

    public static Integer getQuestionId() {
        return questionId;
    }

    public SelectAnsweredQuestion(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String questionId = queryMap.get("questionName");

        System.out.println("Valgte question sin ID: " + questionId);
        System.out.println("Valgte question sin tittel (retrieve): " + questionDao.retrieve(Long.parseLong(questionId)));
        String[] name = String.valueOf(questionDao.retrieve(Long.parseLong(questionId))).split("'");
        System.out.println("qyestion etter split: " + name[1]);

        mapSurvey.put(Integer.valueOf(questionId), name[1]);
        this.questionId = Integer.valueOf(questionId);

        return new HttpMessage("303, See Other", "/listAnsweredQuestions.html", questionId);
    }
}
