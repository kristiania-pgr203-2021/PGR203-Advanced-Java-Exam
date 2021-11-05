package no.kristiania.jdbc;

import javax.sql.DataSource;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User retrieve(Long id) {
        return null;
    }
}
