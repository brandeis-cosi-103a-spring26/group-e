package edu.brandeis.cosi103a.groupe.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.Observers.ClientObserver;
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
        this.om = objectMapper;
        this.observer = new ClientObserver(baseUrl, httpClient, objectMapper, getName());
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
        MakeDecisionBody bodyObject = new MakeDecisionBody(state, options, getName());
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
            return om.readValue(response.body(), Decision.class);

        } catch (HttpTimeoutException e) {
            throw new RuntimeException("Timed out while waiting for decision server response", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.of((GameObserver) observer);
    }
}
