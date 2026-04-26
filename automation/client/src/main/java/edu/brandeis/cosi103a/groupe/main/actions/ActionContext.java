package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.TrashCardEvent;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;
import edu.brandeis.cosi103a.groupe.main.Renderer;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * Clean API bundle passed to ActionCardEffect implementations. Wraps the internals effects need
 * without exposing the full engine internals.
 */
public class ActionContext {

    private final PlayerInfo active;
    private final List<PlayerInfo> allPlayers;
    private final ShopManager shop;
    private final TurnLog turnLog;
    private TurnPhase phase;
    private final MegaObserver observer;

    public ActionContext(
            PlayerInfo active,
            List<PlayerInfo> allPlayers,
            ShopManager shop,
            TurnLog turnLog,
            TurnPhase initialPhase,
            MegaObserver observer) {
        this.active = active;
        this.allPlayers = allPlayers;
        this.shop = shop;
        this.turnLog = turnLog;
        this.phase = initialPhase;
        this.observer = observer;
    }

    public void drawCard() {
        drawCard(active);
    }

    public void drawCard(PlayerInfo player) {
        player.drawCard();
    }

    public void adjustActions(int delta) {
        active.adjustActions(delta);
    }

    public void adjustBuys(int delta) {
        active.adjustBuys(delta);
    }

    public void adjustMoney(int delta) {
        active.adjustMoney(delta);
    }

    public void gainCard(Card.Type type) {
        boolean successfullyBought = shop.gain(type, active);
        if (successfullyBought) {
            GainCardEvent gainEvent = new GainCardEvent(type, active.getDisplayName());
            GameState state =
                    new GameState(
                            active.getDisplayName(),
                            active.getImmutableHand(),
                            phase,
                            active.numActions(),
                            active.numMoney(),
                            active.numBuys(),
                            shop.toImmutableCardStacks());
            observer.notifyEvent(state, gainEvent);
        }
    }

    public void giveCardTo(PlayerInfo p, Card.Type t) {
        boolean successfullyGiven = shop.gain(t, p);
        if (successfullyGiven) {
            GainCardEvent gainEvent = new GainCardEvent(t, p.getDisplayName());
            GameState state =
                    new GameState(
                            active.getDisplayName(),
                            active.getImmutableHand(),
                            phase,
                            active.numActions(),
                            active.numMoney(),
                            active.numBuys(),
                            shop.toImmutableCardStacks());
            observer.notifyEvent(state, gainEvent);
        }
    }

    /**
     * Returns true when the player chooses to reveal Monitoring in REACTION phase and is unaffected
     * by the given attack card.
     */
    public boolean isUnaffectedByAttack(PlayerInfo target, Card.Type attackType)
            throws PlayerViolationException {
        boolean hasMonitoring =
                target.getUnplayedCards().stream()
                        .anyMatch(card -> card.type() == Card.Type.MONITORING);
        if (!hasMonitoring) {
            return false;
        }

        TurnPhase previousPhase = phase;
        phase = TurnPhase.REACTION;
        try {
            ImmutableList<Decision> options = DecisionBuilder.buildReactionDecisions(target);
            Decision decision = makeDecisionForPlayer(target, options);

            if (decision instanceof PlayCardDecision playDecision) {
                if (playDecision.card().type() != Card.Type.MONITORING
                        || !target.getUnplayedCards().contains(playDecision.card())) {
                    throw new PlayerViolationException(
                            "Invalid reaction card played: " + playDecision.card().type());
                }

                log(
                        "  "
                                + target.getDisplayName()
                                + " revealed Monitoring and was unaffected by "
                                + Renderer.displayName(attackType));
                return true;
            }

            if (decision instanceof EndPhaseDecision endPhaseDecision
                    && endPhaseDecision.phase() == TurnPhase.REACTION) {
                return false;
            }

            throw new PlayerViolationException("Invalid reaction decision: " + decision);
        } finally {
            phase = previousPhase;
        }
    }

    public Decision makeDecision(ImmutableList<Decision> opts) throws PlayerViolationException {
        return makeDecisionForPlayer(active, opts);
    }

    public Decision makeDecisionForPlayer(PlayerInfo player, ImmutableList<Decision> opts)
            throws PlayerViolationException {
        GameState gs = buildGameState(player);
        Decision d = player.makeDecision(gs, opts);
        if (d == null || !opts.contains(d)) {
            throw new PlayerViolationException(
                    "Player made an invalid decision: " + (d == null ? "null" : d.toString()));
        }
        return d;
    }

    public void trashCard(Card card) {
        // Ensure trashing actually removed the card from the active player's unplayed hand.
        boolean trashed = active.trashCard(card);
        if (!trashed) {
            throw new IllegalStateException(
                    "Attempted to trash a card that is not in the active player's unplayed hand.");
        } else {
            TrashCardEvent trashEvent = new TrashCardEvent(card.type(), active.getDisplayName());
            GameState state =
                    new GameState(
                            active.getDisplayName(),
                            active.getImmutableHand(),
                            phase,
                            active.numActions(),
                            active.numMoney(),
                            active.numBuys(),
                            shop.toImmutableCardStacks());
            observer.notifyEvent(state, trashEvent);
        }
    }

    public void discardCard(Card card) {
        boolean discarded = active.discardCard(card);
        if (!discarded) {
            throw new IllegalStateException(
                    "Attempted to discard a card that is not in the active player's unplayed hand.");
        }
    }

    public void discardCardForPlayer(PlayerInfo player, Card card) {
        boolean discarded = player.discardCard(card);
        if (!discarded) {
            throw new IllegalStateException(
                    "Attempted to discard a card that is not in the target player's unplayed hand.");
        }
    }

    public void setPhase(TurnPhase p) {
        this.phase = p;
    }

    public TurnPhase getPhase() {
        return phase;
    }

    public PlayerInfo activePlayer() {
        return active;
    }

    /** Returns all non-disqualified players other than the active player, in turn order. */
    public List<PlayerInfo> otherPlayers() {
        List<PlayerInfo> others = new ArrayList<>();
        int activeIdx = allPlayers.indexOf(active);
        for (int i = 1; i < allPlayers.size(); i++) {
            PlayerInfo p = allPlayers.get((activeIdx + i) % allPlayers.size());
            if (!p.isDisqualified()) {
                others.add(p);
            }
        }
        return others;
    }

    public ImmutableSet<Card.Type> availableCardsForCost(int max) {
        return shop.availableForCost(max);
    }

    public void discountShop() {
        shop.setDiscount(true);
    }

    public void log(String entry) {
        if (turnLog != null) {
            turnLog.addEntry(entry);
        }
    }

    /** Builds an immutable GameState snapshot using the current context state. */
    public GameState buildGameState() {
        return buildGameState(active);
    }

    public GameState buildGameState(PlayerInfo perspectivePlayer) {
        return new GameState(
                perspectivePlayer.getDisplayName(),
                perspectivePlayer.getImmutableHand(),
                phase,
                perspectivePlayer.numActions(),
                perspectivePlayer.numMoney(),
                perspectivePlayer.numBuys(),
                shop.toImmutableCardStacks());
    }
}
