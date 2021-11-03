package no.kristiania.survey;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpMessage {
    public static String startLine;
    public static final Map<String, String> headerFields = new HashMap<>();
    public static String messageBody;

    public HttpMessage(Socket socket) throws IOException {
        startLine = readLine(socket);
        readHeaders(socket);
        //Lese http response (message body)
        if (headerFields.containsKey("Content-Length")) {
            messageBody = readBytes(socket, getContentLength());
        }
    }

    public static String readLine(Socket socket) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != '\r') {
            buffer.append((char) c);
        }

        //Leser en karakter til pga CRLF etter headerfields
        socket.getInputStream().read();

        return buffer.toString();
    }

    public static void readHeaders(Socket socket) throws IOException {
        //Lese http response (header line/ name + value)
        String headerLine;
        while (!(headerLine = readLine(socket)).isBlank()) {
            int colonPos = headerLine.indexOf(":");
            String headerName = headerLine.substring(0, colonPos);
            String headerValue = headerLine.substring(colonPos + 1).trim();
            headerFields.put(headerName, headerValue);
        }
    }

    static String readBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());
        }
        return buffer.toString();
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);
    }

    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }
}
