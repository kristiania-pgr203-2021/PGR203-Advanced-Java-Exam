package no.kristiania.http;

import java.util.Map;

public class GetQuestionIdController implements HttpController {

    private static long questionId = -1;

    @Override
    public HttpMessage handle(HttpMessage request) {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        long questionInput = Long.parseLong((queryMap.get("questionInput")));

        if (questionInput != 0) {
            this.questionId = questionInput;
        }

        System.out.println("Question ID i GetQuestionIdController: "+questionId);
        return new HttpMessage("303 See Other", "/createSurvey.html", "Its done");
    }

    public static long getQuestionId() {
        return questionId;
    }
}