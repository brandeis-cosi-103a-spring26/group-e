package edu.brandeis.cosi103a.groupe;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.A;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;

public class OurEngine implements Engine {
    private GameState.TurnPhase phase;
    private List<Player> players;
    private Map<Player, MutableHand> hands;
    private Map<Player, List<Card>> decks;
    private Map<Player, List<Card>> discards;
    private Player activePlayer;
    private int numActions;
    private int numMoney;
    private int numBuys;
    private Map<Card.Type, Integer> shop; // Mutable version of CardStacks. You can convert to CardStacks with `new
    // CardStacks(ImmutableMap.copyOf(shop))`
    private int cardIdCounter = 1;

    // Constants
    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;
    private static final int PLAYER_STARTING_BITCOINS = 7;
    private static final int PLAYER_STARTING_METHODS = 3;
    private static final int HAND_SIZE = 5;

    // Starting shop quantities
    private static final int SHOP_BITCOIN_COUNT = 60;
    private static final int SHOP_ETHEREUM_COUNT = 40;
    private static final int SHOP_DOGECOIN_COUNT = 30;
    private static final int SHOP_METHOD_COUNT = 14;
    private static final int SHOP_MODULE_COUNT = 8;
    private static final int SHOP_FRAMEWORK_COUNT_TWO_PLAYERS = 8;
    private static final int SHOP_FRAMEWORK_COUNT_THREE_FOUR_PLAYERS = 12;
    private static final int SHOP_BUG_COUNT_PER_PLAYER = 10; // Multiplied by number of players
    private static final int SHOP_REFACTOR_COUNT = 10;
    private static final int SHOP_CODE_REVIEW_COUNT = 10;
    private static final int SHOP_EVERGREEN_TEST_COUNT = 10;

    /**
     * 1 Argument constructor which initializes the game engine with the provided
     * players.
     *
     * @param players A list of players participating in the game. It must contain
     *                between 2 and 4 players. The order of players in the list
     *                determines the turn order.
     * @throws IllegalArgumentException If the list of players is null, contains
     *                                  null players, has fewer than 2 players, or
     *                                  more than 4 players.
     */
    public OurEngine(List<Player> players) throws IllegalArgumentException {
        // Throws exceptions for missing or invalid player configurations
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        // Throw exceptions for invalid number of players
        if (players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Provide at most four players");
        }
        if (players.size() < MIN_PLAYERS) {
            throw new IllegalArgumentException("Provide at least two players");
        }
        if (players.contains(null)) {
            throw new IllegalArgumentException("Players list cannot contain null players");
        }

        // Initialize fields
        phase = GameState.TurnPhase.ACTION;
        this.players = new ArrayList<>(players);
        activePlayer = this.players.get(this.players.size() - 1); // So that the first call to getNextPlayer() returns players[0]

        // Create empty hands, decks, and discards for each player
        decks = new HashMap<>();
        discards = new HashMap<>();
        hands = new HashMap<>();
        for (Player p : this.players) {
            hands.put(p, new MutableHand());
            decks.put(p, new ArrayList<>());
            discards.put(p, new ArrayList<>());
        }

        // Initialize decks with the starting cards then shuffles
        for (List<Card> deck : decks.values()) {
            for (int i = 0; i < PLAYER_STARTING_BITCOINS; i++) {
                deck.add(new Card(Type.BITCOIN, cardIdCounter++));
            }
            for (int i = 0; i < PLAYER_STARTING_METHODS; i++) {
                deck.add(new Card(Type.METHOD, cardIdCounter++));
            }
            java.util.Collections.shuffle(deck);
        }

        for (Player p : this.players) {
            // Draw initial hands of 5 cards
            var hand = hands.get(p);
            var deck = decks.get(p);
            var discard = discards.get(p);
            for (int i = 0; i < HAND_SIZE; i++) {
                drawCard(hand, deck, discard);
            }
        }

        // Initialize shop
        shop = new HashMap<>();
        shop.put(Type.BITCOIN, SHOP_BITCOIN_COUNT);
        shop.put(Type.ETHEREUM, SHOP_ETHEREUM_COUNT);
        shop.put(Type.DOGECOIN, SHOP_DOGECOIN_COUNT);
        shop.put(Type.METHOD, SHOP_METHOD_COUNT);
        shop.put(Type.MODULE, SHOP_MODULE_COUNT);
        shop.put(Type.BUG, this.players.size() * SHOP_BUG_COUNT_PER_PLAYER);
        shop.put(Type.REFACTOR, SHOP_REFACTOR_COUNT);
        shop.put(Type.CODE_REVIEW, SHOP_CODE_REVIEW_COUNT);
        shop.put(Type.EVERGREEN_TEST, SHOP_EVERGREEN_TEST_COUNT);
        // Set FRAMEWORK count based on the number of players.
        shop.put(Type.FRAMEWORK, (this.players.size() == 2) ? SHOP_FRAMEWORK_COUNT_TWO_PLAYERS : SHOP_FRAMEWORK_COUNT_THREE_FOUR_PLAYERS);
    }

