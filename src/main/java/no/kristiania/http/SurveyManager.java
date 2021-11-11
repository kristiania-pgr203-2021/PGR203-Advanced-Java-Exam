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
        HttpServer httpServer = new HttpServer(1964);
        SurveyDao surveyDao = new SurveyDao(dataSource);
        QuestionDao questionDao = new QuestionDao(dataSource);
        AlternativeDao alternativeDao = new AlternativeDao(dataSource);
        httpServer.addController("/api/addSurvey", new AddSurveyController(surveyDao));  //TODO: Add new survey
        httpServer.addController("/api/getSurvey", new GetSurveyController(surveyDao)); //TODO: List last survey
        httpServer.addController("/api/addQuestion", new AddQuestionController(questionDao)); //TODO: Add new question
        httpServer.addController("/api/listQuestions", new ListQuestionsController(questionDao)); //TODO: List all questions with surveyID
        httpServer.addController("/api/addAlternative", new AddAlternativeController(alternativeDao)); //TODO: Add new alternative
        httpServer.addController("/api/getQuestionId", new GetQuestionIdController()); //TODO: Gets questionID
        httpServer.addController("/api/listAlternatives", new ListAlternativesByQuestionId(alternativeDao)); //TODO: List all alternatives with questionID
        httpServer.setSurveyDao(new SurveyDao(dataSource));
        httpServer.setQuestionDao(new QuestionDao(dataSource));
        httpServer.setAlternativeDao(new AlternativeDao(dataSource));
        logger.info("Starting http://localhost:{}/index.html", httpServer.getPort());
        logger.info("Starting http://localhost:{}/createSurvey.html", httpServer.getPort());

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
