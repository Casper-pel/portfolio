package de.thro.persistence;

//import de.thro.persistence.EnvConfig;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static EnvConfig createEnvConfig(String offerPersistencePath, String rabbitmqHost, String rabbitmqPort, String rabbitmqUsername, String rabbitmqPassword){
        return new EnvConfig(){
            @Override
            protected String getEnv(String key){
            return switch (key){
                case "OFFER_PERSISTENCE_PATH" -> offerPersistencePath;
                case "RABBITMQ_HOST" -> rabbitmqHost;
                case "RABBITMQ_PORT" -> rabbitmqPort;
                case "RABBITMQ_USERNAME" -> rabbitmqUsername;
                case "RABBITMQ_PASSWORD" -> rabbitmqPassword;
                default -> null;
            };
        }
        };
    }

    public static boolean awaitMessage(ListAppender<ILoggingEvent> appender, String expectedMessage) {
        boolean found = appender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains(expectedMessage));
        if (found) return true;
        try {
            Awaitility.await()
                    .atMost(10, TimeUnit.SECONDS)
                    .pollInterval(100, TimeUnit.MILLISECONDS)
                    .until(() -> appender.list.stream()
                            .anyMatch(event -> event.getFormattedMessage().contains(expectedMessage)));
            return true;
        } catch (Exception e) {
            return false; // timed out
        }
    }
}
