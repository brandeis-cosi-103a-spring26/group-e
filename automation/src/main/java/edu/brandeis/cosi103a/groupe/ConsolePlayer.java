package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.cards.Card;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class ConsolePlayer extends AbstractPlayer {

    private boolean autoPlayTreasures = false;

    /**
     * Makes a decision based on user input from the console.
     * 
     * @param state   The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {

        // If the only option is to end the action phase due to no actions remaining, we
        // can skip prompting the user and just return that decision
        if (state.phase() == TurnPhase.ACTION && options.size() == 1 && options.get(0) instanceof EndPhaseDecision
                && state.availableActions() == 0) {
            return options.get(0);
        }

        // If the only option is to end the buy phase due to no buys remaining, we can
        // skip prompting the user and just return that decision
        if (state.phase() == TurnPhase.BUY && options.size() == 1 && options.get(0) instanceof EndPhaseDecision
                && state.availableBuys() == 0) {
            return options.get(0);
        }

        // Check if we're in auto-play mode during Money phase
        boolean isMoneyPhase = state.phase() == GameState.TurnPhase.MONEY;

        if (autoPlayTreasures && isMoneyPhase) {
            // Look for a treasure card to play automatically
            for (Decision decision : options) {
                if (decision instanceof PlayCardDecision) {
                    Card card = ((PlayCardDecision) decision).card();
                    if (card.getCategory() == Card.Type.Category.MONEY) {
                        // Found a treasure, play it automatically
                        return decision;
                    }
                }
            }
            // No more treasure cards, turn off auto-play
            autoPlayTreasures = false;

            // If the only deicsion left is to end the phase, we can return that immediately
            // without prompting the user
            if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
                return options.get(0);
            }
        }

        // Not in auto-play mode or no treasures available - get user input
        // Render the current game state to the console
        // Renderer.renderGame(state);
        Renderer.renderGameWithDecisions(state, options);

        Decision choice = Harness.getDecisionFromConsole(state, options);

        // Check if user requested auto-play mode
        if (Harness.wasAutoPlayRequested()) {
            autoPlayTreasures = true;
        }

        // Reset auto-play if we're leaving Money phase
        if (!isMoneyPhase) {
            autoPlayTreasures = false;
        }

        return choice;
    }
}
