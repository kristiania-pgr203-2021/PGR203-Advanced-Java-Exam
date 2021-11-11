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
import static no.kristiania.http.UrlEncoding.utf8Value;

public class HttpServer {
    private static ServerSocket serverSocket;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private AlternativeDao alternativeDao;
    private Survey survey;
    private Question question;
    private Alternative alternative;
    private long surveyId;
    private long questionId;
    private long tmpQuestionId;
    private Long questionIdForAlternative;
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

            //TODO: Bruker velger spørsmål som de ønsker å liste ut alt ifra (POST)
        } else if (fileTarget.equals("/api/listAlternativesByQuestion")) {
            String location = "/createSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            this.questionId= Long.parseLong(queryMap.get("questionId"));

            writeOk303Response(clientSocket, "Question ID added", "text/html", location);

            //TODO: Webserver lister ut alle alternativene til ett spesifikt spørsmål (GET)
        } else if (fileTarget.equals("/api/listAlternatives")) {
            String responseTxt = "";
            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(tmpQuestionId)) {
                responseTxt += "<li>" + utf8Value(alternative.getAlternative()) + "</li>";
            }

            writeOk200Response(clientSocket, responseTxt, "text/html");

            //TODO: Viser alle survyene
        } else if (fileTarget.equals("/api/surveyOptions")) {
            String responseText = "";

            for (Survey survey : surveyDao.listAll()) {
                responseText += "<p>" + "ID: " + survey.getId() + " " + "Name: " + survey.getSurveyName() + "</p>";
            }
            writeOk200Response(clientSocket, responseText, "text/html");

            //TODO: Finner id til survey og endrer navnet på den
        } else if (fileTarget.equals("/api/editSurvey")) {
            String location = "/selectSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            SurveyDao dao = new SurveyDao(SurveyManager.createDataSource());

            Long id = Long.valueOf(queryMap.get("surveyID"));
            String name = queryMap.get("surveyName");
            dao.update(id, decodeValue(name));

            writeOk303Response(clientSocket, "Survey edited", "text/html", location);

            //TODO: Finner id til question og endrer navnet på den
        } else if (fileTarget.equals("/api/editQuestion")) {
            String location = "/selectSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            QuestionDao dao = new QuestionDao(SurveyManager.createDataSource());

            Long id = Long.valueOf(queryMap.get("questionId"));
            String name = queryMap.get("questionName");
            dao.update(id, decodeValue(name));

            writeOk303Response(clientSocket, "Question edited", "text/html", location);

            //TODO: Finner id til alternative og endrer navnet på den
        } else if (fileTarget.equals("/api/editAlternative")) {
            String location = "/selectSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            AlternativeDao dao = new AlternativeDao(SurveyManager.createDataSource());

            Long id = Long.valueOf(queryMap.get("alternativeId"));
            String name = queryMap.get("alternativeName");
            dao.update(id, decodeValue(name));

            writeOk303Response(clientSocket, "Alternative edited", "text/html", location);

            //TODO: lister ut alle questions tilhørende et survey etter id fra surveyId
        } else if (fileTarget.equals("/api/newQuestionOptions")) {

            String Test = "";
            for (Question question : questionDao.listQuestionsBySurveyId(questionId)) {
                Test += "<p>" + "ID: " + question.getId() + " " + "Question text: " + utf8Value(question.getQuestionText()) + "</p>";
            }

            writeOk200Response(clientSocket, Test, "text/html");
            //TODO: lister ut alle alternatives tilhørende et question etter id fra questionId *2  !!!!DU JOBBER HER NÅ!!!!
        } else if (fileTarget.equals("/api/alternativesOptions")) {
                System.out.println("Id som skal oppdateres:" + questionIdForAlternative);
                String  responseTxt = "";
                if (questionIdForAlternative != null) {
                    for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(questionIdForAlternative)) {
                        responseTxt += "<p>" + "ID: " + alternative.getId() + " " + "Alternative text: " + utf8Value(alternative.getAlternative()) + "</p>";
                    }
                }
            writeOk200Response(clientSocket, responseTxt, "text/html");
            //TODO: lister ut alle alternatives tilhørende et question etter id fra questionId *2
        } else if (fileTarget.equals("/api/newAlternativesOptions")) {
            System.out.println("Id som skal oppdateres:" + questionIdForAlternative);
            String  responseTxt = "";
            if (questionIdForAlternative != null) {
                for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(questionIdForAlternative)) {
                    responseTxt += "<p>" + "ID: " + alternative.getId() + " " + "Alternative text: " + utf8Value(alternative.getAlternative()) + "</p>";
                }
            }
            writeOk200Response(clientSocket, responseTxt, "text/html");
            //TODO: Henter question ID for å så sette det i questionId
        } else if (fileTarget.equals("/api/getQuestionById")){
            String location = "/selectSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);

            int id = Integer.parseInt(queryMap.get("surveyID"));
            this.questionId = id;

            writeOk303Response(clientSocket, "survey id set", "text/html", location);
            //TODO: Henter question ID for å sette veriden i questionId * 2      !!!!!DU JOBBER HER NÅ!!!!!
        } else if (fileTarget.equals("/api/getAlternativesById")){
            String location = "/selectSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);

            Long id = Long.valueOf(queryMap.get("questionId"));
            this.questionIdForAlternative = id;
            System.out.println("Ny ønsket id:" +id);

            writeOk303Response(clientSocket, "survey id set", "text/html", location);
            //TODO: Henter question ID for å sette veriden i questionId * 2
        } else if (fileTarget.equals("/api/newGetAlternativesById")){
            String location = "/createSurvey.html";
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);

            Long id = Long.valueOf(queryMap.get("questionId"));
            this.questionIdForAlternative = id;


            writeOk303Response(clientSocket, "survey id set", "text/html", location);

           //TODO: Sletter survey og tilhørende questions / alternatives
        } else if (fileTarget.equals("/api/selectSurvey")) {
            String location = "/selectSurvey.html";
            SurveyDao dao = new SurveyDao(SurveyManager.createDataSource());
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String idInput = queryMap.get("surveyID");
            for (Question question : questionDao.listQuestionsBySurveyId(Long.parseLong(idInput))) {
                for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(question.getId())) {
                    alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
                }
            }
            for (Question dq : questionDao.listQuestionsBySurveyId(Long.parseLong(idInput))) {
                questionDao.deleteBySurveyId(Math.toIntExact(dq.getSurveyId()));
            }
            dao.delete(Integer.parseInt(queryMap.get("surveyID")));

            writeOk303Response(clientSocket, "Alternative added", "text/html", location);

            //TODO: Sletter question og tilhørende alternatives
        } else if (fileTarget.equals("/api/deleteQuestion")) {
                String location = "/selectSurvey.html";
                QuestionDao qDao = new QuestionDao(SurveyManager.createDataSource());
                Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
                String idInput = queryMap.get("questionID");

                for (Alternative alternative: alternativeDao.listAlternativesByQuestionId(Long.parseLong(idInput))){
                    alternativeDao.deleteByQuestionId(Math.toIntExact(alternative.getQuestionId()));
                }
                qDao.delete(Integer.parseInt(idInput));

                writeOk303Response(clientSocket, "deleted", "text/html", location);

            //TODO: Sletter alternative
        } else if (fileTarget.equals("/api/deleteAlternative")) {
            String location = "/selectSurvey.html";
            AlternativeDao aDao = new AlternativeDao(SurveyManager.createDataSource());
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            String idInput = queryMap.get("alternativeId");

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
                    String location = "/index.html";
                    writeOk303Response(clientSocket, "index.html", contentType, location);
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
