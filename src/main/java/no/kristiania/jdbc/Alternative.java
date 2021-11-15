package no.kristiania.jdbc;

public class Alternative {
    private long id;
    private String alternative;
    private long questionId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlternative() {
        return alternative;
    }

    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "Alternative{" +
                "id=" + id +
                ", alternative='" + alternative + '\'' +
                ", questionId='" + questionId + '\'' +
                '}';
    }
}
