package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;

public class HandEVPlayer extends BigMoneyPlayer {

    private static final int HAND_SIZE = 5;
    private static final int INITIAL_DECK_SIZE = 10;
    private static final int INITIAL_MONEY_IN_DECK = 7;

    // Track deck statistics to inform buy decisions
    private int deckSize;
    private int totalMoneyInDeck;

    public HandEVPlayer() {
        super();
        this.deckSize = INITIAL_DECK_SIZE;
        this.totalMoneyInDeck = INITIAL_MONEY_IN_DECK;
    }

    /**
     * Handle decision-making for the BUY phase. This method attempts to buy a Framework if possible
     * and if the expected value of a hand is greater than the Framework cost, otherwise it buys the
     * most expensive money card the player can afford.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    protected Decision makeBuyDecision(GameState state, ImmutableList<Decision> options) {
        // Define preferred cards in order of preference (most expensive first)
        Card.Type[] preferredCards = {
            Card.Type.FRAMEWORK, Card.Type.DOGECOIN, Card.Type.ETHEREUM, Card.Type.BITCOIN
        };

        // Try to find each preferred card in order
        for (Card.Type preferredType : preferredCards) {
            // Skip Framework if the expected value of a hand is less than Framework cost

            // This is imperfect because the player can't track when it gains a Bug, but it's
            // a reasonable heuristic to avoid buying Framework too early
            if (preferredType == Card.Type.FRAMEWORK
                    && HAND_SIZE * (double) totalMoneyInDeck / deckSize
                            < Card.Type.FRAMEWORK.cost()) {
                continue;
            }

            for (Decision option : options) {
                if (option instanceof BuyDecision buyDecision) {
                    if (buyDecision.cardType() == preferredType) {
                        // Update deck statistics if we buy a card
                        deckSize++;
                        if (buyDecision.cardType().category() == Card.Type.Category.MONEY) {
                            totalMoneyInDeck += buyDecision.cardType().value();
                        }
                        return buyDecision;
                    }
                }
            }
        }

        // If none of the preferred cards are available, end phase
        return options.stream()
                .filter(option -> option instanceof EndPhaseDecision)
                .findFirst()
                .orElse(options.get(0));
    }
}
