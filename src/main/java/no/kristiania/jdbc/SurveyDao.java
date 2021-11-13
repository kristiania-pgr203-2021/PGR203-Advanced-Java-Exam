package no.kristiania.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

public class SurveyDao {

    private final DataSource dataSource;

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

    public void delete(int id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from surveys where id = ?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        }
    }

    public void update(Long id, String surveyName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("update surveys set survey_name = ? where id = ?")) {
                statement.setString(1, surveyName);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
    }

    public Survey retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from surveys where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return resultFromResultSet(rs);
                }
            }
        }
    }

    private Survey resultFromResultSet(ResultSet rs) throws SQLException {
        Survey survey = new Survey();
        survey.setId(rs.getLong("id"));
        survey.setSurveyName(rs.getString("survey_name"));
        return survey;
    }

    public ArrayList<Survey> listAll() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from surveys")) {
                try (ResultSet rs = statement.executeQuery()) {
                    ArrayList<Survey> result = new ArrayList<>();

                    while (rs.next()) {
                        result.add(resultFromResultSet(rs));
                    }
                    return result;
                }
            }
        }
    }
}
