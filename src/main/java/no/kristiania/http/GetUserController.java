package no.kristiania.http;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.HttpServer.mapUser;

public class GetUserController implements HttpController {
    public GetUserController() {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        String messageBody = "";
        messageBody += "<p>" + mapUser.get(UserFormController.getUserId()) + "</p>";

        return new HttpMessage("200 OK", messageBody);
    }
}
