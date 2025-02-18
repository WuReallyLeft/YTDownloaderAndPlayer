import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class MusicListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String htmlResponse = generatePage("music");
        sendHtmlResponse(exchange, htmlResponse);
    }

    private String generatePage(String folderName) throws IOException {
        Path folderPath = Paths.get(folderName);

        // 確認資料夾存在
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return "<html><body><h1>資料夾不存在: " + folderName + "</h1></body></html>";
        }

        // 獲取檔案清單，生成播放/暫停按鈕供客戶端使用
        String fileList = Files.list(folderPath)
                .filter(Files::isRegularFile)
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    String filePath = folderPath.resolve(fileName).toString().replace("\\", "/");
                    String fullUrl = "/play-music?filePath=" + filePath;
                    
                    System.out.println("生成的 URL：" + fullUrl); // 打印完整 URL 到伺服器控制台
                    return "<li>" +
                           "<button onclick=\"togglePlayPause(this, '" + filePath + "')\">播放</button>" +
                           " " + fileName +
                           "</li>";
                })
                .collect(Collectors.joining("\n"));

        return 
        "<html>"+
        "<head>"+
            "<meta charset='UTF-8'>"+
            "<title>音樂清單</title>"+
            "<script>"+
                "let currentAudio = null;"+
                "let currentButton = null;"+

             "function togglePlayPause(button, filePath) {" +
    "if (currentAudio && currentButton === button) {" +
        "if (!currentAudio.paused) {" +
            "currentAudio.pause();" +
            "button.textContent = '播放';" +
        "} else {" +
            "currentAudio.play().catch(error => {" +
                "console.error('播放時發生錯誤:', error);" +
            "});" +
            "button.textContent = '暫停';" +
        "}" +
    "} else {" +
        "if (currentAudio) {" +
            "currentAudio.pause();" +
            "currentButton.textContent = '播放';" +
        "}" +
        "currentAudio = new Audio('/play-music?filePath=' + encodeURIComponent(filePath));" +
        "currentAudio.play().catch(error => {" +
            "console.error('播放時發生錯誤:', error);" +
        "});" +
        "button.textContent = '暫停';" +
        "currentButton = button;" +
    "}" +
"}"+



            "</script>"+
        "</head>"+
        "<body>"+
            "<h1>音樂清單</h1>"+
            "<ul>" + fileList + "</ul>"+
        "</body>"+
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
    }

    private void sendHtmlResponse(HttpExchange exchange, String htmlResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlResponse.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes("UTF-8"));
        }
    }
}