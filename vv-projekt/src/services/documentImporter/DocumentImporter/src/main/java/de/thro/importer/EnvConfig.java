package de.thro.importer;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import de.thro.shared.LogLevel;
import de.thro.shared.LogLevelUtil;
import de.thro.importer.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Liest und validiert Umgebungsvariablen für die Konfiguration des Dokumentenimporters.
 * Prüft Dateigröße, Pfad, Log-Level und die Offer-Strategie.
 * Wirft bei ungültigen Werten eine {@link ConfigurationException} oder {@link IllegalArgumentException}.
 */
public class EnvConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);

    private final int maxFileSize;
    private final Path pathOffers;
    private final LogLevel logLevel;
    private final String offerStrategy;

    /**
     * Erstellt eine neue Konfiguration basierend auf Umgebungsvariablen.
     * Validiert alle relevanten Werte und wirft bei Fehlern eine Ausnahme.
     *
     * @throws ConfigurationException bei ungültigem Log-Level
     * @throws IllegalArgumentException bei ungültigen Werten für Dateigröße, Pfad oder Strategie
     */
    public EnvConfig() throws ConfigurationException {
        String tempMaxFileSize = getEnv("MAX_FILE_SIZE");
        try{
            maxFileSize = Integer.parseInt(Objects.requireNonNullElse(tempMaxFileSize, "10"));
            if(maxFileSize <= 0 ){
                throw new IllegalArgumentException("MAX_FILE_SIZE Cannot Be Less Than Zero!");
            }
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("Invalid number format: " + tempMaxFileSize +  ". MAX_FILE_SIZE Must Be a Number!", e);
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException(e);
        }

        String tempPathOffers = getEnv("PATH_OFFERS");
        try {
            Path temp = Paths.get(Objects.requireNonNullElse(tempPathOffers, "."));
            if(!Files.exists(temp)){
                throw new InvalidPathException(tempPathOffers, "Path Does Not Exist");
            }else if(!Files.isDirectory(temp)){
                throw new InvalidPathException(tempPathOffers, "Path Is Not A Directory");
            }else{
                pathOffers = temp;
            }
        }catch(InvalidPathException e){
            throw new IllegalArgumentException("Invalid path: " + e);
        }

        try{
            String temp = getEnv("LOG_LEVEL");
            this.logLevel = LogLevelUtil.getLogLevel(temp);
        }catch(IllegalArgumentException e){
            throw new ConfigurationException("Invalid LOG_LEVEL configuration");
        }

        String offerStrategyEnv = Objects.requireNonNullElse(getEnv("OFFER_STRATEGY"), "MessageBusStrategy");
        if(!offerStrategyEnv.equals("LoggingStrategy") && !offerStrategyEnv.equals("MessageBusStrategy")){
            logger.error("OFFER_STRATEGY is null or blank");
            throw new IllegalArgumentException("Invalid OFFER_STRATEGY");
        }
        this.offerStrategy = offerStrategyEnv;
    }

    public Integer getMaxFileSize(){
        return maxFileSize;
    }

    public Path getPathOffers(){
        return pathOffers;
    }

    public LogLevel getLogLevel(){
        return logLevel;
    }

    public String getOfferStrategy(){
        return offerStrategy;
    }

    /**
     * Liest eine Umgebungsvariable aus.
     *
     * @param key Name der Umgebungsvariable
     * @return Wert der Umgebungsvariable oder {@code null}, falls nicht gesetzt
     */
    protected String getEnv(String key) {
        return System.getenv(key);
    }

}
