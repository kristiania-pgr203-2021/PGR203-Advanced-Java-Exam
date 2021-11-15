package no.kristiania.http;

import no.kristiania.jdbc.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpServerTest {

        private final HttpServer server = new HttpServer(0);

        SurveyDao surveyDao = new SurveyDao(TestData.testDataSource());
        QuestionDao questionDao = new QuestionDao(TestData.testDataSource());
        AlternativeDao alternativeDao = new AlternativeDao(TestData.testDataSource());
        UserDao userDao = new UserDao(TestData.testDataSource());
        AnswerDao answerDao = new AnswerDao(TestData.testDataSource());

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
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertTrue(client2.getMessageBody().contains("New Question"));

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=New+Alternative");
            assertEquals(303, postAlternative.getStatusCode());

            HttpPostClient postQuestionId = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getQuestionId",
                    "questionInput=1");
            assertEquals(303, postQuestionId.getStatusCode());

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternatives");
            assertTrue(client3.getMessageBody().endsWith("<li>New Alternative</li>"));
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
                    "surveyInput=New+Survey");
            assertEquals(303, postClient1.getStatusCode());

            HttpPostClient postClient2 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/editSurvey",
                    "surveyIdInput=1&surveyNameInput=Another+Survey");
            assertEquals(303, postClient2.getStatusCode());

            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listSurveys");
            assertThat(client.getMessageBody()).contains("Another Survey");
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
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertTrue(client2.getMessageBody().endsWith("New Question</p>"));

            HttpPostClient postEditQuestion = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/editQuestion",
                    "questionIdInput=1&questionNameInput=Another+Question");
            assertEquals(303, postEditQuestion.getStatusCode());

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertTrue(client3.getMessageBody().contains("Another Question"));
        }

        @Test
        void shouldDeleteQuestion() throws IOException {
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
            server.addController("/api/listQuestionsInEdit", new ListQuestionsInEditController(questionDao));
            server.addController("/api/getSurveyId", new GetSurveyIdController());
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

            HttpPostClient postQuestion2 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addQuestion",
                    "questionInput=Question+Should+Not+Be+Deleted");
            assertEquals(303, postQuestion2.getStatusCode());

            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertEquals(200, client.getStatusCode());
            assertThat(client.getMessageBody()).containsAnyOf("Question Should Be Deleted");

            HttpPostClient postDeleteQuestion = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/deleteQuestion",
                    "questionInput=1");
            assertEquals(303, postDeleteQuestion.getStatusCode());

            HttpPostClient postSurveyId = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getSurveyId",
                    "surveyInput=1");
            assertEquals(303, postDeleteQuestion.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
            assertEquals(200, client2.getStatusCode());
            assertFalse(client2.getMessageBody().contains("Question Should Be Deleted"));
            assertTrue(client2.getMessageBody().contains("Question Should Not Be Deleted"));
        }

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
                    "surveyInput=Color+blindness");
            assertEquals(303, postSurvey.getStatusCode());

            HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
            assertTrue(client1.getMessageBody().contains("Color blindness"));

            HttpPostClient postQuestion = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addQuestion",
                    "questionInput=Do+you+see+some+colors");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
            assertTrue(client2.getMessageBody().contains("Do you see some colors"));

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=Yes");
            assertEquals(303, postAlternative.getStatusCode());

            HttpPostClient postQuestionId = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getQuestionIdInEdit",
                    "questionInput=1");
            assertEquals(303, postQuestionId.getStatusCode());
            HttpClient client4 = new HttpClient("localhost", server.getPort(), "/api/listAlternativesInEdit");
            assertTrue(client4.getMessageBody().contains("Yes"));
        }

        @Test
        void ShouldEditAlternative() throws IOException {
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/listQuestionsInEdit", new ListQuestionsController(questionDao));
            server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
            server.addController("/api/editAlternative", new EditAlternativeController(alternativeDao));
            server.addController("/api/getQuestionIdInEdit", new GetQuestionIdInEditController());
            server.addController("/api/listAlternativesInEdit", new ListAlternativesInEditController(alternativeDao));
            server.addController("/api/deleteSurvey", new DeleteSurveyController(surveyDao, questionDao, alternativeDao));

            HttpPostClient postSurvey = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addSurvey",
                    "surveyInput=Color+blindness");
            assertEquals(303, postSurvey.getStatusCode());

            HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
            assertTrue(client1.getMessageBody().contains("Color blindness"));

            HttpPostClient postQuestion = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addQuestion",
                    "questionInput=Do+you+see+some+colors");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
            assertTrue(client2.getMessageBody().contains("Do you see some colors"));

            HttpPostClient postAlternativeOld = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=Yes");
            assertEquals(303, postAlternativeOld.getStatusCode());

            HttpPostClient postAlternativeNew = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/editAlternative",
                    "alternativeIdInput=1&alternativeNameInput=No");
            assertEquals(303, postAlternativeNew.getStatusCode());

            HttpPostClient postQuestionId = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getQuestionIdInEdit",
                    "questionInput=1");
            assertEquals(303, postQuestionId.getStatusCode());

            HttpPostClient postClient3 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/deleteSurvey",
                    "surveyInput=1"
            );
            assertEquals(303, postClient3.getStatusCode());

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listSurveys");
            assertFalse(client3.getMessageBody().contains("Color blindness"));
        }

        @Test
        void shouldDeleteAlternative() throws IOException {
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/listQuestionsInEdit", new ListQuestionsInEditController(questionDao));
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
            assertEquals(303, postSurvey.getStatusCode());

            HttpClient client1 = new HttpClient("localhost", server.getPort(), "/api/getSurvey");
            assertEquals("Newly added survey: New Survey", client1.getMessageBody());

            HttpPostClient postQuestion = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addQuestion",
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            //HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInEdit");
            //assertEquals("<p>ID: 1 Text: New Question</p>", client2.getMessageBody());

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=Alternative+Should+Be+Deleted");
            assertEquals(303, postAlternative.getStatusCode());

            HttpPostClient postAlternative2 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=Alternative+Should+Not+Be+Deleted");
            assertEquals(303, postAlternative2.getStatusCode());

            HttpPostClient postQuestionId = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/getQuestionIdInEdit",
                    "questionInput=1");
            assertEquals(303, postQuestionId.getStatusCode());

            HttpPostClient postDeleteAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/deleteAlternative",
                    "alternativeInput=1");
            assertEquals(303, postDeleteAlternative.getStatusCode());

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listAlternativesInEdit");
            assertFalse(client3.getMessageBody().contains("Alternative Should Be Deleted"));
            assertTrue(client3.getMessageBody().contains("Alternative Should Not Be Deleted"));
        }

        @Test
        void shouldListAddedSurveyInJoinSurvey() throws IOException {
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/listSurveysForm", new ListSurveysFormController(surveyDao));
            server.addController("/api/joinSurvey", new JoinSurveyController(surveyDao));
            server.addController("/api/selectedSurvey", new SelectedSurveyController());

            HttpPostClient postClient = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addSurvey",
                    "surveyInput=List+Survey");
            assertEquals(303, postClient.getStatusCode());
            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listSurveysForm");
            assertTrue(client.getMessageBody().contains("List Survey"));
        }

        @Test
        void shouldJoinSurveyAndShowJoinedSurveyInNextPage() throws IOException {
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/listSurveysForm", new ListSurveysFormController(surveyDao));
            server.addController("/api/joinSurvey", new JoinSurveyController(surveyDao));
            server.addController("/api/selectedSurvey", new SelectedSurveyController());

            HttpPostClient postClient = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addSurvey",
                    "surveyInput=List+Survey");
            assertEquals(303, postClient.getStatusCode());

            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listSurveysForm");
            assertTrue(client.getMessageBody().contains("List Survey"));

            HttpPostClient postClient1 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/joinSurvey",
                    "surveyName=1");
            assertEquals(303, postClient1.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/selectedSurvey");
            assertTrue(client2.getMessageBody().contains("List Survey"));
        }

        @Test
        void shouldSaveUserAndShowUserInNextPage() throws IOException {
            server.addController("/api/userForm", new UserFormController(userDao));
            server.addController("/api/getUser", new GetUserController());

            HttpPostClient postClient = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/userForm",
                    "firstName=Ola&lastName=Nordmann&email=test%40hotmail.com");
            assertEquals(303, postClient.getStatusCode());
            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/getUser");
            assertTrue(client.getMessageBody().contains("Ola"));
        }

        @Test
        void shouldListQuestionsAndAlternativeAndSaveAsAnswer() throws IOException {
            server.addController("/api/listQuestionsInAnswerSurvey", new ListQuestionsAndAlternativesController(questionDao, alternativeDao));
            server.addController("/api/answer", new AnswerController(alternativeDao, answerDao));
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
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
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertEquals(200, client.getStatusCode());
            assertThat(client.getMessageBody()).containsAnyOf("New Question");

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=New+Alternative");
            assertEquals(303, postAlternative.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInAnswerSurvey");
            assertEquals(200, client2.getStatusCode());
            assertTrue(client2.getMessageBody().contains("New Question"));

        }

        @Test
        void shouldSaveAnswersFromUser() throws IOException {
            server.addController("/api/listQuestionsInAnswerSurvey", new ListQuestionsAndAlternativesController(questionDao, alternativeDao));
            server.addController("/api/answer", new AnswerController(alternativeDao, answerDao));
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
            server.addController("/api/listQuestions", new ListQuestionsController(questionDao));
            server.addController("/api/answer", new AnswerController(alternativeDao, answerDao));
            server.addController("/api/userForm", new UserFormController(userDao));
            server.addController("/api/getUser", new GetUserController());

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
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/listQuestions");
            assertEquals(200, client.getStatusCode());
            assertThat(client.getMessageBody()).containsAnyOf("New Question");

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=New+Alternative");
            assertEquals(303, postAlternative.getStatusCode());

            HttpPostClient postClient = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/userForm",
                    "firstName=Ola&lastName=Nordmann&email=test%40hotmail.com");
            assertEquals(303, postClient.getStatusCode());
            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/getUser");
            assertTrue(client2.getMessageBody().contains("Ola"));

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/listQuestionsInAnswerSurvey");
            assertEquals(200, client2.getStatusCode());
            assertTrue(client3.getMessageBody().contains("New Question"));

            HttpPostClient postAnswer = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/answer",
                    "answerInput=1");
            assertEquals(303, postAnswer.getStatusCode());
        }

        @Test
        void shouldListAllQuestionBySurveyId() throws IOException {
            server.addController("/api/listQuestionsInAnswerSurvey", new ListQuestionsAndAlternativesController(questionDao, alternativeDao));
            server.addController("/api/answer", new AnswerController(alternativeDao, answerDao));
            server.addController("/api/addSurvey", new AddSurveyController(surveyDao));
            server.addController("/api/getSurvey", new GetSurveyController(surveyDao));
            server.addController("/api/addQuestion", new AddQuestionController(questionDao));
            server.addController("/api/addAlternative", new AddAlternativeController(alternativeDao));
            server.addController("/api/answer", new AnswerController(alternativeDao, answerDao));
            server.addController("/api/listAllQuestionsBySurveyIdForAnswers", new ListAllQuestionsBySurveyId(questionDao));
            server.addController("/api/selectAnsweredSurveys", new SelectAnsweredSurveys(surveyDao));
            server.addController("/api/selectedSurveyInListAnswered", new SelectedSurveyInListAnswered());
            server.addController("/api/listAllQuestionsBySurveyId", new ListAllQuestionsBySurveyId(questionDao));
            server.addController("/api/selectAnsweredQuestion", new SelectAnsweredQuestion(questionDao));
            server.addController("/api/listAllAnswers", new ListAllAnswers(answerDao));
            server.addController("/api/selectedQuestion", new SelectedQuestion());
            server.addController("/api/listSurveysForm", new ListSurveysFormController(surveyDao));
            server.addController("/api/joinSurvey", new JoinSurveyController(surveyDao));
            server.addController("/api/selectedSurvey", new SelectedSurveyController());
            server.addController("/api/userForm", new UserFormController(userDao));
            server.addController("/api/getUser", new GetUserController());

            HttpPostClient postClient = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/userForm",
                    "firstName=Ola&lastName=Nordmann&email=test%40hotmail.com");
            assertEquals(303, postClient.getStatusCode());
            HttpClient client = new HttpClient("localhost", server.getPort(), "/api/getUser");
            assertTrue(client.getMessageBody().contains("Ola"));

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
                    "questionInput=New+Question");
            assertEquals(303, postQuestion.getStatusCode());

            HttpPostClient postAlternative = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addAlternative",
                    "questionId=1&alternativeInput=New+Alternative");
            assertEquals(303, postAlternative.getStatusCode());

            HttpPostClient postClient0 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/addSurvey",
                    "surveyInput=List+Survey");
            assertEquals(303, postClient0.getStatusCode());

            HttpClient client2 = new HttpClient("localhost", server.getPort(), "/api/listSurveysForm");
            assertTrue(client2.getMessageBody().contains("List Survey"));

            HttpPostClient postClient1 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/selectAnsweredSurveys",
                    "surveyName=1");
            assertEquals(303, postClient1.getStatusCode());

            HttpPostClient postClient2 = new HttpPostClient(
                    "localhost",
                    server.getPort(),
                    "/api/selectAnsweredQuestion",
                    "questionName=1");
            assertEquals(303, postClient2.getStatusCode());

            HttpClient client3 = new HttpClient("localhost", server.getPort(), "/api/selectedSurveyInListAnswered");
            assertEquals(200, client3.getStatusCode());

            HttpClient client4 = new HttpClient("localhost", server.getPort(), "/api/listAllQuestionsBySurveyId");
            assertEquals(200, client4.getStatusCode());

            HttpClient client5 = new HttpClient("localhost", server.getPort(), "/api/selectedQuestion");
            assertEquals(200, client5.getStatusCode());

            HttpClient client6 = new HttpClient("localhost", server.getPort(), "/api/listAllAnswers");
            assertEquals(200, client5.getStatusCode());
        }

    }

