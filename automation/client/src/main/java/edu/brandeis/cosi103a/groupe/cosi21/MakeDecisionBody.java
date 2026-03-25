package edu.brandeis.cosi103a.groupe.cosi21;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

public record MakeDecisionBody(
        GameState state, ImmutableList<Decision> options, String player_uuid) {}
