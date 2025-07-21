package de.thro.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Überwacht ein Verzeichnis auf neue oder geänderte PDF-Dateien und verarbeitet diese.
 * Konvertiert erkannte PDF-Dateien in JSON und fügt sie einer Warteschlange hinzu.
 * Nutzt einen FileAlterationMonitor für die Verzeichnisüberwachung.
 */
public class FileImport implements Runnable{

    private final EnvConfig env;
    private final BlockingQueue<String> queue;
    private static final Integer SIZETOMB = 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(FileImport.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    volatile boolean running = false;
    private Thread thread;
    private FileAlterationMonitor monitor;
    private final Map<Path, Long> lastModifiedMap = new HashMap<>();
    private ExecutorService executorService;

    /**
     * Erstellt einen neuen FileImport mit gegebener Konfiguration und Warteschlange.
     *
     * @param env Umgebungs-Konfiguration
     * @param queue Warteschlange für JSON-Dokumente
     */
    public FileImport(EnvConfig env, BlockingQueue<String> queue){
        this.env = env;
        this.queue = queue;
    }

    /**
     * Startet den Importprozess in einem eigenen Thread.
     */
    public void start(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Stoppt den Importprozess und den Verzeichnis-Monitor.
     */
    public void stop() {
        running = false;
        stopMonitor();
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Stoppt den FileAlterationMonitor asynchron.
     */
    private void stopMonitor(){
        if (monitor != null) {
            try {
                // Stop monitor asynchronously to avoid blocking
                executorService.submit(() -> {
                    try {
                        monitor.stop();
                    } catch (Exception e) {
                        logger.error("Error stopping FileAlterationMonitor: {}", e.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.error("Error submitting stop monitor task: {}", e.getMessage());
            }
        }
    }

    /**
     * Hauptverarbeitungsschleife zur Überwachung des Verzeichnisses.
     * Erkennt neue oder geänderte PDF-Dateien und verarbeitet sie.
     */
    @Override
    public void run(){
        try{ //(WatchService wService = FileSystems.getDefault().newWatchService())
            Path path = env.getPathOffers();
            FileAlterationObserver watcher = new FileAlterationObserver(path.toFile());
            watcher.addListener(new PdfFileListiner());
            long interval = 5000;

            monitor = new FileAlterationMonitor(interval, watcher);
            logger.info("Starting directory monitor");
            monitor.start();

            while(running && !Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Reset interrupt flag
                    break; // Exit loop
                }
            }
        }catch(Exception e){
            logger.error("Directory monitoring error: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }finally {
            stopMonitor();
            logger.info("FileImporter stopped");
        }
    }

    /**
     * Listener für Dateiänderungen, reagiert auf neue und geänderte PDF-Dateien.
     */
    private class PdfFileListiner extends FileAlterationListenerAdaptor{
        @Override
        public void onFileCreate(File file){
            handleEvent("CREATE", file);
        }

        @Override
        public void onFileChange(File file){
            handleEvent("CHANGE", file);
        }
    }

    /**
     * Behandelt Datei-Ereignisse, prüft Dateigröße und verarbeitet PDF-Dateien.
     *
     * @param watchEvent Typ des Datei-Ereignisses (CREATE, CHANGE)
     * @param file Die betroffene Datei
     */
    private void handleEvent(String watchEvent, File file) {
        String fileName = file.getName();
        Path filePath = file.toPath();

        if (fileName.toLowerCase().endsWith(".pdf") && (watchEvent.equals("CREATE") || watchEvent.equals("CHANGE"))) {
            logger.info("Detected new file {}", fileName);

            try {
                long lastModified = file.lastModified();
                long fileSizeInKb = Files.size(filePath) / SIZETOMB;
                long maxFileSize = env.getMaxFileSize();

                if(lastModifiedMap.containsKey(filePath) && lastModifiedMap.get(filePath) == lastModified){
                    return;
                }
                lastModifiedMap.put(filePath, lastModified);

                if (fileSizeInKb > maxFileSize) {
                    logger.warn("File {} exceeds max size ({} MB). Skipping.", fileName, maxFileSize);
                    return;
                }

                long currentSize;
                long previousSize = -1;
                do {
                    currentSize = Files.size(filePath);
                    if (previousSize == currentSize) break;

                    previousSize = currentSize;
                    TimeUnit.MILLISECONDS.sleep(300);
                } while (currentSize != Files.size(filePath));


                processPdfToJson(filePath);
                logger.info("File {} processed and added to queue", fileName);
            } catch (IOException | InterruptedException e) {
                logger.error("Error while processing file {}: {}", fileName, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }else{
            logger.info("Non PDF-file detected and ignored: {}", fileName);
        }
    }

    /**
     * Konvertiert eine PDF-Datei in einen JSON-String und fügt ihn der Warteschlange hinzu.
     *
     * @param filePath Pfad zur PDF-Datei
     * @throws IOException bei Fehlern während der Konvertierung
     */
    public void processPdfToJson(Path filePath) throws IOException{
        logger.info("Converting to JSON String...");
        try{
            PdfParser pdfParser = new PdfParser(filePath);
            String pdfContent = pdfParser.pdfToString();
            String json = convertToJson(pdfContent, filePath);
            queue.put(json);
            logger.info("Converted to JSON String");
        }catch (InterruptedException e) {
            logger.info("Thread interrupted, shutting down.");
            Thread.currentThread().interrupt();
            stop();
        }catch(Exception e){
            logger.error("Error while converting {} to JSON: ", filePath, e);
            throw new IOException("Failed to Process pdf to json");
        }
    }

    /**
     * Erstellt einen JSON-String aus dem PDF-Inhalt und dem Dateipfad.
     *
     * @param fileContent Inhalt der PDF-Datei als String
     * @param filePath Pfad zur PDF-Datei
     * @return JSON-String mit Inhalt und Pfad
     * @throws IOException bei Fehlern während der JSON-Erstellung
     */
    public String convertToJson(String fileContent, Path filePath) throws IOException{
        Map<String, String> jsonStringMap = new HashMap<>();
        jsonStringMap.put("content", fileContent);
        jsonStringMap.put("path", filePath.toString());
        return objectMapper.writeValueAsString(jsonStringMap);
    }

    /**
     * Gibt den überwachten Verzeichnispfad zurück.
     *
     * @return Pfad des Angebotsverzeichnisses
     */
    public Path getPath(){
        return env.getPathOffers();
    }
}
