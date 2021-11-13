package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.SurveyDao;
import no.kristiania.jdbc.TestData;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    void shouldRespondWithRequestTarget200() throws IOException {
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

    /**
     * This test can either pass by running all test suits at the same time, or it can only pass when
     * runs by itself. Thats because of the data from in-memory database
     **/
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
                "questionId=2&alternativeInput=New+Alternative+Again");
        assertEquals(303, postAlternative.getStatusCode());

        HttpPostClient postQuestionId = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/getQuestionId",
                "questionInput=2");
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
        assertTrue(client.getMessageBody().endsWith("List All Surveys Test</p>"));
    }

    @Test
    void shouldEditSurvey() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/editSurvey", new EditSurveyController(surveyDao));
        server.addController("/api/listSurveys", new ListSurveysController(surveyDao));

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

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listSurveys");
        assertThat(client.getMessageBody()).containsAnyOf("Edited Survey</p>");
    }

    @Test
    void shouldDeleteSurvey() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/deleteSurvey", new DeleteSurveyController(surveyDao, questionDao, alternativeDao));
        server.addController("/api/listSurveys", new ListSurveysController(surveyDao));

        HttpPostClient postClient1 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=This+Survey+Should+Be+Deleted"
        );
        assertEquals(303, postClient1.getStatusCode());

        HttpPostClient postClient2 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=Not+This+Survey"
        );
        assertEquals(303, postClient2.getStatusCode());

        HttpPostClient postClient3 = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/deleteSurvey",
                "surveyInput=1"
        );
        assertEquals(303, postClient3.getStatusCode());

        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listSurveys");
        assertTrue(client2.getMessageBody().startsWith("<p>ID: 2"));
    }

    @Test
    void shouldEditAndListQuestion() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/editQuestion", new EditQuestionController(questionDao));
        server.addController("/api/listQuestions", new ListQuestionsController(questionDao));

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postSurvey.getStatusCode());

        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals(200, client1.getStatusCode());

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=Old+Question");
        assertEquals(303, postQuestion.getStatusCode());

        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertTrue(client2.getMessageBody().endsWith("Old Question</p>"));

        HttpPostClient postEditQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/editQuestion",
                "questionIdInput=2&questionNameInput=New+Question");
        assertEquals(303, postEditQuestion.getStatusCode());

        HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertTrue(client3.getMessageBody().endsWith("New Question</p>"));
    }

    @Test
    void shouldDeleteQuestion() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestionsInEdit", new ListQuestionsInEditController(questionDao));
        server.addController("/api/deleteQuestion", new DeleteQuestionController(questionDao, alternativeDao));
        server.addController("/api/listQuestions", new ListQuestionsController(questionDao));


        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey");
        assertEquals(303, postSurvey.getStatusCode());

        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals(200, client1.getStatusCode());

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=Question+Should+Be+Deleted");
        assertEquals(303, postQuestion.getStatusCode());

        HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
        assertEquals(200, client.getStatusCode());
        assertThat(client.getMessageBody()).containsAnyOf("Question Should Be Deleted");

        HttpPostClient postDeleteQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/deleteQuestion",
                "questionInput=1");
        assertEquals(303, postDeleteQuestion.getStatusCode());

        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
        assertEquals(200, client2.getStatusCode());
        assertThat(client2.getMessageBody()).doesNotContain("Question Should Be Deleted");
    }

    /**
     * This test can either pass by running all test suits at the same time, or it can only pass when
     * runs by itself. Thats because of the data from in-memory database
     **/
    @Test
    void ShouldListAllAlternatives() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestionsInEdit", new ListQuestionsController(questionDao));
        server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
        server.addController("/api/getQuestionIdInEdit", new GetQuestionIdInEditController());
        server.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(alternativeDao));

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=Color+blindness"
        );
        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=Do+you+see+some+colors");
        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");

        HttpPostClient postAlternative = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addAlternative",
                "questionId=2&alternativeInput=Yes");

        HttpPostClient postQuestionId = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/getQuestionIdInEdit",
                "questionInput=2");
        HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternativesInEdit");

        assertTrue(client3.getMessageBody().endsWith("Yes</p>"));
    }

    //TODO: Husk å skrive dette inn på readme

    /**
     * This test can either pass by running all test suits at the same time, or it can only pass when
     * runs by itself. Thats because of the data from in-memory database
     **/
    @Test
    void ShouldEditAlternative() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestionsInEdit", new ListQuestionsController(questionDao));
        server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
        server.addController("/api/getQuestionIdInEdit", new GetQuestionIdInEditController());
        server.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(alternativeDao));

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=Color+blindness"
        );
        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=Do+you+see+some+colors"
        );
        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");

        HttpPostClient postAlternativeOld = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addAlternative",
                "questionId=3&alternativeInput=Yes"
        );

        HttpPostClient postAlternativeNew = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addAlternative",
                "questionId=3&alternativeInput=No");

        HttpPostClient postQuestionId = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/getQuestionIdInEdit",
                "questionInput=3"
        );

        HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternativesInEdit");
        assertTrue(client3.getMessageBody().endsWith("No</p>"));
    }

    @Test
    void shouldDeleteAlternative() throws IOException {
        server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
        server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
        server.addController("/api/addQuestion", new AddQuestionController(questionDao));
        server.addController("/api/listQuestionsInEdit", new ListQuestionsController(questionDao));
        server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
        server.addController("/api/getQuestionIdInEdit", new GetQuestionIdInEditController());
        server.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(alternativeDao));
        server.addController("/api/deleteAlternative", new DeleteAlternativeController(alternativeDao));

        HttpPostClient postSurvey = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addSurvey",
                "surveyInput=New+Survey"
        );
        HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
        assertEquals(303, postSurvey.getStatusCode());
        assertEquals(200, client1.getStatusCode());

        HttpPostClient postQuestion = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addQuestion",
                "questionInput=New+Question");
        HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
        assertEquals(303, postQuestion.getStatusCode())
        ;
        assertEquals(200, client2.getStatusCode());
        HttpPostClient postAlternative = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/addAlternative",
                "questionId=1&alternativeInput=Alternative+Should+Be+Deleted");
        assertEquals(303, postAlternative.getStatusCode());

        HttpPostClient postQuestionId = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/getQuestionIdInEdit",
                "questionInput=1");
        HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternativesInEdit");
        assertEquals(303, postQuestionId.getStatusCode());
        assertTrue(client3.getMessageBody().endsWith("Alternative Should Be Deleted</p>"));

        HttpPostClient postDeleteAlternative = new HttpPostClient(
                "localhost",
                server.getPort(),
                "/api/deleteAlternative",
                "alternativeInput=1");
        assertEquals(303, postDeleteAlternative.getStatusCode());
    }
}