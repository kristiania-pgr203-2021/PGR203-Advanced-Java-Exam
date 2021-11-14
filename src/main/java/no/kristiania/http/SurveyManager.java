package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
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

        httpServer.setSurveyDao(new SurveyDao(dataSource));
        httpServer.setQuestionDao(new QuestionDao(dataSource));
        httpServer.setAlternativeDao(new AlternativeDao(dataSource));

        logger.info("Starting http://localhost:{}/index.html", httpServer.getPort());
        logger.info("Starting http://localhost:{}/createSurvey.html", httpServer.getPort());
        logger.info("Starting http://localhost:{}/editSurvey.html", httpServer.getPort());
        logger.info("Starting http://localhost:{}/joinSurvey.html", httpServer.getPort());
        logger.info("Starting http://localhost:{}/answerSurvey.html", httpServer.getPort());
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
