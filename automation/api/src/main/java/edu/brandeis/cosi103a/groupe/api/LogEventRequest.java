package edu.brandeis.cosi103a.groupe.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public record LogEventRequest(
        GameState state, Event event, @JsonProperty("player_uuid") String playerUUID) {}
