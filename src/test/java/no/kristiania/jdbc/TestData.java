package no.kristiania.jdbc;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Random;

public class TestData {

    private static Random random = new Random();

    SurveyDao sDao = new SurveyDao(testDataSource());

    public static DataSource testDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:persondb;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }

    public static String pickOne(String... alternates) {
        return alternates[random.nextInt(alternates.length)];
    }
    public Survey exampleSurvey() {
        Survey survey = new Survey();
        survey.setSurveyName(TestData.pickOne("Food allergies", "Color blindness", "Favorite soda", "Favorite passwords"));
        return survey;
    }
    public Question exampleQuestion() throws SQLException {
        Survey survey = exampleSurvey();
        sDao.save(survey);
        Question question = new Question();
        question.setQuestionText(TestData.pickOne(
                "Whats your favorite food?",
                "Whats your favorite color?",
                "Whats your favorite soda?",
                "How do you choose your password?"));
        question.setSurveyId(survey.getId());
        return question;
    }
}
