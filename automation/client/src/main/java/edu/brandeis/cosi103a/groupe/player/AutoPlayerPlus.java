package edu.brandeis.cosi103a.groupe.player;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AutoPlayerPlus extends AutoPlayer {

    private static final Card.Type[] VICTORY_CARDS = {
        Card.Type.FRAMEWORK, Card.Type.MODULE, Card.Type.METHOD
    };

    private static final Card.Type[] PLUS_ACTION_CARDS = {
        Card.Type.IPO,
        Card.Type.TECH_DEBT,
        Card.Type.CODE_REVIEW,
        Card.Type.SPRINT_PLANNING,
        Card.Type.UNIT_TEST,
        Card.Type.BACKLOG
    };

    // Track deck statistics to inform buying decisions.
    private int deckSize = 10;
    private int terminalCount = 0;

    /**
     * Gets the human-readable name for this player.
     *
     * @return the player name in a format based on a substring of the UUID
     */
    @Override
    public String getDisplayName() {
        return "AutoPlayerPlus-" + this.getName().substring(0, 4);
    }

    /**
     * Handle decision-making for the BUY phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    protected Decision makeBuyDecision(GameState state, ImmutableList<Decision> options) {

        // Count the number of empty card piles.
        int emptyCount = 0;
        for (Card.Type cardType : state.buyableCards().getCardTypes()) {
            if (state.buyableCards().getNumAvailable(cardType) == 0) {
                emptyCount++;
            }
        }

        // If 2 empty piles, or if fewer than 3 Frameworks left, prioritize buying Victory cards.
        if (emptyCount >= 2 || state.buyableCards().getNumAvailable(Card.Type.FRAMEWORK) < 3) {
            for (Card.Type victoryType : VICTORY_CARDS) {
                for (Decision option : options) {
                    if (option instanceof BuyDecision buyDecision) {
                        if (buyDecision.cardType() == victoryType) {
                            deckSize++;
                            return buyDecision;
                        }
                    }
                }
            }
        }

        // Otherwise, use the default buying strategy with a shuffled option order.
        // Shuffling the options adds some variability to the decision-making process.
        ArrayList<Decision> shuffledOptions = new ArrayList<>(options);
        Collections.shuffle(shuffledOptions);

        // If more than 20% of our deck are terminal cards, prioritize buying plus action cards
        if ((double) terminalCount / deckSize > 0.2) {
            for (Card.Type plusType : PLUS_ACTION_CARDS) {
                for (Decision option : shuffledOptions) {
                    if (option instanceof BuyDecision buyDecision) {
                        if (buyDecision.cardType() == plusType) {
                            deckSize++;
                            return buyDecision;
                        }
                    }
                }
            }
        }

        Decision decision = super.makeBuyDecision(state, ImmutableList.copyOf(shuffledOptions));
        if (decision instanceof BuyDecision buyDecision) {
            // Track terminal cards to inform future decisions.
            if (buyDecision.cardType().category() == Card.Type.Category.ACTION &&
                ! Arrays.asList(PLUS_ACTION_CARDS).contains(buyDecision.cardType())) {
                terminalCount++;
            }
            deckSize++;
        }
        return decision;
    }

    /**
     * Handle decision-making for the GAIN phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeGainDecision(GameState state, ImmutableList<Decision> options) {
        Decision decision = super.makeGainDecision(state, options);
        if (decision instanceof GainCardDecision gainCardDecision) {
            // Track terminal cards to inform future decisions.
            if (gainCardDecision.cardType().category() == Card.Type.Category.ACTION &&
                ! Arrays.asList(PLUS_ACTION_CARDS).contains(gainCardDecision.cardType())) {
                terminalCount++;
            }
            deckSize++;
        }
        return decision;
    }

    /**
     * Handle decision-making for trashing cards.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision handleTrashing(GameState state, ImmutableList<Decision> options) {
        Decision decision = super.handleTrashing(state, options);
        if (decision instanceof TrashCardDecision trashCardDecision) {
            // If we trash a terminal card, decrease the terminal count.
            if (trashCardDecision.card().type().category() == Card.Type.Category.ACTION &&
                ! Arrays.asList(PLUS_ACTION_CARDS).contains(trashCardDecision.card().type())) {
                terminalCount = Math.max(0, terminalCount - 1);
            }
            deckSize = Math.max(0, deckSize - 1);
        }
        return decision;
    }

}
