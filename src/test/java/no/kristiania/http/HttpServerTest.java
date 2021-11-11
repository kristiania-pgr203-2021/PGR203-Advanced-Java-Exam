package no.kristiania.http;

import no.kristiania.jdbc.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    HttpServerTest() throws IOException {

    }

    @Test
    void shouldReturn404ForUnknownRequestTarget() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals(404, client.getStatusCode());
    }

    @Test
    void shouldRespondWithRequestTargetIn404() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/non-existing");
        assertEquals("File not found: /non-existing", client.getMessageBody());
    }

    @Test
    void shouldRespondWithRequestTarget200() throws IOException{
        HttpClient client = new HttpClient("localhost", server.getPort(), "/index.html");
        assertEquals(200, client.getStatusCode());
    }
    @Test
    void shouldListSurveysFromDatabase() throws SQLException, IOException {
        server.addController("/api/getSurvey", new AddSurveyController(new SurveyDao(TestData.testDataSource())));
        SurveyDao dao = new SurveyDao(TestData.testDataSource());
        Survey survey = TestData.exampleSurvey();
        dao.save(survey);
        for (Survey s :
                dao.listAll()) {
            System.out.println(s);
        }

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/index.html");
        assertEquals(200, client.getStatusCode());
       // assertEquals("Newly added survey: " + survey.getSurveyName(), client.getMessageBody());
    }
    @Test
    void shouldCreateNewSurvey() throws IOException, SQLException {
        server.addController("/api/addSurvey", new AddSurveyController(new SurveyDao(TestData.testDataSource())));
        SurveyDao dao = new SurveyDao(TestData.testDataSource());

        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");

        assertEquals(303, postClient.getStatusCode());
        String allSurveys = "";
        for (Survey survey : dao.listAll()){
            allSurveys = survey.getSurveyName();
        }

        String newSurveyName = "New Survey";
        assertThat(allSurveys)
                .containsAnyOf(newSurveyName);
    }
    @Test
    void shouldCreateNewQuestion() throws IOException, SQLException {
        server.addController("/api/addSurvey", new AddQuestionController(new QuestionDao(TestData.testDataSource())));
        SurveyDao dao = new SurveyDao(TestData.testDataSource());
        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postClient.getStatusCode());
        String allSurveys = "";
        for (Survey survey : dao.listAll()){
            allSurveys = survey.getSurveyName();
        }
        String newSurveyName = "New Survey";
        assertThat(allSurveys)
                .containsAnyOf(newSurveyName);
    }
    @Test
    void shouldListQuestionsFromDatabase() throws SQLException, IOException {
        SurveyDao sdao = new SurveyDao(TestData.testDataSource());
        Survey survey = TestData.exampleSurvey();
        sdao.save(survey);
        QuestionDao qdao = new QuestionDao(TestData.testDataSource());
        Question question = TestData.exampleQuestion();
        question.setSurveyId(survey.getId());
        qdao.save(question);
        server.addController("/api/listQuestions", new ListQuestionsController(new QuestionDao(TestData.testDataSource())));

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertEquals(200, client.getStatusCode());
        qdao.listQuestionsBySurveyId(survey.getId());

        // assertEquals("Newly added survey: " + survey.getSurveyName(), client.getMessageBody());
    }
    @Test
    void shouldCreateNewAlternative() throws IOException, SQLException {
        server.addController("/api/newAlternative", new AddAlternativeController(new AlternativeDao(TestData.testDataSource())));
        //TODO:
    }
    @Test
    void shouldGetQuestionIdFromUser() throws SQLException, IOException {
        server.addController("/api/listAlternativesByQuestion", new GetQuestionIdController());
       //TODO:
    }
    @Test
    void shouldListQuestionsFromQuestionId() throws SQLException, IOException {
        server.addController("/api/listAlternatives", new ListAlternativesByQuestionId(new AlternativeDao(TestData.testDataSource())));
        //TODO:
    }
}
