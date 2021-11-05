package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDaoTest {

    QuestionDao dao = new QuestionDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveQuestion() {
        Question question = exampleQuestion();
        dao.save(question);

        assertThat(dao.retrieve(question.getId()));
    }

    private Question exampleQuestion() {
        Question question = new Question();
        question.setQuestionText(TestData.pickOne(
                "Whats your favorite food?",
                "Whats your favorite color?",
                "Whats your favorite soda?",
                "How do you choose your password?"
        ));
        return question;
    }

    @Test
    void shouldListAllQuestions() {
        Question question1 = exampleQuestion();
        dao.save(question1);
        Question question2 = exampleQuestion();
        dao.save(question2);
        assertThat(dao.listAll())
                .extracting(Question::getId)
                .contains(question1.getId, question2.getId());
    }
}
