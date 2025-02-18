import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class DownloadsListHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path videoDir = Paths.get("video");
        StringBuilder fileListHtml = new StringBuilder();
        String selectedVideo = null;

        // 取得選中的影片（如果有）
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("video=")) {
            selectedVideo = java.net.URLDecoder.decode(query.substring(6), StandardCharsets.UTF_8);
        }

        // 生成檔案清單
        if (Files.exists(videoDir)) {
            Files.list(videoDir).forEach(path -> {
                String fileName = path.getFileName().toString();
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
                fileListHtml.append(String.format(
                        "<li><a href='?video=%s'>%s</a></li>",
                        encodedFileName, fileName));
            });
        } else {
            fileListHtml.append("<p>尚無可用影片檔案。</p>");
        }

        // 生成影片播放器（如果選中了影片）
        String videoPlayerHtml = "";
        if (selectedVideo != null && !selectedVideo.isEmpty()) {
            videoPlayerHtml = String.format(
                    "<div class='video-player'>" +
                            "<h2>正在播放: %s</h2>" +
                            "<video controls width='600'>" +
                            "<source src='/videos/%s' type='%s'>" +
                            "您的瀏覽器不支援影片播放。" +
                            "</video>" +
                            "</div>",
                    selectedVideo, URLEncoder.encode(selectedVideo, StandardCharsets.UTF_8).replace("+", "%20"),
                    getMimeType(selectedVideo));
        }

        // 生成完整的 HTML 頁面
        String response = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>影片清單</title>
                    <style>
                        .container {
                            display: flex;
                            gap: 20px;
                        }
                        .file-list {
                            max-height: 400px;
                            overflow-y: scroll;
                            border: 1px solid #ccc;
                            padding: 10px;
                            width: 25%;
                            box-sizing: border-box;
                        }
                        .video-player {
                            flex-grow: 1;
                        }
                        .buttons-section {
                            position: fixed;
                            bottom: 20px;
                            left: 50%;
                            transform: translateX(-50%);
                            display: flex;
                            gap: 10px;
                        }
                        .buttons-section button {
                            padding: 10px 20px;
                            font-size: 16px;
                            cursor: pointer;
                        }
                        .buttons-section button:hover {
                            background-color: #f0f0f0;
                        }
                    </style>
                </head>
                <body>
                    <h1>影片清單</h1>
                    <div class="container">
                        <ul class="file-list">
                """ + fileListHtml + """
                        </ul>
                        """ + videoPlayerHtml + """
                    </div>
                    <div class="buttons-section">
                        <button onclick="window.location.href='/search'">搜尋影片</button>
                        <button onclick="window.location.href='/download'">下載</button>
                        <button onclick="window.location.href='/downloads-list'">影片清單</button>
                        <button onclick="window.location.href='/music-list'">音樂清單</button>
                    </div>
                </body>
                </html>
                """;

        // 發送 HTML 回應
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes("UTF-8"));
        }
    }

    // 根據檔案副檔名取得 MIME 類型
    private String getMimeType(String fileName) {
        if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".webm")) {
            return "video/webm";
        } else if (fileName.endsWith(".opus")) {
            return "audio/ogg";
        }
        return "application/octet-stream";
    }
}
