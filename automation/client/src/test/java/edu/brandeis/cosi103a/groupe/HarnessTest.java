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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for the Harness class. Tests user input validation and decision-making logic. */
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

    // ========== getValidPlayerCount Tests ==========

    @Test
    public void testGetValidPlayerCount_ValidInput() {
        String input = "2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should return 2", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_MinimumBoundary() {
        String input = "0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept minimum value 0", 0, result);
    }

    @Test
    public void testGetValidPlayerCount_MaximumBoundary() {
        String input = "4\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept maximum value 4", 4, result);
    }

    @Test
    public void testGetValidPlayerCount_BelowMinimum_ThenValid() {
        String input = "-1\n2\n"; // Invalid, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 2, result);
        assertTrue(
                "Should display error message", outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_AboveMaximum_ThenValid() {
        String input = "5\n3\n"; // Invalid (above max), then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 3, result);
        assertTrue(
                "Should display error message", outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_NonNumeric_ThenValid() {
        String input = "abc\n2\n"; // Non-numeric, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 2, result);
        assertTrue(
                "Should display error for non-numeric input",
                outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_MultipleInvalidAttempts() {
        String input = "abc\n-5\n10\nhello\n2\n"; // Multiple invalid, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input after multiple attempts", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_DifferentRange() {
        String input = "5\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 1, 10);

        assertEquals("Should accept valid number in different range", 5, result);
    }

    @Test
    public void testGetValidPlayerCount_SingleValueRange() {
        String input = "3\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 3, 3);

        assertEquals("Should accept only valid value in single-value range", 3, result);
    }

    @Test
    public void testGetValidPlayerCount_NegativeRange() {
        String input = "-2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", -5, 0);

        assertEquals("Should handle negative numbers in range", -2, result);
    }

    @Test
    public void testGetValidPlayerCount_DisplaysPrompt() {
        String input = "2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        String prompt = "Test prompt:";

        Harness.getValidPlayerCount(scanner, prompt, 0, 4);

        String output = outputStream.toString();
        assertTrue("Should display the prompt", output.contains(prompt));
    }

    @Test
    public void testGetValidPlayerCount_ZeroInput() {
        String input = "0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept zero when it's in range", 0, result);
    }

    @Test
    public void testGetValidPlayerCount_DecimalInput_ThenValid() {
        String input = "2.5\n2\n"; // Decimal (invalid for int), then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject decimal and accept integer", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_EmptyInput_ThenValid() {
        String input = "\n2\n"; // Empty line, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject empty input and accept valid number", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_WhitespaceBeforeNumber() {
        String input = "  3\n"; // Whitespace before number
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept number with leading whitespace", 3, result);
    }

    // ========== getDecisionFromConsole Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testGetDecisionFromConsole_NullOptions_ThrowsException() {
        Scanner testScanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        Harness.getDecisionFromConsole(null, testScanner);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDecisionFromConsole_EmptyOptions_ThrowsException() {
        Scanner testScanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        Harness.getDecisionFromConsole(ImmutableList.of(), testScanner);
    }

    @Test
    public void testGetDecisionFromConsole_SingleEndPhaseDecision() {
        String input = "\n"; // Just press enter
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        EndPhaseDecision decision = new EndPhaseDecision(TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(decision);

        DecisionResult result =
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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

        Harness.getDecisionFromConsole(
                options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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

        Harness.getDecisionFromConsole(
                options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

        assertSame("Should correctly handle mixed decision types", endPhase, result.getDecision());
    }

    // ========== Edge Cases ==========

    // ========== getCardDescription Tests ==========

    @Test
    public void testGetCardDescription_BitcoinHasDescription() {
        String desc = Harness.getCardDescription(Card.Type.BITCOIN);
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
            String desc = Harness.getCardDescription(type);
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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));
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
                Harness.getDecisionFromConsole(
                        moneyState,
                        options,
                        new Scanner(new ByteArrayInputStream(input.getBytes())));
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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

        assertSame("'E' should select the EndPhaseDecision", endPhase, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_EndInput_ReturnsEndPhaseDecision() {
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.BUY);
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(buy, endPhase);
        String input = "end\n";

        DecisionResult result =
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

        assertSame("'end' should select the EndPhaseDecision", endPhase, result.getDecision());
    }

    @Test
    public void testGetDecisionFromConsole_EInput_WhenNoEndPhaseDecision_ShowsErrorThenValid() {
        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);
        String input = "E\nMethod\n";

        DecisionResult result =
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        moneyState,
                        options,
                        new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        moneyState,
                        options,
                        new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

        assertSame(
                "Should return the TrashCardDecision for matching card",
                trash,
                result.getDecision());
    }

    @Test
    public void testGetValidPlayerCount_LargeNumber_OutOfRange() {
        String input = "999999\n2\n"; // Very large number, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject large out-of-range number", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_SpecialCharacters_ThenValid() {
        String input = "@#$%\n2\n"; // Special characters, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject special characters", 2, result);
    }

    @Test
    public void testGetDecisionFromConsole_SingleNonEndPhaseDecision() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        PlayCardDecision decision = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        ImmutableList<Decision> options = ImmutableList.of(decision);

        DecisionResult result =
                Harness.getDecisionFromConsole(
                        options, new Scanner(new ByteArrayInputStream(input.getBytes())));

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
