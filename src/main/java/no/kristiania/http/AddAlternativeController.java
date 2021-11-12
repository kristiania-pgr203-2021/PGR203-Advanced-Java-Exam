package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;

import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class AddAlternativeController implements HttpController {
    private final AlternativeDao alternativeDao;
    private Alternative alternative;
    private long id = -1;

    public AddAlternativeController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        System.out.println("hello");

        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        this.alternative = new Alternative();
        long id = Long.parseLong(((queryMap.get("questionInput"))));
        String decodedValue = decodeValue(queryMap.get("alternativeInput"));
        alternative.setAlternative(decodedValue);
        alternative.setQuestionId(GetQuestionIdController.getQuestionId());
        alternativeDao.save(alternative);

        return new HttpMessage("303 See Other", "/createSurvey.html","it's done");
    }
}
