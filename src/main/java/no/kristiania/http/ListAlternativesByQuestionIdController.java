package no.kristiania.http;

import no.kristiania.jdbc.Alternative;
import no.kristiania.jdbc.AlternativeDao;
import java.sql.SQLException;

import static no.kristiania.http.UrlEncoding.utf8Value;

public class ListAlternativesByQuestionIdController implements HttpController {
    private final AlternativeDao alternativeDao;


    public ListAlternativesByQuestionIdController(AlternativeDao alternativeDao) {
        this.alternativeDao = alternativeDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException {
        String responseTxt = "";


        for (Alternative alternative : alternativeDao.listAlternativesByQuestionId(GetQuestionIdController.getQuestionId())) {
            responseTxt += "<li>" + utf8Value(alternative.getAlternative()) + "</li>";

        }
        return new HttpMessage("200 OK", responseTxt);
    }
}
