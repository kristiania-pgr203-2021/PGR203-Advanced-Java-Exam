package no.kristiania.http;

import no.kristiania.jdbc.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;

public class HttpServer {
    private static ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private AlternativeDao alternativeDao;
    private UserDao userDao;
    private Survey survey;
    private Question question;
    private Alternative alternative;
    public static HashMap<Integer, String> mapSurvey = new HashMap();
    public static HashMap<Integer, String> mapInAnswered = new HashMap();
    public static HashMap<Integer, String> mapUser = new HashMap();
    private HashMap<Integer, Integer> alternativeQuestionMap = new HashMap<>();
    private HashMap<String, HttpController> controllers = new HashMap<>();
    private Integer surveyId;
    private User user;
    private Integer userId;
    private long questionId;
    private AnswerDao answerdao;

    public Integer getSurveyId() {
        return surveyId;
    }

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

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setAnswerDao(AnswerDao answerdao) {
        this.answerdao = answerdao;
    }

    public void addController(String path, HttpController controller) {
        controllers.put(path, controller);
    }
}
