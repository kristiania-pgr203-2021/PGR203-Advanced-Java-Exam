package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.UrlEncoding.utf8Value;

public class ListAlternativesInEditController implements HttpController {

    private final AlternativeDao alternativeDao;

    public ListAlternativesInEditController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String  responseTxt = "";

            for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(GetQuestionIdInEditController.getQuestionId())) {
                responseTxt += "<p>" + "ID: " + alternative.getId() + " " + "Alternative text: " + utf8Value(alternative.getAlternative()) + "</p>";
            }

        return new HttpMessage("200 OK", responseTxt);
    }
}
