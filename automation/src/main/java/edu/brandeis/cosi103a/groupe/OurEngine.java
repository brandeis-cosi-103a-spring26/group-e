package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
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

    /**
     * 1 Argument constructor which initializes the game engine with the provided
     * players.
     *
     * @param players A list of players participating in the game. It must contain
     *                between 2 and 4 players. The order of players in the list
     *                determines the turn order.
     * @throws IllegalArgumentException if the number of players is less than 2 or
     *                                  more than 4.
     */
    public OurEngine(List<Player> players) throws IllegalArgumentException {
        // Throws exceptions for missing or invalid player configurations
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > 4) {
            throw new IllegalArgumentException("Provide at most four players");
        }
        if (players.size() <= 1) {
            throw new IllegalArgumentException("Provide at least two players");
        }
        if (players.contains(null)) {
            throw new IllegalArgumentException("Players list cannot contain null players");
        }

        // Initialize fields
        phase = GameState.TurnPhase.ACTION;
        this.players = new ArrayList<>(players);
        activePlayer = this.players.get(this.players.size() - 1); // So that the first call to getNextPlayer() returns
                                                                  // players[0]
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
            for (int i = 0; i < 7; i++) {
                deck.add(new Card(Type.BITCOIN, cardIdCounter++));
            }
            for (int i = 0; i < 3; i++) {
                deck.add(new Card(Type.METHOD, cardIdCounter++));
            }
            java.util.Collections.shuffle(deck);
        }

        for (Player p : this.players) {
            // Draw initial hands of 5 cards
            var hand = hands.get(p);
            var deck = decks.get(p);
            var discard = discards.get(p);
            for (int i = 0; i < 5; i++) {
                drawCard(hand, deck, discard);
            }
        }

        // Initialize shop
        shop = new HashMap<>();
        shop.put(Type.BITCOIN, 60);
        shop.put(Type.ETHEREUM, 40);
        shop.put(Type.DOGECOIN, 30);
        shop.put(Type.METHOD, 14);
        shop.put(Type.MODULE, 8);
        shop.put(Type.FRAMEWORK, 8);
        shop.put(Type.BUG, this.players.size() * 10);
        shop.put(Type.REFACTOR, 10);
        shop.put(Type.CODE_REVIEW, 10);
        shop.put(Type.EVERGREEN_TEST, 10);
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
     * Processes the PlayCardDecision made by the active player, updating the game
     * state accordingly.
     * Make sure to modify numActions, numMoney, and numBuys as necessary.
     * If an action triggers a GAIN phase, call executeGainPhase with the
     * appropriate card.
     *
     * @param decision The PlayCardDecision made by the active player.
     */
    private void processPlayCardDecision(PlayCardDecision decision) {
        // TODO: Ana
        // Should probably delegate to different methods based on card type
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Adds the card specified in the GainCardDecision to the active player's
     * discard pile.
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
        // TODO: Ana
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Ana
        throw new UnsupportedOperationException("Not yet implemented");
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