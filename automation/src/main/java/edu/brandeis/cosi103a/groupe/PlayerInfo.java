package edu.brandeis.cosi103a.groupe;

import java.util.List;
import java.util.ArrayList;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.Decision;

public class PlayerInfo {

    private Player player;
    private MutableHand hand;
    private List<Card> deck;
    private List<Card> discard;

    // Constants
    private static final int PLAYER_STARTING_BITCOINS = 7;
    private static final int PLAYER_STARTING_METHODS = 3;
    private static final int HAND_SIZE = 5;

    public PlayerInfo(Player player, int cardId) { // Will need to update the way Engine keeps track of card IDs
        this.player = player;
        hand = new MutableHand();
        deck = new ArrayList<>();
        discard = new ArrayList<>();
        // Initialize the player's deck with starting cards, then shuffle
        for (int i = 0; i < PLAYER_STARTING_BITCOINS; i++) {
            deck.add(new Card(Type.BITCOIN, cardId++));
        }
        for (int i = 0; i < PLAYER_STARTING_METHODS; i++) {
            deck.add(new Card(Type.METHOD, cardId++));
        }
        java.util.Collections.shuffle(deck);
        // Draw initial hands of 5 cards
        for (int i = 0; i < HAND_SIZE; i++) {
            drawCard();
        }
    }

    public String getName() { // does it make more sense to just have a getPlayer() method instead of these two?
        return player.getName();
    }

    public void makeDecision(GameState gameState, com.google.common.collect.ImmutableList<Decision> options) {
        player.makeDecision(gameState, options);
    }

    public void drawCard() {
        if (deck.isEmpty()) {
            // Shuffle discard into deck
            deck.addAll(discard);
            discard.clear();
            java.util.Collections.shuffle(deck);
        }
        if (!deck.isEmpty()) {
            hand.addToUnplayed(deck.remove(deck.size() - 1));
        }
    }

    public void playCard(Card card) {
        hand.playCard(card);
    }

    public void cleanup() {
        hand.moveAllCardsTo(discard); // Discard all cards in hand
        for (int i = 0; i < HAND_SIZE; i++) {
            drawCard(); // Draw new hand of 5 cards
        }
    }

    // more methods to come

}
