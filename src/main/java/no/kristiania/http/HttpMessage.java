package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {
    public static String statusCode;
    public final Map<String, String> headerFields = new HashMap<>();
    public String location;
    public String messageBody;

    public HttpMessage(Socket socket) throws IOException {
        statusCode = HttpMessage.readLine(socket);
        readHeaders(socket);
        if (headerFields.containsKey("Content-Length")) {
            messageBody = HttpMessage.readBytes(socket, getContentLength());
        }
    }

    //response 200
    public HttpMessage(String statusCode, String messageBody){
        this.statusCode = statusCode;
        this.messageBody = messageBody;
    }

    //response 303
    public HttpMessage(String statusCode, String location, String messageBody){
        this.statusCode = statusCode;
        this.messageBody = messageBody;
        this.location = location;
    }

    public static Map<String, String> parseRequestParameters(String query) {
        Map<String, String> queryMap = new HashMap<>();
        for (String queryParameter : query.split("&")) {
            int equalsPos = queryParameter.indexOf('=');
            String parameterName = queryParameter.substring(0, equalsPos);
            String parameterValue = queryParameter.substring(equalsPos+1);
            queryMap.put(parameterName, parameterValue);
        }
        return queryMap;
    }

    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);
    }

    static String readBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());
        }
        return buffer.toString();
    }

    private void readHeaders(Socket socket) throws IOException {
        String headerLine;
        while (!(headerLine = HttpMessage.readLine(socket)).isBlank()) {
            int colonPos = headerLine.indexOf(':');
            String headerField = headerLine.substring(0, colonPos);
            String headerValue = headerLine.substring(colonPos+1).trim();
            headerFields.put(headerField, headerValue);
        }
    }

    static String readLine(Socket socket) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != '\r') {
            buffer.append((char)c);
        }
        int expectedNewline = socket.getInputStream().read();
        assert expectedNewline == '\n';
        return buffer.toString();
    }

    public void write(Socket socket) throws IOException {
        if (location != null) {
            String response = "HTTP/1.1 " + statusCode + "\r\n" +
                    "Content-Length: " + messageBody.getBytes().length + "\r\n" +
                    "Location: http://localhost:" + HttpServer.getPort() + location + "\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    messageBody;
            socket.getOutputStream().write(response.getBytes());

        } else {
            String response = "HTTP/1.1 " + statusCode + "\r\n" +
                    "Content-Length: " + messageBody.getBytes().length + "\r\n" +
                    "Connection: close\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    messageBody;
            socket.getOutputStream().write(response.getBytes());
        }
    }
}
