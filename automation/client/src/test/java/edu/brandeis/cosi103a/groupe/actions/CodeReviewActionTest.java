package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class CodeReviewActionTest extends ActionTestSupport {

    @Test
    public void testCodeReview_MovesToPlayed() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.CODE_REVIEW), List.of(new CodeReviewEffect()));
        s.active.turnReset();

        List<Card> deck = getField(s.active, "deck");
        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        deck.add(codeReview);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        assertFalse(s.active.getUnplayedCards().contains(codeReview));
        MutableHand hand = getField(s.active, "hand");
        assertTrue(hand.getPlayedCards().contains(codeReview));
    }

    @Test
    public void testCodeReview_DecrementsAndAddsActions() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.CODE_REVIEW), List.of(new CodeReviewEffect()));
        s.active.turnReset();
        s.active.adjustActions(1);

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(codeReview);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        assertEquals(3, s.active.numActions());
    }

    @Test
    public void testCodeReview_EmptyDeckStillAddsActions() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.CODE_REVIEW), List.of(new CodeReviewEffect()));
        s.active.turnReset();
        s.active.adjustActions(1);

        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");
        deck.clear();
        discard.clear();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        MutableHand hand = getField(s.active, "hand");
        hand.addToUnplayed(codeReview);

        int initialActions = s.active.numActions();
        int initialHandSize = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        assertEquals(initialActions + 1, s.active.numActions());
        assertEquals(initialHandSize - 1, s.active.getUnplayedCards().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testCodeReview_ZeroActionsThrows() throws Throwable {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.CODE_REVIEW), List.of(new CodeReviewEffect()));
        s.active.turnReset();
        s.active.adjustActions(-1);

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        MutableHand hand = getField(s.active, "hand");
        hand.addToUnplayed(codeReview);

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));
    }
}
