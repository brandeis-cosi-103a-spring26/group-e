package edu.brandeis.cosi103a.groupe.main;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.cosi21.DecisionResult;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupe.player.ConsolePlayer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import org.fusesource.jansi.AnsiConsole;

public class Harness {

    private static Scanner scanner = new Scanner(System.in);

    /** Static map of card descriptions for display purposes. */
    private static final Map<Card.Type, String> CARD_DESCRIPTIONS =
            ImmutableMap.<Card.Type, String>builder()
                    // Victory Cards
                    .put(
                            Card.Type.BUG,
                            "A victory card worth -1 automation points.\n\n"
                                    + "This card is never playable, but subtracts 1 automation"
                                    + " point when the game ends.")
                    .put(
                            Card.Type.METHOD,
                            "A victory card worth 1 automation point.\n\n"
                                    + "This card is never playable, but grants 1 automation point"
                                    + " when the game ends.")
                    .put(
                            Card.Type.MODULE,
                            "A victory card worth 3 automation points.\n\n"
                                    + "This card is never playable, but grants 3 automation points"
                                    + " when the game ends.")
                    .put(
                            Card.Type.FRAMEWORK,
                            "A victory card worth 6 automation points.\n\n"
                                    + "This card is never playable, but grants 6 automation points"
                                    + " when the game ends.")
                    // Money Cards
                    .put(
                            Card.Type.BITCOIN,
                            "A money card worth 1 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 1 spendable money for the turn on which it was"
                                    + " played.")
                    .put(
                            Card.Type.ETHEREUM,
                            "A money card worth 2 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 2 spendable money for the turn on which it was"
                                    + " played.")
                    .put(
                            Card.Type.DOGECOIN,
                            "A money card worth 3 money.\n\n"
                                    + "This card is playable during the MONEY phase. When played,"
                                    + " it grants 3 spendable money for the turn on which it was"
                                    + " played.")
                    // Action Cards
                    .put(
                            Card.Type.REFACTOR,
                            "Trash a card from your hand; Gain a card costing up to 2 more than"
                                    + " the trashed card.\n\n"
                                    + "Trashing a card removes it from the player's deck entirely."
                                    + " When this card is played, the player will be prompted with"
                                    + " TrashCardDecisions for each card in their hand. Trashing is"
                                    + " not optional, so the list of decisions will not include an"
                                    + " EndPhaseDecision. After the player makes a trash decision,"
                                    + " they will be prompted with GainCardDecisions representing"
                                    + " the cards they can possibly gain - every card that costs up"
                                    + " to 2 more than the trashed card. Gaining a card is also not"
                                    + " optional. Gained cards are placed into the player's discard"
                                    + " pile.")
                    .put(
                            Card.Type.CODE_REVIEW,
                            "+1 Card, +2 Actions.\n\n"
                                    + "When this card is played, the player immediately draws a"
                                    + " card, and is granted two additional actions for this turn.")
                    .put(
                            Card.Type.EVERGREEN_TEST,
                            "(Attack card) +2 Cards; Each other player gains a Bug.\n\n"
                                    + "When this card is played, the engine immediately adds a BUG"
                                    + " to the discard pile of each other player.")
                    .build();

    /**
     * Gets the description for a given card type.
     *
     * @param cardType The card type to get the description for
     * @return The card description, or a default message if not found
     */
    public static String getCardDescription(Card.Type cardType) {
        return CARD_DESCRIPTIONS.getOrDefault(cardType, "No description available.");
    }

    /**
     * Prompts user for number/type of players, creates Engine, and runs the game.
     */
    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        ArrayList<Player> players = new ArrayList<>();

