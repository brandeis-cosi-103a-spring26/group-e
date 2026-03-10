package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.cards.Card;

public class DecisionBuilder {

    /**
     * Builds the list of all valid decisions for the ACTION phase. Returns a list
     * of PlayCardDecisions for each action card in the given player's hand as well
     * as an EndPhaseDecision to end the ACTION phase. If the player has no actions
     * remaining, only the EndPhaseDecision is returned.
     *
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
     * PlayCardDecisions for each money card in the given player's hand as well as
     * an EndPhaseDecision to end the MONEY phase.
     *
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
     * Builds the list of all valid decisions for the BUY phase. Returns a list of
     * BuyDecisions for each buyable card type as well as an EndPhaseDecision to end
     * the BUY phase. If the player has no buys remaining, only the EndPhaseDecision
     * is returned.
     *
     * @return An ImmutableList of Decision objects for the BUY phase.
     */
    public static ImmutableList<Decision> buildBuyDecisions(PlayerInfo playerInfo,
            ImmutableSet<Card.Type> buyableCards) {

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

}
