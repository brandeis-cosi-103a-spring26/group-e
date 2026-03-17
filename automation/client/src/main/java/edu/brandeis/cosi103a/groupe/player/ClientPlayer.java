package edu.brandeis.cosi103a.groupe.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MakeDecisionBody;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Optional;

public class ClientPlayer extends AbstractPlayer {
    private final String BASE_URL;
    private final HttpClient httpClient;
    private final ObjectMapper om;

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
        this.om = objectMapper;
    }

    @Override
    public Decision makeDecision(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        try {
            return makeDecisionHandler(state, options, reason);
        } catch (Exception e) {
            return options.get(0);
        }
    }

    private Decision makeDecisionHandler(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        MakeDecisionBody bodyObject = new MakeDecisionBody(state, options);
        try {
            String bodyString = om.writeValueAsString(bodyObject);
            String url = BASE_URL + "/makeDecision";
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
            return parseDecisionResponse(response.body(), options);

        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Timed out while waiting for decision server response", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Decision parseDecisionResponse(String responseBody, ImmutableList<Decision> options)
            throws IOException {
        JsonNode responseNode = om.readTree(responseBody);

        if (responseNode.has("selectedIndex")
                && responseNode.get("selectedIndex").canConvertToInt()) {
            int selectedIndex = responseNode.get("selectedIndex").asInt();
            if (selectedIndex >= 0 && selectedIndex < options.size()) {
                return options.get(selectedIndex);
            }
        }

        if (responseNode.has("decision")) {
            responseNode = responseNode.get("decision");
        }

        for (Decision option : options) {
            if (om.valueToTree(option).equals(responseNode)) {
                return option;
            }
        }

        throw new RuntimeException("Server response did not match any provided decision option");
    }
}
