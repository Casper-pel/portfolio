package de.thro.importer;


/**
 * Ausnahme, die bei Fehlern in der Konfiguration ausgelöst wird.
 * Wird verwendet, um spezifische Konfigurationsprobleme zu kennzeichnen.
 */
public class ConfigurationException extends Exception{

    /**
     * Erstellt eine neue ConfigurationException mit einer Detailnachricht.
     *
     * @param message etailnachricht zur Beschreibung des Konfigurationsfehlers
     */
    public ConfigurationException(String message){
        super(message);
    }
}
