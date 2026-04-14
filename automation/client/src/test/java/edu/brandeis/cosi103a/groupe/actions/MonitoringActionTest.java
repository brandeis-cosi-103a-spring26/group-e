package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MonitoringActionTest extends ActionTestSupport {

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
        s.runner.setCurrentTurnLog(new TurnLog(1, s.active.getDisplayName()));

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

        TurnLog log = getField(s.runner, "currentTurnLog");
        assertTrue(log.getEntries().stream().anyMatch(e -> e.contains("revealed Monitoring")));
    }
}
