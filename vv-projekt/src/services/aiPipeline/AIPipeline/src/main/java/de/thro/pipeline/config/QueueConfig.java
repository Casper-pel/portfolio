package de.thro.pipeline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Spring-Konfigurationsklasse zur Bereitstellung einer BlockingQueue-Bean.
 * Initialisiert eine LinkedBlockingQueue für die Verarbeitung von Nachrichten im AI-Pipeline-Service.
 */
@Configuration
public class QueueConfig {

    /**
     * Erstellt und liefert eine BlockingQueue-Bean für String-Nachrichten.
     * Ermöglicht die thread-sichere Verarbeitung von Nachrichten in der Pipeline.
     *
     * @return BlockingQueue für String-Nachrichten
     */
    @Bean
    public BlockingQueue<String> blockingQueue(){
        return new LinkedBlockingQueue<>();
    }
}
