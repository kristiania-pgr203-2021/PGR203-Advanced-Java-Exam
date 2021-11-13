package no.kristiania.http;

import no.kristiania.jdbc.*;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private static ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private AlternativeDao alternativeDao;
    private Survey survey;
    private Question question;
    private Alternative alternative;

    private int questionIdForAlternative;
    private HashMap<String, HttpController> controllers = new HashMap<>();

    public Survey getSurvey() {
        return survey;
    }

    public Question getQuestion() {
        return question;
    }

    public Alternative getAlternative() {
        return alternative;
    }

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
        String[] requestLine = httpMessage.statusCode.split(" ");
        String requestTarget = requestLine[1];

        int questionPos = requestTarget.indexOf('?');
        String fileTarget;
        String query = null;
        if (questionPos != -1) {
            fileTarget = requestTarget.substring(0, questionPos);
            query = requestTarget.substring(questionPos + 1);
        } else {
            fileTarget = requestTarget;
        }

        if (controllers.containsKey(fileTarget)) {
            HttpMessage response = controllers.get(fileTarget).handle(httpMessage);
            response.write(clientSocket);
            return;
        }

        if (fileTarget.equals("/api/getSurveyId")){
            String location = "/editSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            //this.surveyId = Integer.parseInt(queryMap.get("surveyInput"));
            writeOk303Response(clientSocket, "survey id set", "text/html", location);

            //TODO: Henter question ID for å sette veriden i questionId * 2         !!Refactored!!
        } else if (fileTarget.equals("/api/getQuestionIdInEdit")){
            String location = "/editSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);

            int id = Integer.parseInt(queryMap.get("questionInput"));
            this.questionIdForAlternative = id;

            writeOk303Response(clientSocket, "survey id set", "text/html", location);

            //TODO: Sletter question og tilhørende alternatives     !!Refactored!!
        } else if (fileTarget.equals("/api/deleteAlternative")) {
            String location = "/editSurvey.html";
            AlternativeDao aDao = new AlternativeDao(SurveyManager.createDataSource());
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String idInput = queryMap.get("alternativeInput");

            aDao.delete(Integer.parseInt(idInput));

            writeOk303Response(clientSocket, "deleted", "text/html", location);

            //TODO: Sletter question og tilhørende alternatives     !!Refactored!!
        } else if (fileTarget.equals("/api/deleteQuestion")) {
                String location = "/editSurvey.html";
                QuestionDao qDao = new QuestionDao(SurveyManager.createDataSource());
                Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
                String idInput = queryMap.get("questionInput");

                for (Alternative alternative: alternativeDao.listAlternativesByQuestionId(Long.parseLong(idInput))){
                    alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
                }
                qDao.delete(Integer.parseInt(idInput));

                writeOk303Response(clientSocket, "deleted", "text/html", location);

            //TODO: Sletter alternative                               !!Refactored!!!
        } else if (fileTarget.equals("/api/deleteAlternative")) {
            String location = "/editSurvey.html";
            AlternativeDao aDao = new AlternativeDao(SurveyManager.createDataSource());
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String idInput = queryMap.get("alternativeInput");

            aDao.delete(Integer.parseInt(idInput));

            writeOk303Response(clientSocket, "deleted", "text/html", location);

        } else {
            InputStream fileResource = getClass().getResourceAsStream(fileTarget);

            if (fileResource != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                fileResource.transferTo(buffer);
                String responseText = buffer.toString();

                String contentType = "text/plain";
                if (requestTarget.endsWith(".html")) {
                    contentType = "text/html; charset=utf-8";
                    writeOk200Response(clientSocket, responseText, contentType);
                }

                if (requestTarget.endsWith(".css")) {
                    contentType = "text/css; charset=utf-8";
                    writeOk200Response(clientSocket, responseText, contentType);
                }

                if (requestTarget.endsWith("/")) {
                    contentType = "text/html; charset=utf-8";
                    String location = "/index.html";
                    writeOk303Response(clientSocket, responseText, contentType, location);
                }
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

    private void writeOk200Response(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseText.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    private void writeOk303Response(Socket clientSocket, String responseText, String contentType, String location) throws IOException {
        String response = "HTTP/1.1 303 See Other\r\n" +
                "Location: http://localhost:" + getPort() + location + "\r\n" +
                "Content-Length: " + responseText.getBytes().length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static int getPort() {
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

    public void addController(String path, HttpController controller) {
        controllers.put(path, controller);
    }
}
