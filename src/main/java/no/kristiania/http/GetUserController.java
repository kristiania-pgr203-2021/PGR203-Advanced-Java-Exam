package no.kristiania.http;

import no.kristiania.jdbc.UserDao;

import java.io.IOException;
import java.sql.SQLException;

import static no.kristiania.http.HttpServer.mapUser;

public class GetUserController implements HttpController {
    public GetUserController(UserDao userDao) {
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        System.out.println("Brukernavnet som skal skrives ut: " + mapUser.get(UserFormController.getUserId()));
        String messageBody = "";
        messageBody += "<p>" + mapUser.get(UserFormController.getUserId()) + "</p>";

        return new HttpMessage("200 OK", messageBody);
    }
}
