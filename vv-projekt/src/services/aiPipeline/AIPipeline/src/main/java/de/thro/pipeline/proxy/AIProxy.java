package de.thro.pipeline.proxy;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Proxy-Komponente zur Kommunikation mit dem OpenAI-API-Endpunkt.
 * Verwaltet die Erstellung des OpenAI-Clients und das Ausführen von Chat-Completion-Anfragen.
 */
@Component
public class AIProxy {

    private static final Logger logger = LoggerFactory.getLogger(AIProxy.class);

    private static final String BASE_URL = "https://inference.ai.cnds.io/v1";
    private static final String MODEL_NAME = "artllama-chat-medium";

    private final AICredentialsProxy credentialsProxy;

    /**
     * Erstellt eine neue Instanz des AIProxy mit dem angegebenen Credentials-Proxy.
     *
     * @param credentialsProxy Proxy zur Verwaltung und Bereitstellung des API-Keys
     */
    public AIProxy(AICredentialsProxy credentialsProxy) {
        this.credentialsProxy = credentialsProxy;
    }

    /**
     * Erstellt einen OpenAIClient mit dem aktuellen API-Key.
     *
     * @return eine Instanz von OpenAIClient
     */
    public OpenAIClient createClient(){
        String apiKey = credentialsProxy.getAPIKey();
        logger.info("Creating OpenAIClient for API key {}", apiKey);
        return OpenAIOkHttpClient.builder().baseUrl(BASE_URL).apiKey(apiKey).build();
    }

    /**
     * Führt eine Anfrage an das KI-Modell aus und gibt die Antwort als String zurück.
     *
     * @param prompt Die Eingabeaufforderung für das KI-Modell
     * @return Die generierte Antwort des KI-Modells als String
     */
    public String executeAIRequest(String prompt) {
        OpenAIClient client = createClient();
        logger.info("client created");
        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder().model(MODEL_NAME).addUserMessage(prompt).build();
        logger.info("Creating Chat Completion Request");
        StringBuilder responseBuilder = new StringBuilder();
        logger.info("test");

        try {
            client.chat().completions().create(createParams).choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .forEach(responseBuilder::append);
            logger.info("Chat Completion Response[{}]", responseBuilder);
        }catch (Exception e){
            logger.error("Exception occurred while creating Chat Completion Request: {}", e.getMessage());
        }
        return responseBuilder.toString();

    }
}
