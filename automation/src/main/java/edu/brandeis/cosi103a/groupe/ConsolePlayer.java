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
     * Static map of card descriptions for display purposes.
     */
    private static final Map<Card.Type, String> CARD_DESCRIPTIONS = ImmutableMap.<Card.Type, String>builder()
        // Victory Cards
        .put(Card.Type.BUG, 
            "A victory card worth -1 automation points.\n\n" +
            "This card is never playable, but subtracts 1 automation point when the game ends.")
        .put(Card.Type.METHOD, 
            "A victory card worth 1 automation point.\n\n" +
            "This card is never playable, but grants 1 automation point when the game ends.")
        .put(Card.Type.MODULE, 
            "A victory card worth 3 automation points.\n\n" +
            "This card is never playable, but grants 3 automation points when the game ends.")
        .put(Card.Type.FRAMEWORK, 
            "A victory card worth 6 automation points.\n\n" +
            "This card is never playable, but grants 6 automation points when the game ends.")
        // Money Cards
        .put(Card.Type.BITCOIN, 
            "A money card worth 1 money.\n\n" +
            "This card is playable during the MONEY phase. When played, it grants 1 spendable money for the turn on which it was played.")
        .put(Card.Type.ETHEREUM, 
            "A money card worth 2 money.\n\n" +
            "This card is playable during the MONEY phase. When played, it grants 2 spendable money for the turn on which it was played.")
        .put(Card.Type.DOGECOIN, 
            "A money card worth 3 money.\n\n" +
            "This card is playable during the MONEY phase. When played, it grants 3 spendable money for the turn on which it was played.")
        // Action Cards
        .put(Card.Type.REFACTOR, 
            "Trash a card from your hand; Gain a card costing up to 2 more than the trashed card.\n\n" +
            "Trashing a card removes it from the player's deck entirely. When this card is played, the player will be prompted with " +
            "TrashCardDecisions for each card in their hand. Trashing is not optional, so the list of decisions will not include an " +
            "EndPhaseDecision. After the player makes a trash decision, they will be prompted with GainCardDecisions representing the " +
            "cards they can possibly gain - every card that costs up to 2 more than the trashed card. Gaining a card is also not optional. " +
            "Gained cards are placed into the player's discard pile.")
        .put(Card.Type.CODE_REVIEW, 
            "+1 Card, +2 Actions.\n\n" +
            "When this card is played, the player immediately draws a card, and is granted two additional actions for this turn.")
        .put(Card.Type.EVERGREEN_TEST, 
            "(Attack card) +2 Cards; Each other player gains a Bug.\n\n" +
            "When this card is played, the engine immediately adds a BUG to the discard pile of each other player.")
        .build();

    /**
     * Gets the description for a given card type.
     * 
     * @param cardType The card type to get the description for
     * @return The card description, or a default message if not found
     */
    public static String getCardDescription(Card.Type cardType) {
        return CARD_DESCRIPTIONS.getOrDefault(cardType, "No description available.");
    }

    /**
     * Makes a decision based on user input from the console.
     * 
     * @param state   The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
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

            // If the only deicsion left is to end the phase, we can return that immediately without prompting the user
            if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
                return options.get(0);
            }
        }

        if (state.phase() == TurnPhase.BUY && options.size() == 1 && options.get(0) instanceof EndPhaseDecision && state.availableBuys() == 0) {
            // If the only option is to end the buy phase due to no buys remaining, we can skip prompting the user and just return that decision
            return options.get(0);
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
