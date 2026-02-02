package edu.brandeis.cosi103a.groupe;

import static org.fusesource.jansi.Ansi.*;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class Renderer {
    
    // Renders the current game state to console
    public static void renderGame(GameState state) {
        
        
        System.out.println( ansi().eraseScreen() );
        System.out.println( ansi().render(" @|blue --------|@ @|green Welcome to Automation: The Game|@ @|blue --------|@") );
        System.out.println();

        System.out.println(ansi().fgBrightBlue().a("         ═══ Available Cards ═══").reset());
        
        // Print victory cards in a row
        StringBuilder victoryLine = new StringBuilder("  ");
        for (Card.Type cardType : state.buyableCards().getCardTypes()) {
            if (cardType.category() == Card.Type.Category.VICTORY) {
                int count = state.buyableCards().getNumAvailable(cardType);
                victoryLine.append(String.format("%-15s", count + " " + cardType));
            }
        }
        if (victoryLine.length() > 2) {
            System.out.println(ansi().fgGreen().a(victoryLine.toString()).reset());
        }
        
        // Print treasure cards in a row
        StringBuilder treasureLine = new StringBuilder("  ");
        for (Card.Type cardType : state.buyableCards().getCardTypes()) {
            if (cardType.category() == Card.Type.Category.MONEY) {
                int count = state.buyableCards().getNumAvailable(cardType);
                treasureLine.append(String.format("%-15s", count + " " + cardType));
            }
        }
        if (treasureLine.length() > 2) {
            System.out.println(ansi().fgYellow().a(treasureLine.toString()).reset());
        }
        System.out.println();

        // Print out basic game info with ANSI formatting
        System.out.println(ansi().bold().a("Current Player: ").reset().fgCyan().a(state.currentPlayerName()).reset());
        System.out.println(ansi().bold().a("Phase: ").reset().fgYellow().a(state.phase()).reset());
        System.out.println();
        
        System.out.println(ansi().fgBrightBlue().a("═══ Resources ═══").reset());
        System.out.println(ansi().bold().a("  Actions: ").reset().fgGreen().a(state.availableActions()).reset());
        System.out.println(ansi().bold().a("  Buys:    ").reset().fgGreen().a(state.availableBuys()).reset());
        System.out.println(ansi().bold().a("  Money:   ").reset().fgGreen().a("$" + state.spendableMoney()).reset());
        System.out.println();

        //Played Cards
        System.out.println(ansi().fgBrightBlue().a("═══ Played Cards ═══").reset());
        for (Card card : state.currentPlayerHand().playedCards()) {
            if (card.getCategory() == Card.Type.Category.VICTORY) {
                System.out.println(ansi().a("  • ").fgGreen().a(card.type()).reset());
            } else if (card.getCategory() == Card.Type.Category.MONEY) {
                System.out.println(ansi().a("  • ").fgYellow().a(card.type()).reset());
            } else {
                System.out.println(ansi().a("  • ").fgDefault().a(card.type()).reset());
            }
        }
        System.out.println();
        
        System.out.println(ansi().fgBrightBlue().a("═══ Your Hand ═══").reset());
        for (Card card : state.currentPlayerHand().unplayedCards()) {
            if (card.getCategory() == Card.Type.Category.VICTORY) {
                System.out.println(ansi().a("  • ").fgGreen().a(card.type()).reset());
            } else if (card.getCategory() == Card.Type.Category.MONEY) {
                System.out.println(ansi().a("  • ").fgYellow().a(card.type()).reset());
            } else {
                System.out.println(ansi().a("  • ").fgDefault().a(card.type()).reset());
            }
        }
        System.out.println();



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
                new Card(Type.METHOD, 5)
            )
        );
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int availableActions = 1;
        int spendableMoney = 0;
        int availableBuys = 1;
        CardStacks buyableCards = new CardStacks(
            ImmutableMap.of(
                Type.METHOD, 12,
                Type.MODULE, 12,
                Type.FRAMEWORK, 12,
                Type.BITCOIN, 50,
                Type.ETHEREUM, 40,
                Type.DOGECOIN, 30
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
