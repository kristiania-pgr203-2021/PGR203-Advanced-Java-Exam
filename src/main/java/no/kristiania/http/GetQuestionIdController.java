package no.kristiania.http;

import java.util.Map;

public class GetQuestionIdController implements HttpController {

    private static Long questionId = Long.valueOf(1);

    @Override
    public HttpMessage handle(HttpMessage request) {


        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        Long questionId = Long.valueOf(queryMap.get("questionInput"));
        if (questionId != null){
            this.questionId = questionId;
        }


        System.out.println(questionId);
        return new HttpMessage("303 See Other", "/createSurvey.html" , "Its done");
    }

    public static Long getQuestionId() {
        return questionId;
    }
}
/*
    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String responseTxt = "";
        for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(questionId)) {
            responseTxt += "<li>" + utf8Value(alternative.getAlternative()) + "</li>";
        }
        return new HttpMessage("200 OK", responseTxt);
    }
}

    //TODO: Bruker velger spørsmål som de ønsker å liste ut alt ifra (POST)


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

        }
    */