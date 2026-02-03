package edu.brandeis.cosi103a.groupe;

import static org.fusesource.jansi.Ansi.*;

import java.util.List;
import java.util.ArrayList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;


public class Renderer {
    
    // Layout constants
    private static final int SCREEN_WIDTH = 50;
    private static final int COLUMN_WIDTH = 18;
    private static final int SUPPLY_GAP = 2;


    // Helper: get Display name for card type
    // Transforms to Title Case and replaces underscores with spaces
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
    
    // Helper: get ANSI color for card category
    private static org.fusesource.jansi.Ansi colorForCategory(Card.Type.Category category) {
        switch (category) {
            case ACTION: return ansi().fgBrightBlue();
            case MONEY: return ansi().fgYellow();
            case VICTORY: return ansi().fgGreen();
            default: return ansi().fgDefault();
        }
    }
    
    // Helper: pad or truncate text to fixed width
    private static String fixedWidth(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, width);
        }
        return String.format("%-" + width + "s", text);
    }
    
    // Helper: render a supply pile (count + card name)
    private static String renderPile(Card.Type cardType, int count, int width) {
        String pile = count + " " + displayName(cardType);
        return fixedWidth(pile, width);
    }
    
    // Renders the current game state to console
    public static void renderGame(GameState state) {
        
        
        System.out.println( ansi().eraseScreen() );
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

        System.out.print("Player: " + state.currentPlayerName());

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
        
        String playerText = "Player: " + state.currentPlayerName();
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
                case VICTORY: victoryCards.add(cardType); break;
                case MONEY: treasureCards.add(cardType); break;
                case ACTION: actionCards.add(cardType); break;
            }
        }
        
        // Calculate supply area width
        int pileWidth = COLUMN_WIDTH;
        
        // 2. Supply area (3 rows)
        Iterable<Card> playedCards = state.currentPlayerHand().playedCards();
        
        
        
        
        // Row 1: Victory cards
        StringBuilder row1 = new StringBuilder();
        for (Card.Type cardType : victoryCards) {
            int count = state.buyableCards().getNumAvailable(cardType);
            row1.append(renderPile(cardType, count, pileWidth));
            row1.append(" ".repeat(SUPPLY_GAP));
        }
        System.out.println(ansi().fgGreen().a(row1.toString()).reset());
        
        // Row 2: Treasure cards
        StringBuilder row2 = new StringBuilder();
        for (Card.Type cardType : treasureCards) {
            int count = state.buyableCards().getNumAvailable(cardType);
            row2.append(renderPile(cardType, count, pileWidth));
            row2.append(" ".repeat(SUPPLY_GAP));
        }
        System.out.println(ansi().fgYellow().a(row2.toString()).reset());
        
        // Row 3: Action cards
        StringBuilder row3 = new StringBuilder();
        for (Card.Type cardType : actionCards) {
            int count = state.buyableCards().getNumAvailable(cardType);
            row3.append(renderPile(cardType, count, pileWidth));
            row3.append(" ".repeat(SUPPLY_GAP));
        }
        System.out.println(ansi().fgBrightBlue().a(row3.toString()).reset());
        
        System.out.println();
        System.out.println();
        
        // 3. Played cards section
        String playedCardsLabel = "═══ Played Cards ═══";
        int playedPadding = (SCREEN_WIDTH - playedCardsLabel.length()) / 2;
        System.out.print(" ".repeat(playedPadding));
        System.out.println(ansi().fgBrightBlue().a(playedCardsLabel).reset());
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
        
        // 4. Hand display (3 columns)
        String yourHandLabel = "═══ Your Hand ═══";
        int handPadding = (SCREEN_WIDTH - yourHandLabel.length()) / 2;
        System.out.print(" ".repeat(handPadding));
        System.out.println(ansi().fgBrightBlue().a(yourHandLabel).reset());
        List<Card> actionsInHand = new ArrayList<>();
        List<Card> treasuresInHand = new ArrayList<>();
        List<Card> victoriesInHand = new ArrayList<>();
        
        for (Card card : state.currentPlayerHand().unplayedCards()) {
            switch (card.getCategory()) {
                case ACTION: actionsInHand.add(card); break;
                case MONEY: treasuresInHand.add(card); break;
                case VICTORY: victoriesInHand.add(card); break;
            }
        }
        
        int maxHandRows = Math.max(actionsInHand.size(), Math.max(treasuresInHand.size(), victoriesInHand.size()));
        if (maxHandRows == 0) maxHandRows = 1; // Show at least headers
        
        // Calculate padding to center the treasure column (middle column)
        int totalHandWidth = COLUMN_WIDTH * 3;
        int handLeftPadding = Math.max(0, (SCREEN_WIDTH - totalHandWidth) / 2) + 2;
        
        for (int i = 0; i < maxHandRows; i++) {
            // Add left padding to center the hand
            System.out.print(" ".repeat(handLeftPadding));
            
            // Action column
            if (i < actionsInHand.size()) {
                System.out.print(ansi().fgBrightBlue().a(fixedWidth(displayName(actionsInHand.get(i).type()), COLUMN_WIDTH)).reset());
            } else if (i == 0) {
                System.out.print(ansi().fgBrightBlue().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            
            // Treasure column
            if (i < treasuresInHand.size()) {
                System.out.print(ansi().fgYellow().a(fixedWidth(displayName(treasuresInHand.get(i).type()), COLUMN_WIDTH)).reset());
            } else if (i == 0) {
                System.out.print(ansi().fgYellow().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            
            // Victory column
            if (i < victoriesInHand.size()) {
                System.out.print(ansi().fgGreen().a(fixedWidth(displayName(victoriesInHand.get(i).type()), COLUMN_WIDTH)).reset());
            } else if (i == 0) {
                System.out.print(ansi().fgGreen().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        
        // 5. Status bar
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
     * Test main method
     * Creates a sample GameState and calls renderGame on it
     */
    public static void main(String[] args) {
        // For demonstration, we will create a mock or dummy implementation here.
        String currentPlayerName = "Player 1";
        Hand currentPlayerHand = new Hand(
            ImmutableList.of(
                new Card(Type.BITCOIN,6),
                new Card(Type.BITCOIN,7),
                new Card(Type.BITCOIN,8)
            ),
            ImmutableList.of(
                new Card(Type.BITCOIN, 1),
                new Card(Type.BITCOIN, 2),
                new Card(Type.BITCOIN, 3),
                new Card(Type.METHOD, 4),
                new Card(Type.METHOD, 5),
                new Card(Type.REFACTOR, 9)
            )
        );
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int availableActions = 1;
        int spendableMoney = 3;
        int availableBuys = 1;
        CardStacks buyableCards = new CardStacks(
            ImmutableMap.of(
                Type.METHOD, 12,
                Type.MODULE, 12,
                Type.FRAMEWORK, 12,
                Type.BITCOIN, 50,
                Type.ETHEREUM, 40,
                Type.DOGECOIN, 30,
                Type.CODE_REVIEW, 10,
                Type.EVERGREEN_TEST, 10,
                Type.REFACTOR, 10
            )
        );

        GameState dummyState = new GameState(
            currentPlayerName,
            currentPlayerHand,
            phase,
            availableActions,
            spendableMoney,
            availableBuys,
            buyableCards
        );
        renderGame(dummyState);
    }
}
