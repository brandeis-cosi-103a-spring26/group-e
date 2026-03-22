package edu.brandeis.cosi103a.groupe.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.ActionContext;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a single player's turn: manages the phase state machine and routes decisions to the
 * appropriate handlers. Delegates action card effects to {@link ActionEffectRegistry}.
 */
public class TurnRunner {

    private TurnPhase phase;
    private PlayerInfo activePlayer;
    private final List<PlayerInfo> players;
    private final ShopManager shop;
    private final ActionEffectRegistry registry;
    private TurnLog currentTurnLog;
    private Map<Card.Type, Integer> treasuresPlayedThisTurn = new LinkedHashMap<>();
    private int moneyFromTreasuresThisTurn = 0;

    public TurnRunner(List<PlayerInfo> players, ShopManager shop, ActionEffectRegistry registry) {
        this.players = players;
        this.shop = shop;
        this.registry = registry;
    }

    /**
     * Runs the full inner loop for one player's turn (ACTION → MONEY → BUY → CLEANUP). Calls {@code
     * activePlayer.turnReset()} at the start and cleanup at the end.
     */
    public void runTurn(PlayerInfo activePlayer, TurnLog log) {
        this.activePlayer = activePlayer;
        this.currentTurnLog = log;
        this.phase = TurnPhase.ACTION;
        this.treasuresPlayedThisTurn = new LinkedHashMap<>();
        this.moneyFromTreasuresThisTurn = 0;
        activePlayer.turnReset();

        while (phase != TurnPhase.CLEANUP) {
            try {
                ImmutableList<Decision> options = buildDecisionOptions();
                Decision decision = makeValidDecision(activePlayer, options);
                processDecision(decision, options);
            } catch (PlayerViolationException e) {
                activePlayer.forfeit();
                phase = TurnPhase.CLEANUP;
            }
        }
        activePlayer.cleanup();
    }

    // VisibleForTesting — allow tests to configure state without going through a full turn
    @VisibleForTesting
    public void setActivePlayer(PlayerInfo p) {
        this.activePlayer = p;
    }

    @VisibleForTesting
    public void setPhase(TurnPhase p) {
        this.phase = p;
    }

    @VisibleForTesting
    public TurnPhase getPhase() {
        return phase;
    }

    @VisibleForTesting
    public void setCurrentTurnLog(TurnLog log) {
        this.currentTurnLog = log;
    }

    /**
     * Processes a PlayCardDecision: moves the card to played then handles ACTION or MONEY logic.
     * Public so tests can call it directly without reflection.
     */
    @VisibleForTesting
    public void processPlayCardDecision(PlayCardDecision decision) throws PlayerViolationException {
        Card card = decision.card();
        activePlayer.playCard(card);
        switch (card.category()) {
            case ACTION -> processPlayActionCard(card.type());
            case MONEY -> processPlayMoneyCard(card.type());
            default -> throw new UnsupportedOperationException("Unsupported card category");
        }
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

    @VisibleForTesting
    public void processTrashCardDecision(TrashCardDecision decision) {
        Card cardToTrash = decision.card();
        boolean wasRemoved = activePlayer.trashCard(cardToTrash);
        if (!wasRemoved) {
            throw new IllegalStateException(
                    "Card to trash was not found in active player's hand's unplayed cards");
        }
    }

    @VisibleForTesting
    public void processEndPhaseDecision(EndPhaseDecision decision) {
        if (decision.phase() != phase) {
            throw new IllegalArgumentException(
                    "EndPhaseDecision phase does not match current phase");
        }
        switch (decision.phase()) {
            case ACTION -> phase = TurnPhase.MONEY;
            case MONEY -> {
                if (currentTurnLog != null && !treasuresPlayedThisTurn.isEmpty()) {
                    currentTurnLog.addEntry(buildMoneyLogEntry());
                }
                phase = TurnPhase.BUY;
            }
            case BUY -> phase = TurnPhase.CLEANUP;
            default ->
                    throw new UnsupportedOperationException("Cannot end phase from current phase");
        }
    }

    @VisibleForTesting
    public void processPlayActionCard(Card.Type type) throws PlayerViolationException {
        if (activePlayer.numActions() <= 0) {
            throw new IllegalStateException("No actions remaining for active player");
        }
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Played " + Renderer.displayName(type));
        }

        ActionContext ctx =
                new ActionContext(activePlayer, players, shop, currentTurnLog, phase);

        registry.execute(type, ctx);
        phase = ctx.getPhase(); // sync back — RefactorEffect mutates phase during gain step
        activePlayer.adjustActions(-1);
    }

    @VisibleForTesting
    public void processPlayMoneyCard(Card.Type type) {
        int value;
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

    @VisibleForTesting
    public void processBuyDecision(BuyDecision decision) {
        Card.Type type = decision.cardType();
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
        shop.gain(type, activePlayer);
        if (currentTurnLog != null) {
            currentTurnLog.addEntry("Bought " + Renderer.displayName(type));
        }
    }

    @VisibleForTesting
    public boolean validateDecision(Decision decision, ImmutableList<Decision> validDecisions) {
        if (decision == null || validDecisions == null) return false;
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

    private ImmutableList<Decision> buildDecisionOptions() {
        return switch (phase) {
            case ACTION -> DecisionBuilder.buildActionDecisions(activePlayer);
            case BUY ->
                    DecisionBuilder.buildBuyDecisions(
                            activePlayer, shop.availableForCost(activePlayer.numMoney()));
            case MONEY -> DecisionBuilder.buildMoneyDecisions(activePlayer);
            case CLEANUP ->
                    throw new UnsupportedOperationException("No decisions during CLEANUP phase");
            case GAIN ->
                    throw new UnsupportedOperationException("Call buildGainDecisions directly");
            default ->
                    throw new UnsupportedOperationException(
                            "Invalid phase for building decision options");
        };
    }

    @VisibleForTesting
    public GameState buildGameState() {
        return new GameState(
                activePlayer.getName(),
                activePlayer.getImmutableHand(),
                phase,
                activePlayer.numActions(),
                activePlayer.numMoney(),
                activePlayer.numBuys(),
                shop.toImmutableCardStacks());
    }

    private String buildMoneyLogEntry() {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Card.Type, Integer> e : treasuresPlayedThisTurn.entrySet()) {
            parts.add(e.getValue() + " " + Renderer.displayName(e.getKey()));
        }
        return "Played " + String.join(" and ", parts) + " (+" + moneyFromTreasuresThisTurn + ")";
    }
}
