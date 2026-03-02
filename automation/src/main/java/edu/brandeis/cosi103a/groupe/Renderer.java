package edu.brandeis.cosi103a.groupe;

import static org.fusesource.jansi.Ansi.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.decisions.*;;


public class Renderer {
    
    // Layout constants
    private static final int SCREEN_WIDTH = 50;
    private static final int COLUMN_WIDTH = 22;
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
    
    // Helper: render a supply pile (count + card name + cost)
    private static String renderPile(Card.Type cardType, int count, int width) {
        int cost = getCost(cardType);
        String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
        return fixedWidth(pile, width);
    }
    
    // Helper: get cost for a card type
    private static int getCost(Card.Type cardType) {
        return new Card(cardType, 0).cost();
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
                case VICTORY: victoryCards.add(cardType); break;
                case MONEY: treasureCards.add(cardType); break;
                case ACTION: actionCards.add(cardType); break;
            }
        }
        
        // Sort each category by cost
        actionCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        treasureCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        victoryCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        
        // Calculate supply area width
        int pileWidth = COLUMN_WIDTH;
        
        // 2. Supply area (3 columns: Actions, Treasures, Victories)
        List<Card> playedCards = new ArrayList<>();
        for (Card c : state.currentPlayerHand().playedCards()) {
            playedCards.add(c);
        }
        
        // Print column headers
        System.out.print(" ".repeat(2));
        System.out.print(ansi().fgBrightBlue().bold().a(fixedWidth("ACTIONS", pileWidth)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP));
        System.out.print(ansi().fgYellow().bold().a(fixedWidth("TREASURES", pileWidth)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP));
        System.out.print(ansi().fgGreen().bold().a(fixedWidth("VICTORIES", pileWidth)).reset());
        System.out.println();
        
        System.out.print(" ".repeat(2));
        System.out.println("─".repeat(pileWidth) + " ".repeat(SUPPLY_GAP) + 
                          "─".repeat(pileWidth) + " ".repeat(SUPPLY_GAP) + 
                          "─".repeat(pileWidth));
        
        // Display cards in columns
        int maxRows = Math.max(actionCards.size(), Math.max(treasureCards.size(), victoryCards.size()));
        for (int i = 0; i < maxRows; i++) {
            System.out.print(" ".repeat(2));
            
            // Action column
            if (i < actionCards.size()) {
                Card.Type cardType = actionCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                System.out.print(ansi().fgBrightBlue().a(renderPile(cardType, count, pileWidth)).reset());
            } else {
                System.out.print(fixedWidth("", pileWidth));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));
            
            // Treasure column
            if (i < treasureCards.size()) {
                Card.Type cardType = treasureCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                System.out.print(ansi().fgYellow().a(renderPile(cardType, count, pileWidth)).reset());
            } else {
                System.out.print(fixedWidth("", pileWidth));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));
            
            // Victory column
            if (i < victoryCards.size()) {
                Card.Type cardType = victoryCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                if (cardType == Card.Type.BUG) {
                    System.out.print(ansi().fgMagenta().a(renderPile(cardType, count, pileWidth)).reset());
                } else {
                    System.out.print(ansi().fgGreen().a(renderPile(cardType, count, pileWidth)).reset());
                }
            } else {
                System.out.print(fixedWidth("", pileWidth));
            }
            
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        
        // 3. Played cards section
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
        
        // 4. Hand display (3 columns)
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

    public static void renderGameWithDecisions(GameState state, List<Decision> options) {
        // Build mappings from cards to decision numbers
        Map<Card, Integer> handDecisionMap = new java.util.HashMap<>();
        Map<Card.Type, Integer> supplyDecisionMap = new java.util.HashMap<>();
        
        for (int i = 0; i < options.size(); i++) {
            Decision decision = options.get(i);
            if (decision instanceof PlayCardDecision) {
                handDecisionMap.put(((PlayCardDecision) decision).card(), i + 1);
            } else if (decision instanceof TrashCardDecision) {
                handDecisionMap.put(((TrashCardDecision) decision).card(), i + 1);
            } else if (decision instanceof BuyDecision) {
                supplyDecisionMap.put(((BuyDecision) decision).cardType(), i + 1);
            } else if (decision instanceof GainCardDecision) {
                supplyDecisionMap.put(((GainCardDecision) decision).cardType(), i + 1);
            }
        }
        
        // Render game state with decision numbers
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
                case VICTORY: victoryCards.add(cardType); break;
                case MONEY: treasureCards.add(cardType); break;
                case ACTION: actionCards.add(cardType); break;
            }
        }
        
        // Sort each category by cost
        actionCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        treasureCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        victoryCards.sort((a, b) -> Integer.compare(getCost(a), getCost(b)));
        
        // Calculate supply area width
        int pileWidth = COLUMN_WIDTH;
        
        // Print played cards list
        List<Card> playedCards = new ArrayList<>();
        for (Card c : state.currentPlayerHand().playedCards()) {
            playedCards.add(c);
        }
        
        // Print column headers
        System.out.print(" ".repeat(2));
        System.out.print(ansi().fgBrightBlue().bold().a(fixedWidth("ACTIONS", pileWidth)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP));
        System.out.print(ansi().fgYellow().bold().a(fixedWidth("TREASURES", pileWidth)).reset());
        System.out.print(" ".repeat(SUPPLY_GAP));
        System.out.print(ansi().fgGreen().bold().a(fixedWidth("VICTORIES", pileWidth)).reset());
        System.out.println();
        
        System.out.print(" ".repeat(2));
        System.out.println("─".repeat(pileWidth) + " ".repeat(SUPPLY_GAP) + 
                          "─".repeat(pileWidth) + " ".repeat(SUPPLY_GAP) + 
                          "─".repeat(pileWidth));
        
        // Display cards in columns WITH DECISION NUMBERS
        int maxRows = Math.max(actionCards.size(), Math.max(treasureCards.size(), victoryCards.size()));
        for (int i = 0; i < maxRows; i++) {
            System.out.print(" ".repeat(2));
            
            // Action column
            if (i < actionCards.size()) {
                Card.Type cardType = actionCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                
                // Check if this card type has a decision
                if (supplyDecisionMap.containsKey(cardType)) {
                    int decisionNum = supplyDecisionMap.get(cardType);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(ansi().fgBrightBlue().a(fixedWidth(pile, pileWidth - 2)).reset());
                } else {
                    System.out.print("  ");
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(ansi().fgBrightBlue().a(fixedWidth(pile, pileWidth - 2)).reset());
                }
            } else {
                System.out.print(fixedWidth("", pileWidth));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));
            
            // Treasure column
            if (i < treasureCards.size()) {
                Card.Type cardType = treasureCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                
                // Check if this card type has a decision
                if (supplyDecisionMap.containsKey(cardType)) {
                    int decisionNum = supplyDecisionMap.get(cardType);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(ansi().fgYellow().a(fixedWidth(pile, pileWidth - 2)).reset());
                } else {
                    System.out.print("  ");
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(ansi().fgYellow().a(fixedWidth(pile, pileWidth - 2)).reset());
                }
            } else {
                System.out.print(fixedWidth("", pileWidth));
            }
            System.out.print(" ".repeat(SUPPLY_GAP));
            
            // Victory column
            if (i < victoryCards.size()) {
                Card.Type cardType = victoryCards.get(i);
                int count = state.buyableCards().getNumAvailable(cardType);
                
                // Determine color based on card type
                org.fusesource.jansi.Ansi colorAnsi = (cardType == Card.Type.BUG) ? ansi().fgMagenta() : ansi().fgGreen();
                
                // Check if this card type has a decision
                if (supplyDecisionMap.containsKey(cardType)) {
                    int decisionNum = supplyDecisionMap.get(cardType);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(colorAnsi.a(fixedWidth(pile, pileWidth - 2)).reset());
                } else {
                    System.out.print("  ");
                    int cost = getCost(cardType);
                    String pile = count + " " + displayName(cardType) + " ($" + cost + ")";
                    System.out.print(colorAnsi.a(fixedWidth(pile, pileWidth - 2)).reset());
                }
            } else {
                System.out.print(fixedWidth("", pileWidth));
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
        
        // Hand display WITH DECISION NUMBERS
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
                Card card = actionsInHand.get(i);
                if (handDecisionMap.containsKey(card)) {
                    int decisionNum = handDecisionMap.get(card);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    System.out.print(ansi().fgBrightBlue().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH - 2)).reset());
                } else {
                    System.out.print(ansi().fgBrightBlue().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH)).reset());
                }
            } else if (i == 0) {
                System.out.print(ansi().fgBrightBlue().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            
            // Treasure column
            if (i < treasuresInHand.size()) {
                Card card = treasuresInHand.get(i);
                if (handDecisionMap.containsKey(card)) {
                    int decisionNum = handDecisionMap.get(card);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    System.out.print(ansi().fgYellow().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH - 2)).reset());
                } else {
                    System.out.print(ansi().fgYellow().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH)).reset());
                }
            } else if (i == 0) {
                System.out.print(ansi().fgYellow().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
                System.out.print(fixedWidth("", COLUMN_WIDTH));
            }
            
            // Victory column
            if (i < victoriesInHand.size()) {
                Card card = victoriesInHand.get(i);
                if (handDecisionMap.containsKey(card)) {
                    int decisionNum = handDecisionMap.get(card);
                    System.out.print(ansi().fgRed().a("* ").reset());
                    System.out.print(ansi().fgGreen().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH - 2)).reset());
                } else {
                    System.out.print(ansi().fgGreen().a(fixedWidth(displayName(card.type()), COLUMN_WIDTH)).reset());
                }
            } else if (i == 0) {
                System.out.print(ansi().fgGreen().a(fixedWidth("(none)", COLUMN_WIDTH)).reset());
            } else {
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
        
        // // Bottom instruction
        // String instruction = "Press the number of the decision you want to make or E to end phase.";
        // int instructionPadding = Math.max(0, (SCREEN_WIDTH - instruction.length()) / 2);
        // System.out.print(" ".repeat(instructionPadding));
        // System.out.println(ansi().fgBrightYellow().a(instruction).reset());
        // System.out.println();
    }

    /**
     * Renders the current game state to console with additional debug information showing player decks and discards.
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
                new Card(Type.BITCOIN, 8)
            ),
            ImmutableList.of(
                new Card(Type.BITCOIN, 1),
                new Card(Type.BITCOIN, 2),
                new Card(Type.BITCOIN, 3),
                new Card(Type.METHOD, 4),
                new Card(Type.METHOD, 5),
                new Card(Type.REFACTOR, 9),
                new Card(Type.EVERGREEN_TEST, 10)
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
            new EndPhaseDecision(GameState.TurnPhase.ACTION)
        );
        renderGameWithDecisions(dummyState, playActionDecisions);
    }
}
