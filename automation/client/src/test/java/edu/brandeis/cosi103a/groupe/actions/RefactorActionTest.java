package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class RefactorActionTest extends ActionTestSupport {

    @Test
    public void testRefactor_ReturnsToActionPhase() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.REFACTOR), List.of(new RefactorEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card refactor = new Card(Card.Type.REFACTOR, counter.getAndIncrement());
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(refactor);
        hand.addToUnplayed(bitcoin);

        s.runner.processPlayCardDecision(new PlayCardDecision(refactor));

        assertEquals(GameState.TurnPhase.ACTION, s.runner.getPhase());
    }
}
