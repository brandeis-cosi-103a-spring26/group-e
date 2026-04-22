package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.HackEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class HackEffectTest extends ActionTestSupport {

    private static class TrackingDiscardPlayer implements Player {
        private final String name;
        int promptCount = 0;
        int reactionPromptCount = 0;
        boolean sawEndPhaseDecision = false;
        private final boolean declineReaction;

        TrackingDiscardPlayer(String name) {
            this(name, false);
        }

        TrackingDiscardPlayer(String name, boolean declineReaction) {
            this.name = name;
            this.declineReaction = declineReaction;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            promptCount++;
            if (state.phase() == GameState.TurnPhase.REACTION) {
                reactionPromptCount++;
                if (declineReaction) {
                    for (Decision option : options) {
                        if (option instanceof EndPhaseDecision) {
                            return option;
                        }
                    }
                }
            }
            sawEndPhaseDecision =
                    options.stream().anyMatch(option -> option instanceof EndPhaseDecision);
            for (Decision option : options) {
                if (option instanceof DiscardCardDecision) {
                    return option;
                }
            }
            return options.get(0);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<GameObserver> getObserver() {
            return Optional.empty();
        }
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    public void testHack_GivesTwoMoney_AndForcesDiscardToThree() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.HACK), List.of(new HackEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card hack = new Card(Card.Type.HACK, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        activeHand.addToUnplayed(hack);

        PlayerInfo opponent = s.players.get(1);
        TrackingDiscardPlayer tracker = new TrackingDiscardPlayer("DiscardTracker");
        setField(opponent, "player", tracker);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        for (int i = 0; i < 5; i++) {
            opponentHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        int moneyBefore = s.active.numMoney();

        s.runner.processPlayCardDecision(new PlayCardDecision(hack));

        assertEquals(moneyBefore + 2, s.active.numMoney());
        assertEquals(3, opponent.getUnplayedCards().size());
        assertEquals(2, tracker.promptCount);
        assertFalse(tracker.sawEndPhaseDecision);
    }

    @Test
    public void testHack_MonitoringBlocksDiscardPrompt() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.HACK, Card.Type.MONITORING),
                        List.of(new HackEffect(), new MonitoringEffect()));
        s.active.turnReset();
        s.runner.setCurrentTurnLog(new TurnLog(1, s.active.getDisplayName()));

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card hack = new Card(Card.Type.HACK, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        activeHand.addToUnplayed(hack);

        PlayerInfo opponent = s.players.get(1);
        TrackingDiscardPlayer tracker = new TrackingDiscardPlayer("MonitoringTracker");
        setField(opponent, "player", tracker);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        opponentHand.addToUnplayed(new Card(Card.Type.MONITORING, counter.getAndIncrement()));
        for (int i = 0; i < 4; i++) {
            opponentHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        s.runner.processPlayCardDecision(new PlayCardDecision(hack));

        assertEquals(5, opponent.getUnplayedCards().size());
        assertEquals(1, tracker.promptCount);
        assertEquals(1, tracker.reactionPromptCount);

        TurnLog log = getField(s.runner, "currentTurnLog");
        assertTrue(log.getEntries().stream().anyMatch(e -> e.contains("revealed Monitoring")));
    }

    @Test
    public void testHack_MonitoringCanDeclineReveal_ThenForcedDiscard() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.HACK, Card.Type.MONITORING),
                        List.of(new HackEffect(), new MonitoringEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card hack = new Card(Card.Type.HACK, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        activeHand.addToUnplayed(hack);

        PlayerInfo opponent = s.players.get(1);
        TrackingDiscardPlayer tracker = new TrackingDiscardPlayer("DeclineMonitoring", true);
        setField(opponent, "player", tracker);

        MutableHand opponentHand = getField(opponent, "hand");
        opponentHand.clear();
        opponentHand.addToUnplayed(new Card(Card.Type.MONITORING, counter.getAndIncrement()));
        for (int i = 0; i < 4; i++) {
            opponentHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        s.runner.processPlayCardDecision(new PlayCardDecision(hack));

        assertEquals(3, opponent.getUnplayedCards().size());
        assertEquals(3, tracker.promptCount);
        assertEquals(1, tracker.reactionPromptCount);
    }

    @Test
    public void testHack_MixedOpponents_OnlyNonImmuneDiscards() throws Exception {
        Setup s =
                new Setup(
                        3,
                        ImmutableSet.of(Card.Type.HACK, Card.Type.MONITORING),
                        List.of(new HackEffect(), new MonitoringEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card hack = new Card(Card.Type.HACK, counter.getAndIncrement());

        MutableHand activeHand = getField(s.active, "hand");
        activeHand.addToUnplayed(hack);

        PlayerInfo nonImmune = s.players.get(1);
        TrackingDiscardPlayer nonImmuneTracker = new TrackingDiscardPlayer("NonImmune");
        setField(nonImmune, "player", nonImmuneTracker);
        MutableHand nonImmuneHand = getField(nonImmune, "hand");
        nonImmuneHand.clear();
        for (int i = 0; i < 5; i++) {
            nonImmuneHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        PlayerInfo immune = s.players.get(2);
        TrackingDiscardPlayer immuneTracker = new TrackingDiscardPlayer("Immune");
        setField(immune, "player", immuneTracker);
        MutableHand immuneHand = getField(immune, "hand");
        immuneHand.clear();
        immuneHand.addToUnplayed(new Card(Card.Type.MONITORING, counter.getAndIncrement()));
        for (int i = 0; i < 4; i++) {
            immuneHand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        s.runner.processPlayCardDecision(new PlayCardDecision(hack));

        assertEquals(3, nonImmune.getUnplayedCards().size());
        assertEquals(2, nonImmuneTracker.promptCount);
        assertFalse(nonImmuneTracker.sawEndPhaseDecision);

        assertEquals(5, immune.getUnplayedCards().size());
        assertEquals(1, immuneTracker.promptCount);
        assertEquals(1, immuneTracker.reactionPromptCount);
    }
}
