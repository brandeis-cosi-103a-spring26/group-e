package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.Scanner;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
import org.fusesource.jansi.AnsiConsole;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;

public class Harness {

    private static Scanner scanner = new Scanner(System.in);

    /**
     * Prompts user for number/type of players
     * Creates Player objects
     * Creates Engine object and runs Engine.play(List<Player>);
     */
    public static void main(String[] args) {

        // Uncomment to test:
        // ImmutableList<Decision> testOptions = ImmutableList.of(
        // new EndPhaseDecision(TurnPhase.ACTION),
        // new PlayCardDecision(new Card(Card.Type.REFACTOR, 0)),
        // new PlayCardDecision(new Card(Card.Type.REFACTOR, 1)),
        // new PlayCardDecision(new Card(Card.Type.REFACTOR, 2))
        // );
        // Decision chosen = getDecisionFromConsole(testOptions);
        // System.out.println("You chose: " + chosen.getDescription());

        // ImmutableList<Decision> testOptions2 = ImmutableList.of(
        // new EndPhaseDecision(TurnPhase.ACTION)
        // );
        // Decision chosen2 = getDecisionFromConsole(testOptions2);
        // System.out.println("You chose: " + chosen2.getDescription());

        AnsiConsole.systemInstall();

        ArrayList<Player> players = new ArrayList<>();

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
            e.printStackTrace();
            System.out.println();
        } finally {
            AnsiConsole.systemUninstall();
            scanner.close();
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
    static int getValidPlayerCount(Scanner scanner, String prompt, int min, int max) {
        int value = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt + " ");

            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                scanner.nextLine(); // consume the rest of the line (including newline) Copilot Suggestion
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

    /**
     * Converts a Card.Type to its display name (e.g., EVERGREEN_TEST -> "Evergreen Test")
     */
    private static String getDisplayName(Card.Type cardType) {
        String raw = cardType.toString().toLowerCase().replace('_', ' ');
        String[] words = raw.split(" ");
        StringBuilder display = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            display.append(Character.toUpperCase(word.charAt(0)));
            display.append(word.substring(1));
            display.append(" ");
        }
        return display.toString().trim();
    }

    /**
     * Normalizes a user input string for card name matching (case-insensitive, handles spaces and underscores)
     */
    private static String normalizeCardName(String input) {
        return input.trim().toLowerCase().replace('_', ' ').replace("  ", " ");
    }

    /**
     * Finds all card types that match the given input string (exact match or substring match)
     * Returns a list of matching Card.Type values
     */
    private static java.util.List<Card.Type> findMatchingCardTypes(String input) {
        String normalizedInput = normalizeCardName(input);
        java.util.List<Card.Type> matches = new java.util.ArrayList<>();
        
        for (Card.Type type : Card.Type.values()) {
            String displayName = getDisplayName(type).toLowerCase();
            if (displayName.equals(normalizedInput) || displayName.contains(normalizedInput)) {
                matches.add(type);
            }
        }
        
        return matches;
    }

    /**
     * Attempts to match user input to a card in a decision.
     * Returns true only if the input uniquely identifies this card type among all Card.Type values.
     */
    private static boolean matchesCard(String input, Card card, java.util.List<Card.Type> allMatches) {
        // Only match if there's exactly one match across all card types and it's this card
        return allMatches.size() == 1 && allMatches.get(0) == card.type();
    }

    /**
     * Attempts to match user input to a card type in a decision.
     * Returns true only if the input uniquely identifies this card type among all Card.Type values.
     */
    private static boolean matchesCardType(String input, Card.Type cardType, java.util.List<Card.Type> allMatches) {
        // Only match if there's exactly one match across all card types and it's this type
        return allMatches.size() == 1 && allMatches.get(0) == cardType;
    }

