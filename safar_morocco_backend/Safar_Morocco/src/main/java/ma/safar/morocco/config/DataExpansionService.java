package ma.safar.morocco.config;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.media.entity.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class DataExpansionService {
    private static final Logger log = LoggerFactory.getLogger(DataExpansionService.class);
    private static final String UPLOAD_DIR = "uploads";

    private final DestinationRepository destinationRepository;
    private final EvenementCulturelRepository eventRepository;

    @Transactional
    public void repairData() {
        log.info("🔧 Starting Data Repair and Image Synchronization...");
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            syncImagesAndCleanup();
        } catch (Exception e) {
            log.error("❌ Error during data repair: {}", e.getMessage());
        }
    }

    private void syncImagesAndCleanup() {
        java.util.Set<String> referencedFiles = new java.util.HashSet<>();

        // 1. Sync Event Images
        eventRepository.findAll().forEach(e -> {
            String url = e.getImageUrl();
            if (url == null)
                return;

            if (url.startsWith("http")) {
                // External URL - Download it
                String extension = url.contains(".png") ? ".png" : (url.contains(".webp") ? ".webp" : ".jpg");
                String filename = "event_" + e.getNom().toLowerCase().replaceAll("[^a-z0-9]", "_") + extension;
                String downloaded = downloadImage(url, filename, true);
                if (downloaded != null) {
                    e.setImageUrl("/uploads/" + downloaded);
                    eventRepository.save(e);
                    referencedFiles.add(downloaded);
                }
            } else if (url.startsWith("/uploads/")) {
                String filename = url.substring(9);
                referencedFiles.add(filename);
            }
        });

        // 2. Sync Destination Media Images
        destinationRepository.findAllWithMedias().forEach(d -> {
            boolean changed = false;
            for (Media m : d.getMedias()) {
                String url = m.getUrl();
                if (url == null)
                    continue;

                if (url.startsWith("http")) {
                    String extension = url.contains(".png") ? ".png" : (url.contains(".webp") ? ".webp" : ".jpg");
                    String filename = "dest_" + d.getNom().toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + m.getId()
                            + extension;
                    String downloaded = downloadImage(url, filename, true);
                    if (downloaded != null) {
                        m.setUrl("/uploads/" + downloaded);
                        referencedFiles.add(downloaded);
                        changed = true;
                    }
                } else if (url.startsWith("/uploads/")) {
                    String filename = url.substring(9);
                    referencedFiles.add(filename);
                }
            }
            if (changed) {
                destinationRepository.save(d);
            }
        });

        // 3. Cleanup unused files in uploads/
        cleanupUnusedFiles(referencedFiles);
    }

    private void cleanupUnusedFiles(java.util.Set<String> referencedFiles) {
        log.info("🧹 Starting cleanup of unused images...");
        try {
            Files.list(Paths.get(UPLOAD_DIR)).forEach(path -> {
                if (Files.isDirectory(path))
                    return; // Keep subdirectories like 'invoices' or 'media'

                String filename = path.getFileName().toString();
                // Special case for marker or essential files
                if (filename.equals("marker_workspace.txt"))
                    return;

                if (!referencedFiles.contains(filename)) {
                    try {
                        Files.delete(path);
                        log.info("🗑️ Deleted unused image: {}", filename);
                    } catch (java.io.IOException e) {
                        log.error("❌ Failed to delete {}: {}", filename, e.getMessage());
                    }
                }
            });
        } catch (java.io.IOException e) {
            log.error("❌ Error listing uploads directory: {}", e.getMessage());
        }
    }

    public String downloadImage(String urlStr, String targetFilename, boolean force) {
        try {
            Path targetPath = Paths.get(UPLOAD_DIR, targetFilename);
            if (!force && Files.exists(targetPath)) {
                return targetFilename;
            }

            log.info("📥 Downloading image from: {}", urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (java.io.InputStream in = connection.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("✅ Successfully saved: {}", targetFilename);
                    return targetFilename;
                }
            } else {
                log.warn("⚠️ Failed to download image. HTTP Response: {}", connection.getResponseCode());
            }
        } catch (Exception e) {
            log.error("❌ Download failed for {}: {}", targetFilename, e.getMessage());
        }
        return null;
    }

    // Unused but kept for compatibility if needed elsewhere
    public String downloadImage(String urlStr, String targetFilename) {
        return downloadImage(urlStr, targetFilename, false);
    }
}
