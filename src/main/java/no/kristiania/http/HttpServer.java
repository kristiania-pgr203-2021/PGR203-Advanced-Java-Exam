package no.kristiania.http;

import no.kristiania.jdbc.Question;
import no.kristiania.jdbc.QuestionDao;
import no.kristiania.jdbc.Survey;
import no.kristiania.jdbc.SurveyDao;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private final ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private Survey survey;
    private Question question;

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

            for (Survey survey : surveyDao.listAll()) {
                responseTxt = "Newly added survey: " + survey.getSurveyName();
            }
                writeOk200Response(clientSocket, responseTxt, "text.html");

        } else if (fileTarget.equals("/api/newQuestion")) {
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            QuestionDao qDao = new QuestionDao(createDataSource());
            this.question = new Question();
            String decodedValue = decodeValue(queryMap.get("question_text"));
            question.setQuestionText(decodedValue);
            question.setSurveyId(Long.valueOf(queryMap.get("survey")));
            System.out.println(decodedValue);

            qDao.save(question);

            /*

            for (Question questions : qDao.listQuestionsBySurveyId()) {
                responseText += "<option value=" + (value++) + ">" + survey.getSurveyName() + "</option>";
            }

             */
            writeOk303Response(clientSocket, "Question added", "text/html");

        } else if (fileTarget.equals("/api/surveyOptions")) {
            String responseText = "";

            int value = 1;
            for (Survey survey : surveyDao.listAll()) {
                responseText += "<option value=" + (value++) + ">" + survey.getSurveyName() + "</option>";
            }

            writeOk200Response(clientSocket, responseText, "text.html");
        } else if (fileTarget.equals("/api/newAlternative")) {

            //Skrive ut alternative

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
    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/surveydb");
        dataSource.setUser("surveyuser");
        dataSource.setPassword("test");
        //Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
    public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
