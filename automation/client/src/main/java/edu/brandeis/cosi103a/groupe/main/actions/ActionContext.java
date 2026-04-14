package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.Renderer;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
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

    public ActionContext(
            PlayerInfo active,
            List<PlayerInfo> allPlayers,
            ShopManager shop,
            TurnLog turnLog,
            TurnPhase initialPhase) {
        this.active = active;
        this.allPlayers = allPlayers;
        this.shop = shop;
        this.turnLog = turnLog;
        this.phase = initialPhase;
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
        shop.gain(type, active);
    }

    public void giveCardTo(PlayerInfo p, Card.Type t) {
        shop.gain(t, p);
    }

    /** Returns true when the player should be unaffected by the given attack card. */
    public boolean isUnaffectedByAttack(PlayerInfo target, Card.Type attackType) {
        for (Card card : target.getUnplayedCards()) {
            if (card.type() == Card.Type.MONITORING) {
                log(
                        "  "
                                + target.getDisplayName()
                                + " revealed Monitoring and was unaffected by "
                                + Renderer.displayName(attackType));
                return true;
            }
        }
        return false;
    }

    public Decision makeDecision(ImmutableList<Decision> opts) throws PlayerViolationException {
        GameState gs = buildGameState();
        Decision d = active.makeDecision(gs, opts);
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
        }
    }

    public void discardCard(Card card) {
        boolean discarded = active.discardCard(card);
        if (!discarded) {
            throw new IllegalStateException(
                    "Attempted to discard a card that is not in the active player's unplayed hand.");
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

    public void log(String entry) {
        if (turnLog != null) {
            turnLog.addEntry(entry);
        }
    }

    /** Builds an immutable GameState snapshot using the current context state. */
    public GameState buildGameState() {
        return new GameState(
                active.getDisplayName(),
                active.getImmutableHand(),
                phase,
                active.numActions(),
                active.numMoney(),
                active.numBuys(),
                shop.toImmutableCardStacks());
    }
}
