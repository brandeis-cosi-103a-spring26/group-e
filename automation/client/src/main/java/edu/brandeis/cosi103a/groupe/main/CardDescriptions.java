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
                            Card.Type.IPO,
                            "+2 Cards, +1 Action, +2 Money.\n\n"
                                    + "When this card is played, the player immediately draws two"
                                    + " cards into their hand, is granted an additional action, and"
                                    + " gains 2 spendable money for this turn.")
                    .put(
                            Card.Type.EVERGREEN_TEST,
                            "(Attack card) +2 Cards; Each other player gains a Bug.\n\n"
                                    + "When this card is played, the engine immediately adds a BUG"
                                    + " to the discard pile of each other player.")
                    .put(
                            Card.Type.MONITORING,
                            "(Reaction card) +2 Cards; When another player plays an Attack card,"
                                    + " reveal this from your hand to be unaffected by the"
                                    + " attack.\n\n"
                                    + "When played, this card grants +2 cards. When another player plays an attack card,"
                                    + " players currently holding this card in hand will avoid the attack entirely."
                                    + " When a player avoids an attack like this, a GameEvent should be logged."
                                    + " The card is not considered \"played\" when used to avoid an attack, and remains in the player's hand."
                                    + " Attack cards are: HACK, Evergreen Test, and Ransomware.")
                    .put(
                            Card.Type.HACK,
                            "(Attack card) +2 Money; Each other player discards down to 3 cards"
                                    + " in hand.\n\n"
                                    + "After the active player gains 2 money, each other player is"
                                    + " prompted with DiscardCardDecisions until they have exactly"
                                    + " 3 cards remaining in hand. This discard is not optional, so"
                                    + " EndPhaseDecision is not offered. Players with an unplayed"
                                    + " Monitoring in hand reveal it and are unaffected by this"
                                    + " attack.")
                    .put(
                            Card.Type.TECH_DEBT,
                            "+1 Card, +1 Action, +1 Money; Discard one card per empty Supply"
                                    + " pile.\n\n"
                                    + "When this card is played, the player immediately draws one"
                                    + " card, gains one action, and gains one spendable money. The"
                                    + " engine then counts empty supply piles currently in the game."
                                    + " The player is prompted with DiscardCardDecisions (without an"
                                    + " EndPhaseDecision option) once per empty pile.")
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
                    .put(
                            Card.Type.DAILY_SCRUM,
                            "+4 Cards, +1 Buy; Each other player draws a card.\n\n"
                                    + "When this card is played, the engine immediately draws 4"
                                    + " cards into the active player's hand and adds 1 buy. Then,"
                                    + " before any other actions, each other non-disqualified player"
                                    + " draws exactly 1 card into their hand in turn order.")
                    .put(
                            Card.Type.SPRINT_PLANNING,
                            "+1 Card, +1 Action, +1 Buy.\n\n"
                                    + "When this card is played, the player immediately draws one"
                                    + " card, gains one additional action for this turn, and gains"
                                    + " one additional buy for this turn.")
                    .put(
                            Card.Type.RANSOMWARE,
                            "(Attack card) +3 Cards; Each other player chooses one: discard 2"
                                    + " cards, or gain a Bug.\n\n"
                                    + "When this card is played, each other player with an"
                                    + " unplayed Monitoring in hand reveals it and is unaffected by"
                                    + " this attack. Each non-immune player is then prompted to"
                                    + " choose either the discard-two effect or gain-a-Bug effect."
                                    + " A player who chooses discard may still choose it with fewer"
                                    + " than 2 cards in hand and will discard as many as possible up"
                                    + " to 2.")
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
                                    + " remainder of this turn (minimum cost is $0).")
                    .build();

    public static String get(Card.Type cardType) {
        return DESCRIPTIONS.getOrDefault(cardType, "No description available.");
    }
}
