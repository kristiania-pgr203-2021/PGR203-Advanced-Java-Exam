package no.kristiania.http;

import no.kristiania.jdbc.*;
import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.AnswerDao;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.SurveyDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SurveyManager {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) throws IOException {
        DataSource dataSource = createDataSource();
        HttpServer httpServer = new HttpServer(8080);
        SurveyDao surveyDao = new SurveyDao(dataSource);
        QuestionDao questionDao = new QuestionDao(dataSource);
        AlternativeDao alternativeDao = new AlternativeDao(dataSource);
        UserDao userDao = new UserDao(dataSource);
        AnswerDao answerDao = new AnswerDao(dataSource);

        //createSurvey.html
        httpServer.addController("/api/addSurvey", new AddSurveyController(surveyDao));  //TODO: Add new survey
        httpServer.addController("/api/getSurvey", new GetSurveyController(surveyDao)); //TODO: List last survey
        httpServer.addController("/api/addQuestion", new AddQuestionController(questionDao)); //TODO: Add new question
        httpServer.addController("/api/listQuestions", new ListQuestionsController(questionDao)); //TODO: List all questions with surveyID
        httpServer.addController("/api/addAlternative", new AddAlternativeController(alternativeDao)); //TODO: Add new alternative
        httpServer.addController("/api/getQuestionId", new GetQuestionIdController()); //TODO: Gets questionID
        httpServer.addController("/api/listAlternatives", new ListAlternativesByQuestionIdController(alternativeDao)); //TODO: List all alternatives with questionID

        //editSurvey.html
        httpServer.addController("/api/listSurveys", new ListSurveysController(surveyDao)); //TODO: List all created surveys
        httpServer.addController("/api/editSurvey", new EditSurveyController(surveyDao)); //TODO: Edit existing survey name
        httpServer.addController("/api/deleteSurvey", new DeleteSurveyController(surveyDao, questionDao, alternativeDao)); //TODO: Delete existing survey
        httpServer.addController("/api/getSurveyId", new GetSurveyIdController()); //TODO: Gets surveyID from input
        httpServer.addController("/api/listQuestionsInEdit", new ListQuestionsInEditController(questionDao)); //TODO: Lists all questions belonging to a survey
        httpServer.addController("/api/getQuestionIdInEdit", new GetQuestionIdInEditController()); //TODO: Gets questionID from input
        httpServer.addController("/api/deleteQuestion", new DeleteQuestionController(questionDao, alternativeDao)); //TODO Delete existing question
        httpServer.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(alternativeDao)); //TODO: List all alternatives belonging to a question
        httpServer.addController("/api/editAlternative", new EditAlternativeController(alternativeDao)); //TODO: Edit existing alternative
        httpServer.addController("/api/deleteAlternative", new DeleteAlternativeController(alternativeDao)); //TODO: Dele existing alternative

        //joinSurvey.html
        httpServer.addController("/api/listSurveysForm", new ListSurveysFormController(surveyDao)); //TODO: List all created surveys with a scrollbar
        httpServer.addController("/api/joinSurvey", new JoinSurveyController(surveyDao));
        httpServer.addController("/api/selectedSurvey", new SelectedSurveyController());
        httpServer.addController("/api/userForm", new UserFormController(userDao));
        httpServer.addController("/api/getUser", new GetUserController());
        httpServer.addController("/api/listQuestionsInAnswerSurvey", new ListQuestionsAndAlternativesController(questionDao, alternativeDao));
        httpServer.addController("/api/answer", new AnswerController(alternativeDao, answerDao));

        //listAllAnsweredSurveys.html
        httpServer.addController("/api/selectAnsweredSurveys", new SelectAnsweredSurveys(surveyDao)); //TODO: Gets surveyId
        httpServer.addController("/api/selectedSurveyInListAnswered", new SelectedSurveyInListAnswered());
        httpServer.addController("/api/listAllQuestionsBySurveyId", new ListAllQuestionsBySurveyId(questionDao)); //TODO: List all questions by surveyID
        httpServer.addController("/api/selectAnsweredQuestion", new SelectAnsweredQuestion(questionDao));
        httpServer.addController("/api/listAllAnswers", new ListAllAnswers(answerDao));
        httpServer.addController("/api/selectedQuestion", new SelectedQuestion());

        httpServer.setSurveyDao(new SurveyDao(dataSource));
        httpServer.setQuestionDao(new QuestionDao(dataSource));
        httpServer.setAlternativeDao(new AlternativeDao(dataSource));
        httpServer.setUserDao(new UserDao(dataSource));
        httpServer.setAnswerDao(new AnswerDao(dataSource));

        logger.info("Starting http://localhost:{}/index.html", HttpServer.getPort());
        logger.info("Starting http://localhost:{}/createSurvey.html", HttpServer.getPort());
        logger.info("Starting http://localhost:{}/editSurvey.html", HttpServer.getPort());
        logger.info("Starting http://localhost:{}/joinSurvey.html", HttpServer.getPort());
        logger.info("Starting http://localhost:{}/answerSurvey.html", HttpServer.getPort());
    }

    public static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader("pgr203.properties")) {
            properties.load(reader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty("dataSource.url"));
        dataSource.setUser(properties.getProperty("dataSource.user"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
