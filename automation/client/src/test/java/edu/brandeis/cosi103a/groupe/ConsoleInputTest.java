package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.cosi21.DecisionResult;
import edu.brandeis.cosi103a.groupe.main.ConsoleInput;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.Test;

public class ConsoleInputTest {

    @Test
    public void testGetDecisionFromConsole_DiscardPhase_SelectsDiscardCardDecision() {
        ConsoleInput input = scannerInput("bit\n");

        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card method = new Card(Card.Type.METHOD, 2);
        Decision discardBitcoin = new DiscardCardDecision(bitcoin);
        Decision endDiscard = new EndPhaseDecision(GameState.TurnPhase.DISCARD);

        DecisionResult result =
                input.getDecisionFromConsole(
                        discardStateWithHand(bitcoin, method),
                        ImmutableList.of(discardBitcoin, endDiscard));

        assertSame(
                "Typing a matching card name in discard phase should pick that discard decision",
                discardBitcoin,
                result.getDecision());
        assertFalse("Discard selection should not request auto-play", result.isAutoPlayRequested());
    }

    @Test
    public void testGetDecisionFromConsole_DiscardPhase_AllowsEndPhase() {
        ConsoleInput input = scannerInput("e\n");

        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Decision discardBitcoin = new DiscardCardDecision(bitcoin);
        Decision endDiscard = new EndPhaseDecision(GameState.TurnPhase.DISCARD);

        DecisionResult result =
                input.getDecisionFromConsole(
                        discardStateWithHand(bitcoin),
                        ImmutableList.of(discardBitcoin, endDiscard));

        assertSame("Typing E should choose EndPhaseDecision", endDiscard, result.getDecision());
        assertFalse("End phase should not trigger auto-play", result.isAutoPlayRequested());
    }

    @Test
    public void testGetDecisionFromConsole_ChooseEffectDecision_ByDescription() {
        ConsoleInput input = scannerInput("discard\n");

        Decision discardEffect =
                new ChooseEffectDecision(ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO);
        Decision gainBugEffect =
                new ChooseEffectDecision(ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG);

        DecisionResult result =
                input.getDecisionFromConsole(null, ImmutableList.of(discardEffect, gainBugEffect));

        assertSame(
                "Typing effect description text should choose matching ChooseEffectDecision",
                discardEffect,
                result.getDecision());
        assertFalse("Effect selection should not trigger auto-play", result.isAutoPlayRequested());
    }

    @Test
    public void testGetDecisionFromConsole_ChooseEffectDecision_ByEnumName() {
        ConsoleInput input = scannerInput("ransomware gain bug\n");

        Decision discardEffect =
                new ChooseEffectDecision(ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO);
        Decision gainBugEffect =
                new ChooseEffectDecision(ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG);

        DecisionResult result =
                input.getDecisionFromConsole(null, ImmutableList.of(discardEffect, gainBugEffect));

        assertSame(
                "Typing enum-like effect text should choose matching ChooseEffectDecision",
                gainBugEffect,
                result.getDecision());
        assertFalse("Effect selection should not trigger auto-play", result.isAutoPlayRequested());
    }

    private static ConsoleInput scannerInput(String scriptedInput) {
        return new ConsoleInput(
                new Scanner(
                        new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8))));
    }

    private static GameState discardStateWithHand(Card... handCards) {
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.copyOf(handCards));
        CardStacks supply = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 10));
        return new GameState("Player1", hand, GameState.TurnPhase.DISCARD, 1, 0, 1, supply);
    }
}
