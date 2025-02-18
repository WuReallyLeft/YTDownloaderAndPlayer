package 網頁版下載撥放音樂影片;
import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.BufferedInputStream;
import java.io.InputStream; // �p�G�ݭn InputStream �]�@�֤ޤJ

public class MP3_Stream {
    private BufferedInputStream bss;
    private AdvancedPlayer player;
    private boolean isPlaying;

    public MP3_Stream(BufferedInputStream bss) {
        this.bss = bss;
        this.isPlaying = false;
    }

    public void playSong() {
        if (isPlaying) return;

        isPlaying = true;
        try {
            player = new AdvancedPlayer(bss);
            player.play(); // ���� MP3 �y
        } catch (Exception e) {
            System.out.println("����ɵo�Ϳ��~: " + e.getMessage());
        } finally {
            closePlayer();
        }
    }

    public void stop() {
        isPlaying = false;
        if (player != null) {
            player.close();
        }
    }

    private void closePlayer() {
        if (player != null) {
            player.close();
            player = null;
        }
    }
}
