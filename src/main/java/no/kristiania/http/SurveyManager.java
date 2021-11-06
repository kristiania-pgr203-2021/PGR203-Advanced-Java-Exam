package no.kristiania.http;

import no.kristiania.jdbc.SurveyDao;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class SurveyManager {
    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1964);
        httpServer.setSurveyDao(new SurveyDao(createDataSource()));
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
