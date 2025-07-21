package de.thro.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SharedEnvConfig ist eine Klasse, die Umgebungsvariablen für die Konfiguration von RabbitMQ liest.
 * Diese Klasse stellt sicher, dass alle erforderlichen Umgebungsvariablen gesetzt sind und validiert deren Werte.
 * Sie wird verwendet, um eine konsistente Konfiguration für RabbitMQ in der Anwendung bereitzustellen.
 */
public class SharedEnvConfig {

    private static final Logger logger = LoggerFactory.getLogger(SharedEnvConfig.class);
    private String rabbitmqHost;
    private Integer rabbitmqPort;
    private String rabbitmqUsername;
    private String rabbitmqPassword;

    /**
     * Konstruktor, der die Umgebungsvariablen für RabbitMQ host, port, username und password liest.
     * Wenn eine der Variablen nicht gesetzt ist oder ungültig ist, wird eine Ausnahme ausgelöst.
     */
    public SharedEnvConfig(){
        this.rabbitmqHost = parseRabbitmqHost();
        this.rabbitmqPort = parseRabbitmqPort();
        this.rabbitmqUsername = parseRabbitmqUsername();
        this.rabbitmqPassword = parseRabbitmqPassword();
    }

    /**
     * Konstruktor, der die Umgebungsvariablen für RabbitMQ host, port, username und password liest.
     * Wenn eine der Variablen nicht gesetzt ist oder ungültig ist, wird eine Ausnahme ausgelöst.
     * @return String der RabbitMQ Host
     */
    private String parseRabbitmqHost(){
        String value = getEnv("RABBITMQ_HOST");
        if(value == null || value.isBlank()){
            logger.error("RABBITMQ_HOST is not set, using default 'localhost'");
            throw new IllegalArgumentException("Environment variable RABBITMQ_HOST is not set or is empty.");
        }
        return value.trim();
    }

    /**
     * Liest den RabbitMQ Port aus der Umgebungsvariable RABBITMQ_PORT.
     * Validiert, ob der Port eine gültige Ganzzahl zwischen 1 und 65535 ist.
     *
     * @return den RabbitMQ Port als Integer
     * @throws IllegalArgumentException wenn RABBITMQ_PORT nicht gesetzt ist oder ungültig ist
     */
    private Integer parseRabbitmqPort(){
        String value = getEnv("RABBITMQ_PORT");
        if(value == null || value.isBlank()){
            logger.error("RABBITMQ_PORT is not set");
            throw new IllegalArgumentException("Environment variable RABBITMQ_PORT is not set or is empty.");
        }
        try{
            int port = Integer.parseInt(value.trim());
            if(port < 1 || port > 65535){
                logger.error("RABBITMQ_PORT must be between 1 and 65535. Got: {}", port);
                throw new IllegalArgumentException("RABBITMQ_PORT must be between 1 and 65535. Got: " + port);
            }
            return port;
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("RABBITMQ_PORT must be a valid integer. Got: " + value, e);
        }
    }

    /**
     * Liest den RabbitMQ Benutzernamen aus der Umgebungsvariable RABBITMQ_USERNAME.
     * Validiert, ob der Benutzername nicht null oder leer ist.
     *
     * @return den RabbitMQ Benutzernamen als String
     * @throws IllegalArgumentException wenn RABBITMQ_USERNAME nicht gesetzt ist oder leer ist
     */
    private String parseRabbitmqUsername(){
        String value = getEnv("RABBITMQ_USERNAME");
        if(value == null || value.isBlank()){
            logger.error("RABBITMQ_USERNAME is not set");
            throw new IllegalArgumentException("RABBITMQ_USERNAME must not be null or empty");
        }
        return value.trim();
    }

    /**
     * Liest das RabbitMQ Passwort aus der Umgebungsvariable RABBITMQ_PASSWORD.
     * Validiert, ob das Passwort nicht null oder leer ist.
     *
     * @return das RabbitMQ Passwort als String
     * @throws IllegalArgumentException wenn RABBITMQ_PASSWORD nicht gesetzt ist oder leer ist
     */
    private String parseRabbitmqPassword(){
        String value = getEnv("RABBITMQ_PASSWORD");
        if(value == null || value.isBlank()){
            logger.error("RABBITMQ_PASSWORD is not set");
            throw new IllegalArgumentException("RABBITMQ_PASSWORD must not be null or empty");
        }
        return value;
    }


    // Getter und Setter für RabbitMQ Konfiguration
    public String getRabbitmqHost(){
        return rabbitmqHost;
    }

    public void setRabbitmqHost(String host){
        this.rabbitmqHost = host;
    }

    public Integer getRabbitmqPort(){
        return rabbitmqPort;
    }

    public void setRabbitmqPort(Integer rabbitmqPort) {
        this.rabbitmqPort = rabbitmqPort;
    }

    public String getRabbitmqUsername(){
        return rabbitmqUsername;
    }

    public void setRabbitmqUsername(String rabbitmqUsername) {
        this.rabbitmqUsername = rabbitmqUsername;
    }

    public String getRabbitmqPassword(){
        return rabbitmqPassword;
    }

    public void setRabbitmqPassword(String rabbitmqPassword) {
        this.rabbitmqPassword = rabbitmqPassword;
    }

    protected String getEnv(String param){
        return System.getenv(param);
    }
}