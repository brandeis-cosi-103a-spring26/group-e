package edu.brandeis.cosi103a.groupe.server;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.Optional;

public record DecisionRequest(
        GameState state,
        ImmutableList<Decision> options,
        Optional<Event> reason,
        String player_uuid) {}
