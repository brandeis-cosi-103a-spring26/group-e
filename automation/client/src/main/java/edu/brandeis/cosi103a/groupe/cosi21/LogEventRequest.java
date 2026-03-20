package edu.brandeis.cosi103a.groupe.cosi21;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public record LogEventRequest(GameState state, Event event, String player_uuid) { }
