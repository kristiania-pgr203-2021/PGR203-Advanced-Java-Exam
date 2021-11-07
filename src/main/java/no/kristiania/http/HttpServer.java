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
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private final ServerSocket serverSocket;
    private SurveyDao surveyDao;

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
            Survey survey = new Survey();
            survey.setSurveyName(queryMap.get("surveyTitle"));
            dao.save(survey);

            writeOkResponse(clientSocket, "Survey added", "text/html");

        } else if (fileTarget.equals("/api/newQuestion")) {

            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            QuestionDao qDao = new QuestionDao(createDataSource());
            Question question = new Question();
            question.setQuestionText(queryMap.get("question_text"));
            question.setSurveyId(Long.valueOf(queryMap.get("survey")));

            qDao.save(question);

            /*

            for (Question questions : qDao.listQuestionsBySurveyId()) {
                responseText += "<option value=" + (value++) + ">" + survey.getSurveyName() + "</option>";
            }

             */



            writeOkResponse(clientSocket, "Question added", "text/html");

        } else if (fileTarget.equals("/api/surveyOptions")){
            String responseText = "";

            int value = 1;
            for (Survey survey : surveyDao.listAll()) {
                responseText += "<option value=" + (value++) + ">" + survey.getSurveyName() + "</option>";
            }



            writeOkResponse(clientSocket, responseText, "text.html");

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
                writeOkResponse(clientSocket, responseText, contentType);
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
    private void writeOkResponse(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseText.length() + "\r\n" +
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
}
