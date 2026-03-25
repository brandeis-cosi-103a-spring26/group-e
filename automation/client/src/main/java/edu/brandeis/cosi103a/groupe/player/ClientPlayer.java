package edu.brandeis.cosi103a.groupe.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.api.DecisionRequest;
import edu.brandeis.cosi103a.groupe.api.DecisionResponse;
import edu.brandeis.cosi103a.groupe.observers.ClientObserver;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientPlayer extends AbstractPlayer {
    private static final Logger LOGGER = Logger.getLogger(ClientPlayer.class.getName());

    private final String BASE_URL;
    private final HttpClient httpClient;
    private final ObjectMapper om;

    private final ClientObserver observer;

    public ClientPlayer() {
        this(
                "http://www.example.com/api",
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                new ObjectMapper());
    }

    ClientPlayer(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        super();
        this.BASE_URL = baseUrl;
        this.httpClient = httpClient;
        this.om = objectMapper.findAndRegisterModules();
        this.observer = new ClientObserver(baseUrl, httpClient, this.om, getName());
    }

    @Override
    public Decision makeDecision(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        try {
            return makeDecisionHandler(state, options, reason);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Falling back to first decision after remote failure", e);
            return options.get(0);
        }
    }

    private Decision makeDecisionHandler(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        DecisionRequest bodyObject = new DecisionRequest(state, options, reason, getName());
        try {
            String bodyString = om.writeValueAsString(bodyObject);
            String url = BASE_URL + "/decide";
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException(
                        "makeDecision request failed with status code "
                                + statusCode
                                + ": "
                                + response.body());
            }
            return resolveDecisionFromResponse(response.body(), options);

        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Timed out while waiting for decision server response", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Decision resolveDecisionFromResponse(
            String responseBody, ImmutableList<Decision> options) {
        try {
            DecisionResponse typedResponse = om.readValue(responseBody, DecisionResponse.class);
            if (typedResponse != null && typedResponse.decision() != null) {
                for (Decision option : options) {
                    if (option.equals(typedResponse.decision())) {
                        return option;
                    }
                }

                JsonNode typedDecisionNode = om.valueToTree(typedResponse.decision());
                Decision matchedTypedDecision = tryResolveDecisionNode(typedDecisionNode, options);
                if (matchedTypedDecision != null) {
                    return matchedTypedDecision;
                }
            }
        } catch (IOException ignored) {
            // Fall back to raw node parsing to preserve compatibility for polymorphic decisions.
        }

        JsonNode decisionNode = extractRawDecisionNode(responseBody);
        return resolveDecisionFromNode(decisionNode, options);
    }

    private JsonNode extractRawDecisionNode(String responseBody) {
        try {
            JsonNode rawResponse = om.readTree(responseBody);
            JsonNode decisionNode = rawResponse.get("decision");
            if (decisionNode == null || decisionNode.isNull()) {
                throw new RuntimeException("Decision response must include a decision field");
            }
            return decisionNode;
        } catch (IOException rawParseFailure) {
            throw new RuntimeException("Failed to parse decision response", rawParseFailure);
        }
    }

    private Decision resolveDecisionFromNode(
            JsonNode decisionNode, ImmutableList<Decision> options) {
        Decision match = tryResolveDecisionNode(decisionNode, options);
        if (match != null) {
            return match;
        }

        throw new RuntimeException(
                "Decision response did not match any available option: " + decisionNode);
    }

    private Decision tryResolveDecisionNode(JsonNode decisionNode, ImmutableList<Decision> options) {
        for (Decision option : options) {
            JsonNode optionNode = om.valueToTree(option);
            if (optionNode.equals(decisionNode)) {
                return option;
            }
        }
        return null;
    }

    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.of((GameObserver) observer);
    }
}
