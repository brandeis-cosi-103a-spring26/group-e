package edu.brandeis.cosi103a.groupe.observers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.LogEventRequest;

public class ClientObserver implements GameObserver {
    private final String playerUUID;
    private final String BASE_URL;
    private final HttpClient httpClient;
    private final ObjectMapper om;

    public ClientObserver(String baseUrl, HttpClient httpClient, ObjectMapper om, String playerUUID) {
        super();
        this.BASE_URL = baseUrl;
        this.httpClient = httpClient;
        this.om = om;
        this.playerUUID = playerUUID;
    }

    @Override
    public void notifyEvent(GameState state, Event event) {
        LogEventRequest bodyObject = new LogEventRequest(state, event, playerUUID);
        try {
            String bodyString = om.writeValueAsString(bodyObject);
            String url = BASE_URL + "/log-event";
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(bodyString))
            .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException("Failed to log event: " + statusCode + " - " + response.body());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event log request to JSON", e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to send event log request", e);
        }
    }
}
