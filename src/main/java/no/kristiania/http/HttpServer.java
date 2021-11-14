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
    private HashMap<Integer, Integer> alternativeQuestionMap = new HashMap<>();
    private HashMap<String, HttpController> controllers = new HashMap<>();
    private Integer surveyId;
    private User user;
    private Integer userId;
    private long questionId;
    private AnswerDao answerdao;

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
            String messageBody = "";
            System.out.println("Survey ID som vi henter ned: " + this.surveyId);

            /** Lister ut alle spørsmål tilhørende en survey **/
            int questionId = 0;
            String questionText = "";
            for (Question question : questionDao.listQuestionsBySurveyId(this.surveyId)) {
                /** Starten på messageBody **/
                messageBody +=
                        "<div class=\"white_div\">" +
                                "<h2>Question ID: " + question.getId() + ", Tekst: " + question.getQuestionText() +"</h2>" +
                                "<form action=\"api/answer\" method=\"POSt\" accept-charset=\"UTF-8\">";

                /** Looper ut alle alternativene og bygger messageBody **/
                int alternativeIds = 0;
                String alternativeText= "";
                for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(question.getId())){
                    //alternativeQuestionMap.put(alternativeIds, Math.toIntExact(question.getId()));
                    alternativeIds = Math.toIntExact(alternative.getQuestionId());
                    alternativeText = alternative.getAlternative();
                    messageBody += "<p><label><input type=\"radio\" name=\"answerInput\" value=\"" + alternativeIds + "\">" + alternativeText +"</label></p>";
                }

                /** Avsluttende tag på messageBody**/
                messageBody += "<button>Submit</button>" +
                        "</form>" +
                        "</div>";
            }
            writeOk200Response(clientSocket, messageBody, "text/html");

        } else if (fileTarget.equals("/api/answer")) {
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            int alternativeId = Integer.parseInt(queryMap.get("answerInput"));
            System.out.println("SVAR: Alternativ ID: " + alternativeId);
            String[] alternativeArray = String.valueOf(alternativeDao.retrieve(alternativeId)).split("'");
            System.out.println("Alternative retrieve: " + alternativeDao.retrieve(alternativeId));
            System.out.println(alternativeArray[3]);

            Answer answer = new Answer();
            answer.setAlternativeId(Long.valueOf(alternativeId));
            System.out.println("Alternativ ID satt");
            answer.setQuestionId(Long.valueOf(alternativeArray[3]));
            System.out.println("Question ID satt til: " + Long.valueOf(alternativeArray[3]));
            answer.setUserId(Long.valueOf(this.userId));
            System.out.println("Bruker ID satt");
            AnswerDao answerDao = new AnswerDao(SurveyManager.createDataSource());
            answerDao.save(answer);

            writeOk303Response(clientSocket, "Answer submitted!", "text/html", "/answerSurvey.html");

        } else if(fileTarget.equals("/api/listAnsweredSurveys")) {

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
