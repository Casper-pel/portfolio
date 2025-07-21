package de.thro.importer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.thro.shared.SharedEnvConfig;
import org.awaitility.Awaitility;
import java.util.concurrent.TimeUnit;
//import static de.thro.importer.FileImportTest.appender;

public class Utils {

    public static EnvConfig createEnvConfig(String maxFileSize, String pathOffers, String logLevel, String offerStrategy) throws ConfigurationException {
    return new EnvConfig(){
        @Override
        protected String getEnv(String key){
            return switch (key){
                case "MAX_FILE_SIZE" -> maxFileSize;
                case "PATH_OFFERS" -> pathOffers;
                case "LOG_LEVEL" -> logLevel;
                case "OFFER_STRATEGY" -> offerStrategy;
                default -> null;
            };
        }
    };
    }

    public static SharedEnvConfig createSharedEnvConfig(String rabbitmqHost, String rabbitmqPort, String rabbitmqUsername, String rabbitmqPassword) throws ConfigurationException {
        return new SharedEnvConfig(){
            @Override
            protected String getEnv(String key){
                return switch (key){
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
