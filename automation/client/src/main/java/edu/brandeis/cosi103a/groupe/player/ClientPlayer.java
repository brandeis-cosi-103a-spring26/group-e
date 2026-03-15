package edu.brandeis.cosi103a.groupe.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MakeDecisionBody;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientPlayer extends AbstractPlayer {
    private final String BASE_URL;
    private final HttpClient httpClient;
    private final ObjectMapper om;

    public ClientPlayer() {
        super();
        this.httpClient = HttpClient.newHttpClient();
        this.om = new ObjectMapper();
        // To be changed once the server has an actual URL
        this.BASE_URL = "www.example.com/api";
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        MakeDecisionBody bodyObject = new MakeDecisionBody(state, options);
        try {
            String bodyString = om.writeValueAsString(bodyObject);
            String url = BASE_URL + "/makeDecision";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Decision returnedDecision = om.readValue(response.body(), Decision.class);

            return returnedDecision;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
