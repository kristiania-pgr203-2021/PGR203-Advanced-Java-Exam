package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class SurveyDaoTest {

    TestData testData = new TestData();
    SurveyDao dao = new SurveyDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveSurvey() throws SQLException {
        Survey survey = testData.exampleSurvey();
        dao.save(survey);

        assertThat(dao.retrieve(survey.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(survey);
    }

    @Test
    void shouldListAllSurveys() throws SQLException {
        Survey survey1 = testData.exampleSurvey();
        dao.save(survey1);
        Survey survey2 = testData.exampleSurvey();
        dao.save(survey2);
        assertThat(dao.listAll())
                .extracting(Survey::getId)
                .contains(survey1.getId(), survey2.getId());
    }
    /*
    @Test
    void shouldDeleteSurvey() throws SQLException {
        Survey survey = testData.exampleSurvey();
        dao.save(survey);
        System.out.println(survey);
        dao.delete(survey);
        assertThat(dao.listAll())
                .doesNotContain(survey);
    }

     */
}
