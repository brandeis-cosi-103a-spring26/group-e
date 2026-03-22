package edu.brandeis.cosi103a.groupe.main;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.DecisionResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Handles reading player decisions from stdin and displaying card information. */
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts the user to select a {@link Decision} from the provided options. If the only
     * available option is an {@link EndPhaseDecision}, the user is prompted to press enter to
     * continue and that decision is returned.
     *
     * @param state the current game state (may be null)
     * @param options an immutable list of available {@link Decision} options for the user
     * @return a {@link DecisionResult} containing the chosen decision and whether auto-play was
     *     requested
     */
    public DecisionResult getDecisionFromConsole(
            GameState state, ImmutableList<Decision> options) {

        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException(
                    "getDecisionFromConsole requires at least one Decision option.");
        }

        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            System.out.print(
                    ansi().fg(WHITE).a("No choices available. Press enter to continue...").reset());
            scanner.nextLine();
            return new DecisionResult(options.get(0), false);
        }

        boolean autoPlayRequested = false;

        boolean isMoneyPhase = (state != null && state.phase() == GameState.TurnPhase.MONEY);

        Decision choice = null;
        while (choice == null) {
            if (isMoneyPhase) {
                System.out.print(
                        ansi().fg(GREEN)
                                .a(
                                        "Enter card name, A to play all treasures, E to end phase,"
                                                + " or 'man <cardname>' for info: ")
                                .reset());
            } else {
                System.out.print(
                        ansi().fg(GREEN)
                                .a(
                                        "Enter card name, E to end phase, or 'man <cardname>' for"
                                                + " info: ")
                                .reset());
            }
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(
                        ansi().fg(RED)
                                .a(
                                        "✗ Invalid input. Please enter a card name, E, or 'man"
                                                + " <cardname>'.")
                                .reset());
                continue;
            }

            if (input.toLowerCase().startsWith("man ")) {
                String cardName = input.substring(4).trim();
                if (!cardName.isEmpty()) {
                    showCardDescription(cardName);
                } else {
                    System.out.println(
                            ansi().fg(RED).a("✗ Please specify a card name after 'man'.").reset());
                }
                continue;
            }

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("end")) {
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

            if (input.equalsIgnoreCase("a") || input.equalsIgnoreCase("auto")) {
                if (!isMoneyPhase) {
                    System.out.println(
                            ansi().fg(RED)
                                    .a("✗ Auto-play is only available during the Money phase.")
                                    .reset());
                    continue;
                }
                for (Decision option : options) {
                    if (option instanceof PlayCardDecision) {
                        Card card = ((PlayCardDecision) option).card();
                        if (card.category() == Card.Type.Category.MONEY) {
                            choice = option;
                            autoPlayRequested = true;
                            break;
                        }
                    }
                }
                if (choice == null) {
                    System.out.println(
                            ansi().fg(RED).a("✗ No treasure cards available to play.").reset());
                }
                continue;
            }

            List<Card.Type> allMatches = findMatchingCardTypes(input);

            if (allMatches.isEmpty()) {
                System.out.println(
                        ansi().fg(RED)
                                .a("✗ Card not found matching: '" + input + "'. Try again.")
                                .reset());
                continue;
            } else if (allMatches.size() > 1) {
                System.out.print(
                        ansi().fg(RED)
                                .a("✗ Invalid - multiple cards match '" + input + "': ")
                                .reset());
                for (int i = 0; i < allMatches.size(); i++) {
                    System.out.print(
                            ansi().fg(YELLOW).a(getDisplayName(allMatches.get(i))).reset());
                    if (i < allMatches.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println(ansi().fg(RED).a(". Please be more specific.").reset());
                continue;
            }

            for (Decision option : options) {
                if (option instanceof PlayCardDecision) {
                    Card card = ((PlayCardDecision) option).card();
                    if (matchesCard(card, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof TrashCardDecision) {
                    Card card = ((TrashCardDecision) option).card();
                    if (matchesCard(card, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof BuyDecision) {
                    Card.Type cardType = ((BuyDecision) option).cardType();
                    if (matchesCardType(cardType, allMatches)) {
                        choice = option;
                        break;
                    }
                } else if (option instanceof GainCardDecision) {
                    Card.Type cardType = ((GainCardDecision) option).cardType();
                    if (matchesCardType(cardType, allMatches)) {
                        choice = option;
                        break;
                    }
                }
            }

            if (choice == null) {
                System.out.println(
                        ansi().fg(RED)
                                .a(
                                        "✗ Invalid choice: Card '"
                                                + getDisplayName(allMatches.get(0))
                                                + "' is not available right now.")
                                .reset());
            }
        }

        return new DecisionResult(choice, autoPlayRequested);
    }

    private static String getDisplayName(Card.Type cardType) {
        return Renderer.displayName(cardType);
    }

    private static String normalizeCardName(String input) {
        return input.trim().toLowerCase().replace('_', ' ').replaceAll("\\s+", " ");
    }

    private static List<Card.Type> findMatchingCardTypes(String input) {
        String normalizedInput = normalizeCardName(input);
        List<Card.Type> matches = new ArrayList<>();
        for (Card.Type type : Card.Type.values()) {
            String displayName = getDisplayName(type).toLowerCase();
            if (displayName.equals(normalizedInput) || displayName.contains(normalizedInput)) {
                matches.add(type);
            }
        }
        return matches;
    }

    private static boolean matchesCard(Card card, List<Card.Type> allMatches) {
        return allMatches.size() == 1 && allMatches.get(0) == card.type();
    }

    private static boolean matchesCardType(Card.Type cardType, List<Card.Type> allMatches) {
        return allMatches.size() == 1 && allMatches.get(0) == cardType;
    }

    private static void showCardDescription(String cardName) {
        List<Card.Type> matches = findMatchingCardTypes(cardName);
        if (matches.isEmpty()) {
            System.out.println(ansi().fg(RED).a("✗ Card not found: " + cardName).reset());
        } else if (matches.size() > 1) {
            System.out.print(
                    ansi().fg(RED).a("✗ Multiple cards match '" + cardName + "': ").reset());
            for (int i = 0; i < matches.size(); i++) {
                System.out.print(ansi().fg(YELLOW).a(getDisplayName(matches.get(i))).reset());
                if (i < matches.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(ansi().fg(RED).a(". Please be more specific.").reset());
        } else {
            Card.Type foundType = matches.get(0);
            System.out.println(
                    ansi().fg(CYAN).a("\n═══ " + getDisplayName(foundType) + " ═══").reset());
            System.out.println(ansi().fg(YELLOW).a("Cost: $" + foundType.cost()).reset());
            System.out.println(ansi().fg(WHITE).a("Category: " + foundType.category()).reset());
            System.out.println(
                    ansi().fg(WHITE)
                            .a("Description: " + CardDescriptions.get(foundType))
                            .reset());
            System.out.println();
        }
    }
}
