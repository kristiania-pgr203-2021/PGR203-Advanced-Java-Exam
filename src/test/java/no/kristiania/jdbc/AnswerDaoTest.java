package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class AnswerDaoTest {

    AnswerDao dao = new AnswerDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveAnswer() throws SQLException {
        Answer answer = TestData.exampleAnswer();
        dao.save(answer);

        System.out.println(answer);

        assertThat(dao.retrieve(answer.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(answer);
    }

}
