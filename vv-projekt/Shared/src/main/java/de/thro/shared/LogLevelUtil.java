package de.thro.shared;

/**
 * Utility-Klasse für die Handhabung von Log-Leveln.
 * Diese Klasse bietet Methoden, um den Log-Level aus Umgebungsvariablen zu lesen und in ein Enum zu konvertieren.
 */
public class LogLevelUtil {

    /**
     * Enum für die verschiedenen Log-Level.
     */
    private LogLevelUtil(){
    }

    /**
     * Enum für die verschiedenen Log-Level.
     */
    public static LogLevel fetchLogLevel(){
        return getLogLevel(System.getenv("LOG_LEVEL"));
    }

    /**
     * Enum für die verschiedenen Log-Level.
     */
    public static LogLevel getLogLevel(String logLevel){
        if(logLevel == null || logLevel.isBlank()){
            return LogLevel.INFO;
        }

        try{
            return LogLevel.valueOf(logLevel.trim().toUpperCase());
        }catch(IllegalArgumentException e){
            return LogLevel.INFO;
        }
    }
}
