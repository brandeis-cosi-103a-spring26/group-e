package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.BacklogEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class BacklogEffectTest extends ActionTestSupport {

    @Test
    public void testBacklog_EmptyHand_GivesActionAndDrawsNothing() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.BACKLOG), List.of(new BacklogEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card backlog = new Card(Card.Type.BACKLOG, counter.getAndIncrement());
        hand.addToUnplayed(backlog);

        int actionsBefore = s.active.numActions();

        s.runner.processPlayCardDecision(new PlayCardDecision(backlog));

        assertEquals(actionsBefore, s.active.numActions());
        assertEquals(0, s.active.getUnplayedCards().size());
        assertTrue(hand.getPlayedCards().contains(backlog));
    }

    @Test
    public void testBacklog_NonEmptyHand_DiscardsAndDraws() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.BACKLOG), List.of(new BacklogEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card backlog = new Card(Card.Type.BACKLOG, counter.getAndIncrement());
        Card bitCoin1 = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        Card bitCoin2 = new Card(Card.Type.BITCOIN, counter.getAndIncrement());

        hand.addToUnplayed(backlog);
        hand.addToUnplayed(bitCoin1);
        hand.addToUnplayed(bitCoin2);

        List<Card> deck = getField(s.active, "deck");
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));

        int actionsBefore = s.active.numActions();

        s.runner.processPlayCardDecision(new PlayCardDecision(backlog));

        assertEquals(actionsBefore, s.active.numActions());
        assertEquals(2, s.active.getUnplayedCards().size());

        List<Card> discardPile = getField(s.active, "discard");
        assertTrue(discardPile.contains(bitCoin1));
        assertTrue(discardPile.contains(bitCoin2));
    }
}
