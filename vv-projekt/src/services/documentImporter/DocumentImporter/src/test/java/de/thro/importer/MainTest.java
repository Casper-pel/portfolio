package de.thro.importer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.thro.importer.strategies.IOfferStrategyHandler;
import de.thro.importer.strategies.LoggingOfferStrategyHandler;
import de.thro.importer.strategies.MessageBusOfferStrategyHandler;
import de.thro.shared.SharedEnvConfig;
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
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static de.thro.importer.Utils.*;
import static java.lang.Thread.sleep;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class MainTest {

    public static ListAppender<ILoggingEvent> appender;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup(){
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown(){
        appender.stop();

    }

    @Test
    void logTest() throws InterruptedException {
        EnvConfig envConfig = null;
        SharedEnvConfig sharedEnvConfig =  null;
        try {
            envConfig = createEnvConfig("2", tempDir.toString(), "info", "MessageBusStrategy");
            sharedEnvConfig = createSharedEnvConfig("localhost", "5672", "user", "password");
        }catch(ConfigurationException e){
            fail("An error occurred while loading the configuration");
        }
        EnvConfig finalEnvConfig = envConfig;
        SharedEnvConfig finalSharedEnvConfig = sharedEnvConfig;
//        Thread appThread = new Thread(() -> Main.startApplication(finalEnvConfig, finalSharedEnvConfig));
//        appThread.start();
        Runnable shutdown = Main.startApplication(finalEnvConfig, finalSharedEnvConfig);
        sleep(1000);
        System.out.println("moin");
        appender.list.forEach(log -> System.out.println(log.getFormattedMessage()));
        boolean foundLog = awaitMessage(appender,"Starting Document Processor");
//        appThread.interrupt();

        assertTrue(foundLog);

        shutdown.run();
    }

    @Test
    void offerStrategyTest() throws InterruptedException {
        EnvConfig envConfig = null;
        SharedEnvConfig sharedEnvConfig =  null;
        try {
            envConfig = createEnvConfig("2", tempDir.toString(), "info", "LoggingStrategy");
            sharedEnvConfig = createSharedEnvConfig("localhost", "5672", "user", "password");
        }catch(ConfigurationException e){
            fail("An error occurred while loading the configuration");
        }
        Runnable shutdown = Main.startApplication(envConfig, sharedEnvConfig);
        sleep(1000);
        System.out.println("moin");
        appender.list.forEach(log -> System.out.println(log.getFormattedMessage()));
        boolean foundLog = awaitMessage(appender,"Logging Strategy Type:");

        assertTrue(foundLog);

        shutdown.run();
    }

    @Test
    void loggingStrategyTest() throws IOException, ConfigurationException {
        IOfferStrategyHandler startegy;
        DocumentProcessor documentProcessor;
        FileImport fileImport;
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);
        startegy = new LoggingOfferStrategyHandler();
        EnvConfig envConfig = createEnvConfig("2", tempDir.toString(), "info", "LoggingStrategy");
        fileImport = new FileImport(envConfig, queue);
        fileImport.start();
        documentProcessor = new DocumentProcessor(queue, startegy);
        documentProcessor.start();
        await().atMost(5, TimeUnit.SECONDS).until(() -> fileImport.running);
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

        boolean foundLog = awaitMessage(appender,"PDF content processed via logging strategy");
        assertTrue(foundLog);
    }
}