    /**
     * Main game loop which manages player turns and phases until the game ends.
     *
     * @return The final result of the game encapsulated in a GameResult object.
     * @throws PlayerViolationException if a player makes an invalid decision.
     */
    @Override
    public GameResult play() throws PlayerViolationException {
        while (!isGameOver()) {
            // get next player, sets phase to ACTION, and resets actions, money, and buys
            activePlayer = getNextPlayer();
            phase = GameState.TurnPhase.ACTION;
            numActions = 1;
            numMoney = 0;
            numBuys = 1;

            // A turn consists of multiple runs through this loop, until the CLEANUP phase
            // is reached, then executeCleanupPhase() is called.
            // won't work until everything called in the loop is implemented,
            while (phase != GameState.TurnPhase.CLEANUP) {
                try {
                    // Build decision options and game state
                    ImmutableList<Decision> options = buildDecisionOptions();
                    GameState gs = buildGameState();

                    // Get decision from active player
                    Decision decision = activePlayer.makeDecision(gs, options);

                    // Process decision, this is where the game state should be updated, changing
                    // phases as necessary
                    processDecision(decision, options);

                } catch (PlayerViolationException e) {
                    handleCheating(activePlayer, e);
                }
            }
            // Cleanup phase
            executeCleanupPhase();
        }
        return endGame();
    }

    private void handleCheating(Player cheater, PlayerViolationException e) {
        // TODO future week
        throw new UnsupportedOperationException("Unimplemented method 'handleCheating'");
    }

