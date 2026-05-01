package edu.brandeis.cosi103a.groupe.player;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

public class AutoPlayerPlus extends AutoPlayer {

    private static final Card.Type[] VICTORY_CARDS = {
        Card.Type.FRAMEWORK, Card.Type.MODULE, Card.Type.METHOD
    };

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
            if (state.buyableCards().cardCounts().get(cardType) == 0) {
                emptyCount++;
            }
        }

        // If 2 empty piles, or if fewer than 3 Frameworks left, prioritize buying Victory cards.
        if (emptyCount == 2 || state.buyableCards().cardCounts().get(Card.Type.FRAMEWORK) < 3) {
            for (Card.Type victoryType : VICTORY_CARDS) {
                for (Decision option : options) {
                    if (option instanceof BuyDecision buyDecision) {
                        if (buyDecision.cardType() == victoryType) {
                            return buyDecision;
                        }
                    }
                }
            }
        }

        // Otherwise, use the default buying strategy.
        return super.makeBuyDecision(state, options);
    }
}
