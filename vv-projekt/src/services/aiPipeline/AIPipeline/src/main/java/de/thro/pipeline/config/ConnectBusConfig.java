package de.thro.pipeline.config;

import de.thro.pipeline.EnvConfigAdapter;
import de.thro.shared.ConnectBus;
import de.thro.shared.SharedEnvConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Spring-Konfigurationsklasse zur Bereitstellung einer ConnectBus-Bean.
 * Initialisiert ConnectBus mit Umgebungsparametern aus dem EnvConfigAdapter.
 */
@Configuration
public class ConnectBusConfig {

    private final EnvConfigAdapter envConfigAdapter;

    /**
     * Erstellt eine neue ConnectBusConfig mit gegebenem EnvConfigAdapter.
     *
     * @param envConfigAdapter Adapter für Umgebungs-Konfiguration
     */
    public ConnectBusConfig(EnvConfigAdapter envConfigAdapter){
        this.envConfigAdapter = envConfigAdapter;
    }

    /**
     * Erstellt und liefert eine ConnectBus-Bean für die Spring-Anwendung.
     * Nutzt die umgewandelte SharedEnvConfig aus dem Adapter.
     *
     * @return ConnectBus-Instanz für die Bus-Kommunikation
     */
    @Bean
    public ConnectBus createConnectBus(){
        SharedEnvConfig sharedEnvConfig = envConfigAdapter.toSharedEnvConfig();
        return new ConnectBus(sharedEnvConfig);
    }

}