    /**
     * Executes the cleanup phase, discarding the active player's hand and drawing a
     * new hand of five cards.
     */
    private void executeCleanupPhase() {
        // TODO: Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Processes the decision made by the active player, updating the game state
     * accordingly.
     *
     * @param decision       The decision made by the active player.
     * @param validDecisions The list of valid decisions for the current game state.
     * @throws PlayerViolationException if the decision is invalid.
     */
    private void processDecision(Decision decision, ImmutableList<Decision> validDecisions)
            throws PlayerViolationException {
        // Throws a PlayerViolationException if the decision is invalid
        if (!validateDecision(decision, validDecisions)) {
            throw new PlayerViolationException("Invalid decision made by player");
        }

        // Delegates the Decision based on its type
        switch (decision) {
            case BuyDecision buy -> processBuyDecision(buy);
            case EndPhaseDecision endPhase -> processEndPhaseDecision(endPhase);
            case GainCardDecision gain -> processGainCardDecision(gain);
            case PlayCardDecision play -> processPlayCardDecision(play);
            case TrashCardDecision trash -> processTrashCardDecision(trash);
            default -> throw new UnsupportedOperationException("Decision type not supported");
        }
    }

    /**
     * Removes a card from the game based on the TrashCardDecision. Trashing a card
     * removes it from the player's deck entirely, and does not return it to the
     * supply pile.
     *
     * @param decision The TrashCardDecision containing the card to be trashed.
     */
    private void processTrashCardDecision(TrashCardDecision decision) {
        // TODO: Ana
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Processes the EndPhaseDecision made by the active player, transitioning to
     * the next phase of the turn.
     *
     * @param decision The EndPhaseDecision made by the active player.
     */
    private void processEndPhaseDecision(EndPhaseDecision decision) {
        // TODO: Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Processes the PlayCardDecision made by the active player
     * by calling the process card method for Action or Money cards
     *
     * @param decision The PlayCardDecision made by the active player.
     */
    private void processPlayCardDecision(PlayCardDecision decision) {

        Card card = decision.card();

        // Move card to played cards in hand
        hands.get(activePlayer).playCard(card);
        
        switch (card.getCategory()) {
            case ACTION -> processPlayActionCard(card.type());
            case MONEY -> processPlayMoneyCard(card.type());
            default -> throw new UnsupportedOperationException("Unsupported card category");
        }
    }

    /**
     * Plays an action card
     * Calls the appropriate execute action method based on card type
     * Subtracts one action from player
     * @param type
     */
    private void processPlayActionCard(Card.Type type) {

        // This should never be possible as Player shouldn't be offered to play action card if no remaining actions
        if (numActions <= 0) {
            throw new IllegalStateException("No actions remaining to play an action card");
        }

        switch (type) {
            case CODE_REVIEW -> executeActionCodeReview();
            case REFACTOR -> executeActionRefactor();
            case EVERGREEN_TEST -> executeActionEvergreenTest();
            default -> throw new UnsupportedOperationException("Unsupported action card type");
        }

        // Subtract one action from player
        numActions -= 1;

    }

    /**
     * Updates numMoney based on the type of money card played
     * @param type
     */
    private void processPlayMoneyCard(Card.Type type) {
        switch (type) {
            case BITCOIN -> numMoney += 1;
            case ETHEREUM -> numMoney += 2;
            case DOGECOIN -> numMoney += 3;
            default -> throw new UnsupportedOperationException("Unsupported money card type");
        }
    }

    // --- ACTION IMPLEMENTATIONS ---

    /**
     * +1 Card, +2 Actions.
     * When this card is played, the player immediately draws a card, and is granted two additional actions for this turn.
     */
    private void executeActionCodeReview() {
        // Calls Draw card for current player
        // Adds 2 to numActions
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Trash a card from your hand; Gain a card costing up to 2 more than the trashed card.
     * 
     * Trashing a card removes it from the player's deck entirely
     * When this card is played, the player will be prompted with TrashCardDecisions for each card in their hand. 
     * Trashing is not optional, so the list of decisions will not include an EndPhaseDecision. 
     * After the player makes a trash decision, they will be prompted with GainCardDecisions representing the cards they can possibly gain - every card that costs up to 2 more than the trashed card.
     * Gaining a card is also not optional. Gained cards are placed into the player's discard pile.
     */
    private void executeActionRefactor() {
        
        // Creates trashCardDecision for each card in players hand and prompts them. Not optional unless no cards in hand.
        // (If they did not trash a card, action ends here)
        // After card is trashed, prompt for gain card up to 2 more. This is required unless there are no valid cards to gain (highly unlikely, as there are many bitcoins)

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * (Attack card) +2 Cards; Each other player gains a Bug.
     * When this card is played, the engine immediately adds a BUG to the discard pile of each other player.
     */
    private void executeActionEvergreenTest() {

        // Draws 2 cards for current player
        // For each other player, add a BUG card to their discard pile
        throw new UnsupportedOperationException("Not yet implemented");

    }


    /**
     * Helper method to process gaining a card, used for when a player buys a card, or gains a card from an action effect.
     * This method should reduce the shop count of the gained card by 1, and add a newly instantiated card to the player's discard pile.
     * @param type the Card Type to be gained
     * @param player the Player gaining the card
     */
    private void processGainingCard(Card.Type type, Player player) {

        // Ensure the card is available in the shop
        // Reduces shop count of the card by 1
        // Creates instance of Card and adds the card to the player's discard pile 

        /*
        Uses:
        - Buying a card
        - Gaining a card from a gainDecision (refactor)
        - Gaining a card from an action (evergreen test) 
        */
        throw new UnsupportedOperationException("Not yet implemented");

    }

    /**
     * Adds the card specified in the GainCardDecision to the active player's
     * discard pile by calling processGainingCard
     *
     * @param decision The GainCardDecision containing the card to be gained.
     */
    private void processGainCardDecision(GainCardDecision decision) {
        // TODO: Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Processes the BuyDecision made by the active player, should subtract money
     * and buys, then add the purchased card to the player's discard pile.
     * Make sure to update the shop inventory, numMoney, and numBuys.
     *
     * @param decision The BuyDecision made by the active player.
     */
    private void processBuyDecision(BuyDecision decision) {
        // TODO: Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Validates whether the decision made by the active player is among the list of
     * valid decisions.
     *
     * @param decision       The decision made by the active player.
     * @param validDecisions The list of valid decisions for the current game state.
     * @return true if the decision is valid, false otherwise.
     */
    private boolean validateDecision(Decision decision, ImmutableList<Decision> validDecisions) {
        // TODO: Ana or Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }




    /**
     * Gets the next player in turn order.
     *
     * @return The next player in turn order.
     */
    @VisibleForTesting
    Player getNextPlayer() {
        return players.get((players.indexOf(activePlayer) + 1) % players.size());
    }

    /**
     * Builds the list of decision options available to the active player based on
     * the current phase of the turn.
     *
     * @return An ImmutableList of Decision objects representing the available
     *         options.
     * @throws UnsupportedOperationException if the current phase is invalid.
     */
    private ImmutableList<Decision> buildDecisionOptions() throws UnsupportedOperationException {
        // Delegates to phase-specific decision builders
        switch (phase) {
            case ACTION:
                return buildActionDecisions();
            case BUY:
                return buildBuyDecisions();
            case MONEY:
                return buildMoneyDecisions();
            case CLEANUP:
                throw new UnsupportedOperationException("No decisions available during CLEANUP phase");
            case GAIN:
                throw new UnsupportedOperationException("Call buildGainDecisions directly");
            default:
                throw new UnsupportedOperationException("Invalid phase for building decision options");
        }
    }

    /**
     * Executes a temporary GAIN phase triggered by an action card.
     *
     * @param triggeredBy The Type of card that triggered the GAIN phase.
     * @throws PlayerViolationException if the active player makes an invalid
     *                                  decision during the GAIN phase.
     * @throws IllegalStateException    if the current phase is not ACTION.
     */
    private void executeGainPhase(Type triggeredBy) throws PlayerViolationException, IllegalStateException {
        if (phase != GameState.TurnPhase.ACTION) {
            throw new IllegalStateException("Can only execute temporary GAIN phase from ACTION phase");
        }
        // Swaps to GAIN and builds decisions and gameState
        phase = GameState.TurnPhase.GAIN;
        ImmutableList<Decision> options = buildGainDecisions(triggeredBy);
        GameState gs = buildGameState();

        Decision decision = activePlayer.makeDecision(gs, options);

        processDecision(decision, options);

        // Returns to the previous phase.
        phase = GameState.TurnPhase.ACTION;
    }

    /**
     * Builds the list of all valid decisions for the MONEY phase.
     *
     * @return An ImmutableList of Decision objects for the MONEY phase.
     */
    private ImmutableList<Decision> buildMoneyDecisions() {
        ArrayList<Decision> currencyDecisions = new ArrayList<>();
        List<Card> unplayedCards = hands.get(activePlayer).getUnplayedCards();
        for (Card card : unplayedCards) {
            if (card.getCategory() == Card.Type.Category.MONEY) {
                currencyDecisions.add(new PlayCardDecision(card));
            }
        }
        currencyDecisions.add(new EndPhaseDecision(TurnPhase.MONEY));
        return ImmutableList.copyOf(currencyDecisions);
    }

    /**
     * Builds the list of all valid decisions for the GAIN phase, based on the card
     * that triggered the gain.
     *
     * @return An ImmutableList of Decision objects for the GAIN phase.
     */
    private ImmutableList<Decision> buildGainDecisions(Type triggeredBy) {
        // TODO: Oneg: Likely a future week issue since it's related to action
        // functionality
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds the list of all valid decisions for the BUY phase.
     *
     * @return An ImmutableList of Decision objects for the BUY phase.
     */
    private ImmutableList<Decision> buildBuyDecisions() {
        // TODO: Oneg
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds the list of all valid decisions for the ACTION phase.
     *
     * @return An ImmutableList of Decision objects for the ACTION phase.
     */
    private ImmutableList<Decision> buildActionDecisions() {
        ArrayList<Decision> actDecisions = new ArrayList<>();
        List<Card> unplayedCards = hands.get(activePlayer).getUnplayedCards();
        for (Card card : unplayedCards) {
            if (card.getCategory() == Card.Type.Category.ACTION) {
                actDecisions.add(new PlayCardDecision(card));
            }
        }
        actDecisions.add(new EndPhaseDecision(TurnPhase.ACTION));
        return ImmutableList.copyOf(actDecisions);
    }

    /**
     * Builds the current GameState object representing the state of the game.
     *
     * @return The current GameState object.
     */
    private GameState buildGameState() {
        // TODO: Oneg or Ana
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Checks if the game is over based on the current state of the shop.
     *
     * @return true if the FRAMEWORK cards are sold out or do not exist, false
     *         otherwise.
     */
    @VisibleForTesting
    boolean isGameOver() {
        return getShopQuantity(Type.FRAMEWORK) == 0;
    }

    /**
     * Handles end-of-game procedures and compiles the final GameResult.
     *
     * @return The final GameResult object.
     */
    private GameResult endGame() {
        // TODO: Future Week
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Draws a card from the deck into the hand. If the deck is empty, shuffles the
     * discard pile into the deck first.
     *
     * @param hand    The MutableHand representing the player's hand.
     * @param deck    The list representing the player's deck.
     * @param discard The list representing the player's discard pile.
     */
    @VisibleForTesting
    void drawCard(MutableHand hand, List<Card> deck, List<Card> discard) {
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

    @VisibleForTesting
    int getShopQuantity(Type type) {
        return shop.getOrDefault(type, 0);
    }
}