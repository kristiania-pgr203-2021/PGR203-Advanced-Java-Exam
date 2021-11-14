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
        System.out.println("Survey ID som vi henter ned: " + JoinSurveyController.getSurveyId());

        /** Lister ut alle spørsmål tilhørende en survey **/
        int questionId = 0;
        String questionText = "";
        for (Question question : questionDao.listQuestionsBySurveyId(JoinSurveyController.getSurveyId())) {
            /** Starten på messageBody **/
            messageBody +=
                    "<div class=\"white_div\">" +
                            "<h2>Question ID: " + question.getId() + ", Tekst: " + question.getQuestionText() +"</h2>" +
                            "<form action=\"api/answer\" method=\"POST\" accept-charset=\"UTF-8\">";

            /** Looper ut alle alternativene og bygger messageBody **/
            int alternativeIds = 0;
            String alternativeText= "";
            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(question.getId())){
                alternativeIds = Math.toIntExact(alternative.getId());
                alternativeText = alternative.getAlternative();
                messageBody += "<p><label><input type=\"radio\" name=\"answerInput\" value=\"" + (alternativeIds) + "\">" + alternativeText +"</label></p>";
            }

            /** Avsluttende tag på messageBody**/
            messageBody += "<button>Submit</button>" +
                    "</form>" +
                    "</div>";
            System.out.println(messageBody);
        }

        return new HttpMessage("200 OK", messageBody);
    }
}
