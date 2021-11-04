package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

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

    private Survey exampleSurvey() {
        Survey survey = new Survey();
        survey.setSurveyName(TestData.pickOne("Food allergies", "Color blindness", "Favorite soda", "Favorite passwords"));
        return survey;
    }
}
