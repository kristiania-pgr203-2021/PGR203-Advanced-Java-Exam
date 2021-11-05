package no.kristiania.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDao {

    private final DataSource dataSource;

    public AnswerDao(DataSource dataSource){
        this.dataSource = dataSource;
    }
    public void save(Answer answer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into answers(question_id, alternative_id, user_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            )) {
                statement.setLong(1, answer.getQuestionId());
                statement.setLong(2, answer.getAlternativeId());
                statement.setLong(3, answer.getUserId());
                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();
                    answer.setId(rs.getLong("id"));
                }
            }
        }
    }

    public List<Answer> listAnswersByQuestionId(long questionId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select * from answers where question_id = ?")) {
                statement.setLong(1, questionId);

                try (ResultSet rs = statement.executeQuery()) {
                    ArrayList<Answer> answers = new ArrayList<>();

                    while (rs.next()){
                        answers.add(resultFromResultSet(rs));
                    }
                    return answers;
                }
            }
        }
    }

    public Answer retrieve(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select * from answers where id = ?")) {
                statement.setLong(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    return resultFromResultSet(rs);
                }
            }
        }
    }

    private Answer resultFromResultSet(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getLong("id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setAlternativeId(rs.getLong("alternative_id"));
        answer.setUserId(rs.getLong("user_id"));
        return answer;
    }
}
