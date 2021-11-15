package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;

import java.io.IOException;
import java.sql.SQLException;

import java.util.Map;

public class DeleteAlternativeController implements HttpController {
    private final AlternativeDao alternativeDao;

    public DeleteAlternativeController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String inputId = queryMap.get("alternativeInput");
        alternativeDao.delete(Integer.parseInt(inputId));
        return new HttpMessage("303 See Other", "/editSurvey.html", "Alternative deleted!");
    }
}
