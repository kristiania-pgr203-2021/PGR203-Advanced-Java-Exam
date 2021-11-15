package no.kristiania.http;

import no.kristiania.jdbc.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class DeleteSurveyController implements HttpController {

    private final SurveyDao surveyDao;
    private final QuestionDao questionDao;
    private final AlternativeDao alternativeDao;
    private Survey survey;

    public DeleteSurveyController(SurveyDao surveyDao, QuestionDao questionDao, AlternativeDao alternativeDao) {
        this.surveyDao = surveyDao;
        this.questionDao = questionDao;
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String inputId = queryMap.get("surveyInput");
        for (Question question : questionDao.listQuestionsBySurveyId(Long.parseLong(inputId))) {
            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(question.getId())) {
                alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
            }
        }

        for (Question dq : questionDao.listQuestionsBySurveyId(Long.parseLong(inputId))) {
            questionDao.deleteBySurveyId(Math.toIntExact(dq.getSurveyId()));
        }
        surveyDao.delete(Integer.parseInt(queryMap.get("surveyInput")));
        return new HttpMessage("303 See Other", "/editSurvey.html", "Survey deleted!");
    }
}
