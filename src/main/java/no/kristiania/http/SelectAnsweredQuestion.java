package no.kristiania.http;

import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.HttpServer.mapSurvey;

public class SelectAnsweredQuestion implements HttpController {
    private final QuestionDao questionDao;
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

        String[] name = String.valueOf(questionDao.retrieve(Long.parseLong(questionId))).split("'");

        mapSurvey.put(Integer.valueOf(questionId), name[1]);
        this.questionId = Integer.valueOf(questionId);

        return new HttpMessage("303 See Other", "/listAnsweredQuestions.html", questionId);
    }
}