        // Welcome banner
        System.out.println(ansi().eraseScreen());
        System.out.println(
                ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(
                ansi().fg(YELLOW).bold().a("    Welcome to Automation: The Game").reset());
        System.out.println(
                ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());
        System.out.println(
                ansi().fg(WHITE)
                        .a("This is a 2-4 player game, with Human and AI players.\n")
                        .reset());

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
            System.out.println(
                    ansi().fg(GREEN).a("  ✓ Human player " + (i + 1) + " added").reset());
            players.add(new ConsolePlayer());
        }

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
            players.add(new BigMoneyPlayer());
        }

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
                System.out.println(
                        ansi().fg(GREEN)
                                .a("  ✓ Human player " + (numHuman + i + 1) + " added")
                                .reset());
                players.add(new ConsolePlayer());
            }
            numHuman += additionalHuman;

            int additionalAI = additionalPlayers - additionalHuman;
            for (int i = 0; i < additionalAI; i++) {
                System.out.println(
                        ansi().fg(MAGENTA)
                                .a("  ✓ AI player " + (numAI + i + 1) + " added")
                                .reset());
                players.add(new BigMoneyPlayer());
            }
            numAI += additionalAI;
        }

        System.out.println(
                ansi().fg(CYAN).a("\n═══════════════════════════════════════════════").reset());
        System.out.println(ansi().fg(YELLOW).bold().a("    Starting Game...").reset());
        System.out.println(
                ansi().fg(CYAN).a("═══════════════════════════════════════════════\n").reset());

        System.out.println(
                ansi().fg(CYAN).a("── How to Play ─────────────────────────────────").reset());
        System.out.println(
                ansi().fg(RED)
                        .a("  *")
                        .fg(WHITE)
                        .a(" indicates a card that can be acted upon")
                        .reset());
        System.out.println(ansi().fg(WHITE).a("    (played, bought, gained, or trashed)").reset());
        System.out.println();
        System.out.println(
                ansi().fg(YELLOW)
                        .a("  ❯")
                        .fg(WHITE)
                        .a(" To select a card, type its name or any unique")
                        .reset());
        System.out.println(
                ansi().fg(WHITE)
                        .a("    substring  (e.g. type ")
                        .fg(CYAN)
                        .a("bit")
                        .fg(WHITE)
                        .a(" for Bitcoin)")
                        .reset());
        System.out.println(
                ansi().fg(CYAN).a("────────────────────────────────────────────────\n").reset());

        System.out.print(ansi().fg(WHITE).a("Press enter to start the game...").reset());
        scanner.nextLine();

        OurEngine engine = new OurEngine(players);
        for (Player p : players) {
            if (p instanceof ConsolePlayer) {
                ((ConsolePlayer) p).setTurnLogs(engine.getTurnLogs());
                ((ConsolePlayer) p).setNumPlayers(engine.getNumPlayers());
            }
        }
        try {
            engine.play();
        } catch (Exception e) {
            System.out.println(
                    ansi().fg(RED)
                            .bold()
                            .a("\n✗ An error occurred during the game: ")
                            .a(e.getMessage())
                            .reset());
            e.printStackTrace();
            System.out.println();
        } finally {
            AnsiConsole.systemUninstall();
            scanner.close();
        }
    }

    /**
     * Prompts the user for a valid integer within the specified range.
     *
     * @param scanner The Scanner to read input from
     * @param prompt The message to display to the user
     * @param min Minimum valid value (inclusive)
     * @param max Maximum valid value (inclusive)
     * @return A valid integer within the specified range
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
     * Delegates to {@link ConsoleIO#getDecisionFromConsole} for backward compatibility with tests
     * that call Harness directly.
     */
    public static DecisionResult getDecisionFromConsole(ImmutableList<Decision> options) {
        return ConsoleIO.getDecisionFromConsole(options);
    }

    /**
     * Delegates to {@link ConsoleIO#getDecisionFromConsole} for backward compatibility with tests
     * that call Harness directly.
     */
    public static DecisionResult getDecisionFromConsole(
            ImmutableList<Decision> options, Scanner inputScanner) {
        return ConsoleIO.getDecisionFromConsole(options, inputScanner);
    }

    /**
     * Delegates to {@link ConsoleIO#getDecisionFromConsole} for backward compatibility with tests
     * that call Harness directly.
     */
    public static DecisionResult getDecisionFromConsole(
            edu.brandeis.cosi.atg.state.GameState state, ImmutableList<Decision> options) {
        return ConsoleIO.getDecisionFromConsole(state, options);
    }

    /**
     * Delegates to {@link ConsoleIO#getDecisionFromConsole} for backward compatibility with tests
     * that call Harness directly.
     */
    public static DecisionResult getDecisionFromConsole(
            edu.brandeis.cosi.atg.state.GameState state,
            ImmutableList<Decision> options,
            Scanner inputScanner) {
        return ConsoleIO.getDecisionFromConsole(state, options, inputScanner);
    }
}
