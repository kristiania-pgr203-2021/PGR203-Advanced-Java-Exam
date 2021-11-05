package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDaoTest {

    UserDao dao = new UserDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveUser() throws SQLException {
        User user = TestData.exampleUser();

        assertThat(dao.retrieve(user.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }
}