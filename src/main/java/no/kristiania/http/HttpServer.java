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

        //TODO: Bruker oppretter survey name og legger inn i databasen (GET)
        if (fileTarget.equals("/api/newSurvey")) {
            String location = "/createSurvey.html";
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            SurveyDao dao = new SurveyDao(SurveyManager.createDataSource());
            this.survey = new Survey();
            String decodedValue = decodeValue(queryMap.get("survey_text"));
            survey.setSurveyName(decodedValue);
            dao.save(survey);


            //String test1 = new String("æåæ".getBytes("UTF-8"));


            writeOk303Response(clientSocket, "Survey added", "text/html", location);

            //TODO: Webserver henter ut survey navn fra dtaabasen (POST)
        } else if (fileTarget.equals("/api/surveyName")) {
            String responseTxt = "";
            long tmp = 1;

            for (Survey survey : surveyDao.listAll()) {
                //String encoded = new String(survey.getSurveyName().getBytes("UTF-8"));
                responseTxt = "Newly added survey: " + utf8Value(survey.getSurveyName());
                tmp = survey.getId();
                System.out.println(survey.getSurveyName());

            }

            this.surveyId = tmp;
            writeOk200Response(clientSocket, responseTxt, "text/html");

            //TODO: Bruker lager spørsmål og sender inn i databasen, knyttet til surveyId (GET)
        } else if (fileTarget.equals("/api/newQuestion")) {
            String location = "/createSurvey.html";
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            QuestionDao dao = new QuestionDao(SurveyManager.createDataSource());
            this.question = new Question();
            String decodedValue = decodeValue(queryMap.get("question_text"));
            question.setQuestionText(decodedValue);
            question.setSurveyId(surveyId);
            dao.save(question);
            writeOk303Response(clientSocket, "Question added", "text/html", location);

            //TODO: Webserver lister ut spørsmålene som har blitt laget (POST)
        } else if (fileTarget.equals("/api/questionOptions")) {
            int value = 1;
            String responseTxt = "";
            long tmp = 0;
            System.out.println(responseTxt);
            for (Question question: questionDao.listQuestionsBySurveyId(surveyId)) {
                responseTxt +=  "<option value=" + (value++) + ">" + question.getId() + ". " + utf8Value(question.getQuestionText()) + "</option>";
                tmp = question.getId();
                System.out.println(responseTxt);
            }
            this.questionId = tmp;
            writeOk200Response(clientSocket, responseTxt, "text/html");

            //TODO: Bruker velger gjeldende spørsmål og legger til alternativer (GET)
        } else if (fileTarget.equals("/api/newAlternative")) {
            String location = "/createSurvey.html";
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            AlternativeDao dao = new AlternativeDao(SurveyManager.createDataSource());
            this.alternative = new Alternative();
            String decodedValue = decodeValue(queryMap.get("alternative_text"));
            alternative.setAlternative(decodedValue);
            alternative.setQuestionId(questionId);
            dao.save(alternative);

            writeOk303Response(clientSocket, "Alternative added", "text/html", location);

            //TODO: Bruker velger spørsmål som de ønsker å liste ut alt ifra (POST)
        } else if (fileTarget.equals("/api/listAlternativesByQuestion")) {
            String location = "/createSurvey.html";
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            this.tmpQuestionId = Long.parseLong(queryMap.get("questionId"));
            writeOk303Response(clientSocket, "Question ID added", "text/html", location);

            //TODO: Webserver lister ut alle alternativene til ett spesifikt spørsmål (GET)
        } else if (fileTarget.equals("/api/listAlternatives")) {
            String responseTxt = "";
            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(tmpQuestionId)) {
                responseTxt += "<li>" + utf8Value(alternative.getAlternative()) + "</li>";
                System.out.println(responseTxt);
            }

            writeOk200Response(clientSocket, responseTxt, "text/html");

            //TODO: Viser alle survyene i en scroll bar
        } else if (fileTarget.equals("/api/surveyOptions")) {
            int value = 1;
            String responseText = "";

            for (Survey survey : surveyDao.listAll()) {
                responseText += "<option value=" + (value++) + ">" + "ID: " + survey.getId() + " " + "Name: " + survey.getSurveyName() + "</option>";
            }
            writeOk200Response(clientSocket, responseText, "text/html");

            //TODO: POST
        } else if (fileTarget.equals("/api/selectSurvey")) {
            String location = "/selectSurvey.html";
            SurveyDao dao = new SurveyDao(SurveyManager.createDataSource());
            Map<String, String> queryMap = parseRequestParameters(httpMessage.messageBody);
            String test = queryMap.get("surveyID");
            dao.delete(Integer.parseInt(queryMap.get("surveyID")));

            System.out.println(test);

            writeOk303Response(clientSocket, "Alternative added", "text/html", location);

        } else {
            InputStream fileResource = getClass().getResourceAsStream(fileTarget);

            if (fileResource != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                fileResource.transferTo(buffer);
                String responseText = buffer.toString();

                String contentType = "text/plain";
                if (requestTarget.endsWith(".html")) {
                    contentType = "text/html; charset=utf-8";
                }

                if (requestTarget.endsWith(".css")) {
                    contentType = "text/css; charset=utf-8";
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
}
