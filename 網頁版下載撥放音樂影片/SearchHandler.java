package 網頁版下載撥放音樂影片;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SearchHandler implements HttpHandler {
    private static final String API_KEY = "AIzaSyAZpgjPnarZfFYxAg5Fu5ePwEBLURIcAmM"; // �д������z�� API ���_

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
            String query = json.has("query") ? json.get("query").getAsString() : "�L�Ī��j�M�r��";

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
                    <title>�j�M�v��</title>
                    <script>
                        async function searchVideos() {
                            const query = document.getElementById('queryInput').value;
                            if (!query) {
                                alert('�п�J����r!');
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
                                            <button onclick="downloadVideo('${item.id.videoId}')">�U��</button>
                                        </div>
                                    `;
                                    resultsContainer.appendChild(video);
                                });
                            } else {
                                resultsContainer.innerHTML = '<p>�L�j�M���G�C</p>';
                            }
                        }
    
                        function downloadVideo(videoId) {
                            // �U�����s�I����A�ǻ��v�� ID ��U�������A�åB�� query string �ǻ��v�� URL
                            window.location.href = `/download?videoId=${videoId}`;
                        }
                    </script>
                </head>
                <body>
                    <h1>�j�M�v��</h1>
                    <div>
                        <input type="text" id="queryInput" placeholder="��J�v������r">
                        <button onclick="searchVideos()">�j�M</button>
                    </div>
                    <div id="results"></div>
    
                    <div class="buttons-section">
                        <button onclick="window.location.href='/search'">�j�M�v��</button>
                        <button onclick="window.location.href='/download'">�U��</button>
                        <button onclick="window.location.href='/downloads-list'">�v���M��</button>
                        <button onclick="window.location.href='/music-list'">���ֲM��</button>
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
