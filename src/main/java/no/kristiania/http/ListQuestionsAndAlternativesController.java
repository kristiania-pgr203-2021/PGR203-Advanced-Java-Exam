package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;

import java.io.IOException;
import java.sql.SQLException;

public class ListQuestionsAndAlternativesController implements HttpController {

    private final QuestionDao questionDao;
    private final AlternativeDao alternativeDao;

    public ListQuestionsAndAlternativesController(QuestionDao questionDao, AlternativeDao alternativeDao) {
        this.questionDao = questionDao;
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";

        for (Question question : questionDao.listQuestionsBySurveyId(JoinSurveyController.getSurveyId())) {
            messageBody +=
                    "<div class=\"white_div\">" +
                            "<h2>Question ID: " + question.getId() + ", Tekst: " + question.getQuestionText() +"</h2>" +
                            "<form action=\"api/answer\" method=\"POST\" accept-charset=\"UTF-8\">";

            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(question.getId())){
                int alternativeIds = Math.toIntExact(alternative.getId());
                String alternativeText = alternative.getAlternative();
                messageBody += "<p><label><input type=\"radio\" name=\"answerInput\" value=\"" + (alternativeIds) + "\">" + alternativeText +"</label></p>";
            }

            messageBody += "<button>Submit</button>" +
                    "</form>" +
                    "</div>";
        }

        return new HttpMessage("200 OK", messageBody);
    }
}
