package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;

public class DecisionBuilder {

    /**
     * Builds the list of all valid decisions for the ACTION phase. Returns a list of
     * PlayCardDecisions for each action card in the given player's hand as well as an
     * EndPhaseDecision to end the ACTION phase. If the player has no actions remaining, only the
     * EndPhaseDecision is returned.
     *
     * @param playerInfo the current player's info, including actions remaining and hand contents
     * @return An ImmutableList of Decision objects for the ACTION phase.
     */
    public static ImmutableList<Decision> buildActionDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();

        if (playerInfo.numActions() <= 0) {
            return decisionBuilder.add(new EndPhaseDecision(TurnPhase.ACTION)).build();
        }

        ImmutableList<Card> actionsInHand = playerInfo.getActionsInHand();

        for (Card card : actionsInHand) {
            decisionBuilder.add(new PlayCardDecision(card));
        }

        decisionBuilder.add(new EndPhaseDecision(TurnPhase.ACTION));
        return decisionBuilder.build();
    }

    /**
     * Builds the list of all valid decisions for the MONEY phase. Returns a list of
     * PlayCardDecisions for each money card in the given player's hand as well as an
     * EndPhaseDecision to end the MONEY phase.
     *
     * @param playerInfo the current player's info, including money cards in hand
     * @return An ImmutableList of Decision objects for the MONEY phase.
     */
    public static ImmutableList<Decision> buildMoneyDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();
        ImmutableList<Card> moneysInHand = playerInfo.getMoneysInHand();
        for (Card card : moneysInHand) {
            decisionBuilder.add(new PlayCardDecision(card));
        }
        decisionBuilder.add(new EndPhaseDecision(TurnPhase.MONEY));
        return decisionBuilder.build();
    }

    /**
     * Builds the list of all valid decisions for the BUY phase. Returns a list of BuyDecisions for
     * each buyable card type as well as an EndPhaseDecision to end the BUY phase. If the player has
     * no buys remaining, only the EndPhaseDecision is returned.
     *
     * @param playerInfo the current player's info, including buys remaining and available coins
     * @param buyableCards the set of card types available for purchase from the supply
     * @return An ImmutableList of Decision objects for the BUY phase.
     */
    public static ImmutableList<Decision> buildBuyDecisions(
            PlayerInfo playerInfo, ImmutableSet<Card.Type> buyableCards) {

        ImmutableList.Builder<Decision> buyDecisions = ImmutableList.builder();

        if (playerInfo.numBuys() <= 0) {
            return buyDecisions.add(new EndPhaseDecision(TurnPhase.BUY)).build();
        }

        for (Card.Type cardType : buyableCards) {
            buyDecisions.add(new BuyDecision(cardType));
        }

        buyDecisions.add(new EndPhaseDecision(TurnPhase.BUY));
        return buyDecisions.build();
    }

    /**
     * Builds mandatory trash decision options from the given player's current unplayed cards.
     *
     * @param playerInfo the current player's info, including the unplayed cards available to trash
     * @return List containing one TrashCardDecision per unplayed card; empty if none are available
     */
    public static ImmutableList<Decision> buildTrashCardDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();

        ImmutableList<Card> unplayedCards = playerInfo.getUnplayedCards();

        if (unplayedCards.isEmpty()) {
            return decisionBuilder.build();
        }

        for (Card card : unplayedCards) {
            decisionBuilder.add(new TrashCardDecision(card));
        }

        return decisionBuilder.build();
    }

    /**
     * Builds decision options for discarding cards from the given player's current hand. Returns
     * DiscardCardDecisions for each card, and an EndPhaseDecision to end the discard phase.
     *
     * @param playerInfo the current player's info, including the unplayed cards available to
     *     discard
     * @return List of discard decisions, and a single EndPhaseDecision
     */
    public static ImmutableList<Decision> buildDiscardDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();

        ImmutableList<Card> unplayedCards = playerInfo.getUnplayedCards();

        for (Card card : unplayedCards) {
            decisionBuilder.add(new DiscardCardDecision(card));
        }

        decisionBuilder.add(new EndPhaseDecision(TurnPhase.DISCARD));

        return decisionBuilder.build();
    }

    /**
     * Builds non-optional discard options for the given player.
     *
     * <p>Unlike {@link #buildDiscardDecisions(PlayerInfo)}, this list intentionally excludes
     * EndPhaseDecision so the caller can enforce mandatory discards.
     *
     * @param playerInfo the player whose unplayed cards can be discarded
     * @return List containing one DiscardCardDecision per unplayed card
     */
    public static ImmutableList<Decision> buildForcedDiscardDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();

        ImmutableList<Card> unplayedCards = playerInfo.getUnplayedCards();
        for (Card card : unplayedCards) {
            decisionBuilder.add(new DiscardCardDecision(card));
        }

        return decisionBuilder.build();
    }

    /**
     * Builds decision options for the REACTION phase.
     *
     * <p>Only Monitoring can be revealed as a reaction in this flow.
     *
     * @param playerInfo the player who may react to an attack
     * @return List containing one PlayCardDecision per Monitoring in hand and EndPhaseDecision
     */
    public static ImmutableList<Decision> buildReactionDecisions(PlayerInfo playerInfo) {
        ImmutableList.Builder<Decision> decisionBuilder = ImmutableList.builder();

        for (Card card : playerInfo.getUnplayedCards()) {
            if (card.type() == Card.Type.MONITORING) {
                decisionBuilder.add(new PlayCardDecision(card));
            }
        }

        decisionBuilder.add(new EndPhaseDecision(TurnPhase.REACTION));
        return decisionBuilder.build();
    }
}
