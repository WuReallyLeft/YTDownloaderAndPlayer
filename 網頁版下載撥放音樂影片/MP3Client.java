package 網頁版下載撥放音樂影片;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MP3Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("��J���O�]�Ҧp�Gplay music/MAYDAY����� [ ����ʪ�Party Animal ] Official Music Video.mp3�^�G");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.startsWith("play ")) {
                String filePath = command.substring(5).trim();
                playMusic(filePath);
            } else if (command.equalsIgnoreCase("exit")) {
                System.out.println("�h�X�{���C");
                break;
            } else {
                System.out.println("�������O�A�Шϥ� 'play <filePath>' �� 'exit'�C");
            }
        }

        scanner.close();
    }

    private static void playMusic(String filePath) {
        try {
            // �N filePath �s�X�� URL
            String encodedFilePath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.name());
            String urlString = "http://localhost:80/play-music?filePath=" + encodedFilePath;

            // �إ� HTTP �s��
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // �T�{�s���O�_���\
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("�L�k���񭵼֡A���A����^���~�N�X�G" + responseCode);
                return;
            }

            // ������W��y
            try (InputStream inputStream = connection.getInputStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                // �ϥ� MP3_Stream ����
                MP3_Stream player = new MP3_Stream(bufferedInputStream);
                System.out.println("�}�l����...");
                player.playSong();
            }

        } catch (IOException e) {
            System.out.println("���񭵼֮ɵo�Ϳ��~�G" + e.getMessage());
        }
    }
}
