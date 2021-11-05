package no.kristiania.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlternativeDao {

    private final DataSource dataSource;

    public AlternativeDao(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void save(Alternative alternative) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into alternatives (question_id, alternative) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, alternative.getQuestionId());
                statement.setString(2, alternative.getAlternative());
                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();
                    alternative.setId(rs.getLong("id"));
                }
            }
        }
    }

    public Alternative retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from alternatives where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return resultFromResultSet(rs);
                }
            }
        }
    }

    public List<Alternative> listAlternativesByQuestionId(long questionId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select * from alternatives where question_id = ?")) {
                statement.setLong(1, questionId);

                try (ResultSet rs = statement.executeQuery()) {
                    ArrayList<Alternative> alternatives = new ArrayList<>();

                    while (rs.next()){
                        alternatives.add(resultFromResultSet(rs));
                    }
                    return alternatives;
                }
            }
        }
    }

    private Alternative resultFromResultSet(ResultSet rs) throws SQLException {
        Alternative alternative = new Alternative();
        alternative.setId(rs.getLong("id"));
        alternative.setQuestionId(rs.getLong("question_id"));
        alternative.setAlternative(rs.getString("alternative"));
        return alternative;
    }
}
