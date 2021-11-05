package no.kristiania.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDao {
    private DataSource dataSource;

    public QuestionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Question question) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into questions (question, survey_id) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, question.getQuestionText());
                statement.setLong(2, question.getSurveyId());
                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();
                    question.setId(rs.getLong("id"));
                }
            }
        }
    }

    public Question retrieve(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from questions where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return resultFromResultSet(rs);
                }
            }
        }
    }

    public List<Question> listAll() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select * from questions")) {
                try (ResultSet rs = statement.executeQuery()) {
                    ArrayList<Question> result = new ArrayList<>();

                    while (rs.next()) {
                        result.add(resultFromResultSet(rs));
                    }
                    return result;
                }
            }
        }
    }

    private Question resultFromResultSet(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getLong("id"));
        question.setQuestionText(rs.getString("question"));
        question.setSurveyId(rs.getLong("survey_id"));
        return question;
    }
}
