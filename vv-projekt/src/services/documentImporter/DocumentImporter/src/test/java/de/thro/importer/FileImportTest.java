package de.thro.importer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static de.thro.importer.Utils.awaitMessage;
import static de.thro.importer.Utils.createEnvConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class FileImportTest {

    private BlockingQueue<String> queue;
    private FileImport fileImport;
    public static ListAppender<ILoggingEvent> appender;

    @TempDir
    Path tempDir;

    @BeforeEach
    void testSetup() throws ConfigurationException {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);

        queue = new ArrayBlockingQueue<>(10);
        EnvConfig envConfig = createEnvConfig("2", tempDir.toString(), "info", "MessageBusStrategy");
        fileImport = new FileImport(envConfig, queue);
        fileImport.start();
        await().atMost(5, TimeUnit.SECONDS).until(() -> fileImport.running);
    }

    @AfterEach
    void testShutDown() {
        fileImport.stop(); // Stop the FileImporter
        await().atMost(5, TimeUnit.SECONDS).until(() -> !fileImport.running);
    }


    @Test
    void fileImportTest() throws ConfigurationException {
        EnvConfig envConfig = createEnvConfig("2", tempDir.toString(), "info", "MessageBusStrategy");

        FileImport fileImporter = new FileImport(envConfig, queue);
        assertEquals(fileImporter.getPath(), tempDir);
    }

    @Test
    void fileDetectionTest() {
        String fileName = "test.pdf";
        Path testFile = tempDir.resolve(fileName);
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(testFile.toFile());
        } catch (Exception e) {
            fail("Failed to create test File: " + e.getMessage());
        }
        boolean foundLog = awaitMessage(appender, "Detected new file " + fileName);

        assertTrue(foundLog, "No File detected");
    }

    @Test
    void convertToJsonTest() throws IOException {
        Path path = Paths.get("C:\\Users\\caspe\\Uni");
        String content = "Test sample content";
        String json = fileImport.convertToJson(content, path);
        assertEquals("{\"path\":\"C:\\\\Users\\\\caspe\\\\Uni\",\"content\":\"Test sample content\"}", json);
    }

    @Test
    void processPdfToJsonTest() throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        String fileName = "test.pdf";
        Path file = tempDir.resolve(fileName);
        String[] lines = {
                "Angebot",
                "GeoBau Solutions GmbH",
                "Bauhofstraße 7",
                "10115 Berlin",
                "Telefon: +49 30 123456789",
                "E-Mail: kontakt@geobau-solutions.de",
                "An:",
                "Tunnelgräber GmbH",
                "Herr Max Mustermann",
                "Bohrweg 12",
                "12345 Tiefstadt",
                "Angebotsnummer: ANG-20250518-8266",
                "Datum: 18.05.2025",
                "Pos. Beschreibung Menge Preis (EUR)",
                "B001 Tunnelbohrung 50m Tiefe 1 4000.00",
                "B002 Stahlbetonverstärkung 3 850.00",
                "B003 Baugrundanalyse vor Ort 2 620.00",
                "B004 Sprengvorbereitung & Absicherung 1 2900.00",
                "Gesamtpreis: 10690.00 EUR",
                "Dieses Angebot ist freibleibend und gültig bis zum 01.06.2025.",
                "Alle genannten Preise verstehen sich zzgl. der gesetzlichen Mehrwertsteuer.",
                "Es gelten unsere Allgemeinen Geschäftsbedingungen."
        };
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(100, 700);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }
                contentStream.endText();
            }
            document.save(file.toFile());
            File pdf = file.toFile();
            System.out.println("PDF exists: " + pdf.exists());
            System.out.println("PDF size: " + pdf.length() + " bytes");
        } catch (Exception e) {
            fail("Failed to create test File: " + e.getMessage());
        }

        boolean foundLog = awaitMessage(appender, "Converted to JSON String");
        assertTrue(foundLog);
    }

    @Test
    void exceedMaxFileSizeTest() throws IOException {
        String largeFile = "test.pdf";
        Path pdfFile = tempDir.resolve(largeFile);

        byte[] data = new byte[5_000_000];
        new Random().nextBytes(data);
        Files.write(pdfFile, data);

        boolean foundLog = awaitMessage(appender, "File " + largeFile + " exceeds max size");
        assertTrue(foundLog);
    }

    @Test
    void testMonitoringException() throws Exception {
        EnvConfig testConfig = createEnvConfig("2", tempDir.toString(), "info", "MessageBusStrategy");
        BlockingQueue<String> testQueue = new ArrayBlockingQueue<>(10);
        FileImport testFileImporter = new FileImport(testConfig, testQueue);

        FileAlterationMonitor mockMonitor = mock(FileAlterationMonitor.class);
        doThrow(new Exception("Test stop exception")).when(mockMonitor).stop();

        Field field = FileImport.class.getDeclaredField("monitor");
        field.setAccessible(true);
        field.set(testFileImporter, mockMonitor);

        testFileImporter.stop();

        boolean foundLog = awaitMessage(appender, "Error submitting stop monitor task:");
        assertTrue(foundLog);
    }

    @Test
    void processPdfToJsonFailTest() {
        String fileName = "test.pdf";
        Path file = tempDir.resolve(fileName);
        try (PDDocument pdf = new PDDocument()) {
            pdf.addPage(new PDPage());
            pdf.save(file.toFile());
        } catch (Exception e) {
            fail("Failed to create test File: " + e.getMessage());
        }
        boolean foundLog = awaitMessage(appender, "Error while processing file " + fileName);
        assertTrue(foundLog);
    }

}
