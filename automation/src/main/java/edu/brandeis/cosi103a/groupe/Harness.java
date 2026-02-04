package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.Scanner;

import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
import org.fusesource.jansi.AnsiConsole;

import edu.brandeis.cosi.atg.player.Player;

public class Harness {

    /**
     * Prompts user for number/type of players
     * Creates Player objects
     * Creates Engine object and runs Engine.play(List<Player>);
     */
    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        ArrayList<Player> players = new ArrayList<Player>();

        Scanner scanner = new Scanner(System.in);

        // Welcome banner
        System.out.println(ansi().eraseScreen());
        System.out.println(ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(ansi().fg(YELLOW).bold().a("    Welcome to Automation: The Game").reset());
        System.out.println(ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());
        System.out.println(ansi().fg(WHITE).a("This is a 2-4 player game, with Human and AI players.\n").reset());

        // Get number of human players with validation
        int numHuman = getValidPlayerCount(scanner,
                ansi().fg(GREEN).a("How many Human players? ").fg(CYAN).a("(0-4)").reset().toString(), 0, 4);
        for (int i = 0; i < numHuman; i++) {
            System.out.println(ansi().fg(GREEN).a("  ✓ Human player " + (i + 1) + " added").reset());
            players.add(new ConsolePlayer());
        }

        // Get number of AI players with validation
        int maxAI = 4 - numHuman;
        int numAI = getValidPlayerCount(scanner,
                ansi().fg(MAGENTA).a("How many AI players? ").fg(CYAN).a("(0-" + maxAI + ")").reset().toString(), 0,
                maxAI);
        for (int i = 0; i < numAI; i++) {
            System.out.println(ansi().fg(MAGENTA).a("  ✓ AI player " + (i + 1) + " added").reset());
            players.add(new AIPlayer());
        }

        // Ensure at least 2 players total
        while (numHuman + numAI < 2) {
            System.out.println(
                    ansi().fg(YELLOW).a("\n⚠ You need at least 2 players total. Please add more players.\n").reset());
            int remaining = 4 - numHuman - numAI;
            int additionalPlayers = getValidPlayerCount(scanner,
                    ansi().fg(WHITE).a("How many additional players? ").fg(CYAN).a("(1-" + remaining + ")").reset()
                            .toString(),
                    1, remaining);

            int additionalHuman = getValidPlayerCount(scanner,
                    ansi().fg(GREEN).a("How many of these should be Human? ").fg(CYAN)
                            .a("(0-" + additionalPlayers + ")").reset().toString(),
                    0, additionalPlayers);
            for (int i = 0; i < additionalHuman; i++) {
                System.out.println(ansi().fg(GREEN).a("  ✓ Human player " + (numHuman + i + 1) + " added").reset());
                players.add(new ConsolePlayer());
            }
            numHuman += additionalHuman;

            int additionalAI = additionalPlayers - additionalHuman;
            for (int i = 0; i < additionalAI; i++) {
                System.out.println(ansi().fg(MAGENTA).a("  ✓ AI player " + (numAI + i + 1) + " added").reset());
                players.add(new AIPlayer());
            }
            numAI += additionalAI;
        }

        System.out.println(ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(ansi().fg(YELLOW).bold().a("    Starting Game...").reset());
        System.out.println(ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());

        OurEngine engine = new OurEngine(players);
        try {
            engine.play();
        } catch (Exception e) {
            System.out.println(
                    ansi().fg(RED).bold().a("\n✗ An error occurred during the game: ").a(e.getMessage()).reset());
            System.out.println();
        } finally {
            AnsiConsole.systemUninstall();
        }

    }

    /**
     * Prompts the user for a valid integer within the specified range.
     * Reprompts on invalid input (non-integers or out of range).
     * 
     * @param scanner The Scanner to read input from
     * @param prompt  The message to display to the user
     * @param min     Minimum valid value (inclusive)
     * @param max     Maximum valid value (inclusive)
     * @return A valid integer within the specified range
     */
    private static int getValidPlayerCount(Scanner scanner, String prompt, int min, int max) {
        int value = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt + " ");

            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value >= min && value <= max) {
                    valid = true;
                } else {
                    System.out.println(ansi().fg(RED)
                            .a("✗ Invalid input. Please enter a number between " + min + " and " + max + ".").reset());
                }
            } else {
                System.out.println(ansi().fg(RED).a("✗ Invalid input. Please enter a valid number.").reset());
                scanner.next(); // consume the invalid input
            }
        }

        return value;
    }
}
