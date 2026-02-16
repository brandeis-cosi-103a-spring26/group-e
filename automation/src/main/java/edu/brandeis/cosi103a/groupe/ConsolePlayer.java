package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

import com.google.common.collect.ImmutableList;

public class ConsolePlayer extends AbstractPlayer {
    /**
     * Makes a decision based on user input from the console.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        // Render the current game state to the console
        Renderer.renderGame(state);

        return Harness.getDecisionFromConsole(options);
    }
}
