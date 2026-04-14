package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ParallelizationEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ParallelizationEffectTest extends ActionTestSupport {

    private Setup newSetup(int numPlayers) throws Exception {
        ParallelizationEffect parallelization = new ParallelizationEffect();
        Setup s =
                new Setup(
                        numPlayers,
                        ImmutableSet.of(
                                Card.Type.CODE_REVIEW,
                                Card.Type.REFACTOR,
                                Card.Type.EVERGREEN_TEST,
                                Card.Type.PARALLELIZATION),
                        List.of(
                                new CodeReviewEffect(),
                                new RefactorEffect(),
                                new EvergreenTestEffect(),
                                parallelization));
        parallelization.setRegistry(
                new edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry(
                        List.of(
                                new CodeReviewEffect(),
                                new RefactorEffect(),
                                new EvergreenTestEffect(),
                                parallelization)));
        return s;
    }

    @Test
    public void testParallelization_DoubleCodeReview_DrawsTwoExtraCards() throws Exception {
        Setup s = newSetup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        MutableHand hand = getField(s.active, "hand");
        Card parallelization = new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(codeReview);

        int unplayedBefore = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertEquals(4, s.active.numActions());
        assertEquals(unplayedBefore, s.active.getUnplayedCards().size());
    }

    @Test
    public void testParallelization_DoubleEvergreenTest_EachPlayerGetsTwoBugs() throws Exception {
        Setup s = newSetup(3);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        Card parallelization = new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(evergreenTest);

        PlayerInfo opponent1 = s.players.get(1);
        PlayerInfo opponent2 = s.players.get(2);
        List<Card> discard1 = getField(opponent1, "discard");
        List<Card> discard2 = getField(opponent2, "discard");
        int before1 = discard1.size();
        int before2 = discard2.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertEquals(before1 + 2, discard1.size());
        assertEquals(before2 + 2, discard2.size());
    }

    @Test
    public void testParallelization_NoOtherActionCard_DoesNothing() throws Exception {
        Setup s = newSetup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        Card parallelization = new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);

        List<Card> toRemove = new ArrayList<>();
        for (Card c : s.active.getUnplayedCards()) {
            if (c.category() == Card.Type.Category.ACTION && !c.equals(parallelization)) {
                toRemove.add(c);
            }
        }
        for (Card c : toRemove) {
            s.active.trashCard(c);
        }

        int actionsBefore = s.active.numActions();
        int unplayedBefore = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertEquals(actionsBefore - 1, s.active.numActions());
        assertEquals(unplayedBefore - 1, s.active.getUnplayedCards().size());
    }

    @Test
    public void testParallelization_ChosenCardMovedToPlayed() throws Exception {
        Setup s = newSetup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card parallelization = new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(evergreenTest);

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertFalse(s.active.getUnplayedCards().contains(evergreenTest));
        MutableHand finalHand = getField(s.active, "hand");
        assertTrue(finalHand.getPlayedCards().contains(evergreenTest));
    }
}
