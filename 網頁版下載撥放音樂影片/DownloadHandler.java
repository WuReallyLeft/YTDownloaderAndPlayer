package 網頁版下載撥放音樂影片;
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
        // �ѪR URL �d�߰ѼơA�óB�z�ѽX
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("videoId=")) {
                    videoId = param.split("=")[1];
                    try {
                        // �ѽX�v�� ID �H�T�O�v�����}���T
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
            // �B�z POST �ШD�A�q JSON ��������}�M�榡
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
                // �o�̫O���A���޿�A�ϥμv�����D�@���ɮצW��
                String savePath = DownloadUtils.getSavePath(url, format);
                boolean success = DownloadUtils.downloadYouTubeVideo(url, savePath, format);

                if (success) {
                    response = "�U�����\!";
                } else {
                    response = "�U������!";
                }
            } else {
                response = "�L�Ī��v�����}!";
            }

            sendHtmlResponse(exchange, response);
        } else {
            // ��ܤU�������A�ñN�v�����}��J��J��
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
                "<title>�U���v��</title>" +
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
                "        alert('�U�����\!');" +
                "    } else {" +
                "        alert('�U������!');" +
                "    }" +
                "}" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<h1>�U���v��</h1>" +
                "<div>" +
                "    <input type=\"text\" id=\"videoUrlInput\" value=\"" + decodedVideoUrl + "\" placeholder=\"��J�v�����}\">" +
                "    <select id=\"formatSelect\">" +
                "        <option value=\"mp4\">�v�� (MP4)</option>" +
                "        <option value=\"mp3\">���� (MP3)</option>" +
                "    </select>" +
                "    <button onclick=\"download()\">�U��</button>" +
                "</div>" +
                "<!-- �����ɯ���s -->" +
                "<div class=\"buttons-section\">" +
                "    <button onclick=\"window.location.href='/search'\">�j�M�v��</button>" +
                "    <button onclick=\"window.location.href='/download'\">�U��</button>" +
                "    <button onclick=\"window.location.href='/downloads-list'\">�v���M��</button>" +
                "    <button onclick=\"window.location.href='/music-list'\">���ֲM��</button>" +
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