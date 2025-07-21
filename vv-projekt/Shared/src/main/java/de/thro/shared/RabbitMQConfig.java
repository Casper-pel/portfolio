package de.thro.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfiguration für RabbitMQ-Verbindungseinstellungen.
 * Diese Klasse liest die RabbitMQ-Konfiguration aus den Anwendungseigenschaften.
 */
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;

    public String getHost(){
        return host;
    }

    /**
     * Setzt den Hostnamen für die RabbitMQ-Verbindung.
     *
     * @param host der Hostname
     */
    public void setHost(String host){
        this.host = host;
    }

   /**
     * Gibt den Port für die RabbitMQ-Verbindung zurück.
     *
     * @return der Port
     */
    public Integer getPort(){
        return port;
    }

    /**
     * Setzt den Port für die RabbitMQ-Verbindung.
     *
     * @param port der Port
     */
    public void setPort(Integer port){
        this.port = port;
    }

   /**
     * Gibt den Benutzernamen für die RabbitMQ-Verbindung zurück.
     *
     * @return der Benutzername
     */
    public String getUsername(){
        return username;
    }

    /**
     * Setzt den Benutzernamen für die RabbitMQ-Verbindung.
     *
     * @param username der Benutzername
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Gibt das Passwort für die RabbitMQ-Verbindung zurück.
     *
     * @return das Passwort
     */
    public String getPassword(){
        return password;
    }

    /**
     * Setzt das Passwort für die RabbitMQ-Verbindung.
     *
     * @param password das Passwort
     */
    public void setPassword(String password){
        this.password = password;
    }
}
