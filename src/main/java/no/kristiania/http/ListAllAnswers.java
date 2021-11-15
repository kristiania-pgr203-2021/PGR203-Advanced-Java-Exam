package no.kristiania.http;

import no.kristiania.jdbc.AnswerDao;
import no.kristiania.jdbc.FullAnswer;

import java.io.IOException;
import java.sql.SQLException;

public class ListAllAnswers implements HttpController {
    private final AnswerDao answerDao;

    public ListAllAnswers(AnswerDao answerDao) {
        this.answerDao = answerDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        AnswerDao answerDao = new AnswerDao(SurveyManager.createDataSource());
        String responseTxt = "";

            for (FullAnswer fullAnswer : answerDao.retrieveFullAnswer(SelectAnsweredSurveys.getSurveyId(), SelectAnsweredQuestion.getQuestionId())) {
                responseTxt +=
                        "<h4>" + fullAnswer.getFirstName() + " " + fullAnswer.getLastName() + " " +
                                " | " + fullAnswer.getAlternative() + "</h4>";
            }
        return new HttpMessage("200 OK", responseTxt);
    }
}
