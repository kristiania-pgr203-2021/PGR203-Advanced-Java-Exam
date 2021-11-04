package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class SurveysTest {

    SurveyDao dao = new SurveyDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveSurvey() throws SQLException {
        Survey survey = exampleSurvey();
        dao.save(survey);

        assertThat(dao.retrieve(survey.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(survey);
    }

    @Test
    void shouldListAllSurveys() throws SQLException {
        Survey survey1 = exampleSurvey();
        dao.save(survey1);
        Survey survey2 = exampleSurvey();
        dao.save(survey2);
        assertThat(dao.listAll())
                .extracting(Survey::getId)
                .contains(survey1.getId(), survey2.getId());
    }

    private Survey exampleSurvey() {
        Survey survey = new Survey();
        survey.setSurveyName(TestData.pickOne("Food allergies", "Color blindness", "Favorite soda", "Favorite passwords"));
        return survey;
    }
}
