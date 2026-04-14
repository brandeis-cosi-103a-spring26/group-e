package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import java.util.Map;

/** Static descriptions for each card type. */
public class CardDescriptions {

    public static final Map<Card.Type, String> DESCRIPTIONS =
            ImmutableMap.<Card.Type, String>builder()
                    // Victory Cards
                    .put(
                            Card.Type.BUG,
                            "A victory card worth -1 automation points.\n\n"
                                    + "This card is never playable, but subtracts 1 automation"
                                    + " point when the game ends.")
                    .put(
                            Card.Type.METHOD,
                            "A victory card worth 1 automation point.\n\n"
                                    + "This card is never playable, but grants 1 automation point"
                                    + " when the game ends.")
                    .put(
                            Card.Type.MODULE,
                            "A victory card worth 3 automation points.\n\n"
                                    + "This card is never playable, but grants 3 automation points"
                                    + " when the game ends.")
                    .put(
                            Card.Type.FRAMEWORK,
                            "A victory card worth 6 automation points.\n\n"
                                    + "This card is never playable, but grants 6 automation points"
                                    + " when the game ends.")
                    // Money Cards
                    .put(
                            Card.Type.BITCOIN,
                            "A money card worth 1 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 1 spendable money for the turn on which it was"
                                    + " played.")
                    .put(
                            Card.Type.ETHEREUM,
                            "A money card worth 2 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 2 spendable money for the turn on which it was"
                                    + " played.")
                    .put(
                            Card.Type.DOGECOIN,
                            "A money card worth 3 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 3 spendable money for the turn on which it was"
                                    + " played.")
                    // Action Cards
                    .put(
                            Card.Type.REFACTOR,
                            "Trash a card from your hand; Gain a card costing up to 2 more than"
                                    + " the trashed card.\n\n"
                                    + "Trashing a card removes it from the player's deck entirely."
                                    + " When this card is played, the player will be prompted with"
                                    + " TrashCardDecisions for each card in their hand. Trashing is"
                                    + " not optional, so the list of decisions will not include an"
                                    + " EndPhaseDecision. After the player makes a trash decision,"
                                    + " they will be prompted with GainCardDecisions representing"
                                    + " the cards they can possibly gain - every card that costs up"
                                    + " to 2 more than the trashed card. Gaining a card is also not"
                                    + " optional. Gained cards are placed into the player's discard"
                                    + " pile.")
                    .put(
                            Card.Type.CODE_REVIEW,
                            "+1 Card, +2 Actions.\n\n"
                                    + "When this card is played, the player immediately draws a"
                                    + " card, and is granted two additional actions for this turn.")
                    .put(
                            Card.Type.EVERGREEN_TEST,
                            "(Attack card) +2 Cards; Each other player gains a Bug.\n\n"
                                    + "When this card is played, the engine immediately adds a BUG"
                                    + " to the discard pile of each other player.")
                    .put(
                            Card.Type.PARALLELIZATION,
                            "You may play an Action card from your hand twice.\n\n"
                                    + "When this card is played, the player is immediately prompted"
                                    + " with PlayCardDecisions for every other unplayed action card"
                                    + " in their hand. The chosen card will then be executed twice."
                                    + " Executing the chosen card does not use additional actions"
                                    + " beyond the one action used to play Parallelization.")
                    .put(
                            Card.Type.BACKLOG,
                            "+1 Action; Discard any number of cards, then draw that many.\n\n"
                                    + "After playing this card, the player will be prompted with a"
                                    + " list of DiscardCardDecisions, one for each card in their"
                                    + " hand. The list of decisions will also contain a"
                                    + " EndPhaseDecision. The player can continue to choose"
                                    + " DiscardCardDecisions until they wish to stop or run out of"
                                    + " cards, at which point they should choose the"
                                    + " EndPhaseDecision. After the Discard phase ends, the Engine"
                                    + " will draw the number of cards discarded into the player's"
                                    + " hand, and prompt the player with the next appropriate set of"
                                    + " decisions, based on actions remaining.")
                    .build();

    public static String get(Card.Type cardType) {
        return DESCRIPTIONS.getOrDefault(cardType, "No description available.");
    }
}
