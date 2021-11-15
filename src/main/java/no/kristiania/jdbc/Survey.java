package no.kristiania.jdbc;

public class Survey {
    private Long id;
    private String surveyName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getSurveyName() {
        return surveyName;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id=" + id +
                ", surveyName='" + surveyName + '\'' +
                '}';
    }
}
