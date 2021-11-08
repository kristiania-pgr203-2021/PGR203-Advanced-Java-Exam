package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.SurveyDao;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class SurveyManager {
    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);
        httpServer.setSurveyDao(new SurveyDao(createDataSource()));
        httpServer.setQuestionDao(new QuestionDao(createDataSource()));
        httpServer.setAlternativeDao(new AlternativeDao(createDataSource()));
        System.out.println("http://localhost:" + httpServer.getPort() + "/index.html");
        System.out.println("http://localhost:" + httpServer.getPort() + "/newQuestions.html");

    }
    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/surveydb");
        dataSource.setUser("surveyuser");
        dataSource.setPassword("test");
        //Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
