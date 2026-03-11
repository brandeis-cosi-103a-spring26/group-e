package edu.brandeis.cosi103a.groupe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.state.PlayerResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OurEngine implements Engine {
    private GameState.TurnPhase phase;
    private List<PlayerInfo>
            players; // PlayerInfo wrappers for each player, maintaining their game state (hand,
    // deck, discard, turn resources)
    private PlayerInfo activePlayer;
    private MutableCardStacks
            shop; // Mutable version of CardStacks, convert to an immutable CardStacks with
    // `shop.toImmutableCardStacks()`
    private AtomicInteger cardIdCounter = new AtomicInteger(1);

    // --- Turn logging ---
    private int turnNumber = 0;
    private TurnLog currentTurnLog = null;
    private final List<TurnLog> allTurnLogs = new ArrayList<>();
    // Tracks treasure cards played during the money phase for the log summary.
    private Map<Card.Type, Integer> treasuresPlayedThisTurn = new LinkedHashMap<>();
    private int moneyFromTreasuresThisTurn = 0;

    // Constants
    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    /**
     * 1 Argument constructor which initializes the game engine with the provided players. Each
     * Player is wrapped in a PlayerInfo object to manage their game state (hand, deck, discard
     * pile, turn resources, score, and disqualification status).
     *
     * @param players A list of players participating in the game. It must contain between 2 and 4
     *     players. The order of players in the list determines the turn order.
     * @throws IllegalArgumentException If the list of players is null, contains null players, has
     *     fewer than 2 players, or more than 4 players.
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
        this.players = new ArrayList<>();
        // Wrap each Player in a PlayerInfo to manage their game state
        for (Player p : players) {
            this.players.add(new PlayerInfo(p, cardIdCounter));
        }
        // Set activePlayer to the last player so getNextPlayer() returns the first player
        activePlayer = this.players.get(this.players.size() - 1);

        ImmutableSet<Type> actionCardsForGame =
                ImmutableSet.of(Type.CODE_REVIEW, Type.REFACTOR, Type.EVERGREEN_TEST);

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
            // Get next PlayerInfo, set phase to ACTION, and reset turn resources
            activePlayer = getNextPlayer();
            turnNumber++;
            currentTurnLog = new TurnLog(turnNumber, activePlayer.getName());
            allTurnLogs.add(currentTurnLog);
            // Keep memory usage bounded to the last 10 turns
            if (allTurnLogs.size() > 10) {
                allTurnLogs.remove(0);
            }
            treasuresPlayedThisTurn = new LinkedHashMap<>();
            moneyFromTreasuresThisTurn = 0;
            phase = GameState.TurnPhase.ACTION;
            activePlayer.turnReset();

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
                    phase = GameState.TurnPhase.CLEANUP;
                }
            }
            // Cleanup phase
            activePlayer.cleanup();
        }
        GameResult finalResult = endGame();
        Renderer.printGameResult(finalResult);
        return finalResult;
    }

    /**
     * Handles when a player makes an invalid decision or violates game rules. Marks the player as
     * disqualified by calling forfeit() on their PlayerInfo.
     *
     * @param cheater The PlayerInfo of the player who violated the rules
     * @param e The exception detailing the violation
     */
    private void handleCheating(PlayerInfo cheater, PlayerViolationException e) {
        cheater.forfeit();
    }

    /**
     * Processes the decision made by the active player, updating the game state accordingly.
     *
     * @param decision The decision made by the active player.
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
            case PlayCardDecision play -> processPlayCardDecision(play);
            case TrashCardDecision trash -> processTrashCardDecision(trash);
            default -> throw new UnsupportedOperationException("Decision type not supported");
        }
    }

    /**
     * Processes trashing a card, permanently removing it from the active player's hand. The trashed
     * card is removed from the game entirely, not returned to the supply. If the trashed card is a
     * victory card, the player's score is decreased by the card's point value.
     *
     * @param decision Decision specifying which card to trash
     * @throws IllegalStateException if the card is not found in the player's unplayed cards
     */
    private void processTrashCardDecision(TrashCardDecision decision) {
        Card cardToTrash = decision.card();
        boolean wasRemoved = activePlayer.trashCard(cardToTrash);
        if (!wasRemoved) {
            throw new IllegalStateException(
                    "Card to trash was not found in active player's hand's unplayed cards");
        }
    }

    /**
     * Processes the EndPhaseDecision made by the active player, transitioning to the next phase of
     * the turn. Throws an IllegalArgumentException if the phase specified in the EndPhaseDecision
     * does not match the current phase, and throws an UnsupportedOperationException if the current
     * phase cannot be ended (CLEANUP or GAIN).
     *
     * @param decision The EndPhaseDecision made by the active player.
     * @throws IllegalArgumentException if the phase in the EndPhaseDecision does not match the
     *     current phase.
     * @throws UnsupportedOperationException if the current phase cannot be ended (CLEANUP or GAIN).
     */
    private void processEndPhaseDecision(EndPhaseDecision decision)
            throws IllegalArgumentException, UnsupportedOperationException {
        // Validate that the phase in the EndPhaseDecision matches the current phase
        if (decision.phase() != phase) {
            throw new IllegalArgumentException(
                    "EndPhaseDecision phase does not match current phase");
        }

        // Go to the next phase based on the current phase
        switch (decision.phase()) {
            case ACTION -> phase = GameState.TurnPhase.MONEY;
            case MONEY -> {
                if (currentTurnLog != null && !treasuresPlayedThisTurn.isEmpty()) {
                    currentTurnLog.addEntry(buildMoneyLogEntry());
                }
                phase = GameState.TurnPhase.BUY;
            }
            case BUY -> phase = GameState.TurnPhase.CLEANUP;
            default ->
                    throw new UnsupportedOperationException("Cannot end phase from current phase");
        }
    }

    /**
     * Processes the PlayCardDecision made by the active player by calling the process card method
     * for Action or Money cards
     *
     * @param decision The PlayCardDecision made by the active player.
     * @throws PlayerViolationException if the player makes an ivalid decision while processing the
     *     card.
     * @throws UnsupportedOperationException if the card category is not supported.
     */
    private void processPlayCardDecision(PlayCardDecision decision)
            throws PlayerViolationException, UnsupportedOperationException {

        Card card = decision.card();

        // Move card to played cards in hand
        activePlayer.playCard(card);

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
     * @throws IllegalStateException if no actions remain for the active player
     * @throws UnsupportedOperationException if the action card type is not supported.
     */
    private void processPlayActionCard(Card.Type type)
            throws PlayerViolationException, UnsupportedOperationException {

        // Note: under current implementation, when PARALLELIZATION (Throne Room) is
        // immplemented,
        // it must add 1 to numActions before calling this method again,
        // to avoid the thrown error and to keep numActions correct.
        if (activePlayer.numActions() <= 0) {
            throw new IllegalStateException("No actions remaining for active player");
        }

        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Played " + cardDisplayName(type));
        }

        switch (type) {
            case CODE_REVIEW -> executeActionCodeReview();
            case REFACTOR -> executeActionRefactor();
            case EVERGREEN_TEST -> executeActionEvergreenTest();
            default -> throw new UnsupportedOperationException("Unsupported action card type");
        }

        // Subtract one action from player
        activePlayer.adjustActions(-1);
    }

    /**
     * Processes a money card, adding its value to the player's spendable money for this turn.
     *
     * @param type The type of money card being played (BITCOIN adds 1, ETHEREUM adds 2, DOGECOIN
     *     adds 3)
     * @throws UnsupportedOperationException if the money card type is not supported.
     */
    private void processPlayMoneyCard(Card.Type type) throws UnsupportedOperationException {
        int value = 0;
        switch (type) {
            case BITCOIN -> {
                activePlayer.adjustMoney(1);
                value = 1;
            }
            case ETHEREUM -> {
                activePlayer.adjustMoney(2);
                value = 2;
            }
            case DOGECOIN -> {
                activePlayer.adjustMoney(3);
                value = 3;
            }
            default -> throw new UnsupportedOperationException("Unsupported money card type");
        }
        treasuresPlayedThisTurn.merge(type, 1, Integer::sum);
        moneyFromTreasuresThisTurn += value;
    }

    // --- ACTION IMPLEMENTATIONS ---

    /**
     * +1 Card, +2 Actions. When this card is played, the player immediately draws a card, and is
     * granted two additional actions for this turn.
     */
    private void executeActionCodeReview() {
        activePlayer.drawCard();
        activePlayer.adjustActions(2);
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  +1 Card, +2 Actions");
        }
    }

    /**
     * Trash a card from your hand; Gain a card costing up to 2 more than the trashed card.
     *
     * <p>Trashing a card removes it from the player's deck entirely. If the trashed card is a
     * victory card, the player's score decreases by the card's point value. When this card is
     * played, the player will be prompted with TrashCardDecisions for each card in their hand.
     * Trashing is not optional, so the list of decisions will not include an EndPhaseDecision.
     * After the player makes a trash decision, they will be prompted with GainCardDecisions
     * representing the cards they can possibly gain - every card that costs up to 2 more than the
     * trashed card. Gaining a card is also not optional. Gained cards are placed into the player's
     * discard pile.
     *
     * @throws PlayerViolationException if the player makes an invalid decision during trash or gain
     *     phases.
     */
    private void executeActionRefactor() throws PlayerViolationException {

        ImmutableList<Decision> trashOptions = buildTrashCardDecisionsForCurrentHand();
        Decision playerChoice = makeValidDecision(activePlayer, trashOptions);

        if (playerChoice instanceof EndPhaseDecision) {
            // Player had no cards in hand to trash. End action execution here
            return;
        }

        // Cast to TrashCardDecision and process the trashing of the card
        TrashCardDecision trashCardDecision = (TrashCardDecision) playerChoice;
        if (currentTurnLog != null) {
            currentTurnLog.addEntry(
                    "  Trashed " + cardDisplayName(trashCardDecision.card().type()));
        }
        processTrashCardDecision(trashCardDecision);

        int cardCost = trashCardDecision.card().cost();
        int maxGainCost = cardCost + 2;

        // Create list of gain decisions based on all cards available to gain in the
        // cardstacks that cost <= max cost
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
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  Gained " + cardDisplayName(gainCardDecision.cardType()));
        }
        processGainingCard(gainCardDecision.cardType(), activePlayer);
        phase = TurnPhase.ACTION; // Reset phase to action
    }

    /**
     * (Attack card) +2 Cards; Each other player gains a Bug. When this card is played, the engine
     * immediately adds a BUG to the discard pile of each other player.
     */
    private void executeActionEvergreenTest() {

        // Draws 2 cards for current player
        activePlayer.drawCard();
        activePlayer.drawCard();
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  +2 Cards; each other player gains a Bug");
        }

        // For each other player, add a BUG card to their discard pile in turn order
        // starting from the next player
        for (int i = 1; i < players.size(); i++) {
            int playerIndex = (players.indexOf(activePlayer) + i) % players.size();
            PlayerInfo p = players.get(playerIndex);
            // Skip disqualified players, they do not gain the bug
            if (!p.isDisqualified()) {
                processGainingCard(Type.BUG, p);
            }
        }
    }

    /**
     * Builds decision options for trashing cards from the active player's current hand. Returns
     * TrashCardDecisions for each card, or EndPhaseDecision if hand is empty.
     *
     * @return List of trash decisions, or a single EndPhaseDecision if no cards available
     */
    private ImmutableList<Decision> buildTrashCardDecisionsForCurrentHand() {
        ArrayList<Decision> trashyDecisions = new ArrayList<>();
        List<Card> unplayedCards = activePlayer.getUnplayedCards();
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
     * Processes a card gain by decrementing shop supply and adding the card to a player's discard
     * pile. Returns false if the card type is unavailable in the shop.
     *
     * @param type The card type to be gained
     * @param player The PlayerInfo of the player receiving the card
     * @return true if the card was successfully gained, false if unavailable in shop
     */
    private boolean processGainingCard(Card.Type type, PlayerInfo player) {

        if (shop.getNumAvailable(type) <= 0) {
            // There are no cards to gain
            // Note – this does not necessarily signify an illegal state. For example, this
            // will happen if Evergreen Test attempts to give a player a Bug, but there are
            // none left in the supply
            return false;
        }

        shop.decrementCount(type);

        // Create new card instance and add to player's discard pile
        Card gainedCard = new Card(type, cardIdCounter.getAndIncrement());
        player.gainCard(gainedCard);
        return true;
    }

    /**
     * Processes the BuyDecision made by the active player, should subtract money and buys, then add
     * the purchased card to the player's discard pile. Make sure to update the shop inventory,
     * numMoney, and numBuys.
     *
     * @param decision The BuyDecision made by the active player.
     * @throws IllegalStateException if the card is out of stock, the player has insufficient money,
     *     or the player has no buys remaining.
     */
    private void processBuyDecision(BuyDecision decision) {
        Type type = decision.cardType();

        // Validate that the card is available in the shop, and that the player has
        // enough money and buys to purchase it
        if (shop.getNumAvailable(type) <= 0) {
            throw new IllegalStateException("Cannot buy card that is out of stock");
        }
        if (activePlayer.numMoney() < type.cost()) {
            throw new IllegalStateException("Not enough money to buy this card");
        }
        if (activePlayer.numBuys() <= 0) {
            throw new IllegalStateException("No buys remaining to buy a card");
        }

        // Update money and buys
        activePlayer.adjustMoney(-type.cost());
        activePlayer.adjustBuys(-1);

        // Update shop and add card to player's discard pile
        processGainingCard(type, activePlayer);
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Bought " + cardDisplayName(type));
        }
    }

    /**
     * Validates whether the decision made by the active player is among the list of valid
     * decisions.
     *
     * @param decision The decision made by the active player.
     * @param validDecisions The list of valid decisions for the current game state.
     * @return true if the decision is valid, false otherwise or if either argument is null.
     */
    private boolean validateDecision(Decision decision, ImmutableList<Decision> validDecisions) {
        if (decision == null || validDecisions == null) {
            return false;
        }
        return validDecisions.contains(decision);
    }

    /**
     * Prompts a player to make a decision and validates it against the list of valid options.
     *
     * @param player The PlayerInfo of the player making the decision
     * @param validDecisions List of valid decision options for the current game state
     * @return A validated decision that is guaranteed to be in the validDecisions list
     */
    private Decision makeValidDecision(PlayerInfo player, ImmutableList<Decision> validDecisions)
            throws PlayerViolationException {
        Decision decision = player.makeDecision(buildGameState(), validDecisions);
        if (!validateDecision(decision, validDecisions)) {
            String decisionStr = (decision == null) ? "null" : decision.toString();
            throw new PlayerViolationException("Player made an invalid decision: " + decisionStr);
        }
        return decision;
    }

    /**
     * Gets the next PlayerInfo in turn order, skipping disqualified players.
     *
     * @return The next PlayerInfo in turn order
     * @throws IllegalStateException if all but one player is disqualified
     */
    @VisibleForTesting
    PlayerInfo getNextPlayer() throws IllegalStateException {
        if (players.stream().filter(PlayerInfo::isDisqualified).count() >= players.size() - 1) {
            throw new IllegalStateException(
                    "All but one player is disqualified, game should be over");
        }
        PlayerInfo nextPlayer = players.get((players.indexOf(activePlayer) + 1) % players.size());
        while (nextPlayer.isDisqualified()) {
            nextPlayer = players.get((players.indexOf(nextPlayer) + 1) % players.size());
        }
        return nextPlayer;
    }

    /**
     * Builds the list of decision options available to the active player based on the current phase
     * of the turn.
     *
     * @return An ImmutableList of Decision objects representing the available options.
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
                throw new UnsupportedOperationException(
                        "No decisions available during CLEANUP phase");
            case GAIN:
                throw new UnsupportedOperationException("Call buildGainDecisions directly");
            default:
                throw new UnsupportedOperationException(
                        "Invalid phase for building decision options");
        }
    }

    /**
     * Builds the list of all valid decisions for the MONEY phase.
     *
     * @return An ImmutableList of Decision objects for the MONEY phase.
     */
    private ImmutableList<Decision> buildMoneyDecisions() {
        ArrayList<Decision> currencyDecisions = new ArrayList<>();
        List<Card> moneysInHand = activePlayer.getMoneysInHand();
        for (Card card : moneysInHand) {
            currencyDecisions.add(new PlayCardDecision(card));
        }
        currencyDecisions.add(new EndPhaseDecision(TurnPhase.MONEY));
        return ImmutableList.copyOf(currencyDecisions);
    }

    /**
     * Builds the list of all valid decisions for the BUY phase.
     *
     * @return An ImmutableList of Decision objects for the BUY phase.
     */
    private ImmutableList<Decision> buildBuyDecisions() {

        ImmutableList.Builder<Decision> buyDecisions = ImmutableList.builder();

        if (activePlayer.numBuys() <= 0) {
            buyDecisions.add(new EndPhaseDecision(TurnPhase.BUY));
            return buyDecisions.build();
        }

        ImmutableSet<Card.Type> availableCards =
                shop.availableCardsForCost(activePlayer.numMoney());
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
        if (activePlayer.numActions() <= 0) {
            actDecisions.add(new EndPhaseDecision(TurnPhase.ACTION));
            return ImmutableList.copyOf(actDecisions);
        }
        List<Card> actionsInHand = activePlayer.getActionsInHand();
        for (Card card : actionsInHand) {
            actDecisions.add(new PlayCardDecision(card));
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
        GameState gamestate =
                new GameState(
                        activePlayer.getName(),
                        activePlayer.getImmutableHand(),
                        phase,
                        activePlayer.numActions(),
                        activePlayer.numMoney(),
                        activePlayer.numBuys(),
                        shop.toImmutableCardStacks());
        return gamestate;
    }

    /**
     * Checks if the game is over based on the current state of the shop.
     *
     * @return true if the FRAMEWORK cards are sold out or do not exist, false otherwise.
     */
    @VisibleForTesting
    boolean isGameOver() {
        long disqualifiedPlayerCount = players.stream().filter(PlayerInfo::isDisqualified).count();
        if (disqualifiedPlayerCount >= players.size() - 1) {
            return true;
        }
        return shop.getNumAvailable(Type.FRAMEWORK) == 0;
    }

    /**
     * Handles end-of-game procedures and compiles the final GameResult. Sorts PlayerInfo objects by
     * score and creates PlayerResult for each.
     *
     * @return The final GameResult object containing scores and final decks for all players
     */
    private GameResult endGame() {
        ImmutableList.Builder<PlayerResult> playerResultsBuilder = ImmutableList.builder();
        // Sort PlayerInfo objects by score in descending order for nicer end game results display
        players.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        for (PlayerInfo p : players) {
            playerResultsBuilder.add(
                    new PlayerResult(p.getName(), p.getScore(), p.getEndingDeck()));
        }
        GameResult result = new GameResult(playerResultsBuilder.build());
        return result;
    }

    /**
     * Returns an unmodifiable view of the last 10 turn histories, including the
     * currently-in-progress turn. Entries are appended live as events occur, so callers holding a
     * reference will always see the latest state.
     */
    public List<TurnLog> getTurnLogs() {
        return java.util.Collections.unmodifiableList(allTurnLogs);
    }

    /** Returns the total number of players in this game. */
    public int getNumPlayers() {
        return players.size();
    }

    /**
     * Converts a {@link Card.Type} to a human-readable Title Case name, e.g. {@code CODE_REVIEW →
     * "Code Review"}.
     */
    private static String cardDisplayName(Card.Type type) {
        return Renderer.displayName(type);
    }

    /**
     * Builds the money-phase summary log entry, e.g. {@code "Played 3 Bitcoin and 1 Ethereum
     * (+5)"}.
     */
    private String buildMoneyLogEntry() {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Card.Type, Integer> e : treasuresPlayedThisTurn.entrySet()) {
            parts.add(e.getValue() + " " + cardDisplayName(e.getKey()));
        }
        return "Played " + String.join(" and ", parts) + " (+" + moneyFromTreasuresThisTurn + ")";
    }
}
