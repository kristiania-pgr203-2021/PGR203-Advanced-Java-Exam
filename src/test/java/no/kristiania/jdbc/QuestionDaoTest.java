package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;


import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDaoTest {

    QuestionDao qDao = new QuestionDao(TestData.testDataSource());
    TestData testData = new TestData();

    @Test
    void shouldCreateAndRetrieveQuestion() throws SQLException {
        Question question = testData.exampleQuestion();
        qDao.save(question);

        assertThat(qDao.retrieve(question.getId()));
    }

    @Test
    void shouldListAllQuestions() throws SQLException {
        Survey survey = new Survey();
        survey.setSurveyName("test");
        Question question1 = testData.exampleQuestion();
        qDao.save(question1);
        Question question2 = testData.exampleQuestion();
        qDao.save(question2);
        assertThat(qDao.listAll())
                .extracting(Question::getId);
    }

}
