package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.state.CardStacks;
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
    private MutableCardStacks shop; // Mutable version of CardStacks, convert to an immutable CardStacks with `shop.toImmutableCardStacks()`
    private int cardIdCounter = 1;


    // Constants
    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;
    private static final int PLAYER_STARTING_BITCOINS = 7;
    private static final int PLAYER_STARTING_METHODS = 3;
    private static final int HAND_SIZE = 5;


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
            for (int i = 0; i < HAND_SIZE; i++) {
                drawCard(p);
            }
        }

        ImmutableSet<Type> actionCardsForGame = ImmutableSet.of(
            Type.CODE_REVIEW,
            Type.REFACTOR,
            Type.EVERGREEN_TEST
        );

        // Set up shop with starting card quantities
        shop = new MutableCardStacks(this.players.size(), actionCardsForGame);

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

    /**
     * Handles when a player makes an invalid decision or violates game rules.
     * 
     * @param cheater The player who violated the rules
     * @param e The exception detailing the violation
     */
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
     * Processes trashing a card, permanently removing it from the active player's hand.
     * The trashed card is removed from the game entirely, not returned to the supply.
     * 
     * @param decision Decision specifying which card to trash
     * @throws IllegalStateException if the card is not found in the player's unplayed cards
     */
    private void processTrashCardDecision(TrashCardDecision decision) {
        Card cardToTrash = decision.card();
        if (hands.getOrDefault(activePlayer, null) == null) {
            throw new IllegalStateException("Active player does not have a hand");
        }
        boolean wasRemoved = hands.get(activePlayer).removeFromUnplayed(cardToTrash);
        if (!wasRemoved) {
            throw new IllegalStateException("Card to trash was not found in active player's hand's unplayed cards");
        }
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
     * Processes an action card by executing its effect and decrementing the player's action count.
     * Delegates to specific execute methods based on card type.
     * 
     * @param type The type of action card being played
     */
    private void processPlayActionCard(Card.Type type) {

        // Note: under current implementation, when PARALLELIZATION (Throne Room) is immplemented,
        // it must add 1 to numActions before calling this method again,
        // to avoid the thrown error and to keep numActions correct.
        if (numActions <= 0) {
            throw new IllegalStateException("No actions remaining for active player");
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
     * Processes a money card, adding its value to the player's spendable money for this turn.
     * 
     * @param type The type of money card being played (BITCOIN adds 1, ETHEREUM adds 2, DOGECOIN adds 3)
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
        drawCard(activePlayer);
        numActions += 2;
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

        ImmutableList<Decision> trashOptions = buildTrashCardDecisionsForCurrentHand();
        Decision playerChoice = makeValidDecision(activePlayer, trashOptions);

        if (playerChoice instanceof EndPhaseDecision) {
            // Player had no cards in hand to trash. End action execution here
            return;
        }

        // Cast to TrashCardDecision and process the trashing of the card
        TrashCardDecision trashCardDecision = (TrashCardDecision) playerChoice;
        processTrashCardDecision(trashCardDecision);

        int cardCost = trashCardDecision.card().cost();
        int maxGainCost = cardCost + 2;

        // Create list of gain decisions based on all cards available to gain in the cardstacks that cost <= max cost
        ImmutableList.Builder<Decision> gainDecisionsBuilder = ImmutableList.builder();

        ImmutableSet<Type> availableCardTypes = shop.availableCardsForCost(maxGainCost);

        if (availableCardTypes.isEmpty()) {
            // If there are no possible cards to gain (unlikely) give a end phase decision
            gainDecisionsBuilder.add(new EndPhaseDecision(TurnPhase.GAIN));
        }
        
        for (Type cardType : availableCardTypes) {
            gainDecisionsBuilder.add(new GainCardDecision(cardType));
        }

        // Build the final list of gain decisions
        ImmutableList<Decision> gainDecisions = gainDecisionsBuilder.build();

        // Set phase to GAIN
        phase = TurnPhase.GAIN;
        Decision gainDecisionOrEndPhase = makeValidDecision(activePlayer, gainDecisions);
        if (gainDecisionOrEndPhase instanceof EndPhaseDecision) {
            // Player had no cards available to gain. End action execution here
            phase = TurnPhase.ACTION; // Reset phase to Action
            return;
        }

        GainCardDecision gainCardDecision = (GainCardDecision) gainDecisionOrEndPhase;
        processGainingCard(gainCardDecision.cardType(), activePlayer);
        phase = TurnPhase.ACTION; // Reset phase to action

    }

    /**
     * (Attack card) +2 Cards; Each other player gains a Bug.
     * When this card is played, the engine immediately adds a BUG to the discard pile of each other player.
     */
    private void executeActionEvergreenTest() {

        // Draws 2 cards for current player
        drawCard(activePlayer);
        drawCard(activePlayer);

        // For each other player, add a BUG card to their discard pile in turn order starting from the next player
        for (int i = 1; i < players.size(); i++) {
            int playerIndex = (players.indexOf(activePlayer) + i) % players.size();
            Player p = players.get(playerIndex);
            processGainingCard(Type.BUG, p);
        }
    }


    /**
     * Builds decision options for trashing cards from the active player's current hand.
     * Returns TrashCardDecisions for each card, or EndPhaseDecision if hand is empty.
     * 
     * @return List of trash decisions, or a single EndPhaseDecision if no cards available
     */
    private ImmutableList<Decision> buildTrashCardDecisionsForCurrentHand() {
        ArrayList<Decision> trashyDecisions = new ArrayList<>();
        List<Card> unplayedCards = hands.get(activePlayer).getUnplayedCards();
        if (unplayedCards.isEmpty()) {
            trashyDecisions.add(new EndPhaseDecision(TurnPhase.ACTION));
        } else {
            for (Card card : unplayedCards) {
                trashyDecisions.add(new TrashCardDecision(card));
            }
        }
        return ImmutableList.copyOf(trashyDecisions);
    }

    /**
     * Processes a card gain by decrementing shop supply and adding the card to a player's discard pile.
     * Returns false if the card type is unavailable in the shop.
     * 
     * @param type The card type to be gained
     * @param player The player receiving the card
     * @return true if the card was successfully gained, false if unavailable in shop
     */
    private boolean processGainingCard(Card.Type type, Player player) {

        if (shop.getNumAvailable(type) <= 0) {
            // There are no cards to gain
            // Note – this does not necessarily signify an illegal state. For example, this will happen if Evergreen Test attempts to give a player a Bug, but there are none left in the supply
            return false;
        }
        
        shop.decrementCount(type);

        // Create new card instance and add to player's discard pile
        Card gainedCard = new Card(type, cardIdCounter++);
        discards.get(player).add(gainedCard);
        return true;
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
     * Prompts a player to make a decision and validates it against the list of valid options.
     * 
     * @param player The player making the decision
     * @param validDecisions List of valid decision options for the current game state
     * @return A validated decision that is guaranteed to be in the validDecisions list
     */
    private Decision makeValidDecision(Player player, ImmutableList<Decision> validDecisions) {
        Decision decision = player.makeDecision(buildGameState(), validDecisions);
        if (!validateDecision(decision, validDecisions)) {
            throw new UnsupportedOperationException("Invalid decisions not yet handled");
            // throw new PlayerViolationException("Invalid decision made");
        }
        return decision;
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
        
        ImmutableList.Builder<Decision> buyDecisions = ImmutableList.builder();

        if (numBuys <= 0) {
            buyDecisions.add(new EndPhaseDecision(TurnPhase.BUY));
            return buyDecisions.build();
        }

        ImmutableSet<Card.Type> availableCards = shop.availableCardsForCost(numMoney);
        for (Card.Type cardType : availableCards) {
            buyDecisions.add(new BuyDecision(cardType));
        }

        buyDecisions.add(new EndPhaseDecision(TurnPhase.BUY));
        return buyDecisions.build();
    }

    /**
     * Builds the list of all valid decisions for the ACTION phase.
     *
     * @return An ImmutableList of Decision objects for the ACTION phase.
     */
    private ImmutableList<Decision> buildActionDecisions() {
        ArrayList<Decision> actDecisions = new ArrayList<>();
        if (numActions <= 0) {
            actDecisions.add(new EndPhaseDecision(TurnPhase.ACTION));
            return ImmutableList.copyOf(actDecisions);
        }
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
        GameState gamestate = new GameState(
            activePlayer.getName(),
            hands.get(activePlayer).toImmutableHand(),
            phase,
            numActions,
            numMoney,
            numBuys,
            shop.toImmutableCardStacks()
        );
        return gamestate;
    }

    /**
     * Checks if the game is over based on the current state of the shop.
     *
     * @return true if the FRAMEWORK cards are sold out or do not exist, false
     *         otherwise.
     */
    @VisibleForTesting
    boolean isGameOver() {
        return shop.getNumAvailable(Type.FRAMEWORK) == 0;
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
     * @param player The player drawing a card
     */
    private void drawCard(Player player) {
        MutableHand hand = hands.get(player);
        List<Card> deck = decks.get(player);
        List<Card> discard = discards.get(player);

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

}