package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDaoTest {

    QuestionDao qDao = new QuestionDao(TestData.testDataSource());
    SurveyDao sDao = new SurveyDao(TestData.testDataSource());
    TestData testData = new TestData();

    @Test
    void shouldCreateAndRetrieveQuestion() throws SQLException {
        Survey survey = testData.exampleSurvey();
        sDao.save(survey);
        Question question = testData.exampleQuestion();
        question.setSurveyId(survey.getId());
        qDao.save(question);

        assertThat(qDao.retrieve(question.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(question);
    }

    @Test
    void shouldListQuestionsBySurveyId() throws SQLException {
        Survey survey = testData.exampleSurvey();
        sDao.save(survey);

        Question question1 = testData.exampleQuestion();
        question1.setSurveyId(survey.getId());
        qDao.save(question1);

        Question question2 = testData.exampleQuestion();
        question2.setSurveyId(survey.getId());
        qDao.save(question2);

        Question question3 = testData.exampleQuestion();
        question3.setSurveyId(survey.getId());
        qDao.save(question3);

        assertThat(qDao.listQuestionsBySurveyId(survey.getId()))
                .extracting(Question::getId)
                .contains(question1.getId(), question2.getId(), question3.getId());
    }

}
