package no.kristiania.http;

import no.kristiania.jdbc.User;
import no.kristiania.jdbc.UserDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static no.kristiania.http.HttpServer.mapUser;
import static no.kristiania.http.UrlEncoding.decodeValue;



public class UserFormController implements HttpController {
    
    private final UserDao userDao;

    private static Integer userId;

    public UserFormController(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public HttpMessage handle(HttpMessage request) throws SQLException, IOException {
        Map<String, String> queryMap = HttpMessage.parseRequestParameters(request.messageBody);
        String firstName = decodeValue(queryMap.get("firstName"));
        String lastName = decodeValue(queryMap.get("lastName"));
        String email = decodeValue(queryMap.get("email"));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userDao.save(user);
        mapUser.put(Math.toIntExact(user.getId()), user.getFirstName());
        userId = Math.toIntExact(user.getId());

        return new HttpMessage("303 See Other","/answerSurvey.html", "Personal information submitted!");
    }

    public static Integer getUserId() {
        return userId;
    }
}
