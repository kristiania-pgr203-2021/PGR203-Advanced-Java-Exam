package no.kristiania.http;

import no.kristiania.jdbc.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;
import static no.kristiania.http.UrlEncoding.encodeValue;

public class HttpServer {
    private final ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private AlternativeDao alternativeDao;
    private Survey survey;
    private Question question;
    private Alternative alternative;
    private long surveyId;
    private long questionId;
    private long tmpQuestionId;

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);
        new Thread(this::handleClients).start();
    }

    private void handleClients() {
        try {
            while (true) {
                handleClient();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleClient() throws IOException, SQLException {
        Socket clientSocket = serverSocket.accept();

        HttpMessage httpMessage = new HttpMessage(clientSocket);
        String[] requestLine = httpMessage.startLine.split(" ");
        String requestTarget = requestLine[1];

        int questionPos = requestTarget.indexOf('?');
        String fileTarget;
        String query = null;
        if (questionPos != -1) {
            fileTarget = requestTarget.substring(0, questionPos);
            query = requestTarget.substring(questionPos+1);
        } else {
            fileTarget = requestTarget;
        }

        if (fileTarget.equals("/api/newSurvey")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            SurveyDao dao = new SurveyDao(createDataSource());
            this.survey = new Survey();
            String encodedValue = encodeValue(queryMap.get("survey_text"));
            String decodedValue = decodeValue(queryMap.get("survey_text"));
            System.out.println(decodedValue);
            System.out.println(encodedValue);
            survey.setSurveyName(decodedValue);
            dao.save(survey);
            writeOk303Response(clientSocket, survey.toString(), "text/html");

        } else if (fileTarget.equals("/api/surveyName")) {
            String responseTxt = "";
            long tmp = 1;

            for (Survey survey : surveyDao.listAll()) {
                responseTxt = "Newly added survey: " + survey.getSurveyName();
                tmp = survey.getId();
            }
            this.surveyId = tmp;
            writeOk200Response(clientSocket, responseTxt, "text.html");

        } else if (fileTarget.equals("/api/newQuestion")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            QuestionDao dao = new QuestionDao(createDataSource());
            this.question = new Question();
            String decodedValue = decodeValue(queryMap.get("question_text"));
            question.setQuestionText(decodedValue);
            question.setSurveyId(surveyId);
            dao.save(question);
            writeOk303Response(clientSocket, "Question added", "text/html");

        } else if (fileTarget.equals("/api/questionOptions")) {
            int value = 1;
            String responseTxt = "";
            long tmp = 0;
            System.out.println(responseTxt);
            for (Question question: questionDao.listQuestionsBySurveyId(surveyId)) {
                responseTxt +=  "<option value=" + (value++) + ">" + question.getId() + ". "+question.getQuestionText() + "</option>";
                tmp = question.getId();
                System.out.println(responseTxt);
            }
            this.questionId = tmp;
            writeOk200Response(clientSocket, responseTxt, "text/html");

        } else if (fileTarget.equals("/api/newAlternative")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            AlternativeDao dao = new AlternativeDao(createDataSource());
            this.alternative = new Alternative();
            String decodedValue = decodeValue(queryMap.get("alternative_text"));
            alternative.setAlternative(decodedValue);
            alternative.setQuestionId(questionId);
            dao.save(alternative);

            writeOk303Response(clientSocket, "Alternative added", "text/html");

        } else if (fileTarget.equals("/api/listAlternativesByQuestion")){
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            this.tmpQuestionId = Long.parseLong(queryMap.get("questionId"));
            writeOk303Response(clientSocket, "Question ID added", "text/html");

        } else if (fileTarget.equals("/api/listAlternatives")) {
            String responseTxt = "";

            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(tmpQuestionId)) {
                responseTxt += "<li>" + alternative.getAlternative() + "</li>";
                System.out.println(responseTxt);
            }

            writeOk200Response(clientSocket, responseTxt, "text/html");

        } else if (fileTarget.equals("/api/surveyOptions")) {
            int value = 1;
            String responseText = "";

            for (Survey survey : surveyDao.listAll()) {
                responseText += "<option value=" + (value++) + ">" + survey.getSurveyName() + "</option>";
                System.out.println(responseText);
            }
            writeOk200Response(clientSocket, responseText, "text/html");

        } else {
            InputStream fileResource = getClass().getResourceAsStream(fileTarget);

            if (fileResource != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                fileResource.transferTo(buffer);
                String responseText = buffer.toString();
                String contentType = "text/plain";

                if (requestTarget.endsWith(".html")) {
                    contentType = "text/html";
                }
                writeOk200Response(clientSocket, responseText, contentType);
                return;
            }

            String responseText = "File not found: " + requestTarget;
            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length: " + responseText.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        }
    }

    private Map<String, String> parseRequestParameters(String query) {
        Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) {
            int equalsPos = queryParameter.indexOf('=');
            String parameterName = queryParameter.substring(0, equalsPos);
            String parameterValue = queryParameter.substring(equalsPos+1);
            queryMap.put(parameterName, parameterValue);
        }
        return queryMap;
    }

    private void writeOk200Response(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseText.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void writeOk303Response(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 303 See Other\r\n" +
                "Location: http://localhost:1962/newQuestions.html\r\n" +
                "Content-Length: " + responseText.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setSurveyDao(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    public void setQuestionDao(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    public void setAlternativeDao(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/surveydb");
        dataSource.setUser("surveyuser");
        dataSource.setPassword("test");
        //Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
