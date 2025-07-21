package de.thro.importer;

/**
 * Exception, die beim Lesen oder Parsen einer PDF-Datei auftritt.
 * Wird geworfen, wenn ein Fehler beim Zugriff oder der Verarbeitung einer PDF entsteht.
 */
public class PdfReadException extends RuntimeException{

    /**
     * Erstellt eine neue PdfReadException mit einer Fehlermeldung und einer Ursache.
     *
     * @param message Beschreibung des Fehlers
     * @param cause Ursprüngliche Exception, die den Fehler ausgelöst hat
     */
    public PdfReadException(String message, Throwable cause){
        super(message, cause);
    }
}
