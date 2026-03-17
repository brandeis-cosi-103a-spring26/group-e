package edu.brandeis.cosi103a.groupe.main;

import static org.fusesource.jansi.Ansi.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.DebugGameState;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Renderer {

    // Layout constants
    private static final int SCREEN_WIDTH = 76;
    private static final int COLUMN_WIDTH = 22;
    private static final int SUPPLY_GAP = 2;
    private static final int MARKER_PREFIX = 2; // "* " or " " before each card name
    private static final int LOG_PANEL_WIDTH = 38;

    /** Matches ANSI CSI escape sequences (e.g. color codes) for visual-length calculation. */
    private static final Pattern ANSI_ESCAPE = Pattern.compile("\\033\\[[0-9;]*[A-Za-z]");

    /**
     * Converts a {@link Card.Type} enum value to a human-readable Title Case name. Underscores are
     * replaced with spaces (e.g. {@code CODE_REVIEW} → "Code Review").
     */
    public static String displayName(Card.Type cardType) {
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
     * Returns an {@link org.fusesource.jansi.Ansi} instance set to the display color for the given
     * card category.
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
     * Pads {@code text} with trailing spaces to exactly {@code width} chars, or truncates if
     * longer.
     */
    private static String fixedWidth(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return String.format("%-" + width + "s", text);
    }

    /**
     * Prints one supply column cell: the '*' actionable marker (or a blank placeholder), one space,
     * then the pile text (count + name + cost) padded to {@link #COLUMN_WIDTH}.
     *
     * @param cardType the card type being displayed
     * @param count number of copies remaining in this pile
     * @param actionable whether this pile has an available decision (e.g. BuyDecision)
     * @param color the ANSI color to use for the pile text
     */
    private static void renderSupplyCell(
            Card.Type cardType, int count, boolean actionable, org.fusesource.jansi.Ansi color) {
        System.out.print(actionable ? ansi().fgRed().a("*").reset() : " ");
        System.out.print(" ");
        String pile = count + " " + displayName(cardType) + " ($" + cardType.cost() + ")";
        System.out.print(color.a(fixedWidth(pile, COLUMN_WIDTH)).reset());
    }

    /**
     * Prints a blank supply column cell of the correct width so subsequent columns remain aligned
     * when a category has fewer rows than others.
     */
    private static void renderEmptySupplyCell() {
        System.out.print(" "); // marker placeholder
        System.out.print(" "); // space
        System.out.print(fixedWidth("", COLUMN_WIDTH));
    }

    /**
     * Prints one hand column cell: the '* ' actionable marker (or ' '), then the card name padded
     * to {@link #COLUMN_WIDTH}.
     *
     * @param card the card to display
     * @param actionableCards set of cards that have an available decision
     * @param color the ANSI color to use for the card name
     */
    private static void renderHandCell(
            Card card, Set<Card> actionableCards, org.fusesource.jansi.Ansi color) {
        System.out.print(actionableCards.contains(card) ? ansi().fgRed().a("* ").reset() : "  ");
        System.out.print(color.a(fixedWidth(displayName(card.type()), COLUMN_WIDTH)).reset());
    }

    /**
     * Renders the current game state to the console with no decision indicators. Equivalent to
     * calling {@link #renderGameWithDecisions(GameState, List)} with an empty list.
     *
     * @param state the current game state to render
     */
    public static void renderGame(GameState state) {
        renderGameWithDecisions(state, ImmutableList.of());
    }

    /**
     * Renders the current game state to console, including indicators for which cards in hand and
     * supply are actionable based on the provided decisions.
     *
     * @param state Current game state to render
     * @param options List of available decisions for the current player - used to determine which
     *     cards are actionable and should be marked with *
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
        System.out.print(
                ansi().fgBrightBlue().bold().a(fixedWidth("ACTIONS", COLUMN_WIDTH)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP + MARKER_PREFIX));
        System.out.print(ansi().fgYellow().bold().a(fixedWidth("TREASURES", COLUMN_WIDTH)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP + MARKER_PREFIX));
        System.out.print(ansi().fgGreen().bold().a(fixedWidth("VICTORIES", COLUMN_WIDTH)).reset());
        System.out.println();

        System.out.print(" "); // marker placeholder
        System.out.print(" "); // space
        System.out.println(
                "─".repeat(COLUMN_WIDTH)
                        + " ".repeat(SUPPLY_GAP + MARKER_PREFIX)
                        + "─".repeat(COLUMN_WIDTH)
                        + " ".repeat(SUPPLY_GAP + MARKER_PREFIX)
                        + "─".repeat(COLUMN_WIDTH));

        // Display supply cards in three columns, one row per card
        int maxRows =
                Math.max(actionCards.size(), Math.max(treasureCards.size(), victoryCards.size()));
        for (int i = 0; i < maxRows; i++) {

            // Action column
            if (i < actionCards.size()) {
                Card.Type cardType = actionCards.get(i);
                renderSupplyCell(
                        cardType,
                        state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType),
                        ansi().fgBrightBlue());
            } else {
                renderEmptySupplyCell();
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Treasure column
            if (i < treasureCards.size()) {
                Card.Type cardType = treasureCards.get(i);
                renderSupplyCell(
                        cardType,
                        state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType),
                        ansi().fgYellow());
            } else {
                renderEmptySupplyCell();
            }
            System.out.print(" ".repeat(SUPPLY_GAP));

            // Victory column (BUG renders in magenta; all others in green)
            if (i < victoryCards.size()) {
                Card.Type cardType = victoryCards.get(i);
                org.fusesource.jansi.Ansi color =
                        (cardType == Card.Type.BUG) ? ansi().fgMagenta() : ansi().fgGreen();
                renderSupplyCell(
                        cardType,
                        state.buyableCards().getNumAvailable(cardType),
                        actionableSupplyTypes.contains(cardType),
                        color);
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
                System.out.println(colorForCategory(card.category()).a(cardText).reset());
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
            switch (card.category()) {
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

        int maxHandRows =
                Math.max(
                        actionsInHand.size(),
                        Math.max(treasuresInHand.size(), victoriesInHand.size()));
        if (maxHandRows == 0) maxHandRows = 1; // Show at least the (none) row

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
                System.out.print(
                        ansi().fgBrightBlue().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
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
        String statusText =
                "Actions: "
                        + state.availableActions()
                        + "  ".repeat(spaces)
                        + "|"
                        + " ".repeat(spaces)
                        + "Buys: "
                        + state.availableBuys()
                        + "  ".repeat(spaces)
                        + "|"
                        + " ".repeat(spaces)
                        + "Money: "
                        + state.spendableMoney();
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
     * Renders the current game state side-by-side with a turn log panel. Delegates to the 4-arg
     * overload with a default of 2 players.
     *
     * @param state current game state
     * @param options available decisions (used for actionable-card markers)
     * @param logs the engine's turn log list (shared live reference)
     */
    public static void renderGameWithDecisions(
            GameState state, List<Decision> options, List<TurnLog> logs) {
        renderGameWithDecisions(state, options, logs, 2);
    }

    /**
     * Renders the current game state side-by-side with a turn log panel on the right. The log panel
     * shows the current turn (live) plus one full previous round of turns (i.e. the last {@code
     * numPlayers} completed turns), so every player can see what happened on their own last turn.
     *
     * @param state current game state
     * @param options available decisions (used for actionable-card markers)
     * @param logs the engine's turn log list (shared live reference)
     * @param numPlayers total number of players; controls how many past turns are shown alongside
     *     the current turn
     */
    public static void renderGameWithDecisions(
            GameState state, List<Decision> options, List<TurnLog> logs, int numPlayers) {
        // Capture the main panel output into a buffer so we can merge it with the
        // log panel line-by-line.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(baos);
        PrintStream realOut = System.out;
        System.setOut(capture);
        try {
            renderGameWithDecisions(state, options);
        } finally {
            // Ensure we restore the real System.out and then close the capture stream.
            System.out.flush();
            System.setOut(realOut);
            capture.flush();
            capture.close();
        }

        // Split captured output into lines, normalising CRLF → LF.
        String[] rawLines = baos.toString().split("\n", -1);
        List<String> mainLines = new ArrayList<>();
        for (String line : rawLines) {
            mainLines.add(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
        }
        // Drop trailing blank lines produced by the final println.
        while (!mainLines.isEmpty() && mainLines.get(mainLines.size() - 1).isEmpty()) {
            mainLines.remove(mainLines.size() - 1);
        }

        // Build the log panel: one full previous round + current turn.
        List<String> logLines = buildLogPanelLines(logs, numPlayers);

        // Print main panel and log panel side by side.
        int maxRows = Math.max(mainLines.size(), logLines.size());
        for (int i = 0; i < maxRows; i++) {
            String mainLine = (i < mainLines.size()) ? mainLines.get(i) : "";
            String logLine = (i < logLines.size()) ? logLines.get(i) : "";

            // Pad main line so the separator always appears at column SCREEN_WIDTH.
            int vLen = visualLength(mainLine);
            String pad = " ".repeat(Math.max(0, SCREEN_WIDTH - vLen));

            if (!logLine.isEmpty()) {
                System.out.println(mainLine + pad + "  \u2502  " + logLine);
            } else {
                System.out.println(mainLine + pad);
            }
        }
    }

    // ─── Turn log panel helpers ────────────────────────────────────────────────

    /** Returns the visible (non-ANSI) length of {@code s}. */
    static int visualLength(String s) {
        return ANSI_ESCAPE.matcher(s).replaceAll("").length();
    }

    /**
     * Truncates {@code s} to at most {@code maxLen} visible characters, appending {@code …} if
     * truncated. Does not account for embedded ANSI codes, so this should only be called on plain
     * (uncoloured) strings.
     */
    private static String truncateVisible(String s, int maxLen) {
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, Math.max(0, maxLen - 1)) + "\u2026";
    }

    /**
     * Builds the lines of the right-hand turn log panel. Shows the current turn plus the previous
     * {@code numPlayers} turns (one full round), so every player can see their own last turn. The
     * current turn is shown in full colour; older turns are dimmed.
     *
     * @param logs the engine's live turn log list
     * @param numPlayers total number of players; determines the window size
     * @return list of ANSI-coloured strings, each at most {@link #LOG_PANEL_WIDTH} visible
     *     characters wide
     */
    private static List<String> buildLogPanelLines(List<TurnLog> logs, int numPlayers) {
        List<String> lines = new ArrayList<>();

        // Panel header bar
        String headerLabel = "Turn Log";
        int fillLen = Math.max(0, LOG_PANEL_WIDTH - 4 - headerLabel.length());
        String headerBar = "\u2550\u2550 " + headerLabel + " " + "\u2550".repeat(fillLen);
        lines.add(
                ansi().fgBrightBlue()
                        .a(truncateVisible(headerBar, LOG_PANEL_WIDTH))
                        .reset()
                        .toString());

        if (logs == null || logs.isEmpty()) {
            lines.add(ansi().fgBrightBlack().a("  (no turns yet)").reset().toString());
            return lines;
        }

        // Show the current turn + one full previous round (numPlayers turns back).
        int windowSize = numPlayers + 1;
        int startIdx = Math.max(0, logs.size() - windowSize);
        for (int t = startIdx; t < logs.size(); t++) {
            TurnLog log = logs.get(t);
            boolean isCurrent = (t == logs.size() - 1);

            if (t > startIdx) {
                // Separator between turns.
                String sep = "  " + "\u2500".repeat(Math.max(0, LOG_PANEL_WIDTH - 2));
                lines.add(
                        ansi().fgBrightBlack()
                                .a(truncateVisible(sep, LOG_PANEL_WIDTH))
                                .reset()
                                .toString());
            }

            // Turn header.
            String turnHeader = log.getHeader();
            if (isCurrent) {
                lines.add(
                        ansi().bold()
                                .fgBrightCyan()
                                .a(truncateVisible(turnHeader, LOG_PANEL_WIDTH))
                                .reset()
                                .toString());
            } else {
                lines.add(
                        ansi().fgBrightBlack()
                                .a(truncateVisible(turnHeader, LOG_PANEL_WIDTH))
                                .reset()
                                .toString());
            }

            // Log entries for this turn.
            for (String entry : log.getEntries()) {
                String entryLine = "  " + entry;
                if (isCurrent) {
                    lines.add(
                            ansi().fgDefault()
                                    .a(truncateVisible(entryLine, LOG_PANEL_WIDTH))
                                    .reset()
                                    .toString());
                } else {
                    lines.add(
                            ansi().fgBrightBlack()
                                    .a(truncateVisible(entryLine, LOG_PANEL_WIDTH))
                                    .reset()
                                    .toString());
                }
            }
        }

        return lines;
    }

    /**
     * Renders the current game state to console with additional debug information showing player
     * decks and discards.
     *
     * @param debugState Container with game state and internal player deck/discard information
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

    /** Test main method Creates a sample GameState and calls renderGame on it */
    public static void main(String[] args) {
        // For demonstration, we will create a mock or dummy implementation here.
        String currentPlayerName = "Player 1";
        Hand currentPlayerHand =
                new Hand(
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
        CardStacks buyableCards =
                new CardStacks(
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

        GameState dummyState =
                new GameState(
                        currentPlayerName,
                        currentPlayerHand,
                        phase,
                        availableActions,
                        spendableMoney,
                        availableBuys,
                        buyableCards);
        // renderGame(dummyState);

        ImmutableList<Decision> playActionDecisions =
                ImmutableList.of(
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
        renderGame(dummyState);

        // Dummy GameResult for visual testing of printGameResult
        ImmutableList<Card> aliceDeck =
                ImmutableList.of(
                        new Card(Type.MODULE, 1),
                        new Card(Type.MODULE, 2),
                        new Card(Type.MODULE, 3),
                        new Card(Type.BITCOIN, 4),
                        new Card(Type.BITCOIN, 5),
                        new Card(Type.ETHEREUM, 6),
                        new Card(Type.METHOD, 7),
                        new Card(Type.REFACTOR, 8));
        ImmutableList<Card> bobDeck =
                ImmutableList.of(
                        new Card(Type.MODULE, 9),
                        new Card(Type.MODULE, 10),
                        new Card(Type.BITCOIN, 11),
                        new Card(Type.BITCOIN, 12),
                        new Card(Type.BITCOIN, 13),
                        new Card(Type.DOGECOIN, 14),
                        new Card(Type.CODE_REVIEW, 15));
        ImmutableList<Card> charlieDeck =
                ImmutableList.of(
                        new Card(Type.FRAMEWORK, 16),
                        new Card(Type.BITCOIN, 17),
                        new Card(Type.METHOD, 18),
                        new Card(Type.METHOD, 19));

        PlayerResult prAlice = new edu.brandeis.cosi.atg.state.PlayerResult("Alice", 18, aliceDeck);
        PlayerResult prBob = new edu.brandeis.cosi.atg.state.PlayerResult("Bob", 14, bobDeck);
        PlayerResult prCharlie =
                new edu.brandeis.cosi.atg.state.PlayerResult("Charlie", 18, charlieDeck);

        GameResult dummyResult =
                new edu.brandeis.cosi.atg.state.GameResult(
                        ImmutableList.of(prAlice, prBob, prCharlie));
        printGameResult(dummyResult);
    }

    /**
     * Prints the final game results in a readable format. Displays:
     *
     * <ul>
     *   <li>A winner announcement (handles ties)
     *   <li>A scoreboard ranked by score (descending)
     *   <li>Each player's final deck organized by card category with counts
     * </ul>
     *
     * @param result the final game result
     */
    public static void printGameResult(GameResult result) {
        // ── Header ──────────────────────────────────────────────────────────
        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("                 Game Over!                  ");
        System.out.println("═══════════════════════════════════════════════\n");

        // ── Determine winner(s) ─────────────────────────────────────────────
        if (result.playerResults().isEmpty()) {
            return;
        }
        int maxScore = Integer.MIN_VALUE;
        for (PlayerResult pr : result.playerResults()) {
            if (pr.score() > maxScore) maxScore = pr.score();
        }
        List<String> winnerNames = new ArrayList<>();
        for (PlayerResult pr : result.playerResults()) {
            if (pr.score() == maxScore) winnerNames.add(pr.playerName());
        }

        if (winnerNames.size() == 1) {
            System.out.println(
                    ansi().fgYellow()
                            .bold()
                            .a(
                                    "  *** Winner: "
                                            + winnerNames.get(0)
                                            + " with "
                                            + maxScore
                                            + " point"
                                            + (maxScore == 1 ? "" : "s")
                                            + "! ***")
                            .reset());
        } else {
            String names = String.join(", ", winnerNames);
            System.out.println(
                    ansi().fgYellow()
                            .bold()
                            .a(
                                    "  *** It's a tie! Winners: "
                                            + names
                                            + " with "
                                            + maxScore
                                            + " point"
                                            + (maxScore == 1 ? "" : "s")
                                            + " each! ***")
                            .reset());
        }
        System.out.println();

        // ── Scoreboard (sorted descending) ──────────────────────────────────
        System.out.println(ansi().fgBrightBlue().a("  ─── Scoreboard ───").reset());
        List<PlayerResult> sortedResults = new ArrayList<>(result.playerResults());
        sortedResults.sort((a, b) -> Integer.compare(b.score(), a.score()));
        int rank = 1;
        for (PlayerResult pr : sortedResults) {
            String line =
                    "  "
                            + rank
                            + ". "
                            + pr.playerName()
                            + "  —  scored "
                            + pr.score()
                            + " point"
                            + (pr.score() == 1 ? "" : "s");
            if (pr.score() == maxScore) {
                System.out.println(ansi().fgYellow().a(line).reset());
            } else {
                System.out.println(line);
            }
            rank++;
        }
        System.out.println();

        // ── Per-player deck breakdown ────────────────────────────────────────
        System.out.println(ansi().fgBrightBlue().a("  ─── Final Decks ───").reset());
        System.out.println();
        for (PlayerResult pr : sortedResults) {
            System.out.println(
                    ansi().bold()
                            .a(
                                    "  "
                                            + pr.playerName()
                                            + " scored "
                                            + pr.score()
                                            + " points with the following cards:")
                            .reset());

            List<Card> deck = new ArrayList<>(pr.endingDeck());
            if (deck.isEmpty()) {
                System.out.println("    (no cards)");
            } else {
                List<Card> victories = new ArrayList<>();
                List<Card> treasures = new ArrayList<>();
                List<Card> actions = new ArrayList<>();
                for (Card card : deck) {
                    switch (card.category()) {
                        case VICTORY:
                            victories.add(card);
                            break;
                        case MONEY:
                            treasures.add(card);
                            break;
                        case ACTION:
                            actions.add(card);
                            break;
                    }
                }
                if (!victories.isEmpty()) {
                    System.out.println(ansi().fgGreen().a("    Victory Cards:").reset());
                    printCardGroup(victories, Card.Type.Category.VICTORY);
                }
                if (!treasures.isEmpty()) {
                    System.out.println(ansi().fgYellow().a("    Treasure Cards:").reset());
                    printCardGroup(treasures, Card.Type.Category.MONEY);
                }
                if (!actions.isEmpty()) {
                    System.out.println(ansi().fgBrightBlue().a("    Action Cards:").reset());
                    printCardGroup(actions, Card.Type.Category.ACTION);
                }
                System.out.println(
                        "    Total: " + deck.size() + " card" + (deck.size() == 1 ? "" : "s"));
            }
            System.out.println();
        }
    }

    /**
     * Prints a group of cards with counts, colored by category. Cards with the same type are
     * collapsed into a single line (e.g. "Bitcoin x3").
     */
    private static void printCardGroup(List<Card> cards, Card.Type.Category category) {
        Map<Card.Type, Integer> counts = new java.util.LinkedHashMap<>();
        for (Card card : cards) {
            counts.merge(card.type(), 1, Integer::sum);
        }
        for (Map.Entry<Card.Type, Integer> entry : counts.entrySet()) {
            String line =
                    "      \u2022 "
                            + displayName(entry.getKey())
                            + (entry.getValue() > 1 ? " x" + entry.getValue() : "");
            System.out.println(colorForCategory(category).a(line).reset());
        }
    }
}
