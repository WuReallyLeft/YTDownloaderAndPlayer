import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchHandler implements HttpHandler {
    private static final String API_KEY = loadApiKey();

    private static String loadApiKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader("youtube_api.txt"))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from youtube_api.txt", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(requestBody.toString(), JsonObject.class);
            String query = json.has("query") ? json.get("query").getAsString() : "無效的搜尋字詞";

            String apiUrl = String.format(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q=%s&key=%s&maxResults=5",
                    URLEncoder.encode(query, "UTF-8"), API_KEY);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder apiResponse = new StringBuilder();
            while ((line = apiReader.readLine()) != null) {
                apiResponse.append(line);
            }

            sendJsonResponse(exchange, apiResponse.toString());
            return;
        } else {
            response = generateSearchPage();
            sendHtmlResponse(exchange, response);
        }
    }

    private String generateSearchPage() {
        return """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>搜尋影片</title>
                    <script>
                        async function searchVideos() {
                            const query = document.getElementById('queryInput').value;
                            if (!query) {
                                alert('請輸入關鍵字!');
                                return;
                            }

                            const response = await fetch('/search', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ query })
                            });

                            const data = await response.json();
                            const resultsContainer = document.getElementById('results');
                            resultsContainer.innerHTML = '';

                            if (data.items && data.items.length > 0) {
                                data.items.forEach(item => {
                                    const video = document.createElement('div');
                                    video.innerHTML = `
                                        <div>
                                            <h3>${item.snippet.title}</h3>
                                            <a href="https://www.youtube.com/watch?v=${item.id.videoId}" target="_blank">
                                                <img src="${item.snippet.thumbnails.default.url}" alt="${item.snippet.title}">
                                            </a>
                                            <button onclick="downloadVideo('${item.id.videoId}')">下載</button>
                                        </div>
                                    `;
                                    resultsContainer.appendChild(video);
                                });
                            } else {
                                resultsContainer.innerHTML = '<p>無搜尋結果。</p>';
                            }
                        }

                        function downloadVideo(videoId) {
                            // 下載按鈕點擊後，傳遞影片 ID 到下載頁面，並且用 query string 傳遞影片 URL
                            window.location.href = `/download?videoId=${videoId}`;
                        }
                    </script>
                </head>
                <body>
                    <h1>搜尋影片</h1>
                    <div>
                        <input type="text" id="queryInput" placeholder="輸入影片關鍵字">
                        <button onclick="searchVideos()">搜尋</button>
                    </div>
                    <div id="results"></div>

                    <div class="buttons-section">
                        <button onclick="window.location.href='/search'">搜尋影片</button>
                        <button onclick="window.location.href='/download'">下載</button>
                        <button onclick="window.location.href='/downloads-list'">影片清單</button>
                        <button onclick="window.location.href='/music-list'">音樂清單</button>
                    </div>

                    <style>
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
                </body>
                </html>
                """;
    }

    private void sendJsonResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes("UTF-8"));
        }
    }

    private void sendHtmlResponse(HttpExchange exchange, String htmlResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlResponse.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes("UTF-8"));
        }
    }
}
