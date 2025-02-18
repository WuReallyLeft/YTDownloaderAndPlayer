import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.swing.text.html.HTML;

import java.io.UnsupportedEncodingException;

public class DownloadHandler implements HttpHandler {
    String videoUrl = "";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        
        String format = "mp4"; // Default format

        String query = exchange.getRequestURI().getQuery();
        String videoId = null;
        // 解析 URL 查詢參數，並處理解碼
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("videoId=")) {
                    videoId = param.split("=")[1];
                    try {
                        // 解碼影片 ID 以確保影片網址正確
                        videoUrl = URLDecoder.decode("https://www.youtube.com/watch?v=" + videoId, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (param.startsWith("format=")) {
                    format = param.split("=")[1];
                }
            }
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // 處理 POST 請求，從 JSON 中獲取網址和格式
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(requestBody.toString(), JsonObject.class);
            String url = json.has("url") ? json.get("url").getAsString() : "";
            format = json.has("format") ? json.get("format").getAsString() : "mp4";

            if (!url.isEmpty()) {
                // 這裡保持你的邏輯，使用影片標題作為檔案名稱
                String savePath = DownloadUtils.getSavePath(url, format);
                boolean success = DownloadUtils.downloadYouTubeVideo(url, savePath, format);

                if (success) {
                    response = "下載成功!";
                } else {
                    response = "下載失敗!";
                }
            } else {
                response = "無效的影片網址!";
            }

            sendHtmlResponse(exchange, response);
        } else {
            // 顯示下載頁面，並將影片網址填入輸入框
            response = generateDownloadPage(videoUrl);
            sendHtmlResponse(exchange, response);
        }
    }

    private String generateDownloadPage(String videoUrl) {
        String decodedVideoUrl = "";
        try {
            decodedVideoUrl = videoUrl.isEmpty() ? "" : URLDecoder.decode(videoUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }          
        String htmlResponse = "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>下載影片</title>" +
                "<script>" +
                "async function download() {" +
                "    const videoUrl = document.getElementById('videoUrlInput').value;" +
                "    const format = document.getElementById('formatSelect').value;" +
                "    const response = await fetch('/download', {" +
                "        method: 'POST'," +
                "        headers: { 'Content-Type': 'application/json' }," +
                "        body: JSON.stringify({ url: videoUrl, format: format })" +
                "    });" +
                "    if (response.ok) {" +
                "        alert('下載成功!');" +
                "    } else {" +
                "        alert('下載失敗!');" +
                "    }" +
                "}" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<h1>下載影片</h1>" +
                "<div>" +
                "    <input type=\"text\" id=\"videoUrlInput\" value=\"" + decodedVideoUrl + "\" placeholder=\"輸入影片網址\">" +
                "    <select id=\"formatSelect\">" +
                "        <option value=\"mp4\">影片 (MP4)</option>" +
                "        <option value=\"mp3\">音樂 (MP3)</option>" +
                "    </select>" +
                "    <button onclick=\"download()\">下載</button>" +
                "</div>" +
                "<!-- 底部導航按鈕 -->" +
                "<div class=\"buttons-section\">" +
                "    <button onclick=\"window.location.href='/search'\">搜尋影片</button>" +
                "    <button onclick=\"window.location.href='/download'\">下載</button>" +
                "    <button onclick=\"window.location.href='/downloads-list'\">影片清單</button>" +
                "    <button onclick=\"window.location.href='/music-list'\">音樂清單</button>" +
                "</div>" +
                "<style>" +
                ".buttons-section {" +
                "    position: fixed;" +
                "    bottom: 20px;" +
                "    left: 50%;" +
                "    transform: translateX(-50%);" +
                "    display: flex;" +
                "    gap: 10px;" +
                "}" +
                ".buttons-section button {" +
                "    padding: 10px 20px;" +
                "    font-size: 16px;" +
                "    cursor: pointer;" +
                "}" +
                ".buttons-section button:hover {" +
                "    background-color: #f0f0f0;" +
                "}" +
                "</style>" +
                "</body>" +
                "</html>";
        return htmlResponse;
    }

    private void sendHtmlResponse(HttpExchange exchange, String htmlResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlResponse.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes("UTF-8"));
        }
    }

    private String encodeURIComponent(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value;
        }
    }
}