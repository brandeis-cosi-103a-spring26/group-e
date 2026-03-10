package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.cards.Card;

public class DecisionBuilder {

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
}
