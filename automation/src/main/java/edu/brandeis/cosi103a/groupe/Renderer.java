package edu.brandeis.cosi103a.groupe;

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
        System.out.println("Automation: The Game");

        // Print out basic game info
        System.out.println("Current Player: " + state.currentPlayerName());
        System.out.println("Phase: " + state.phase());
        System.out.println("Available Actions: " + state.availableActions());
        System.out.println("Available Buys: " + state.availableBuys());
        System.out.println("Spendable Money: " + state.spendableMoney());
        System.out.println("Current player hand: " + state.currentPlayerHand());
        System.out.println("Buyable cards: " + state.buyableCards());

    }

    /**
     * Test main method
     * Creates a sample GameState and calls renderGame on it
     */
    public static void main(String[] args) {
        // For demonstration, we will create a mock or dummy implementation here.
        String currentPlayerName = "Player 1";
        Hand currentPlayerHand = new Hand(
            ImmutableList.of(),
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
                Type.BITCOIN, 10,
                Type.METHOD, 10
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
