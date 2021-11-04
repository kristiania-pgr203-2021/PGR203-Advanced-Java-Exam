package no.kristiania.jdbc;

import javax.sql.DataSource;
import java.sql.*;

public class SurveyDao {
    private DataSource dataSource;

    public SurveyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Survey survey) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into surveys (survey_name) values (?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, survey.getSurveyName());
                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();
                    survey.setId(rs.getLong("id"));
                }
            }
        }
    }

    public Survey retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from surveys where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return mapFromResultSet(rs);
                }
            }
        }
    }

    private Survey mapFromResultSet(ResultSet rs) throws SQLException {
        Survey survey = new Survey();
        survey.setId(rs.getLong("id"));
        survey.setSurveyName(rs.getString("survey_name"));
        return survey;
    }

}
