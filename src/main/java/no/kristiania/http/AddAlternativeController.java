package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;

import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class AddAlternativeController implements HttpController {
    private final AlternativeDao alternativeDao;
    private Alternative alternative;

    public AddAlternativeController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        this.alternative = new Alternative();

        Long id = Long.valueOf(queryMap.get("questionId"));
        String alternative = decodeValue(queryMap.get("alternativeInput"));

        this.alternative.setAlternative(alternative);
        this.alternative.setQuestionId(id);
        alternativeDao.save(this.alternative);

        return new HttpMessage("303 See Other", "/createSurvey.html","Alternative added!");
    }
}
