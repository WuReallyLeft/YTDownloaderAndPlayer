package 網頁版下載撥放音樂影片;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleHttpServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);

        server.createContext("/search", new SearchHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/downloads-list", new DownloadsListHandler());
        server.createContext("/music-list", new MusicListHandler());
        server.createContext("/play-music", new MusicPlayHandler());
        
        server.createContext("/videos", exchange -> {
            String filePath = "video" + exchange.getRequestURI().getPath().replace("/videos/", "/");
            Path file = Paths.get(filePath);

            if (Files.exists(file)) {
                String mimeType = Files.probeContentType(file);
                if (mimeType == null) {
                    
                    if (file.toString().endsWith(".mp4")) {
                        mimeType = "video/mp4";
                    } else if (file.toString().endsWith(".mp3")) {
                        mimeType = "audio/mpeg";
                    } else {
                        mimeType = "application/octet-stream"; 
                    }
                }

                exchange.getResponseHeaders().set("Content-Type", mimeType);
                exchange.sendResponseHeaders(200, Files.size(file));

                
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(file, os);
                }
            } else {
                exchange.sendResponseHeaders(404, -1); 
            }
        });

        server.setExecutor(null); // Creates a default executor
        System.out.println("���A���w�ҰʡA�гX�� http://localhost:80/search");
        server.start();
    }

   
}
