package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class AlternativeDaoTest {

    TestData testData = new TestData();
    SurveyDao sDao = new SurveyDao(TestData.testDataSource());
    QuestionDao qDao = new QuestionDao(TestData.testDataSource());
    AlternativeDao aDao = new AlternativeDao(TestData.testDataSource());

    @Test void shouldCreateAndRetrieveAlternative() throws SQLException {
        Survey survey = testData.exampleSurvey();
        sDao.save(survey);

        Question question = testData.exampleQuestion();
        question.setSurveyId(survey.getId());
        qDao.save(question);

        Alternative alternative = testData.exampleAlternative();
        alternative.setQuestionId(question.getId());
        aDao.save(alternative);

        assertThat(aDao.retrieve(alternative.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(alternative);
    }

    @Test void shouldListAllAlternativesByQuestionId() throws SQLException {
        Survey survey = testData.exampleSurvey();
        sDao.save(survey);
        Question question1 = testData.exampleQuestion();
        question1.setSurveyId(survey.getId());
        qDao.save(question1);

        Alternative alternative1 = testData.exampleAlternative();
        alternative1.setQuestionId(question1.getId());
        aDao.save(alternative1);
        Alternative alternative2 = testData.exampleAlternative();
        alternative2.setQuestionId(question1.getId());
        aDao.save(alternative2);

        assertThat(aDao.listAlternativesByQuestionId(question1.getId()))
                .extracting(Alternative::getId)
                .contains(alternative1.getId(), alternative2.getId());

        assertThat(aDao.listAll())
                .extracting(Alternative::getId)
                .contains(alternative1.getId(), alternative2.getId());
    }

    @Test void shouldListAllAlternatives() throws SQLException {
        Survey survey = testData.exampleSurvey();
        sDao.save(survey);
        Question question1 = testData.exampleQuestion();
        question1.setSurveyId(survey.getId());
        qDao.save(question1);

        Alternative alternative1 = testData.exampleAlternative();
        alternative1.setQuestionId(question1.getId());
        aDao.save(alternative1);
        Alternative alternative2 = testData.exampleAlternative();
        alternative2.setQuestionId(question1.getId());
        aDao.save(alternative2);

        assertThat(aDao.listAll())
                .extracting(Alternative::getId)
                .contains(alternative1.getId(), alternative2.getId());
    }

}
