import java.io.*;
import java.net.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DownloadUtils {
    private static final String API_KEY = loadApiKey();

    private static String loadApiKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader("youtube_api.txt"))) {
            return reader.readLine().trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from youtube_api.txt", e);
        }
    }

    public static String getSavePath(String videoUrl, String format) {
        String videoId = extractVideoIdFromUrl(videoUrl);
        String videoTitle = "";
        
        if (videoId != null) {
            videoTitle = getYouTubeVideoTitle(videoId);
        }

        if (videoTitle.isEmpty()) {
            videoTitle = "default_video_title";
        }

        return format.equals("mp3") ? "music/" + videoTitle + ".mp3" : "video/" + videoTitle + ".mp4";
    }

    private static String extractVideoIdFromUrl(String videoUrl) {
        String videoId = null;
        try {
            if (videoUrl.contains("v=")) {
                videoId = videoUrl.split("v=")[1];
                if (videoId.contains("&")) {
                    videoId = videoId.split("&")[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoId;
    }

    private static String getYouTubeVideoTitle(String videoId) {
        String videoTitle = "";
        try {
            String apiUrl = String.format(
                    "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=%s&key=%s",
                    videoId, API_KEY);
            System.out.println("API URL: " + apiUrl);
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // ??R JSON ?T??????o???D
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
                if (json.has("items") && json.getAsJsonArray("items").size() > 0) {
                    JsonObject snippet = json.getAsJsonArray("items")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("snippet");
                    videoTitle = snippet.get("title").getAsString();
                    videoTitle = sanitizeTitle(videoTitle); 
                }
            }
        } catch (IOException e) {
            videoTitle = sanitizeTitle("default_video_title"); 
            e.printStackTrace();
        }

        return videoTitle;
    }

    private static String sanitizeTitle(String title) {
        return title.replaceAll("[\\p{C}]", "").trim();
    }


    public static boolean downloadYouTubeVideo(String videoUrl,String savePath, String format) {
        try {

            System.out.println("Found Video Title: " + savePath);

            // Ensure the save directory exists
            File saveFile = new File(savePath);
            String parentPath = saveFile.getParent();
            if (parentPath != null) {
                File parentDirectory = new File(parentPath);
                if (!parentDirectory.exists()) {
                    parentDirectory.mkdirs();
                }
            }

            // Append the correct file extension directly to the provided save path
            String formattedSavePath = savePath.endsWith("." + format) ? savePath : savePath + (format.equals("mp3") ? ".mp3" : ".mp4");
            System.out.println("Final Save Path: " + formattedSavePath);

            // Command to download the video
            String command = "";
            if ("mp3".equals(format)) {
                command = "yt-dlp -x --audio-format mp3 --restrict-filenames -o \"" + formattedSavePath + "\" " + videoUrl;
            } else {
                command = "yt-dlp -f bestvideo+bestaudio --merge-output-format mp4 --restrict-filenames -o \"" + formattedSavePath + "\" " + videoUrl;
            }

            // Execute the command to download the video or audio
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read and log the output of the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // Output the progress of the download
                }
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Download process exited with code: " + exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
