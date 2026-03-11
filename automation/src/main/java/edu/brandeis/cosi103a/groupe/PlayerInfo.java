package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulates all game state information for a single player. Manages the player's hand, deck,
 * discard pile, turn resources (actions/money/buys), and game status (score and disqualification).
 *
 * <p>This class serves as a mutable wrapper around the immutable Player interface, maintaining the
 * player's evolving state throughout the game.
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
     * Constructs a new PlayerInfo with initialized deck and hand. Creates a starting deck of 7
     * BITCOIN and 3 METHOD cards, shuffles it, and draws an initial hand of 5 cards.
     *
     * @param player The player interface that will make decisions for this player
     * @param cardID A shared AtomicInteger used to generate unique card IDs for the initial deck
     *     cards. The counter is incremented for each card created during construction to ensure
     *     uniqueness across all players.
     */
    public PlayerInfo(Player player, AtomicInteger cardID) {
        this.player = player;
        hand = new MutableHand();
        deck = new ArrayList<>();
        discard = new ArrayList<>();
        turnInfo = new TurnInfo();
        score = PLAYER_STARTING_METHODS; // Starting score from METHOD cards in deck
        disqualified = false;
        // Initialize the player's deck with starting cards, then shuffle
        for (int i = 0; i < PLAYER_STARTING_BITCOINS; i++) {
            deck.add(new Card(Type.BITCOIN, cardID.getAndIncrement()));
        }
        for (int i = 0; i < PLAYER_STARTING_METHODS; i++) {
            deck.add(new Card(Type.METHOD, cardID.getAndIncrement()));
        }
        java.util.Collections.shuffle(deck);
        // Draw initial hands of 5 cards
        for (int i = 0; i < HAND_SIZE; i++) {
            drawCard();
        }
    }

    /**
     * Gets the underlying Player object. Useful for tests and situations where the raw Player
     * interface is needed.
     *
     * @return The wrapped Player
     */
    public Player getPlayer() {
        return player;
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
    public Decision makeDecision(
            GameState gameState, com.google.common.collect.ImmutableList<Decision> options) {
        return player.makeDecision(gameState, options);
    }

    /**
     * Draws one card from the deck into the player's hand. If the deck is empty, shuffles the
     * discard pile into the deck first. If both deck and discard are empty, no card is drawn.
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
     * Plays a card from the player's hand. Moves the card from unplayed to played within the hand.
     *
     * @param card The card to play
     */
    public void playCard(Card card) {
        hand.playCard(card);
    }

    /**
     * Performs end-of-turn cleanup. Discards all cards currently in hand (both played and
     * unplayed), then draws a new hand of 5 cards.
     */
    public void cleanup() {
        hand.moveAllCardsTo(discard); // Discard all cards in hand
        for (int i = 0; i < HAND_SIZE; i++) {
            drawCard(); // Draw new hand of 5 cards
        }
    }

    /**
     * Gains a new card (from a purchase or action effect). The card is added directly to the
     * discard pile. If the card is a victory card, the player's score is increased by its point
     * value.
     *
     * @param card The card to gain
     */
    public void gainCard(Card card) {
        discard.add(card);
        if (card.getCategory() == Card.Type.Category.VICTORY) {
            score += card.value();
        }
    }

    /**
     * Trashes a card from the player's hand. Removes the card from the unplayed cards in hand. Once
     * removed, the card is no longer in the player's possession. If the card is successfully
     * trashed and it is a victory card, the player's score is decreased by its point value.
     *
     * @param card The card to trash (must be in unplayed cards)
     * @return true if the card was successfully removed, false if not found in hand
     */
    public boolean trashCard(Card card) {
        boolean trashSuccessful = hand.removeFromUnplayed(card);
        if (trashSuccessful && card.getCategory() == Card.Type.Category.VICTORY) {
            score -= card.value();
        }
        return trashSuccessful;
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
     * Gets all action cards currently unplayed in the player's hand. Filters unplayed cards for
     * those with ACTION category.
     *
     * @return An immutable list of unplayed action cards in hand
     */
    public ImmutableList<Card> getActionsInHand() {
        List<Card> actions = new ArrayList<>();
        List<Card> unplayed = getUnplayedCards();
        for (Card card : unplayed) {
            if (card.getCategory() == Card.Type.Category.ACTION) {
                actions.add(card);
            }
        }
        return ImmutableList.copyOf(actions);
    }

    /**
     * Gets all money cards currently unplayed in the player's hand. Filters unplayed cards for
     * those with MONEY category.
     *
     * @return An immutable list of unplayed money cards in hand
     */
    public ImmutableList<Card> getMoneysInHand() {
        List<Card> money = new ArrayList<>();
        List<Card> unplayed = getUnplayedCards();
        for (Card card : unplayed) {
            if (card.getCategory() == Card.Type.Category.MONEY) {
                money.add(card);
            }
        }
        return ImmutableList.copyOf(money);
    }

    /**
     * Gets an immutable view of the player's current hand. Used for creating GameState snapshots.
     *
     * @return An immutable Hand object representing the current hand state
     */
    public Hand getImmutableHand() {
        return hand.toImmutableHand();
    }

    /**
     * Gets all cards owned by this player at the end of the game. Consolidates cards from hand,
     * deck, and discard pile into a single list. This method modifies the player's state by moving
     * all cards to the discard pile, and should only be called when the game has ended.
     *
     * @return An immutable list containing all cards owned by the player
     */
    public ImmutableList<Card> getEndingDeck() {
        // move cards to discard from hand and deck
        hand.moveAllCardsTo(discard);
        deck.forEach(discard::add);
        deck.clear();
        return ImmutableList.copyOf(discard);
    }

    /**
     * Gets the number of actions available for the current turn. Delegates to the underlying
     * TurnInfo.
     *
     * @return The number of actions remaining
     */
    public int numActions() {
        return turnInfo.getNumActions();
    }

    /**
     * Adjusts the number of actions by the specified amount. Positive values increase actions,
     * negative values decrease them. Delegates to the underlying TurnInfo.
     *
     * @param amount The amount to adjust actions by (can be positive or negative)
     */
    public void adjustActions(int amount) {
        turnInfo.adjustActions(amount);
    }

    /**
     * Gets the amount of money available for the current turn. Delegates to the underlying
     * TurnInfo.
     *
     * @return The amount of money available
     */
    public int numMoney() {
        return turnInfo.getNumMoney();
    }

    /**
     * Adjusts the amount of money by the specified amount. Positive values increase money, negative
     * values decrease it. Delegates to the underlying TurnInfo.
     *
     * @param amount The amount to adjust money by (can be positive or negative)
     */
    public void adjustMoney(int amount) {
        turnInfo.adjustMoney(amount);
    }

    /**
     * Gets the number of buy opportunities remaining for the current turn. Delegates to the
     * underlying TurnInfo.
     *
     * @return The number of buys available
     */
    public int numBuys() {
        return turnInfo.getNumBuys();
    }

    /**
     * Adjusts the number of buys by the specified amount. Positive values increase buys, negative
     * values decrease them. Delegates to the underlying TurnInfo.
     *
     * @param amount The amount to adjust buys by (can be positive or negative)
     */
    public void adjustBuys(int amount) {
        turnInfo.adjustBuys(amount);
    }

    /**
     * Resets turn resources to default values. Called at the beginning of each new turn to restore
     * starting values (1 action, 0 money, 1 buy).
     */
    public void turnReset() {
        turnInfo.reset();
    }

    /**
     * Gets the player's current score. Score is tracked incrementally as victory cards are gained
     * or trashed. Initialized to 3 to account for the starting METHOD cards in the deck.
     *
     * @return The player's current score including starting METHOD cards and any victory cards
     *     gained/trashed
     */
    public int getScore() {
        return score;
    }

    /**
     * Forfeits the game for this player, marking them as disqualified. Called when the player
     * violates game rules or makes invalid decisions.
     */
    public void forfeit() {
        disqualified = true;
    }

    /**
     * Checks whether this player has been disqualified from the game.
     *
     * @return true if the player has been disqualified, false otherwise
     */
    public boolean isDisqualified() {
        return disqualified;
    }

    /**
     * Determines if this PlayerInfo represents the same player as another object. Two PlayerInfo
     * objects are equal if they wrap the same underlying Player. Used primarily in getNextPlayer()
     * in the engine.
     *
     * @param o The object to compare with
     * @return true if both PlayerInfo objects wrap the same Player, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerInfo)) return false;
        PlayerInfo that = (PlayerInfo) o;
        return player.equals(that.player);
    }

    /**
     * Returns a hash code value for this PlayerInfo. Consistent with equals() - PlayerInfo objects
     * wrapping the same Player will have the same hash code, ensuring proper behavior in hash-based
     * collections (HashMap, HashSet, etc.).
     *
     * @return The hash code of the underlying Player
     */
    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
