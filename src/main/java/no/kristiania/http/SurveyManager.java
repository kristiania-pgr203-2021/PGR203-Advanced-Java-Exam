package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.SurveyDao;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SurveyManager {

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);
        httpServer.setSurveyDao(new SurveyDao(createDataSource()));
        httpServer.setQuestionDao(new QuestionDao(createDataSource()));
        httpServer.setAlternativeDao(new AlternativeDao(createDataSource()));
        System.out.println("http://localhost:" + httpServer.getPort() + "/index.html");
        System.out.println("http://localhost:" + httpServer.getPort() + "/createSurvey.html");

    }
    public static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader("pgr203.properties")) {
            properties.load(reader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(properties.getProperty(
                "dataSource.url",
                "jdbc:postgresql://localhost:5432/surveydb"
        ));
        dataSource.setUser(properties.getProperty("dataSource.user", "surveyuser"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
