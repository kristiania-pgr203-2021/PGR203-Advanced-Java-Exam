package no.kristiania.http;

import no.kristiania.jdbc.*;

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

public class HttpServer {
    private static ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private AlternativeDao alternativeDao;
    private UserDao userDao;
    private Survey survey;
    private Question question;
    private Alternative alternative;
    private HashMap<Integer, String> mapSurvey = new HashMap();
    private HashMap<String, HttpController> controllers = new HashMap<>();
    private Integer surveyId;
    private User user;
    private Integer userId;

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
      
        if (fileTarget.equals("/api/listSurveysForm")){
            String messageBody = "";

            for (Survey survey : surveyDao.listAll()) {
                int value = Math.toIntExact(survey.getId());
                System.out.println("SurveyID: " + value);
                messageBody += "<option value=" + (value) + ">" + "ID: " + survey.getId() + " " + survey.getSurveyName() + "</option>";
            }

            System.out.println("This is messagebody (HTML): " + messageBody);
            writeOk200Response(clientSocket, messageBody, "text/html");

        } else if(fileTarget.equals("/api/joinSurvey")){
            String location = "/addUser.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String surveyId = queryMap.get("surveyName");

            System.out.println("Valgte survey sin ID: " + surveyId);
            System.out.println("Valgte survey sin tittel (retrieve): " + surveyDao.retrieve(Long.parseLong(surveyId)));
            String[] name = String.valueOf(surveyDao.retrieve(Long.parseLong(surveyId))).split("'");
            System.out.println("Survey etter split: " + name[1]);

            mapSurvey.put(Integer.valueOf(surveyId), name[1]);
            this.surveyId = Integer.valueOf(surveyId);

            writeOk303Response(clientSocket, surveyId, "text/html", location);

        } else if (fileTarget.equals("/api/selectedSurvey")) {
            System.out.println("Survey tittel som skrives ut: " + mapSurvey.get(this.surveyId));
            String messageBody = "";
            messageBody += "<h1>" + mapSurvey.get(this.surveyId) + "</h1>";
            writeOk200Response(clientSocket, messageBody, "text/html");

        } else if (requestTarget.equals("/api/userForm")) {
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String firstName = decodeValue(queryMap.get("firstName"));
            String lastName = decodeValue(queryMap.get("lastName"));
            String email = decodeValue(queryMap.get("email"));

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            UserDao userDao = new UserDao(SurveyManager.createDataSource());
            userDao.save(user);
            System.out.println("Henter ut bruker ID og navn: " + user.getId() + ", " + user.getFirstName());
            mapSurvey.put(Math.toIntExact(user.getId()), user.getFirstName());
            this.userId = Math.toIntExact(user.getId());

            System.out.println("userForm post request inn i databasen: \rFirst name: " + firstName + " last name: " + lastName + " email: " + email);
            writeOk303Response(clientSocket, "Personal information submitted!", "text/html", "/answerSurvey.html");

        } else if (requestTarget.equals("/api/getUser")) {
            System.out.println("Brukernavnet som skal skrives ut: " + mapSurvey.get(this.userId));

            String messageBody = "";
            messageBody += "<p>" + mapSurvey.get(this.userId) + "</p>";
            writeOk200Response(clientSocket, messageBody, "text/html");
        } else if (requestTarget.equals("/api/listQuestionsInAnswerSurvey")) {




            /*
            int questionId = 0;
            for (Question question: questionDao.listQuestionsBySurveyId(surveyId)){
                messageBody +=

                        "<div class=\"white_div\">   \n" +
                        "        <form action=\"\">\n" +
                        "            <h1>"+ question.getQuestionText() +"</h1>\n" +
                        "            <p><label><input type=\"radio\" name=\"alternative\">Alternative 1</label></p>\n" +
                        "            <p><label><input type=\"radio\" name=\"alternative\">Alternative 2</label></p>\n" +
                        "            <p></p><label><input type=\"radio\" name=\"alternative\">Alternative 3</label></p>\n" +
                        "            <button>Submit</button>\n" +
                        "        </form>\n" +
                        "</div>";

            }
            */
            int alternativeIds = 0;
            String alternativeText= "";
            int surveyId = 0;
            for (Alternative alternative : alternativeDao.listAll()){
                alternativeIds = Math.toIntExact(alternative.getQuestionId());
                //alternativeText =
                System.out.println("Alternativ ID: " + alternativeIds);
                System.out.println("Alternativ tekst: " + alternativeText + "\r");
            }
            for (Question question: questionDao.listAll()){
                question.getId();
            }

            String messageBody = "";
            for (Question question: questionDao.listQuestionsBySurveyId(surveyId)){
                messageBody += "Dette er question tekst: " +question.getQuestionText() + "Dette er alternative ID: " + alternativeIds +
                        "<div class=\"white_div\">   \n" +
                                "        <form action=\"\">\n" +
                                "            <h1>"+ question.getQuestionText() +"</h1>\n" +
                                "            <p><label><input type=\"radio\" name=\"alternative\">" + ""  + "</label></p>\n" +
                                "            <p><label><input type=\"radio\" name=\"alternative\">" + "" + "</p>\n" +
                                "            <p></p><label><input type=\"radio\" name=\"alternative\">" + "" +"</label></p>\n" +
                                "            <button>Submit</button>\n" +
                                "        </form>\n" +
                                "</div>";

            }

            writeOk200Response(clientSocket, messageBody, "text/html");
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

    public void addController(String path, HttpController controller) {
        controllers.put(path, controller);
    }
}
