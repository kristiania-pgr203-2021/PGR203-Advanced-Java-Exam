package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;

public class HttpPostClient {

    private final HttpMessage httpMessage;
    private final int statusCode;

    public HttpPostClient(String host, int port, String requestTarget, String contentBody) throws IOException {
        Socket socket = new Socket(host, port);

        String requestMessage = "POST " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + contentBody.getBytes().length + "\r\n" +
                "\r\n" +
                contentBody;

        socket.getOutputStream().write(requestMessage.getBytes());

        httpMessage = new HttpMessage(socket);
        String[] statusLine = HttpMessage.statusCode.split(" ");
        this.statusCode = Integer.parseInt(statusLine[1]);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
