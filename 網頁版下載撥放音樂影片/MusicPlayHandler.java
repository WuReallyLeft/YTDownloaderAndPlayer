package 網頁版下載撥放音樂影片;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class MusicPlayHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        // �ϥ� parseParameter �ѪR filePath �Ѽ�
        String filePath = parseParameter(query, "filePath");

        if ("GET".equalsIgnoreCase(method) && filePath != null) {
            handlePlayback(exchange, filePath);
        } else {
            sendErrorResponse(exchange, "�L�Ī��ШD�ίʤְѼơI");
        }
    }

    private void handlePlayback(HttpExchange exchange, String filePath) throws IOException {
    filePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8.name());
    File file = new File(filePath);

    if (!file.exists() || !file.isFile()) {
        sendErrorResponse(exchange, "�ɮפ��s�b�I");
        return;
    }

    exchange.getResponseHeaders().set("Content-Type", "audio/mpeg");
    exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
    exchange.sendResponseHeaders(200, file.length());

    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
         OutputStream os = exchange.getResponseBody()) {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = bis.read(buffer)) != -1) {
            try {
                os.write(buffer, 0, bytesRead);
            } catch (IOException e) {
                System.out.println("�Ȥ�ݤ���s���G" + e.getMessage());
                break;
            }
        }
        os.flush();
        System.out.println("���ֶǿ駹��");
    } catch (IOException e) {
        System.out.println("���ֶǿ饢�ѡG" + e.getMessage());
    }
}

    
    private void sendErrorResponse(HttpExchange exchange, String errorMessage) throws IOException {
        exchange.sendResponseHeaders(400, errorMessage.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorMessage.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String parseParameter(String query, String parameterName) {
        if (query != null && query.contains(parameterName + "=")) {
            String[] parts = query.split("&");
            for (String part : parts) {
                if (part.startsWith(parameterName + "=")) {
                    return part.substring((parameterName + "=").length());
                }
            }
        }
        return null;
    }
}
