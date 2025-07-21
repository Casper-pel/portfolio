package de.thro.persistence;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.rabbitmq.client.Channel;
import de.thro.shared.ConnectBus;
import de.thro.shared.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static de.thro.persistence.Utils.awaitMessage;
import static de.thro.persistence.Utils.createEnvConfig;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class PersistenceEnvConfigTest {

    public static ListAppender<ILoggingEvent> appender;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

//    @Test
//    void testMain() {
//        EnvConfig envConfig = createEnvConfig("/data", "localhost", "5672", "user", "password");
//
//        Runnable shutdown = Main.start(envConfig);
//
//        appender.list.forEach(log -> System.out.println(log.getFormattedMessage()));
//        boolean foundLog = awaitMessage(appender, "Persistence service starting...");
//        assertTrue(foundLog);
//
//        shutdown.run();
//    }

    @Test
    void testMain() {
        EnvConfig envConfig = createEnvConfig("/data", "rabbitmq", "5672", "user", "password");

        try {
            Runnable shutdown = Main.start(envConfig);
//            sleep(5000);

//            appender.list.forEach(log -> System.out.println(log.getFormattedMessage()));
            boolean foundLog = awaitMessage(appender, "Persistence service starting...");
            assertTrue(foundLog);

            // optional:
//            Thread.sleep(1000);
            shutdown.run();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception in test: " + e.getMessage());
        }
    }

    @Test
    void opersistenceShutdownTest() {
        EnvConfig envConfig = createEnvConfig("/data", "rabbitmq", "5672", "user", "password");
        Runnable shutdown = Main.start(envConfig);

        shutdown.run();
        boolean foundLog = awaitMessage(appender, "Shutting down PersistenceService...");
        assertTrue(foundLog);


    }

    @Test
    void testMessageConsumer() throws IOException{
        Queue consumerQueue = new Queue();
        MessageConsumer messageConsumer = new MessageConsumer(consumerQueue, tempDir.toString());
        messageConsumer.saveToFile("test");
        try (Stream<Path> files = Files.list(tempDir)) {
            Path savedFile = files
                    .filter(path -> path.getFileName().toString().startsWith("offer_"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No file was saved"));

            String content = Files.readString(savedFile);
            assertEquals("test", content);
        }
    }
}
