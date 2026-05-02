package edu.brandeis.cosi103a.groupe.main;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayerPlus;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupe.player.ConsolePlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Handles prompting for player counts and constructing the player list. */
public class GameCreator {

    private final Scanner scanner;
    private final ConsoleInput consoleInput;

    public GameCreator(Scanner scanner) {
        this.scanner = scanner;
        this.consoleInput = new ConsoleInput(scanner);
    }

    /**
     * Prompts the user for the number of human and AI players, then constructs and returns the
     * player list.
     *
     * @return a list of at least 2 players
     */
    public List<Player> createPlayers() {
        List<Player> players = new ArrayList<>();

        // Get number of human players with validation
        int numHuman =
                getValidPlayerCount(
                        scanner,
                        ansi().fg(GREEN)
                                .a("How many Human players? ")
                                .fg(CYAN)
                                .a("(0-4)")
                                .reset()
                                .toString(),
                        0,
                        4);
        for (int i = 0; i < numHuman; i++) {
            String defaultName = "Human " + (i + 1);
            String customName =
                    getValidPlayerName(
                            scanner,
                            ansi().fg(GREEN)
                                    .a("Name for Human player " + (i + 1) + " ")
                                    .fg(CYAN)
                                    .a("(press Enter for \"" + defaultName + "\")")
                                    .reset()
                                    .toString(),
                            defaultName);
            System.out.println(
                    ansi().fg(GREEN)
                            .a("  ✓ Human player " + (i + 1) + " added as \"" + customName + "\"")
                            .reset());
            players.add(new ConsolePlayer(consoleInput, customName));
        }

        // Get number of AI players with validation
        int maxAI = 4 - numHuman;
        int numAI =
                getValidPlayerCount(
                        scanner,
                        ansi().fg(MAGENTA)
                                .a("How many AI players? ")
                                .fg(CYAN)
                                .a("(0-" + maxAI + ")")
                                .reset()
                                .toString(),
                        0,
                        maxAI);
        for (int i = 0; i < numAI; i++) {
            System.out.println(ansi().fg(MAGENTA).a("  ✓ AI player " + (i + 1) + " added").reset());
            players.add(new AutoPlayerPlus());
        }

        // Ensure at least 2 players total
        while (numHuman + numAI < 2) {
            System.out.println(
                    ansi().fg(YELLOW)
                            .a("\n⚠ You need at least 2 players total. Please add more players.\n")
                            .reset());
            int remaining = 4 - numHuman - numAI;
            int additionalPlayers =
                    getValidPlayerCount(
                            scanner,
                            ansi().fg(WHITE)
                                    .a("How many additional players? ")
                                    .fg(CYAN)
                                    .a("(1-" + remaining + ")")
                                    .reset()
                                    .toString(),
                            1,
                            remaining);

            int additionalHuman =
                    getValidPlayerCount(
                            scanner,
                            ansi().fg(GREEN)
                                    .a("How many of these should be Human? ")
                                    .fg(CYAN)
                                    .a("(0-" + additionalPlayers + ")")
                                    .reset()
                                    .toString(),
                            0,
                            additionalPlayers);
            for (int i = 0; i < additionalHuman; i++) {
                String defaultName = "Human " + (numHuman + i + 1);
                String customName =
                        getValidPlayerName(
                                scanner,
                                ansi().fg(GREEN)
                                        .a("Name for Human player " + (numHuman + i + 1) + " ")
                                        .fg(CYAN)
                                        .a("(press Enter for \"" + defaultName + "\")")
                                        .reset()
                                        .toString(),
                                defaultName);
                System.out.println(
                        ansi().fg(GREEN)
                                .a(
                                        "  ✓ Human player "
                                                + (numHuman + i + 1)
                                                + " added as \""
                                                + customName
                                                + "\"")
                                .reset());
                players.add(new ConsolePlayer(consoleInput, customName));
            }
            numHuman += additionalHuman;

            int additionalAI = additionalPlayers - additionalHuman;
            for (int i = 0; i < additionalAI; i++) {
                System.out.println(
                        ansi().fg(MAGENTA)
                                .a("  ✓ AI player " + (numAI + i + 1) + " added")
                                .reset());
                players.add(new AutoPlayerPlus());
            }
            numAI += additionalAI;
        }

        return players;
    }

    /**
     * Prompts the user for a valid integer within the specified range. Reprompts on invalid input
     * (non-integers or out of range).
     *
     * @param scanner the Scanner to read input from
     * @param prompt the message to display to the user
     * @param min minimum valid value (inclusive)
     * @param max maximum valid value (inclusive)
     * @return a valid integer within the specified range
     */
    public static int getValidPlayerCount(Scanner scanner, String prompt, int min, int max) {
        int value = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt + " ");

            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                scanner.nextLine();
                if (value >= min && value <= max) {
                    valid = true;
                } else {
                    System.out.println(
                            ansi().fg(RED)
                                    .a(
                                            "✗ Invalid input. Please enter a number between "
                                                    + min
                                                    + " and "
                                                    + max
                                                    + ".")
                                    .reset());
                }
            } else {
                System.out.println(
                        ansi().fg(RED).a("✗ Invalid input. Please enter a valid number.").reset());
                scanner.next();
            }
        }

        return value;
    }

    /**
     * Prompts the user for a player name. Empty or whitespace-only input falls back to the provided
     * default name.
     *
     * @param scanner The Scanner to read input from
     * @param prompt The message to display to the user
     * @param defaultName Name to use if the user enters an empty value
     * @return The chosen player name (trimmed), or defaultName when blank
     */
    public static String getValidPlayerName(Scanner scanner, String prompt, String defaultName) {
        System.out.print(prompt + " ");

        // If there is no next line (EOF), fall back to the default name instead of throwing.
        if (!scanner.hasNextLine()) {
            return defaultName;
        }

        String input = scanner.nextLine();
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return defaultName;
        }

        return trimmed;
    }
}
