package de.thro.pipeline.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Proxy-Komponente zur Authentifizierung und zum Abrufen von API-Keys für die OpenAI-Schnittstelle.
 * Stellt Methoden bereit, um ein JWT zu generieren und den API-Key über HTTP-Anfragen zu erhalten.
 */
@Component
public class AICredentialsProxy {

private static final Logger logger = LoggerFactory.getLogger(AICredentialsProxy.class);

    private static final String EMAIL = "casper.pelsma@stud.th-rosenheim.de";
    private static final String BASE_URL = "https://ss25-vv-openai-api-auth.azurewebsites.net";
    private final ObjectMapper mapper;
    private final HttpClient client;

    /**
     * Erstellt eine neue Instanz des Proxys mit initialisiertem HTTP-Client und ObjectMapper.
     */
    public AICredentialsProxy() {
        mapper = new ObjectMapper();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Fordert einen API-Key von der Authentifizierungs-API an.
     *
     * @return den API-Key als String oder {@code null} bei Fehler
     */
    public String getAPIKey(){
        logger.info("Getting API Key");

        try {
            String jwt = generateJWT();

            Map<String, String> requestData = Map.of("mailAddress", EMAIL);
            String body = mapper.writeValueAsString(requestData);

            logger.info("Request body created");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/v1/ApiKey/GetApiKey"))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            logger.info("Post request created");

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("Post request sent.");

            if (response.statusCode() == 200) {
                String resBody = response.body().trim();
                if (!resBody.isEmpty()) {
                    logger.info("API Key created");
                } else {
                    logger.error("Api Key creation failed");
                }
                JsonNode json = mapper.readTree(response.body());
                String keyObject = json.get("apiKey").asText();
                JsonNode apiKey = mapper.readTree(keyObject);
                logger.info("Test");
                return apiKey.get("apiKey").asText();
            } else {
                logger.error("Request failed with status code: {}", response.statusCode());
            }

        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted while creating API Key: {}", e.getMessage());
        }catch(Exception e){
            logger.error("Something went wrong while creating API Key: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generiert ein JWT-Token durch Anmeldung bei der Authentifizierungs-API.
     *
     * @return das JWT-Token als String oder {@code null} bei Fehler
     */
    public String generateJWT(){

        try {
            logger.info("JWT Token Generation started");

            Map<String, String> authData = Map.of("username", "PelsmaCasper", "password", "vvSS25");
            String body = mapper.writeValueAsString(authData);

            logger.info("Request body created");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/v1/User/Login"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            logger.info("Post request created");

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("Post request sent.");
            logger.info("Response received: {}", response);

            if (response.statusCode() == 200) {
                String token = response.body().trim();
                if (!token.isEmpty()) {
                    logger.info("JWT Token Generated");
                } else {
                    logger.error("JWT Token Generation Failed");
                }
                return token;
            } else {
                logger.error("Request Failed with status code {}", response.statusCode());
            }
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted while generating JWT Token: {}", e.getMessage());
        }catch(Exception e){
            logger.error("Something went wrong while generating JWT Token");
        }
        return null;
    }
}
