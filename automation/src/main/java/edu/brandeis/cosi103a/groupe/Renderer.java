package edu.brandeis.cosi103a.groupe;

import static org.fusesource.jansi.Ansi.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.decisions.*;
import java.util.Set;
import java.util.HashSet;

public class Renderer {

    // Layout constants
    private static final int SCREEN_WIDTH = 76;
    private static final int COLUMN_WIDTH = 22;
    private static final int SUPPLY_GAP = 2;
    private static final int MARKER_PREFIX = 2; // "* " or " " before each card name

    /**
     * Converts a {@link Card.Type} enum value to a human-readable Title Case name.
     * Underscores are replaced with spaces (e.g. {@code CODE_REVIEW} → "Code
     * Review").
     */
    private static String displayName(Card.Type cardType) {
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
     * Returns an {@link org.fusesource.jansi.Ansi} instance set to the display
     * color for the given card category.
     */
    private static org.fusesource.jansi.Ansi colorForCategory(Card.Type.Category category) {
        switch (category) {
            case ACTION:
                return ansi().fgBrightBlue();
            case MONEY:
                return ansi().fgYellow();
            case VICTORY:
                return ansi().fgGreen();
            default:
                return ansi().fgDefault();
        }
    }

    /**
     * Pads {@code text} with trailing spaces to exactly {@code width} chars, or
     * truncates if longer.
     */
    private static String fixedWidth(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return String.format("%-" + width + "s", text);
    }

    /**
     * Prints one supply column cell: the '*' actionable marker (or a blank
     * placeholder),
     * one space, then the pile text (count + name + cost) padded to
     * {@link #COLUMN_WIDTH}.
     *
     * @param cardType   the card type being displayed
     * @param count      number of copies remaining in this pile
     * @param actionable whether this pile has an available decision (e.g.
     *                   BuyDecision)
     * @param color      the ANSI color to use for the pile text
     */
    private static void renderSupplyCell(Card.Type cardType, int count,
            boolean actionable, org.fusesource.jansi.Ansi color) {
        System.out.print(actionable ? ansi().fgRed().a("*").reset() : " ");
        System.out.print(" ");
        String pile = count + " " + displayName(cardType) + " ($" + cardType.cost() + ")";
        System.out.print(color.a(fixedWidth(pile, COLUMN_WIDTH)).reset());
    }

    /**
     * Prints a blank supply column cell of the correct width so subsequent columns
     * remain aligned when a category has fewer rows than others.
     */
    private static void renderEmptySupplyCell() {
        System.out.print(" "); // marker placeholder
        System.out.print(" "); // space
        System.out.print(fixedWidth("", COLUMN_WIDTH));
    }

    /**
     * Prints one hand column cell: the '* ' actionable marker (or ' '), then the
     * card name padded to {@link #COLUMN_WIDTH}.
     *
     * @param card            the card to display
     * @param actionableCards set of cards that have an available decision
     * @param color           the ANSI color to use for the card name
     */
    private static void renderHandCell(Card card, Set<Card> actionableCards,
            org.fusesource.jansi.Ansi color) {
        System.out.print(actionableCards.contains(card) ? ansi().fgRed().a("* ").reset() : "  ");
        System.out.print(color.a(fixedWidth(displayName(card.type()), COLUMN_WIDTH)).reset());
    }

    /**
     * Renders the current game state to the console with no decision indicators.
     * Equivalent to calling {@link #renderGameWithDecisions(GameState, List)} with
     * an empty list.
     *
     * @param state the current game state to render
     */
    public static void renderGame(GameState state) {
        renderGameWithDecisions(state, ImmutableList.of());
    }

    /**
     * Renders the current game state to console, including indicators for which
     * cards in hand and supply are actionable based on the provided decisions.
     * 
     * @param state   Current game state to render
     * @param options List of available decisions for the current player - used to
     *                determine which cards are actionable and should be marked with
     * *
     */
    public static void renderGameWithDecisions(GameState state, List<Decision> options) {
        // Build sets of actionable cards from the available decisions
        Set<Card> actionableHandCards = new HashSet<>();
        Set<Card.Type> actionableSupplyTypes = new HashSet<>();

        for (Decision decision : options) {
            if (decision instanceof PlayCardDecision) {
                actionableHandCards.add(((PlayCardDecision) decision).card());
            } else if (decision instanceof TrashCardDecision) {
                actionableHandCards.add(((TrashCardDecision) decision).card());
            } else if (decision instanceof BuyDecision) {
                actionableSupplyTypes.add(((BuyDecision) decision).cardType());
            } else if (decision instanceof GainCardDecision) {
                actionableSupplyTypes.add(((GainCardDecision) decision).cardType());
            }
        }

        // Render game state with actionable card indicators
        System.out.println(ansi().eraseScreen());
        System.out.println();

        // 1. Welcome message
        String prepostfix = " -------- ";
        String welcome = "Welcome to Automation: The Game";
        int welcomeLength = prepostfix.length() + welcome.length() + prepostfix.length();
        int welcomePadding = (SCREEN_WIDTH - welcomeLength) / 2;
        System.out.print(" ".repeat(welcomePadding));
        System.out.print(ansi().fgBrightBlue().a(prepostfix).reset());
        System.out.print(ansi().fgBrightGreen().bold().a(welcome).reset());
        System.out.println(ansi().fgBrightBlue().a(prepostfix).reset());
        System.out.println();

        String playerText = "Player: " + state.currentPlayerName();
        System.out.print(playerText);

        String phaseText = "";
        switch (state.phase()) {
            case GameState.TurnPhase.ACTION:
                phaseText = "Phase: Action";
                break;
            case GameState.TurnPhase.BUY:
                phaseText = "Phase: Buy";
                break;
            case GameState.TurnPhase.MONEY:
                phaseText = "Phase: Money";
                break;
            case GameState.TurnPhase.CLEANUP:
                phaseText = "Phase: Cleanup";
                break;
            case GameState.TurnPhase.GAIN:
                phaseText = "Phase: Gain";
                break;
        }

        int phasePosition = SCREEN_WIDTH - phaseText.length();
        int spacesNeeded = phasePosition - playerText.length();
        System.out.print(" ".repeat(Math.max(1, spacesNeeded)));

        switch (state.phase()) {
            case GameState.TurnPhase.ACTION:
                System.out.print(ansi().fgBrightBlue().a(phaseText).reset());
                break;
            case GameState.TurnPhase.BUY:
                System.out.print(ansi().fgYellow().a(phaseText).reset());
                break;
            case GameState.TurnPhase.MONEY:
                System.out.print(ansi().fgYellow().a(phaseText).reset());
                break;
            case GameState.TurnPhase.CLEANUP:
                System.out.print(ansi().fgBrightBlue().a(phaseText).reset());
                break;
            case GameState.TurnPhase.GAIN:
                System.out.print(ansi().fgGreen().a(phaseText).reset());
                break;
        }

        System.out.println();
        System.out.println();

        String availableCardsLabel = "═══ Available Cards ═══";
        int availablePadding = (SCREEN_WIDTH - availableCardsLabel.length()) / 2;
        System.out.print(" ".repeat(availablePadding));
        System.out.println(ansi().fgBrightBlue().a(availableCardsLabel).reset());
        System.out.println();

        // Prepare supply piles by category
        List<Card.Type> victoryCards = new ArrayList<>();
        List<Card.Type> treasureCards = new ArrayList<>();
        List<Card.Type> actionCards = new ArrayList<>();

        for (Card.Type cardType : state.buyableCards().getCardTypes()) {
            switch (cardType.category()) {
                case VICTORY:
                    victoryCards.add(cardType);
                    break;
                case MONEY:
                    treasureCards.add(cardType);
                    break;
                case ACTION:
                    actionCards.add(cardType);
                    break;
            }
        }

        // Sort each category by cost
        actionCards.sort((a, b) -> Integer.compare(a.cost(), b.cost()));
        treasureCards.sort((a, b) -> Integer.compare(a.cost(), b.cost()));
        victoryCards.sort((a, b) -> Integer.compare(a.cost(), b.cost()));

        // Collect played cards for the Played Cards section
        List<Card> playedCards = new ArrayList<>();
        for (Card c : state.currentPlayerHand().playedCards()) {
            playedCards.add(c);
        }

        // Print column headers. Each header is aligned to where data rows print their
        // content: [marker][space][content]. Headers for columns 2 and 3 therefore
        // need an extra MARKER_PREFIX gap in addition to SUPPLY_GAP.
        System.out.print(" "); // marker placeholder
        System.out.print(" "); // space
        System.out.print(ansi().fgBrightBlue().bold().a(fixedWidth("ACTIONS", COLUMN_WIDTH)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP + MARKER_PREFIX));
        System.out.print(ansi().fgYellow().bold().a(fixedWidth("TREASURES", COLUMN_WIDTH)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP + MARKER_PREFIX));
        System.out.print(ansi().fgGreen().bold().a(fixedWidth("VICTORIES", COLUMN_WIDTH)).reset());
        System.out.println();

        System.out.print(" "); // marker placeholder
        System.out.print(" "); // space
        System.out.println("─".repeat(COLUMN_WIDTH) + " ".repeat(SUPPLY_GAP + MARKER_PREFIX) +
                "─".repeat(COLUMN_WIDTH) + " ".repeat(SUPPLY_GAP + MARKER_PREFIX) +
                "─".repeat(COLUMN_WIDTH));

        // Display supply cards in three columns, one row per card
        int maxRows = Math.max(actionCards.size(), Math.max(treasureCards.size(), victoryCards.size()));
        for (int i = 0; i < maxRows; i++) {

            // Action column
            if (i < actionCards.size()) {
                Card.Type cardType = actionCards.get(i);
                renderSupplyCell(cardType, state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType), ansi().fgBrightBlue());
            } else {
                renderEmptySupplyCell();
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Treasure column
            if (i < treasureCards.size()) {
                Card.Type cardType = treasureCards.get(i);
                renderSupplyCell(cardType, state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType), ansi().fgYellow());
            } else {
                renderEmptySupplyCell();
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Victory column (BUG renders in magenta; all others in green)
            if (i < victoryCards.size()) {
                Card.Type cardType = victoryCards.get(i);
                org.fusesource.jansi.Ansi color = (cardType == Card.Type.BUG)
                        ? ansi().fgMagenta()
                        : ansi().fgGreen();
                renderSupplyCell(cardType, state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType), color);
            } else {
                renderEmptySupplyCell();
            }

            System.out.println();
        }

        System.out.println();
        System.out.println();

        // Played cards section
        String playedCardsLabel = "═══ Played Cards ═══";
        int playedPadding = (SCREEN_WIDTH - playedCardsLabel.length()) / 2;
        System.out.print(" ".repeat(playedPadding));
        System.out.println(ansi().fgBrightBlue().a(playedCardsLabel).reset());
        System.out.println();
        if (playedCards.isEmpty()) {
            System.out.print(" ".repeat(playedPadding));
            System.out.println("(none)");
        } else {
            for (Card card : playedCards) {
                String cardText = "• " + displayName(card.type());
                int cardPadding = (SCREEN_WIDTH - cardText.length()) / 2;
                System.out.print(" ".repeat(cardPadding));
                System.out.println(colorForCategory(card.getCategory()).a(cardText).reset());
            }
        }

        System.out.println();
        System.out.println();
        System.out.println();

        // Hand display: three columns (Actions | Treasures | Victories) with * marker
        // prefix.
        // Total width = 3 * (MARKER_PREFIX + COLUMN_WIDTH) + 2 * SUPPLY_GAP, centred on
        // screen.
        String yourHandLabel = "═══ Your Hand ═══";
        int handPadding = (SCREEN_WIDTH - yourHandLabel.length()) / 2;
        System.out.print(" ".repeat(handPadding));
        System.out.println(ansi().fgBrightBlue().a(yourHandLabel).reset());
        System.out.println();
        List<Card> actionsInHand = new ArrayList<>();
        List<Card> treasuresInHand = new ArrayList<>();
        List<Card> victoriesInHand = new ArrayList<>();

        for (Card card : state.currentPlayerHand().unplayedCards()) {
            switch (card.getCategory()) {
                case ACTION:
                    actionsInHand.add(card);
                    break;
                case MONEY:
                    treasuresInHand.add(card);
                    break;
                case VICTORY:
                    victoriesInHand.add(card);
                    break;
            }
        }

        int maxHandRows = Math.max(actionsInHand.size(), Math.max(treasuresInHand.size(), victoriesInHand.size()));
        if (maxHandRows == 0)
            maxHandRows = 1; // Show at least the (none) row

        int totalHandWidth = (MARKER_PREFIX + COLUMN_WIDTH) * 3 + 2 * SUPPLY_GAP;
        int handLeftPadding = Math.max(0, (SCREEN_WIDTH - totalHandWidth) / 2);

        for (int i = 0; i < maxHandRows; i++) {
            // Add left padding to center the hand
            System.out.print(" ".repeat(handLeftPadding));

            // Action column
            if (i < actionsInHand.size()) {
                renderHandCell(actionsInHand.get(i), actionableHandCards, ansi().fgBrightBlue());
            } else if (i == 0) {
                System.out.print("  ");
                System.out.print(ansi().fgBrightBlue().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print("  ");
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Treasure column
            if (i < treasuresInHand.size()) {
                renderHandCell(treasuresInHand.get(i), actionableHandCards, ansi().fgYellow());
            } else if (i == 0) {
                System.out.print("  ");
                System.out.print(ansi().fgYellow().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print("  ");
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Victory column
            if (i < victoriesInHand.size()) {
                renderHandCell(victoriesInHand.get(i), actionableHandCards, ansi().fgGreen());
            } else if (i == 0) {
                System.out.print("  ");
                System.out.print(ansi().fgGreen().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print("  ");
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }

            System.out.println();
        }

        System.out.println();
        System.out.println();

        // Status bar
        int spaces = 2;
        String statusText = "Actions: " + state.availableActions() + "  ".repeat(spaces) + "|" + " ".repeat(spaces) +
                "Buys: " + state.availableBuys() + "  ".repeat(spaces) + "|" + " ".repeat(spaces) +
                "Money: " + state.spendableMoney();
        int statusPadding = Math.max(0, (SCREEN_WIDTH - statusText.length()) / 2);
        System.out.print(" ".repeat(statusPadding));

        System.out.print(ansi().fgBrightBlue().a("Actions: " + state.availableActions()).reset());
        System.out.print(" ".repeat(spaces) + "|" + " ".repeat(spaces));
        System.out.print(ansi().fgGreen().a("Buys: " + state.availableBuys()).reset());
        System.out.print(" ".repeat(spaces) + "|" + " ".repeat(spaces));
        System.out.print(ansi().fgYellow().a("Money: " + state.spendableMoney()).reset());

        System.out.println("\n");

    }

    /**
     * Renders the current game state to console with additional debug information
     * showing player decks and discards.
     *
     * @param debugState Container with game state and internal player deck/discard
     *                   information
     */
    public static void renderGameDebugMode(DebugGameState debugState) {
        renderGame(debugState.state());
        System.out.println("=== DEBUG MODE: Player Decks ===");
        for (Map.Entry<Player, List<Card>> entry : debugState.decks().entrySet()) {
            Player player = entry.getKey();
            List<Card> deck = entry.getValue();
            System.out.println(player.getName() + "'s deck:");
            for (Card card : deck) {
                System.out.println("  - " + displayName(card.type()));
            }
        }
        System.out.println("=== DEBUG MODE: Player Discards ===");
        for (Map.Entry<Player, List<Card>> entry : debugState.discards().entrySet()) {
            Player player = entry.getKey();
            List<Card> discard = entry.getValue();
            System.out.println(player.getName() + "'s discard pile:");
            for (Card card : discard) {
                System.out.println("  - " + displayName(card.type()));
            }
        }
    }

    /**
     * Test main method
     * Creates a sample GameState and calls renderGame on it
     */
    public static void main(String[] args) {
        // For demonstration, we will create a mock or dummy implementation here.
        String currentPlayerName = "Player 1";
        Hand currentPlayerHand = new Hand(
                ImmutableList.of(
                        new Card(Type.BITCOIN, 6),
                        new Card(Type.BITCOIN, 7),
                        new Card(Type.BITCOIN, 8)),
                ImmutableList.of(
                        new Card(Type.BITCOIN, 1),
                        new Card(Type.BITCOIN, 2),
                        new Card(Type.BITCOIN, 3),
                        new Card(Type.METHOD, 4),
                        new Card(Type.METHOD, 5),
                        new Card(Type.REFACTOR, 9),
                        new Card(Type.EVERGREEN_TEST, 10)));
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int availableActions = 1;
        int spendableMoney = 3;
        int availableBuys = 1;
        CardStacks buyableCards = new CardStacks(
                ImmutableMap.of(
                        Type.BUG, 12,
                        Type.METHOD, 12,
                        Type.MODULE, 12,
                        Type.FRAMEWORK, 12,
                        Type.BITCOIN, 50,
                        Type.ETHEREUM, 40,
                        Type.DOGECOIN, 30,
                        Type.CODE_REVIEW, 10,
                        Type.EVERGREEN_TEST, 10,
                        Type.REFACTOR, 10));

        GameState dummyState = new GameState(
                currentPlayerName,
                currentPlayerHand,
                phase,
                availableActions,
                spendableMoney,
                availableBuys,
                buyableCards);
        // renderGame(dummyState);

        ImmutableList<Decision> playActionDecisions = ImmutableList.of(
                new BuyDecision(Type.CODE_REVIEW),
                new BuyDecision(Type.REFACTOR),
                new BuyDecision(Type.BITCOIN),
                new BuyDecision(Type.ETHEREUM),
                new BuyDecision(Type.METHOD),
                new PlayCardDecision(new Card(Type.REFACTOR, 9)),
                new PlayCardDecision(new Card(Type.EVERGREEN_TEST, 10)),
                new PlayCardDecision(new Card(Type.BITCOIN, 1)),
                new PlayCardDecision(new Card(Type.BITCOIN, 2)),
                new PlayCardDecision(new Card(Type.BITCOIN, 3)),
                new EndPhaseDecision(GameState.TurnPhase.ACTION));
        renderGameWithDecisions(dummyState, playActionDecisions);
    }

    /**
     *
     * Prints the final game results in a readable format, showing each player's
     * score
     * and the cards in their discard pile at the end of the game.
     *
     * @param result
     */
    public static void printGameResult(GameResult result) {
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("                 Game Over!                  ");
        System.out.println("═══════════════════════════════════════════════\n");
        for (PlayerResult pr : result.playerResults()) {
            System.out.println(pr.playerName() + " scored " + pr.score()
                    + " points with the following cards in their discard pile:");
            for (Card card : pr.endingDeck()) {
                System.out.println("  - " + card);
            }
            System.out.println();
        }
    }

}
