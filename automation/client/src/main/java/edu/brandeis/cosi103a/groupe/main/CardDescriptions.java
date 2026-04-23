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
                            Card.Type.MERGE_CONFLICT,
                            "Trash a card from your hand. +1 Card per $1 it costs.\n\n"
                                    + "Trashing a card removes it from the player's deck entirely. When"
                                    + " this card is played, the player will be prompted with"
                                    + " TrashCardDecisions for each card in their hand. Trashing is not"
                                    + " optional, so the list of decisions will not include an"
                                    + " EndPhaseDecision. After the player makes a trash decision, the"
                                    + " engine will draw a number of cards equal to the cost of"
                                    + " the trashed card into the player's hand.")
                    .put(
                            Card.Type.UNIT_TEST,
                            "Choose one: +2 Actions; +$2; +2 Cards.\n\n"
                                    + "When this card is played, the engine prompts the player with three"
                                    + " options: either gain two additional actions for this turn, gain $2"
                                    + " to spend for this turn, or draw 2 cards. The player must choose one"
                                    + " option.")
                    .put(
                            Card.Type.DEPLOYMENT_PIPELINE,
                            "+1 Buy, +$1. This turn, cards cost $1 less (but not less than $0).\n\n"
                                    + "When this card is played, the player gains an additional buy and"
                                    + " $1 for this turn. Additionally, all cards cost $1 less for the"
                                    + " remainder of this turn (minimum cost is $$0).")
                    .build();

    public static String get(Card.Type cardType) {
        return DESCRIPTIONS.getOrDefault(cardType, "No description available.");
    }
}
