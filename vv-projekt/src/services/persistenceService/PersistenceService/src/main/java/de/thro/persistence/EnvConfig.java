package de.thro.persistence;

import de.thro.shared.SharedEnvConfig;

/**
 * Erweiterung der gemeinsamen Umgebungs-Konfiguration für den Persistenz-Service.
 * Stellt den Pfad für die Angebots-Persistenz aus der Umgebungsvariable bereit.
 */
public class EnvConfig extends SharedEnvConfig {

    /**
     * Gibt den Pfad für die Angebots-Persistenz zurück.
     * Liest die Umgebungsvariable `OFFER_PERSISTENCE_PATH`.
     *
     * @return Pfad als String oder {@code null}, falls die Variable nicht gesetzt ist
     */
    public String getPersistencePath(){
        return getEnv("OFFER_PERSISTENCE_PATH");
    }
}
