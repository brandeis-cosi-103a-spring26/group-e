package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;

import com.google.common.collect.ImmutableList;

public class AIPlayer extends AbstractPlayer {

    /**
     * Makes a decision based on the current game state and available options.
     * 
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        // If the only option is EndPhaseDecision, return it
        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            return options.get(0);
        }

        // Delegate decision based on the current phase
        // Note that CLEANUP is not listed since this method is not called during the CLEANUP phase
        switch (state.phase()) {
            case ACTION: return makeActionDecision(state, options);
            case BUY: return makeBuyDecision(state, options);
            case GAIN: return makeGainDecision(state, options);
            case MONEY: return makeMoneyDecision(state, options);
            default: throw new IllegalStateException("Unknown phase: " + state.phase());
        }
    }

    /**
     * Handle decision-making for the ACTION phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    private Decision makeActionDecision(GameState state, ImmutableList<Decision> options) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Handle decision-making for the BUY phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    private Decision makeBuyDecision(GameState state, ImmutableList<Decision> options) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Handle decision-making for the GAIN phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    private Decision makeGainDecision(GameState state, ImmutableList<Decision> options) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Handle decision-making for the MONEY phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    private Decision makeMoneyDecision(GameState state, ImmutableList<Decision> options) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}