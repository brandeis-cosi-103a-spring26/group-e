package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MonitoringActionTest extends ActionTestSupport {

    private static class DeclineReactionPlayer implements Player {
        private final String name;

        DeclineReactionPlayer(String name) {
            this.name = name;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            if (state.phase() == GameState.TurnPhase.REACTION) {
                for (Decision option : options) {
                    if (option instanceof EndPhaseDecision) {
                        return option;
                    }
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
    public void testMonitoring_DrawsTwoCards() throws Exception {
        Setup s =
                new Setup(
                        2, ImmutableSet.of(Card.Type.MONITORING), List.of(new MonitoringEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        MutableHand hand = getField(s.active, "hand");
        Card monitoring = new Card(Card.Type.MONITORING, counter.getAndIncrement());
        hand.addToUnplayed(monitoring);

        int initialHandSize = s.active.getUnplayedCards().size();
        int initialActions = s.active.numActions();

        s.runner.processPlayCardDecision(new PlayCardDecision(monitoring));

        assertEquals(initialHandSize + 1, s.active.getUnplayedCards().size());
        assertEquals(initialActions - 1, s.active.numActions());
    }

    @Test
    public void testMonitoring_BlocksEvergreenAndStaysInHand() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.EVERGREEN_TEST, Card.Type.MONITORING),
                        List.of(new EvergreenTestEffect(), new MonitoringEffect()));
        s.active.turnReset();

        PlayerInfo opponent = s.players.get(1);
        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand activeHand = getField(s.active, "hand");
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        activeHand.addToUnplayed(evergreenTest);

        MutableHand opponentHand = getField(opponent, "hand");
        Card monitoring = new Card(Card.Type.MONITORING, counter.getAndIncrement());
        opponentHand.addToUnplayed(monitoring);

        List<Card> opponentDiscard = getField(opponent, "discard");
        int initialOpponentDiscardSize = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(evergreenTest));

        assertEquals(initialOpponentDiscardSize, opponentDiscard.size());
        assertTrue(opponent.getUnplayedCards().contains(monitoring));
        assertTrue(
                s.eventSink.getEvents().stream()
                        .map(n -> n.getEvent().getDescription())
                        .anyMatch(desc -> desc.contains("revealed Monitoring")));
    }

    @Test
    public void testMonitoring_CanDeclineReaction_AndEvergreenStillApplies() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.EVERGREEN_TEST, Card.Type.MONITORING),
                        List.of(new EvergreenTestEffect(), new MonitoringEffect()));
        s.active.turnReset();

        PlayerInfo opponent = s.players.get(1);
        setField(opponent, "player", new DeclineReactionPlayer("DeclineReaction"));
        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand activeHand = getField(s.active, "hand");
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        activeHand.addToUnplayed(evergreenTest);

        MutableHand opponentHand = getField(opponent, "hand");
        Card monitoring = new Card(Card.Type.MONITORING, counter.getAndIncrement());
        opponentHand.addToUnplayed(monitoring);

        List<Card> opponentDiscard = getField(opponent, "discard");
        int initialOpponentDiscardSize = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(evergreenTest));

        assertEquals(initialOpponentDiscardSize + 1, opponentDiscard.size());
        assertEquals(Card.Type.BUG, opponentDiscard.get(opponentDiscard.size() - 1).type());
        assertTrue(opponent.getUnplayedCards().contains(monitoring));
    }
}
