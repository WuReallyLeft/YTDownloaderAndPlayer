package 網頁版下載撥放音樂影片;
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

        // �T�{��Ƨ��s�b
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return "<html><body><h1>��Ƨ����s�b: " + folderName + "</h1></body></html>";
        }

        // ����ɮײM��A�ͦ�����/�Ȱ����s�ѫȤ�ݨϥ�
        String fileList = Files.list(folderPath)
                .filter(Files::isRegularFile)
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    String filePath = folderPath.resolve(fileName).toString().replace("\\", "/");
                    String fullUrl = "/play-music?filePath=" + filePath;
                    
                    System.out.println("�ͦ��� URL�G" + fullUrl); // ���L���� URL ����A������x
                    return "<li>" +
                           "<button onclick=\"togglePlayPause(this, '" + filePath + "')\">����</button>" +
                           " " + fileName +
                           "</li>";
                })
                .collect(Collectors.joining("\n"));

        return 
        "<html>"+
        "<head>"+
            "<meta charset='UTF-8'>"+
            "<title>���ֲM��</title>"+
            "<script>"+
                "let currentAudio = null;"+
                "let currentButton = null;"+

             "function togglePlayPause(button, filePath) {" +
    "if (currentAudio && currentButton === button) {" +
        "if (!currentAudio.paused) {" +
            "currentAudio.pause();" +
            "button.textContent = '����';" +
        "} else {" +
            "currentAudio.play().catch(error => {" +
                "console.error('����ɵo�Ϳ��~:', error);" +
            "});" +
            "button.textContent = '�Ȱ�';" +
        "}" +
    "} else {" +
        "if (currentAudio) {" +
            "currentAudio.pause();" +
            "currentButton.textContent = '����';" +
        "}" +
        "currentAudio = new Audio('/play-music?filePath=' + encodeURIComponent(filePath));" +
        "currentAudio.play().catch(error => {" +
            "console.error('����ɵo�Ϳ��~:', error);" +
        "});" +
        "button.textContent = '�Ȱ�';" +
        "currentButton = button;" +
    "}" +
"}"+



            "</script>"+
        "</head>"+
        "<body>"+
            "<h1>���ֲM��</h1>"+
            "<ul>" + fileList + "</ul>"+
        "</body>"+
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
    }

    private void sendHtmlResponse(HttpExchange exchange, String htmlResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlResponse.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes("UTF-8"));
        }
    }
}