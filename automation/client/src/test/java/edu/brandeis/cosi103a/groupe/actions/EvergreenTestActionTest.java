package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class EvergreenTestActionTest extends ActionTestSupport {

    @Test
    public void testEvergreen_DrawsTwoAndAttacks() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.EVERGREEN_TEST), List.of(new EvergreenTestEffect()));
        s.active.turnReset();

        PlayerInfo opponent = s.players.get(1);

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        MutableHand hand = getField(s.active, "hand");
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(evergreenTest);

        int initialHandSize = s.active.getUnplayedCards().size();
        List<Card> opponentDiscard = getField(opponent, "discard");
        int initialOpponentDiscardSize = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(evergreenTest));

        assertEquals(initialHandSize + 1, s.active.getUnplayedCards().size());
        assertEquals(initialOpponentDiscardSize + 1, opponentDiscard.size());
        assertEquals(Card.Type.BUG, opponentDiscard.get(opponentDiscard.size() - 1).type());
    }
}
