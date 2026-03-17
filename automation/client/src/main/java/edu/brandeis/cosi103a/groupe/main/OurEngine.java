package edu.brandeis.cosi103a.groupe.main;

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
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.ActionContext;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OurEngine implements Engine {

    private GameState.TurnPhase phase;
    private List<PlayerInfo> players;
    private PlayerInfo activePlayer;
    private MutableCardStacks shop;
    private AtomicInteger cardIdCounter = new AtomicInteger(1);

    // --- Turn logging ---
    private int turnNumber = 0;
    private TurnLog currentTurnLog = null;
    private final List<TurnLog> allTurnLogs = new ArrayList<>();
    private Map<Card.Type, Integer> treasuresPlayedThisTurn = new LinkedHashMap<>();
    private int moneyFromTreasuresThisTurn = 0;

    // New infrastructure — used by processPlayActionCard and processGainingCard
    private ShopManager shopManager;
    private ActionEffectRegistry registry;

    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    /**
     * Initializes the game engine with the provided players. Each Player is wrapped in a PlayerInfo
     * to manage game state.
     *
     * @param players A list of 2–4 players in turn order.
     * @throws IllegalArgumentException If the list is null, too short, too long, or contains nulls.
     */
    public OurEngine(List<Player> players) throws IllegalArgumentException {
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Provide at most four players");
        }
        if (players.size() < MIN_PLAYERS) {
            throw new IllegalArgumentException("Provide at least two players");
        }
        if (players.contains(null)) {
            throw new IllegalArgumentException("Players list cannot contain null players");
        }

        phase = GameState.TurnPhase.ACTION;
        this.players = new ArrayList<>();
        for (Player p : players) {
            this.players.add(new PlayerInfo(p, cardIdCounter));
        }
        activePlayer = this.players.get(this.players.size() - 1);

        ImmutableSet<Type> actionCardsForGame =
                ImmutableSet.of(Type.CODE_REVIEW, Type.REFACTOR, Type.EVERGREEN_TEST);
        shop = new MutableCardStacks(this.players.size(), actionCardsForGame);

        shopManager = new ShopManager(shop, cardIdCounter);
        registry =
                new ActionEffectRegistry(
                        List.of(
                                new CodeReviewEffect(),
                                new RefactorEffect(),
                                new EvergreenTestEffect()));
    }

    /**
     * Main game loop. Advances turns until the game is over, then returns the result.
     *
     * @return The final GameResult.
     */
    @Override
    public GameResult play() throws PlayerViolationException {
        while (!isGameOver()) {
            activePlayer = getNextPlayer();
            turnNumber++;
            currentTurnLog = new TurnLog(turnNumber, activePlayer.getName());
            allTurnLogs.add(currentTurnLog);
            if (allTurnLogs.size() > 10) {
                allTurnLogs.remove(0);
            }
            treasuresPlayedThisTurn = new LinkedHashMap<>();
            moneyFromTreasuresThisTurn = 0;
            phase = GameState.TurnPhase.ACTION;
            activePlayer.turnReset();

            while (phase != GameState.TurnPhase.CLEANUP) {
                try {
                    ImmutableList<Decision> options = buildDecisionOptions();
                    GameState gs = buildGameState();
                    Decision decision = activePlayer.makeDecision(gs, options);
                    processDecision(decision, options);
                } catch (PlayerViolationException e) {
                    handleCheating(activePlayer, e);
                    phase = GameState.TurnPhase.CLEANUP;
                }
            }
            activePlayer.cleanup();
        }
        GameResult finalResult = endGame();
        Renderer.printGameResult(finalResult);
        return finalResult;
    }

    private void handleCheating(PlayerInfo cheater, PlayerViolationException e) {
        cheater.forfeit();
    }

    private void processDecision(Decision decision, ImmutableList<Decision> validDecisions)
            throws PlayerViolationException {
        if (!validateDecision(decision, validDecisions)) {
            throw new PlayerViolationException("Invalid decision made by player");
        }
        switch (decision) {
            case BuyDecision buy -> processBuyDecision(buy);
            case EndPhaseDecision endPhase -> processEndPhaseDecision(endPhase);
            case PlayCardDecision play -> processPlayCardDecision(play);
            case TrashCardDecision trash -> processTrashCardDecision(trash);
            default -> throw new UnsupportedOperationException("Decision type not supported");
        }
    }

    private void processTrashCardDecision(TrashCardDecision decision) {
        Card cardToTrash = decision.card();
        boolean wasRemoved = activePlayer.trashCard(cardToTrash);
        if (!wasRemoved) {
            throw new IllegalStateException(
                    "Card to trash was not found in active player's hand's unplayed cards");
        }
    }

    private void processEndPhaseDecision(EndPhaseDecision decision)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (decision.phase() != phase) {
            throw new IllegalArgumentException(
                    "EndPhaseDecision phase does not match current phase");
        }
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

    private void processPlayCardDecision(PlayCardDecision decision)
            throws PlayerViolationException, UnsupportedOperationException {
        Card card = decision.card();
        activePlayer.playCard(card);
        switch (card.getCategory()) {
            case ACTION -> processPlayActionCard(card.type());
            case MONEY -> processPlayMoneyCard(card.type());
            default -> throw new UnsupportedOperationException("Unsupported card category");
        }
    }

    /**
     * Dispatches an action card to its registered effect via {@link ActionEffectRegistry}. Creates
     * an {@link ActionContext} so effects can interact with the game without accessing engine
     * internals directly.
     */
    private void processPlayActionCard(Card.Type type)
            throws PlayerViolationException, UnsupportedOperationException {
        if (activePlayer.numActions() <= 0) {
            throw new IllegalStateException("No actions remaining for active player");
        }
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Played " + cardDisplayName(type));
        }

        ActionContext[] ctxHolder = new ActionContext[1];
        ActionContext ctx =
                new ActionContext(
                        activePlayer,
                        players,
                        shopManager,
                        currentTurnLog,
                        phase,
                        options -> {
                            GameState gs = ctxHolder[0].buildGameState();
                            Decision d = activePlayer.makeDecision(gs, options);
                            if (!validateDecision(d, options)) {
                                throw new PlayerViolationException(
                                        "Player made an invalid decision: "
                                                + (d == null ? "null" : d.toString()));
                            }
                            return d;
                        });
        ctxHolder[0] = ctx;

        registry.execute(type, ctx);
        phase = ctx.getPhase(); // sync back — RefactorEffect may have changed phase
        activePlayer.adjustActions(-1);
    }

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

    // --- ACTION IMPLEMENTATIONS (kept for backward compatibility with OurEngineTest) ---

    private void executeActionCodeReview() {
        activePlayer.drawCard();
        activePlayer.adjustActions(2);
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  +1 Card, +2 Actions");
        }
    }

    private void executeActionRefactor() throws PlayerViolationException {
        ImmutableList<Decision> trashOptions =
                DecisionBuilder.buildTrashCardDecisions(activePlayer);
        Decision playerChoice = makeValidDecision(activePlayer, trashOptions);

        if (playerChoice instanceof EndPhaseDecision) {
            return;
        }

        TrashCardDecision trashCardDecision = (TrashCardDecision) playerChoice;
        if (currentTurnLog != null) {
            currentTurnLog.addEntry(
                    "  Trashed " + cardDisplayName(trashCardDecision.card().type()));
        }
        processTrashCardDecision(trashCardDecision);

        int cardCost = trashCardDecision.card().cost();
        int maxGainCost = cardCost + 2;

        ImmutableList.Builder<Decision> gainDecisionsBuilder = ImmutableList.builder();
        ImmutableSet<Type> availableCardTypes = shop.availableCardsForCost(maxGainCost);

        if (availableCardTypes.isEmpty()) {
            gainDecisionsBuilder.add(new EndPhaseDecision(TurnPhase.GAIN));
        }
        for (Type cardType : availableCardTypes) {
            gainDecisionsBuilder.add(new GainCardDecision(cardType));
        }

        ImmutableList<Decision> gainDecisions = gainDecisionsBuilder.build();

        phase = TurnPhase.GAIN;
        Decision gainDecisionOrEndPhase = makeValidDecision(activePlayer, gainDecisions);
        if (gainDecisionOrEndPhase instanceof EndPhaseDecision) {
            phase = TurnPhase.ACTION;
            return;
        }

        GainCardDecision gainCardDecision = (GainCardDecision) gainDecisionOrEndPhase;
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  Gained " + cardDisplayName(gainCardDecision.cardType()));
        }
        processGainingCard(gainCardDecision.cardType(), activePlayer);
        phase = TurnPhase.ACTION;
    }

    private void executeActionEvergreenTest() {
        activePlayer.drawCard();
        activePlayer.drawCard();
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("  +2 Cards; each other player gains a Bug");
        }
        for (int i = 1; i < players.size(); i++) {
            int playerIndex = (players.indexOf(activePlayer) + i) % players.size();
            PlayerInfo p = players.get(playerIndex);
            if (!p.isDisqualified()) {
                processGainingCard(Type.BUG, p);
            }
        }
    }

    private boolean processGainingCard(Card.Type type, PlayerInfo player) {
        return shopManager.gain(type, player);
    }

    private void processBuyDecision(BuyDecision decision) {
        Type type = decision.cardType();
        if (shop.getNumAvailable(type) <= 0) {
            throw new IllegalStateException("Cannot buy card that is out of stock");
        }
        if (activePlayer.numMoney() < type.cost()) {
            throw new IllegalStateException("Not enough money to buy this card");
        }
        if (activePlayer.numBuys() <= 0) {
            throw new IllegalStateException("No buys remaining to buy a card");
        }
        activePlayer.adjustMoney(-type.cost());
        activePlayer.adjustBuys(-1);
        processGainingCard(type, activePlayer);
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Bought " + cardDisplayName(type));
        }
    }

    private boolean validateDecision(Decision decision, ImmutableList<Decision> validDecisions) {
        if (decision == null || validDecisions == null) {
            return false;
        }
        return validDecisions.contains(decision);
    }

    private Decision makeValidDecision(PlayerInfo player, ImmutableList<Decision> validDecisions)
            throws PlayerViolationException {
        Decision decision = player.makeDecision(buildGameState(), validDecisions);
        if (!validateDecision(decision, validDecisions)) {
            String decisionStr = (decision == null) ? "null" : decision.toString();
            throw new PlayerViolationException("Player made an invalid decision: " + decisionStr);
        }
        return decision;
    }

    @VisibleForTesting
    public PlayerInfo getNextPlayer() throws IllegalStateException {
        if (players.stream().filter(PlayerInfo::isDisqualified).count() >= players.size() - 1) {
            throw new IllegalStateException(
                    "All but one player is disqualified, game should be over");
        }
        PlayerInfo nextPlayer = players.get((players.indexOf(activePlayer) + 1) % players.size());
        while (nextPlayer.isDisqualified()) {
            nextPlayer =
                    players.get((players.indexOf(nextPlayer) + 1) % players.size());
        }
        return nextPlayer;
    }

    private ImmutableList<Decision> buildDecisionOptions() throws UnsupportedOperationException {
        switch (phase) {
            case ACTION:
                return DecisionBuilder.buildActionDecisions(activePlayer);
            case BUY:
                return DecisionBuilder.buildBuyDecisions(
                        activePlayer, shop.availableCardsForCost(activePlayer.numMoney()));
            case MONEY:
                return DecisionBuilder.buildMoneyDecisions(activePlayer);
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

    private GameState buildGameState() {
        return new GameState(
                activePlayer.getName(),
                activePlayer.getImmutableHand(),
                phase,
                activePlayer.numActions(),
                activePlayer.numMoney(),
                activePlayer.numBuys(),
                shop.toImmutableCardStacks());
    }

    @VisibleForTesting
    public boolean isGameOver() {
        long disqualifiedPlayerCount = players.stream().filter(PlayerInfo::isDisqualified).count();
        if (disqualifiedPlayerCount >= players.size() - 1) {
            return true;
        }
        return shop.getNumAvailable(Type.FRAMEWORK) == 0;
    }

    private GameResult endGame() {
        ImmutableList.Builder<PlayerResult> playerResultsBuilder = ImmutableList.builder();
        players.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        for (PlayerInfo p : players) {
            playerResultsBuilder.add(
                    new PlayerResult(p.getName(), p.getScore(), p.getEndingDeck()));
        }
        return new GameResult(playerResultsBuilder.build());
    }

    /**
     * Returns an unmodifiable view of the last 10 turn logs. Entries are appended live as events
     * occur, so callers holding a reference will always see the latest state.
     */
    public List<TurnLog> getTurnLogs() {
        return java.util.Collections.unmodifiableList(allTurnLogs);
    }

    /** Returns the total number of players in this game. */
    public int getNumPlayers() {
        return players.size();
    }

    private static String cardDisplayName(Card.Type type) {
        return Renderer.displayName(type);
    }

    private String buildMoneyLogEntry() {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Card.Type, Integer> e : treasuresPlayedThisTurn.entrySet()) {
            parts.add(e.getValue() + " " + cardDisplayName(e.getKey()));
        }
        return "Played " + String.join(" and ", parts) + " (+" + moneyFromTreasuresThisTurn + ")";
    }
}