    /**
     * Shows a description/manual for a card type
     */
    private static void showCardDescription(String cardName) {
        // Try to find the card type using the same matching logic
        java.util.List<Card.Type> matches = findMatchingCardTypes(cardName);
        
        if (matches.isEmpty()) {
            System.out.println(ansi().fg(RED).a("✗ Card not found: " + cardName).reset());
        } else if (matches.size() > 1) {
            System.out.print(ansi().fg(RED).a("✗ Multiple cards match '" + cardName + "': ").reset());
            for (int i = 0; i < matches.size(); i++) {
                System.out.print(ansi().fg(YELLOW).a(getDisplayName(matches.get(i))).reset());
                if (i < matches.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(ansi().fg(RED).a(". Please be more specific.").reset());
        } else {
            Card.Type foundType = matches.get(0);
            Card card = new Card(foundType, 0);
            System.out.println(ansi().fg(CYAN).a("\n═══ " + getDisplayName(foundType) + " ═══").reset());
            System.out.println(ansi().fg(YELLOW).a("Cost: $" + card.cost()).reset());
            System.out.println(ansi().fg(WHITE).a("Category: " + foundType.category()).reset());
            System.out.println(ansi().fg(WHITE).a("Description: " + card.description()).reset());
            System.out.println();
        }
    }

    /**
     * Prompts the user to select a {@link Decision} from the provided options.
     * If the only available option is an {@link EndPhaseDecision}, the user is
     * prompted to press enter to continue and that decision is returned.
     *
     * @param options an immutable list of available {@link Decision} options for
     *                the user
     * @return the {@link Decision} chosen by the user from the provided options
     */
    public static Decision getDecisionFromConsole(ImmutableList<Decision> options) {
        return getDecisionFromConsole(options, scanner);
    }

    /**
     * Prompts the user to select a {@link Decision} from the provided options using the specified Scanner.
     * If the only available option is an {@link EndPhaseDecision}, the user is
     * prompted to press enter to continue and that decision is returned.
     *
     * @param options an immutable list of available {@link Decision} options for the user
     * @param inputScanner the Scanner to use for reading user input
     * @return the {@link Decision} chosen by the user from the provided options
     */
    static Decision getDecisionFromConsole(ImmutableList<Decision> options, Scanner inputScanner) {

        // Defensively ensure we have at least one option; otherwise, we could loop
        // forever.
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("getDecisionFromConsole requires at least one Decision option.");
        }

        // If the only option is EndPhaseDecision, prompt player to press enter to
        // continue
        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            System.out.print(ansi().fg(WHITE).a("No choices available. Press enter to continue...").reset());
            inputScanner.nextLine(); // Wait for enter key
            return options.get(0);
        }

        // // Print available options
        // System.out.println(ansi().fg(CYAN).a("\nAvailable options:").reset());
        // for (int i = 0; i < options.size(); i++) {
        //     System.out.println(ansi().fg(YELLOW).a("  " + (i + 1) + ". " + options.get(i).getDescription()).reset());
        // }

        // Prompt user for input until a valid choice is made
        Decision choice = null;
        while (choice == null) {
            System.out.print(ansi().fg(GREEN).a("Enter card name, E to end phase, or 'man <cardname>' for info: ").reset());
            String input = inputScanner.nextLine().trim();
            
            // Handle empty input
            if (input.isEmpty()) {
                System.out.println(ansi().fg(RED).a("✗ Invalid input. Please enter a card name, E, or 'man <cardname>'.").reset());
                continue;
            }
            
            // Handle "man cardname" for card descriptions
            if (input.toLowerCase().startsWith("man ")) {
                String cardName = input.substring(4).trim();
                if (!cardName.isEmpty()) {
                    showCardDescription(cardName);
                } else {
                    System.out.println(ansi().fg(RED).a("✗ Please specify a card name after 'man'.").reset());
                }
                continue;
            }
            
            // Handle "E" for end phase
            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("end")) {
                // Find EndPhaseDecision in options
                for (Decision option : options) {
                    if (option instanceof EndPhaseDecision) {
                        choice = option;
                        break;
                    }
                }
                if (choice == null) {
                    System.out.println(ansi().fg(RED).a("✗ Cannot end phase right now.").reset());
                }
                continue;
            }
            
            // First, find all card types that match the input
            java.util.List<Card.Type> allMatches = findMatchingCardTypes(input);
            
            // Check if the input uniquely identifies a card
            if (allMatches.isEmpty()) {
                System.out.println(ansi().fg(RED).a("✗ Card not found matching: '" + input + "'. Try again.").reset());
                continue;
            } else if (allMatches.size() > 1) {
                // Multiple cards match - show which ones
                System.out.print(ansi().fg(RED).a("✗ Invalid - multiple cards match '" + input + "': ").reset());
                for (int i = 0; i < allMatches.size(); i++) {
                    System.out.print(ansi().fg(YELLOW).a(getDisplayName(allMatches.get(i))).reset());
                    if (i < allMatches.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println(ansi().fg(RED).a(". Please be more specific.").reset());
                continue;
            }
            
            // Exactly one card matches - try to find it in the available decisions
            for (Decision option : options) {
                if (option instanceof PlayCardDecision) {
                    Card card = ((PlayCardDecision) option).card();
                    if (matchesCard(input, card, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof TrashCardDecision) {
                    Card card = ((TrashCardDecision) option).card();
                    if (matchesCard(input, card, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof BuyDecision) {
                    Card.Type cardType = ((BuyDecision) option).cardType();
                    if (matchesCardType(input, cardType, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof GainCardDecision) {
                    Card.Type cardType = ((GainCardDecision) option).cardType();
                    if (matchesCardType(input, cardType, allMatches)) {
                        choice = option;
                        break;
                    }
                }
            }
            
            if (choice == null) {
                System.out.println(ansi().fg(RED).a("✗ Invalid choice: Card '" + getDisplayName(allMatches.get(0)) + "' is not available right now.").reset());
            }
        }

        return choice;
    }

}
