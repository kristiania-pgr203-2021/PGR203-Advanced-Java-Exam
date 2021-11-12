package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.SurveyDao;
import no.kristiania.jdbc.TestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerTest {

    private final HttpServer server = new HttpServer(0);
    SurveyDao surveyDao = new SurveyDao(TestData.testDataSource());
    QuestionDao questionDao = new QuestionDao(TestData.testDataSource());
    AlternativeDao alternativeDao = new AlternativeDao(TestData.testDataSource());

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
    void localhostShouldRedirectToIndexHtml() throws IOException {
        HttpClient client = new HttpClient("localhost", server.getPort(), "/");
        assertEquals(303, client.getStatusCode());
    }

    @Test
    void shouldPostAndGetNewSurvey() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));

        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=List+Survey");
        assertEquals(303, postClient.getStatusCode());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals("Newly added survey: List Survey", client.getMessageBody());
    }

    @Test
    void shouldPostAndGetNewQuestion() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestions", new ListQuestionsController(questionDao));

        HttpPostClient postClient1 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postClient1.getStatusCode());

        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals("Newly added survey: New Survey", client1.getMessageBody());

        HttpPostClient postClient2 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=New+Question");
        assertEquals(303, postClient2.getStatusCode());

        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertTrue(client2.getMessageBody().endsWith("New Question</p>"));
    }

    @Test
    void shouldPostAndGetNewAlternative() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestions", new ListQuestionsController(questionDao));
        server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
        server.addController("/api/listAlternatives", new ListAlternativesByQuestionIdController(alternativeDao));
        server.addController("/api/getQuestionId", new GetQuestionIdController());

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postSurvey.getStatusCode());


        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals("Newly added survey: New Survey", client1.getMessageBody());

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=New+Question+Again");
        assertEquals(303, postQuestion.getStatusCode());

        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertTrue(client2.getMessageBody().endsWith("New Question Again</p>"));

        HttpPostClient postAlternative = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addAlternative",
                "questionId=1&alternativeInput=New+Alternative+Again");
        assertEquals(303, postAlternative.getStatusCode());

        HttpPostClient postQuestionId = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/getQuestionId",
                "questionInput=1");
        assertEquals(303, postQuestionId.getStatusCode());

        HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternatives");
        assertTrue(client3.getMessageBody().endsWith("<li>New Alternative Again</li>"));
    }
    @Test
    void shouldListAllSurveys() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/listSurveys", new ListSurveysController(new SurveyDao(TestData.testDataSource())));

        HttpPostClient postClient = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=List+All+Surveys+Test");
        assertEquals(303, postClient.getStatusCode());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listSurveys");
        System.out.println(client.getMessageBody());
        assertTrue(client.getMessageBody().endsWith("List All Surveys Test</p>"));
    }

    @Test
    void shouldEditSurvey() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/editSurvey", new EditSurveyController(surveyDao));

        HttpPostClient postClient1 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=List+All+Surveys+Test");
        assertEquals(303, postClient1.getStatusCode());

        HttpPostClient postClient2 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/editSurvey",
                "surveyIdInput=1&surveyNameInput=Edited+Survey");
        assertEquals(303, postClient2.getStatusCode());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/editSurvey");
        //assertEquals("Edited Survey", client.getMessageBody());
    }

    @Test
    void shouldDeleteSurvey() {
        server.addController("/api/deleteSurvey", new DeleteSurveyController(new SurveyDao(TestData.testDataSource())));
    }

    @Test
    void shouldListAllQuestions() throws IOException {
        server.addController("/api/getSurveyId", new GetSurveyIdController());

        HttpPostClient getSurveyIdPost = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getSurveyId",
                    "surveyInput=1");
        assertEquals(303, getSurveyIdPost.getStatusCode());
    }
    @Test
    void shouldEditQuestion() throws IOException {
        server.addController("/api/editQuestion", new EditQuestionController(new QuestionDao(TestData.testDataSource()))); //TODO: HER HOLDER MARTIN PÅ

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postSurvey.getStatusCode());

        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals("Newly added survey: New Survey", client1.getMessageBody());

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=Old+Question");
        assertEquals(303, postQuestion.getStatusCode());

        HttpPostClient postEditQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/editQuestion",
                "questionIdInput=1&questionNameInput=New+Question");
        assertEquals(303, postEditQuestion.getStatusCode());

    }

    @Test
    void shouldDeleteQuestion() {
        server.addController("/api/DeleteQuestion", new DeleteQuestionController(new QuestionDao(TestData.testDataSource())));
    }

    @Test
    void ShouldListAllAlternatives() {
        server.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(new AlternativeDao(TestData.testDataSource())));
    }

    @Test
    void ShouldEditAlternative() {
        server.addController("/api/editAlternative", new EditAlternativeController(new AlternativeDao(TestData.testDataSource())));
    }

    @Test
    void shouldDeleteAlternative() {
        server.addController("/api/deleteAlternative", new DeleteAlternativeController(new AlternativeDao(TestData.testDataSource())));
    }
}
