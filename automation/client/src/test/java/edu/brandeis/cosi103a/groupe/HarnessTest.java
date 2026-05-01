package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.cosi21.DecisionResult;
import edu.brandeis.cosi103a.groupe.main.CardDescriptions;
import edu.brandeis.cosi103a.groupe.main.ConsoleInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for console decision-making and card description logic. */
public class HarnessTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        // Redirect System.out to capture console output
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() {
        // Restore original System.in and System.out
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // ========== getDecisionFromConsole Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testGetDecisionFromConsole_NullOptions_ThrowsException() {
        Scanner testScanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        new ConsoleInput(testScanner).getDecisionFromConsole(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDecisionFromConsole_EmptyOptions_ThrowsException() {
        Scanner testScanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        new ConsoleInput(testScanner).getDecisionFromConsole(null, ImmutableList.of());
    }

    @Test
    public void testGetDecisionFromConsole_SingleEndPhaseDecision() {
        String input = "\n"; // Just press enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        EndPhaseDecision decision = new EndPhaseDecision(TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(decision);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should return the single EndPhaseDecision", decision, result.getDecision());
        assertTrue(
                "Should prompt to press enter",
                outputStream.toString().contains("Press enter to continue"));
    }

    @Test
    public void testGetDecisionFromConsole_MultipleOptions_ValidChoice() {
        String input = "Method\n"; // Choose second option (METHOD card)
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        EndPhaseDecision decision1 = new EndPhaseDecision(TurnPhase.ACTION);
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision decision3 = new BuyDecision(Card.Type.BITCOIN);

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2, decision3);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should return the second decision", decision2, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_FirstOption() {
        String input = "Method\n"; // Choose first option
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should return the first decision", decision1, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_LastOption() {
        String input = "Bitcoin\n"; // Choose last option
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        BuyDecision decision3 = new BuyDecision(Card.Type.BITCOIN);

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2, decision3);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should return the last decision", decision3, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_InvalidThenValid() {
        String input = "InvalidCard\nRefactor\n"; // Invalid card name, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid choice", decision2, result.getDecision());
        assertTrue(
                "Should display error for invalid choice",
                outputStream.toString().contains("not found")
                        || outputStream.toString().contains("invalid"));
    }

    @Test
    public void testGetDecisionFromConsole_TooHighThenValid() {
        String input = "NonExistentCard\nMethod\n"; // Invalid card, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid choice", decision1, result.getDecision());
        assertTrue(
                "Should display error for invalid card",
                outputStream.toString().contains("not found")
                        || outputStream.toString().contains("invalid"));
    }

    @Test
    public void testGetDecisionFromConsole_NonNumericThenValid() {
        String input = "abc\nMethod\n"; // Invalid card name, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));

        ImmutableList<Decision> options = ImmutableList.of(decision1);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should handle invalid card name and return valid choice",
                decision1,
                result.getDecision());
        assertTrue(
                "Should display error for invalid input",
                outputStream.toString().contains("not found")
                        || outputStream.toString().contains("invalid"));
    }

    @Test
    public void testGetDecisionFromConsole_DisplaysOptions() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision decision2 = new BuyDecision(Card.Type.BITCOIN);

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                .getDecisionFromConsole(null, options);

        String output = outputStream.toString();
        assertTrue(
                "Should display prompt for input",
                output.contains("card name") || output.contains("Enter"));
    }

    @Test
    public void testGetDecisionFromConsole_DisplaysNumberedOptions() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision decision2 = new BuyDecision(Card.Type.BITCOIN);

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                .getDecisionFromConsole(null, options);

        String output = outputStream.toString();
        assertTrue("Should display prompt", output.contains("Enter") || output.contains("card"));
    }

    @Test
    public void testGetDecisionFromConsole_MultipleInvalidAttempts() {
        String input =
                "abc\nxyz\ninvalid\nwrong\nRefactor\n"; // Multiple invalid inputs, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));

        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should eventually return valid choice after multiple attempts",
                decision2,
                result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_MixedDecisionTypes() {
        String input = "E\n"; // Choose end phase
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision playCard = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision buyCard = new BuyDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(playCard, buyCard, endPhase);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should correctly handle mixed decision types", endPhase, result.getDecision());
    }

    // ========== getCardDescription Tests ==========

    @Test
    public void testGetCardDescription_BitcoinHasDescription() {
        String desc = CardDescriptions.get(Card.Type.BITCOIN);
        assertNotNull("Description should not be null", desc);
        assertFalse("Description should not be empty", desc.isEmpty());
        assertNotEquals(
                "Should return specific description, not default",
                "No description available.",
                desc);
    }

    @Test
    public void testGetCardDescription_AllKnownCardTypesHaveDescriptions() {
        Card.Type[] knownTypes = {
            Card.Type.BUG,
            Card.Type.METHOD,
            Card.Type.MODULE,
            Card.Type.FRAMEWORK,
            Card.Type.BITCOIN,
            Card.Type.ETHEREUM,
            Card.Type.DOGECOIN,
            Card.Type.REFACTOR,
            Card.Type.CODE_REVIEW,
            Card.Type.EVERGREEN_TEST
        };
        for (Card.Type type : knownTypes) {
            String desc = CardDescriptions.get(type);
            assertNotNull("Description for " + type + " should not be null", desc);
            assertFalse("Description for " + type + " should not be empty", desc.isEmpty());
            assertNotEquals(
                    "Description for " + type + " should not be default message",
                    "No description available.",
                    desc);
        }
    }

    // ========== autoPlay encoded in DecisionResult Tests ==========

    @Test
    public void testDecisionResult_NonAutoPlayInput_FlagIsFalse() {
        // Any input other than 'A' must produce autoPlayRequested == false
        PlayCardDecision play = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(play);
        String input = "Method\n";
        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);
        assertFalse(
                "Auto-play flag should be false for non-A inputs", result.isAutoPlayRequested());
    }

    @Test
    public void testDecisionResult_AutoPlayEncodedInReturnValue() {
        // The 'A' command encodes auto-play directly in the returned DecisionResult;
        // each call is independent — no global state involved.
        GameState moneyState = createMoneyPhaseState();
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoin);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin);
        String input = "A\n";
        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(moneyState, options);
        assertTrue(
                "Result should indicate auto-play was requested for 'A' command",
                result.isAutoPlayRequested());
    }

    // ========== 'E' / 'end' command tests ==========

    @Test
    public void testGetDecisionFromConsole_EInput_ReturnsEndPhaseDecision() {
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.ACTION);
        PlayCardDecision play = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(play, endPhase);
        String input = "E\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("'E' should select the EndPhaseDecision", endPhase, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_EndInput_ReturnsEndPhaseDecision() {
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.BUY);
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buy, endPhase);
        String input = "end\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("'end' should select the EndPhaseDecision", endPhase, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_EInput_WhenNoEndPhaseDecision_ShowsErrorThenValid() {
        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);
        String input = "E\nMethod\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid card decision", decision, result.getDecision());
        assertTrue(
                "Should show cannot end phase error",
                outputStream.toString().contains("Cannot end phase"));
    }

    // ========== 'A' (auto-play) command tests ==========

    @Test
    public void testGetDecisionFromConsole_ACommand_InMoneyPhase_ReturnsFirstTreasure() {
        GameState moneyState = createMoneyPhaseState();
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoin);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin);
        String input = "A\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(moneyState, options);

        assertSame(
                "'A' should return the first treasure card decision",
                playBitcoin,
                result.getDecision());
        assertTrue("Should set auto-play requested flag", result.isAutoPlayRequested());
    }

    @Test
    public void testGetDecisionFromConsole_ACommand_NotInMoneyPhase_ShowsError() {
        // null state → not in Money phase → A should show error
        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);
        String input = "a\nMethod\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid decision", decision, result.getDecision());
        assertTrue(
                "Should show auto-play only in Money phase error",
                outputStream.toString().contains("Money phase"));
    }

    @Test
    public void testGetDecisionFromConsole_ACommand_NoTreasureAvailable_ShowsError() {
        GameState moneyState = createMoneyPhaseState();
        // Options contain no PlayCardDecision for a treasure
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.MONEY);
        BuyDecision buyCard = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyCard, endPhase);
        String input = "A\nE\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(moneyState, options);

        assertSame("Should return end phase after A fails", endPhase, result.getDecision());
        assertTrue(
                "Should show no treasure cards error",
                outputStream.toString().contains("No treasure cards"));
    }

    // ========== 'man' command tests ==========

    @Test
    public void testGetDecisionFromConsole_ManCommand_ShowsCardDescription() {
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buy);
        String input = "man bitcoin\nBitcoin\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return the buy decision", buy, result.getDecision());
        // Description output should mention Bitcoin cost
        assertTrue(
                "Should display card info after 'man' command",
                outputStream.toString().contains("Cost:"));
    }

    @Test
    public void testGetDecisionFromConsole_ManCommand_UnknownCard_ShowsError() {
        // "man" with an unrecognized card name should display "Card not found"
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buy);
        String input = "man unknownxyz\nBitcoin\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return the buy decision", buy, result.getDecision());
        assertTrue(
                "Should show card not found message",
                outputStream.toString().contains("Card not found"));
    }

    // ========== Empty input test ==========

    @Test
    public void testGetDecisionFromConsole_EmptyInput_ShowsErrorThenValid() {
        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);
        String input = "\nMethod\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid choice", decision, result.getDecision());
        assertTrue(
                "Should show error for empty input",
                outputStream.toString().contains("Invalid input"));
    }

    // ========== Ambiguous card name test ==========

    @Test
    public void testGetDecisionFromConsole_AmbiguousInput_ShowsMultipleMatchesError() {
        // "m" matches Method, Module, Ethereum, Framework, etc. - many results
        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);
        String input = "m\nMethod\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should eventually find valid card after ambiguous input",
                decision,
                result.getDecision());
        assertTrue(
                "Should show multiple matches error",
                outputStream.toString().contains("multiple cards match"));
    }

    // ========== Card found globally but not in current options ==========

    @Test
    public void testGetDecisionFromConsole_CardNotInOptions_ShowsError() {
        // Ethereum uniquely matches but only Bitcoin buy is in options
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buy);
        String input = "Ethereum\nBitcoin\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame("Should eventually return valid matching decision", buy, result.getDecision());
        assertTrue(
                "Should show 'not available right now' message",
                outputStream.toString().contains("not available right now"));
    }

    // ========== GainCardDecision and TrashCardDecision matching ==========

    @Test
    public void testGetDecisionFromConsole_GainCardDecision_MatchesByCardTypeName() {
        GainCardDecision gain = new GainCardDecision(Card.Type.MODULE);
        ImmutableList<Decision> options = ImmutableList.of(gain);
        String input = "Module\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should return the GainCardDecision for matching card type",
                gain,
                result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_TrashCardDecision_MatchesByCardName() {
        Card refactor = new Card(Card.Type.REFACTOR, 1);
        TrashCardDecision trash = new TrashCardDecision(refactor);
        ImmutableList<Decision> options = ImmutableList.of(trash);
        String input = "Refactor\n";

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should return the TrashCardDecision for matching card",
                trash,
                result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_SingleNonEndPhaseDecision() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);

        DecisionResult result =
                new ConsoleInput(new Scanner(new ByteArrayInputStream(input.getBytes())))
                        .getDecisionFromConsole(null, options);

        assertSame(
                "Should return the single non-EndPhase decision", decision, result.getDecision());
        // Should not show "press enter" prompt since it's not an EndPhaseDecision
        assertFalse(
                "Should not show press enter prompt for non-EndPhase decisions",
                outputStream.toString().contains("Press enter to continue"));
    }

    // ========== Helper Methods ==========

    /**
     * Creates a GameState in the MONEY phase for testing auto-play and phase-sensitive commands.
     */
    private GameState createMoneyPhaseState() {
        return new GameState(
                "Player1",
                new Hand(ImmutableList.<Card>of(), ImmutableList.<Card>of()),
                TurnPhase.MONEY,
                0,
                0,
                1,
                new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 30)));
    }
}
