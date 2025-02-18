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

        // 使用 parseParameter 解析 filePath 參數
        String filePath = parseParameter(query, "filePath");

        if ("GET".equalsIgnoreCase(method) && filePath != null) {
            handlePlayback(exchange, filePath);
        } else {
            sendErrorResponse(exchange, "無效的請求或缺少參數！");
        }
    }

    private void handlePlayback(HttpExchange exchange, String filePath) throws IOException {
    filePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8.name());
    File file = new File(filePath);

    if (!file.exists() || !file.isFile()) {
        sendErrorResponse(exchange, "檔案不存在！");
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
                System.out.println("客戶端中止連接：" + e.getMessage());
                break;
            }
        }
        os.flush();
        System.out.println("音樂傳輸完成");
    } catch (IOException e) {
        System.out.println("音樂傳輸失敗：" + e.getMessage());
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
