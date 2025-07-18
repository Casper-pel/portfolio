package ip.project.backend.backend.security;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class BackendSecurityConfigTest {

    @InjectMocks
    private BackendSecurityConfig backendSecurityConfig;

    @Test
    void corsConfigurationSource_shouldConfigureAllowedOriginsAndMethods() {
        CorsConfigurationSource source = backendSecurityConfig.corsConfigurationSource();

        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);

        // Leider getCorsConfiguration mit mock HttpServletRequest schwer zu testen wegen Servlet API Limitierungen.
        // Deshalb testen wir direkt die Registrierung der CorsConfiguration:
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        // Hole alle registrierten Pfade
        var registeredPaths = urlSource.getCorsConfigurations().keySet();

        // Prüfe, dass /** registriert ist
        assertTrue(registeredPaths.contains("/**"));

        // Prüfe, dass die Konfiguration für /** die gewünschten Werte hat
        CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

        assertNotNull(config);
        assertEquals(List.of("*"), config.getAllowedOriginPatterns());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), config.getAllowedMethods());
        assertEquals(List.of("*"), config.getAllowedHeaders());
        assertTrue(config.getAllowCredentials());
    }


}
