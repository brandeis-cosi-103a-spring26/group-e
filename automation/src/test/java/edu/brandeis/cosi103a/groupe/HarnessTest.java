package edu.brandeis.cosi103a.groupe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;

/**
 * Unit tests for the Harness class.
 * Tests user input validation and decision-making logic.
 */
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
        assertTrue("Should display error message", 
                   outputStream.toString().contains("Invalid input"));
    }
    
    @Test
    public void testGetValidPlayerCount_AboveMaximum_ThenValid() {
        String input = "5\n3\n"; // Invalid (above max), then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);
        
        assertEquals("Should eventually return valid input", 3, result);
        assertTrue("Should display error message", 
                   outputStream.toString().contains("Invalid input"));
    }
    
    @Test
    public void testGetValidPlayerCount_NonNumeric_ThenValid() {
        String input = "abc\n2\n"; // Non-numeric, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        int result = Harness.getValidPlayerCount(scanner, "Enter number:", 0, 4);
        
        assertEquals("Should eventually return valid input", 2, result);
        assertTrue("Should display error for non-numeric input", 
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
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should return the single EndPhaseDecision", decision, result);
        assertTrue("Should prompt to press enter", 
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
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should return the second decision", decision2, result);
    }
    
    @Test
    public void testGetDecisionFromConsole_FirstOption() {
        String input = "Method\n"; // Choose first option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should return the first decision", decision1, result);
    }
    
    @Test
    public void testGetDecisionFromConsole_LastOption() {
        String input = "Bitcoin\n"; // Choose last option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        BuyDecision decision3 = new BuyDecision(Card.Type.BITCOIN);
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2, decision3);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should return the last decision", decision3, result);
    }
    
    @Test
    public void testGetDecisionFromConsole_InvalidThenValid() {
        String input = "InvalidCard\nRefactor\n"; // Invalid card name, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should eventually return valid choice", decision2, result);
        assertTrue("Should display error for invalid choice", 
                   outputStream.toString().contains("not found") || outputStream.toString().contains("invalid"));
    }
    
    @Test
    public void testGetDecisionFromConsole_TooHighThenValid() {
        String input = "NonExistentCard\nMethod\n"; // Invalid card, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should eventually return valid choice", decision1, result);
        assertTrue("Should display error for invalid card", 
                   outputStream.toString().contains("not found") || outputStream.toString().contains("invalid"));
    }
    
    @Test
    public void testGetDecisionFromConsole_NonNumericThenValid() {
        String input = "abc\nMethod\n"; // Invalid card name, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        
        ImmutableList<Decision> options = ImmutableList.of(decision1);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should handle invalid card name and return valid choice", decision1, result);
        assertTrue("Should display error for invalid input", 
                   outputStream.toString().contains("not found") || outputStream.toString().contains("invalid"));
    }
    
    @Test
    public void testGetDecisionFromConsole_DisplaysOptions() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision decision2 = new BuyDecision(Card.Type.BITCOIN);
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        String output = outputStream.toString();
        assertTrue("Should display prompt for input", output.contains("card name") || output.contains("Enter"));
    }
    
    @Test
    public void testGetDecisionFromConsole_DisplaysNumberedOptions() {
        String input = "Method\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision decision2 = new BuyDecision(Card.Type.BITCOIN);
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        String output = outputStream.toString();
        assertTrue("Should display prompt", output.contains("Enter") || output.contains("card"));
    }
    
    @Test
    public void testGetDecisionFromConsole_MultipleInvalidAttempts() {
        String input = "abc\nxyz\ninvalid\nwrong\nRefactor\n"; // Multiple invalid inputs, then valid
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision decision1 = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        PlayCardDecision decision2 = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        
        ImmutableList<Decision> options = ImmutableList.of(decision1, decision2);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should eventually return valid choice after multiple attempts", decision2, result);
    }
    
    @Test
    public void testGetDecisionFromConsole_MixedDecisionTypes() {
        String input = "E\n"; // Choose end phase
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        PlayCardDecision playCard = new PlayCardDecision(new Card(Card.Type.METHOD, 1));
        BuyDecision buyCard = new BuyDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(TurnPhase.ACTION);
        
        ImmutableList<Decision> options = ImmutableList.of(playCard, buyCard, endPhase);
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should correctly handle mixed decision types", endPhase, result);
    }
    
    // ========== Edge Cases ==========
    
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
        
        Decision result = Harness.getDecisionFromConsole(options, new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        assertSame("Should return the single non-EndPhase decision", decision, result);
        // Should not show "press enter" prompt since it's not an EndPhaseDecision
        assertFalse("Should not show press enter prompt for non-EndPhase decisions",
                    outputStream.toString().contains("Press enter to continue"));
    }
}
