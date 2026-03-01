package edu.brandeis.cosi103a.groupe;

import java.util.List;

import java.util.ArrayList;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.Hand;

/**
 * Encapsulates all game state information for a single player.
 * Manages the player's hand, deck, discard pile, turn resources (actions/money/buys),
 * and game status (score and disqualification).
 * 
 * This class serves as a mutable wrapper around the immutable Player interface,
 * maintaining the player's evolving state throughout the game.
 */
public class PlayerInfo {

    private Player player;
    private MutableHand hand;
    private List<Card> deck;
    private List<Card> discard;
    private TurnInfo turnInfo;
    private int score;
    private boolean disqualified;

    // Constants
    private static final int PLAYER_STARTING_BITCOINS = 7;
    private static final int PLAYER_STARTING_METHODS = 3;
    private static final int HAND_SIZE = 5;

    /**
     * Constructs a new PlayerInfo with initialized deck and hand.
     * Creates a starting deck of 7 BITCOIN and 3 METHOD cards, shuffles it,
     * and draws an initial hand of 5 cards.
     * 
     * @param player The player interface that will make decisions for this player
     * @param cardId The starting card ID for this player's cards (will be incremented
     *               as cards are created to ensure unique IDs across the game)
     */
    public PlayerInfo(Player player, int cardId) { // Will need to update the way Engine keeps track of card IDs
        this.player = player;
        hand = new MutableHand();
        deck = new ArrayList<>();
        discard = new ArrayList<>();
        turnInfo = new TurnInfo();
        score = 0;
        disqualified = false;
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

    /**
     * Gets the name of the underlying player.
     * 
     * @return The player's name
     */
    public String getName() {
        return player.getName();
    }

    /**
     * Delegates decision-making to the underlying player.
     * 
     * @param gameState The current state of the game
     * @param options The list of valid decisions the player can choose from
     * @return The decision chosen by the player
     */
    public Decision makeDecision(GameState gameState, com.google.common.collect.ImmutableList<Decision> options) {
        return player.makeDecision(gameState, options);
    }

    /**
     * Draws one card from the deck into the player's hand.
     * If the deck is empty, shuffles the discard pile into the deck first.
     * If both deck and discard are empty, no card is drawn.
     */
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

    /**
     * Plays a card from the player's hand.
     * Moves the card from unplayed to played within the hand.
     * 
     * @param card The card to play
     */
    public void playCard(Card card) {
        hand.playCard(card);
    }

    /**
     * Performs end-of-turn cleanup.
     * Discards all cards currently in hand (both played and unplayed),
     * then draws a new hand of 5 cards.
     */
    public void cleanup() {
        hand.moveAllCardsTo(discard); // Discard all cards in hand
        for (int i = 0; i < HAND_SIZE; i++) {
            drawCard(); // Draw new hand of 5 cards
        }
    }

    /**
     * Gains a new card (from a purchase or action effect).
     * The card is added directly to the discard pile.
     * 
     * @param card The card to gain
     */
    public void gainCard(Card card) {
        discard.add(card);
    }

    /**
     * Trashes a card from the player's hand.
     * Removes the card permanently from the game (not just from hand).
     * 
     * @param card The card to trash
     * @return true if the card was successfully removed, false otherwise
     */
    public boolean trashCard(Card card) {
        return hand.removeFromUnplayed(card);
    }

    /**
     * Gets all unplayed cards currently in the player's hand.
     * 
     * @return A list of cards that haven't been played this turn
     */
    public List<Card> getUnplayedCards() {
        return hand.getUnplayedCards();
    }

    /**
     * Gets all action cards currently unplayed in the player's hand.
     * Filters unplayed cards for those with ACTION category.
     * 
     * @return A list of unplayed action cards in hand
     */
    public List<Card> getActionsInHand() {
        List<Card> actions = new ArrayList<>();
        List<Card> unplayed = getUnplayedCards();
        for (Card card : unplayed) {
            if (card.getCategory() == Card.Type.Category.ACTION) {
                actions.add(card);
            }
        }
        return actions;
    }

    /**
     * Gets all money cards currently unplayed in the player's hand.
     * Filters unplayed cards for those with MONEY category.
     * 
     * @return A list of unplayed money cards in hand
     */
    public List<Card> getMoneysInHand() {
        List<Card> money = new ArrayList<>();
        List<Card> unplayed = getUnplayedCards();
        for (Card card : unplayed) {
            if (card.getCategory() == Card.Type.Category.MONEY) {
                money.add(card);
            }
        }
        return money;
    }

    /**
     * Gets an immutable view of the player's current hand.
     * Used for creating GameState snapshots.
     * 
     * @return An immutable Hand object representing the current hand state
     */
    public Hand getImmutableHand() {
        return hand.toImmutableHand();
    }

    /**
     * Gets the number of actions available for the current turn.
     * Delegates to the underlying TurnInfo.
     * 
     * @return The number of actions remaining
     */
    public int numActions() {
        return turnInfo.getNumActions();
    }

    /**
     * Increases the number of actions by the specified amount.
     * Delegates to the underlying TurnInfo.
     * 
     * @param amount The number of actions to add
     */
    public void incrementActions(int amount) {
        turnInfo.incrementActions(amount);
    }

    /**
     * Decreases the number of actions by 1.
     * Delegates to the underlying TurnInfo.
     */
    public void decrementActions() {
        turnInfo.decrementActions();
    }

    /**
     * Gets the amount of money available for the current turn.
     * Delegates to the underlying TurnInfo.
     * 
     * @return The amount of money available
     */
    public int numMoney() {
        return turnInfo.getNumMoney();
    }

    /**
     * Increases the amount of money by the specified amount.
     * Delegates to the underlying TurnInfo.
     * 
     * @param amount The amount of money to add
     */
    public void incrementMoney(int amount) {
        turnInfo.incrementMoney(amount);
    }

    /**
     * Decreases the amount of money by the specified amount.
     * Delegates to the underlying TurnInfo.
     * 
     * @param amount The amount of money to subtract
     */
    public void decrementMoney(int amount) {
        turnInfo.decrementMoney(amount);
    }

    /**
     * Gets the number of buy opportunities remaining for the current turn.
     * Delegates to the underlying TurnInfo.
     * 
     * @return The number of buys available
     */
    public int numBuys() {
        return turnInfo.getNumBuys();
    }

    /**
     * Increases the number of buys by 1.
     * Delegates to the underlying TurnInfo.
     */
    public void incrementBuys() {
        turnInfo.incrementBuys();
    }

    /**
     * Decreases the number of buys by 1.
     * Delegates to the underlying TurnInfo.
     */
    public void decrementBuys() {
        turnInfo.decrementBuys();
    }

    /**
     * Resets turn resources to default values.
     * Called at the beginning of each new turn to restore starting values
     * (1 action, 0 money, 1 buy).
     */
    public void turnReset() {
        turnInfo.reset();
    }

    // TODO: public void calculateScore()

    // TODO: public int getScore()

    // TODO: public void forfeit()

    // TODO: public boolean isDisqualified()

    /**
     * Determines if this PlayerInfo represents the same player as another object.
     * Two PlayerInfo objects are equal if they wrap the same underlying Player.
     * Used primarily in getNextPlayer() in the engine.
     * 
     * @param o The object to compare with
     * @return true if both PlayerInfo objects wrap the same Player, false otherwise
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerInfo)) return false;
        PlayerInfo that = (PlayerInfo) o;
        return player.equals(that.player);
    }

}
