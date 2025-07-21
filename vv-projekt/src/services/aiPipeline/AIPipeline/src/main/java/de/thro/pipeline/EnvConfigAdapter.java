package de.thro.pipeline;

import de.thro.shared.RabbitMQConfig;
import de.thro.shared.SharedEnvConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adapter-Klasse, die die RabbitMQ-Konfiguration in eine SharedEnvConfig konvertiert.
 */
@Component
public class EnvConfigAdapter {

    private final RabbitMQConfig rabbitMQConfig;

    /**
     * Konstruktor, der die RabbitMQ-Konfiguration injiziert.
     *
     * @param rabbitMQConfig RabbitMQ-Konfiguration
     */
    @Autowired
    public EnvConfigAdapter(RabbitMQConfig rabbitMQConfig){
        this.rabbitMQConfig = rabbitMQConfig;
    }

    /**
     * Konvertiert die RabbitMQ-Konfiguration in eine SharedEnvConfig.
     *
     * @return eine SharedEnvConfig, die die RabbitMQ-Konfiguration enthält
     */
    public SharedEnvConfig toSharedEnvConfig(){
        SharedEnvConfig env = new SharedEnvConfig();
        env.setRabbitmqHost(rabbitMQConfig.getHost());
        env.setRabbitmqPort(rabbitMQConfig.getPort());
        env.setRabbitmqUsername(rabbitMQConfig.getUsername());
        env.setRabbitmqPassword(rabbitMQConfig.getPassword());
        return env;
    }
}
