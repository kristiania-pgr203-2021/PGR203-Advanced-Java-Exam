package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;


import java.sql.SQLException;


import static org.assertj.core.api.Assertions.assertThat;

public class AnswerDaoTest {

    AnswerDao dao = new AnswerDao(TestData.testDataSource());
    SurveyDao sDao = new SurveyDao(TestData.testDataSource());
    QuestionDao qDao = new QuestionDao(TestData.testDataSource());
    AlternativeDao aDao = new AlternativeDao(TestData.testDataSource());
    UserDao uDao = new UserDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveAnswersByQuestion() throws SQLException {
        Survey survey = TestData.exampleSurvey();
        sDao.save(survey);

        Question question = TestData.exampleQuestion();
        question.setSurveyId(survey.getId());
        qDao.save(question);

        Alternative alternative1 = TestData.exampleAlternative();
        alternative1.setQuestionId(question.getId());
        aDao.save(alternative1);

        Alternative alternative2 = TestData.exampleAlternative();
        alternative2.setQuestionId(question.getId());
        aDao.save(alternative2);

        User user1 = TestData.exampleUser();
        uDao.save(user1);

        User user2 = TestData.exampleUser();
        uDao.save(user2);

        Answer answer1 = new Answer();
        answer1.setQuestionId(question.getId());
        answer1.setAlternativeId(alternative1.getId());
        answer1.setUserId(user1.getId());
        dao.save(answer1);

        Answer answer2 = new Answer();
        answer2.setQuestionId(question.getId());
        answer2.setAlternativeId(alternative2.getId());
        answer2.setUserId(user2.getId());
        dao.save(answer2);

        assertThat(dao.retrieve(answer1.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(answer1);

        assertThat(dao.listAnswersByQuestionId(Math.toIntExact(question.getId())))
                .extracting(Answer::getId)
                .contains(answer1.getId(), answer2.getId());
    }
/*
    @Test
    void shouldListAnswer() throws SQLException {

        Survey survey = TestData.exampleSurvey();
        sDao.save(survey);

        Question question = TestData.exampleQuestion();
        question.setSurveyId(survey.getId());
        qDao.save(question);

        Alternative alternative1 = TestData.exampleAlternative();
        alternative1.setQuestionId(question.getId());
        aDao.save(alternative1);

        Alternative alternative2 = TestData.exampleAlternative();
        alternative2.setQuestionId(question.getId());
        aDao.save(alternative2);

        User user1 = TestData.exampleUser();
        uDao.save(user1);

        User user2 = TestData.exampleUser();
        uDao.save(user2);

        Answer answer1 = new Answer();
        answer1.setQuestionId(question.getId());
        answer1.setAlternativeId(alternative1.getId());
        answer1.setUserId(user1.getId());
        dao.save(answer1);

        Answer answer2 = new Answer();
        answer2.setQuestionId(question.getId());
        answer2.setAlternativeId(alternative2.getId());
        answer2.setUserId(user2.getId());
        dao.save(answer2);

        assertEquals("test", dao.retrieveFullAnswer(survey.getId()));
    }

 */
}
