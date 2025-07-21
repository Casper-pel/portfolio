package de.thro.importer;

import de.thro.shared.LogLevel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.thro.importer.Utils.createEnvConfig;
import static org.junit.jupiter.api.Assertions.*;

class EnvConfigTest {

    @Test
    void testDefaultValues() throws ConfigurationException {
        var envConfig = new EnvConfig();
        assertTrue(envConfig.getMaxFileSize() > 0);
        assertNotNull(envConfig.getPathOffers());
        assertNotNull(envConfig.getLogLevel());
        assertNotNull(envConfig.getOfferStrategy());
    }

    @Test
        void testMaxFileSizeNotANumber(){
            try{
                createEnvConfig("error", "C:/Users", "info", "MessageBusStrategy");
                fail("Expected IllegalArgumentException");
            }catch(IllegalArgumentException | ConfigurationException e){
                assertTrue(e.getMessage().contains("MAX_FILE_SIZE Must Be a Number!"));
            }
    }

    @Test
    void testMaxFileSizeBelowZero(){
        try{
            createEnvConfig("-1", "C:/Users", "info", "MessageBusStrategy");
            fail("Expected IllegalArgumentException");
        }catch(IllegalArgumentException | ConfigurationException e){
            assertTrue(e.getMessage().contains("MAX_FILE_SIZE Cannot Be Less Than Zero!"));
        }
    }

    @Test
    void testPathOffersNotExistent(){
        try{
            createEnvConfig("2", "C:moin", "info", "MessageBusStrategy");
            fail("Expected IllegalArgumentException");
        }catch(IllegalArgumentException | ConfigurationException e){
            assertTrue(e.getMessage().contains("Path Does Not Exist"));
        }
    }

    @Test
    void testPathOffersNotDirectory() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt"); // or any extension your app expects
        String path = tempFile.toString();
        try{
            createEnvConfig("2", path, "info", "MessageBusStrategy");
            fail("Expected IllegalArgumentException");
        }catch(IllegalArgumentException e){
            assertTrue(e.getMessage().contains("Path Is Not A Directory"));
        }catch(Exception e){
            fail("Unexpected Exception " + e.getMessage());
        }
    }

    @Test
    void testLogLevelInvalid() throws IOException{
        Path tempDir = Files.createTempDirectory("test");
        String path = tempDir.toString();
        try{
            EnvConfig envConfig = createEnvConfig("2",path, "hallo", "MessageBusStrategy");
            assertTrue(envConfig.getLogLevel() == LogLevel.INFO);
        }catch(Exception e){
            fail("Unexpected Exception " + e.getMessage());
        }
    }

    @Test
    void testLogLevelError(){
            try{
                Path tempDir = Files.createTempDirectory("test");
                String path = tempDir.toString();
                EnvConfig envConfig = createEnvConfig("2", path, "error", "MessageBusStrategy");
                assertEquals(LogLevel.ERROR, envConfig.getLogLevel());
            }catch(Exception e){
                fail("Unexpected Exception " + e.getMessage());
            }
    }

    @Test
    void testLogLevelWarn(){
        try{
            Path tempDir = Files.createTempDirectory("test");
            String path = tempDir.toString();
            EnvConfig envConfig = createEnvConfig("2", path, "warn", "MessageBusStrategy");
            assertEquals(LogLevel.WARN, envConfig.getLogLevel());
        }catch(Exception e){
            fail("Unexpected Exception " + e.getMessage());
        }
    }

    @Test
    void testLogLevelDebug(){
        try{
            Path tempDir = Files.createTempDirectory("test");
            String path = tempDir.toString();
            EnvConfig envConfig = createEnvConfig("2", path, "debug", "MessageBusStrategy");
            assertEquals(LogLevel.DEBUG, envConfig.getLogLevel());
        }catch(Exception e){
            fail("Unexpected Exception " + e.getMessage());
        }
     }
}
