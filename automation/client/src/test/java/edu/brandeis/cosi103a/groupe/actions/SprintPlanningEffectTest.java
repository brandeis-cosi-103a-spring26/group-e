package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.SprintPlanningEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class SprintPlanningEffectTest extends ActionTestSupport {

    @Test
    public void testSprintPlanning_DrawsAndAddsActionAndBuy() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.SPRINT_PLANNING),
                        List.of(new SprintPlanningEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card sprintPlanning = new Card(Card.Type.SPRINT_PLANNING, counter.getAndIncrement());
        hand.addToUnplayed(sprintPlanning);
        deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));

        int initialHandSize = s.active.getUnplayedCards().size();
        int initialActions = s.active.numActions();
        int initialBuys = s.active.numBuys();

        s.runner.processPlayCardDecision(new PlayCardDecision(sprintPlanning));

        assertEquals(initialHandSize, s.active.getUnplayedCards().size());
        assertEquals(initialActions, s.active.numActions());
        assertEquals(initialBuys + 1, s.active.numBuys());
    }
}
