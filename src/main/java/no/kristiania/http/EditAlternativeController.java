package no.kristiania.http;

import no.kristiania.jdbc.AlternativeDao;

import java.io.IOException;
import java.sql.SQLException;

import java.util.Map;

import static no.kristiania.http.UrlEncoding.decodeValue;

public class EditAlternativeController implements HttpController {

    private final AlternativeDao alternativeDao;

    public EditAlternativeController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        AlternativeDao dao = new AlternativeDao(SurveyManager.createDataSource());
        Long id = Long.valueOf(queryMap.get("alternativeIdInput"));
        String alternative = queryMap.get("alternativeNameInput");
        dao.update(id, decodeValue(alternative));
        return new HttpMessage("303 See Other", "/editSurvey.html", "Edited alternative!");
    }
}
