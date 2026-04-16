package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import edu.brandeis.cosi103a.groupe.main.GameCreator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for GameCreator: player count and name input validation. */
public class GameCreatorTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    // ========== getValidPlayerCount Tests ==========

    @Test
    public void testGetValidPlayerCount_ValidInput() {
        String input = "2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should return 2", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_MinimumBoundary() {
        String input = "0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept minimum value 0", 0, result);
    }

    @Test
    public void testGetValidPlayerCount_MaximumBoundary() {
        String input = "4\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept maximum value 4", 4, result);
    }

    @Test
    public void testGetValidPlayerCount_BelowMinimum_ThenValid() {
        String input = "-1\n2\n"; // Invalid, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 2, result);
        assertTrue(
                "Should display error message", outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_AboveMaximum_ThenValid() {
        String input = "5\n3\n"; // Invalid (above max), then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 3, result);
        assertTrue(
                "Should display error message", outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_NonNumeric_ThenValid() {
        String input = "abc\n2\n"; // Non-numeric, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input", 2, result);
        assertTrue(
                "Should display error for non-numeric input",
                outputStream.toString().contains("Invalid input"));
    }

    @Test
    public void testGetValidPlayerCount_MultipleInvalidAttempts() {
        String input = "abc\n-5\n10\nhello\n2\n"; // Multiple invalid, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should eventually return valid input after multiple attempts", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_DifferentRange() {
        String input = "5\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 1, 10);

        assertEquals("Should accept valid number in different range", 5, result);
    }

    @Test
    public void testGetValidPlayerCount_SingleValueRange() {
        String input = "3\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 3, 3);

        assertEquals("Should accept only valid value in single-value range", 3, result);
    }

    @Test
    public void testGetValidPlayerCount_NegativeRange() {
        String input = "-2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", -5, 0);

        assertEquals("Should handle negative numbers in range", -2, result);
    }

    @Test
    public void testGetValidPlayerCount_DisplaysPrompt() {
        String input = "2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        String prompt = "Test prompt:";

        GameCreator.getValidPlayerCount(scanner, prompt, 0, 4);

        String output = outputStream.toString();
        assertTrue("Should display the prompt", output.contains(prompt));
    }

    @Test
    public void testGetValidPlayerCount_ZeroInput() {
        String input = "0\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept zero when it's in range", 0, result);
    }

    @Test
    public void testGetValidPlayerCount_DecimalInput_ThenValid() {
        String input = "2.5\n2\n"; // Decimal (invalid for int), then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject decimal and accept integer", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_EmptyInput_ThenValid() {
        String input = "\n2\n"; // Empty line, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject empty input and accept valid number", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_WhitespaceBeforeNumber() {
        String input = "  3\n"; // Whitespace before number
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should accept number with leading whitespace", 3, result);
    }

    @Test
    public void testGetValidPlayerCount_LargeNumber_OutOfRange() {
        String input = "999999\n2\n"; // Very large number, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject large out-of-range number", 2, result);
    }

    @Test
    public void testGetValidPlayerCount_SpecialCharacters_ThenValid() {
        String input = "@#$%\n2\n"; // Special characters, then valid
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = GameCreator.getValidPlayerCount(scanner, "Enter number:", 0, 4);

        assertEquals("Should reject special characters", 2, result);
    }

    // ========== getValidPlayerName Tests ==========

    @Test
    public void testGetValidPlayerName_UsesProvidedName() {
        String input = "Alice\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = GameCreator.getValidPlayerName(scanner, "Enter name:", "Human 1");

        assertEquals("Should return entered name", "Alice", result);
    }

    @Test
    public void testGetValidPlayerName_TrimsWhitespace() {
        String input = "   Alice   \n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = GameCreator.getValidPlayerName(scanner, "Enter name:", "Human 1");

        assertEquals("Should trim leading and trailing whitespace", "Alice", result);
    }

    @Test
    public void testGetValidPlayerName_BlankUsesDefault() {
        String input = "   \n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = GameCreator.getValidPlayerName(scanner, "Enter name:", "Human 1");

        assertEquals("Blank input should fall back to default name", "Human 1", result);
    }
}
