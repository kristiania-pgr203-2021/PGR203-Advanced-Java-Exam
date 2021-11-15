package no.kristiania.jdbc;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Random;

public class TestData {

    private static final Random random = new Random();

    public static DataSource testDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:survey_db;DB_CLOSE_DELAY=-1");
        Flyway.configure().dataSource(dataSource).load().clean();
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }

    public static String pickOne(String... alternates) {
        return alternates[random.nextInt(alternates.length)];
    }

    public static Survey exampleSurvey() {
        Survey survey = new Survey();
        survey.setSurveyName(TestData.pickOne("Food allergies", "Color blindness", "Favorite soda", "Favorite passwords"));
        return survey;
    }

    public static Question exampleQuestion() {
        Question question = new Question();
        question.setQuestionText(TestData.pickOne(
                "Whats your favorite food?",
                "Whats your favorite color?",
                "Whats your favorite soda?",
                "How do you choose your password?"));
        return question;
    }

    public static Alternative exampleAlternative() {
        Alternative alternative = new Alternative();
        alternative.setAlternative(TestData.pickOne(
    "Taco", "Pizza", "Cola", "Urge", "password123", "password", "Blue", "Yellow"));
        return alternative;
    }

    public static User exampleUser() {
        User user = new User();
        user.setLastName(TestData.pickOne("Wedvik", "Fung", "Johansen", "Nordmann", "Erikson"));
        user.setFirstName(TestData.pickOne("Martin", "Jessica", "Ola", "Mette", "Erik"));
        user.setEmail(TestData.pickOne("wedvik@gmail.com", "fung@gmail.com", "hestejente@hotmail.com", "heiheihei@live.no", "123fotball@hotmail.com"));
        return user;
    }
}
