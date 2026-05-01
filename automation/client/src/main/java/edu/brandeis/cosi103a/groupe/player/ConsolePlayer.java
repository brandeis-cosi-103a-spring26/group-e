package edu.brandeis.cosi103a.groupe.player;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.DecisionResult;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.ConsoleInput;
import edu.brandeis.cosi103a.groupe.main.Renderer;
import edu.brandeis.cosi103a.groupe.observers.Observer;
import edu.brandeis.cosi.atg.event.GameObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConsolePlayer extends AbstractPlayer {

    private final ConsoleInput consoleInput;
    private final String customDisplayName;
    private boolean autoPlayTreasures = false;
    private Observer observer;

    /**
     * Creates a console player with the default display name format.
     *
     * @param consoleInput the ConsoleInput instance for handling user input
     */
    public ConsolePlayer(ConsoleInput consoleInput) {
        this(consoleInput, null);
    }

    /**
     * Creates a console player with an optional custom display name.
     *
     * @param consoleInput the ConsoleInput instance for handling user input
     * @param customDisplayName the custom name to use in logs/UI; blank values are ignored
     */
    public ConsolePlayer(ConsoleInput consoleInput, String customDisplayName) {
        this.consoleInput = Objects.requireNonNull(consoleInput);
        String trimmedName = customDisplayName == null ? "" : customDisplayName.trim();
        this.customDisplayName = trimmedName.isEmpty() ? null : trimmedName;
        this.observer = new Observer();
    }

    /**
     * Gets the GameObserver for this player. 
     *
     * @return an Optional containing a GameObserver used by the Console Player
     */
    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.of(observer);
    }

    @Override
    public String getDisplayName() {
        if (customDisplayName != null) {
            return customDisplayName;
        }
        return "Human " + super.getDisplayName();
    }

    /**
     * Total number of players in the game; controls how many past turns are shown in the log panel
     * (one full previous round + current turn).
     */
    private int numPlayers = 2;

    /**
     * Sets the number of players so the log panel can show a complete previous round. Should be
     * called alongside setTurnLogs(List).
     *
     * @param numPlayers total players in the game
     */
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * Makes a decision based on user input from the console.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @param reason The reason for the decision.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {

        // If the only option is to end the action phase due to no actions remaining, we
        // can skip prompting the user and just return that decision
        if (state.phase() == TurnPhase.ACTION
                && options.size() == 1
                && options.get(0) instanceof EndPhaseDecision
                && state.availableActions() == 0) {
            return options.get(0);
        }

        // If the only option is to end the buy phase due to no buys remaining, we can
        // skip prompting the user and just return that decision
        if (state.phase() == TurnPhase.BUY
                && options.size() == 1
                && options.get(0) instanceof EndPhaseDecision
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
                    if (card.category() == Card.Type.Category.MONEY) {
                        // Found a treasure, play it automatically
                        return decision;
                    }
                }
            }
            // No more treasure cards, turn off auto-play
            autoPlayTreasures = false;

            // If the only decision left is to end the phase, we can return that immediately
            // without prompting the user
            if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
                return options.get(0);
            }
        }

        Renderer.renderGameWithDecisions(state, options, observer.getEvents(), numPlayers);

        DecisionResult result = consoleInput.getDecisionFromConsole(state, options);

        // Check if user requested auto-play mode
        if (result.isAutoPlayRequested()) {
            autoPlayTreasures = true;
        }

        // Reset auto-play if we're leaving Money phase
        if (!isMoneyPhase) {
            autoPlayTreasures = false;
        }

        return result.getDecision();
    }
}
