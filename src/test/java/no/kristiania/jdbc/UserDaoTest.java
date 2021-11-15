package no.kristiania.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDaoTest {

    UserDao dao = new UserDao(TestData.testDataSource());

    @Test
    void shouldCreateAndRetrieveUser() throws SQLException {
        User user = TestData.exampleUser();
        dao.save(user);

        assertThat(dao.retrieve(user.getId()))
                .hasNoNullFieldsOrProperties()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void shouldListAllUsers() throws SQLException {
        User user1 = TestData.exampleUser();
        dao.save(user1);
        User user2 = TestData.exampleUser();
        dao.save(user2);
        assertThat(dao.listAll())
                .extracting(User::getId)
                .contains(user1.getId(), user2.getId());
    }

}