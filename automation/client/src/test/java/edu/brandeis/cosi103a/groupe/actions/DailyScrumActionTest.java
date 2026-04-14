package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.actions.DailyScrumEffect;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class DailyScrumActionTest extends ActionTestSupport {

    @Test
    public void testDailyScrum_DrawsAddsBuyAndOthersDraw() throws Exception {
        Setup s = new Setup(3, ImmutableSet.of(Card.Type.DAILY_SCRUM), List.of(new DailyScrumEffect()));
        s.active.turnReset();

        PlayerInfo opponent1 = s.players.get(1);
        PlayerInfo opponent2 = s.players.get(2);

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        List<Card> activeDeck = getField(s.active, "deck");
        while (activeDeck.size() < 4) {
            activeDeck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        List<Card> opponent1Deck = getField(opponent1, "deck");
        List<Card> opponent2Deck = getField(opponent2, "deck");
        if (opponent1Deck.isEmpty()) {
            opponent1Deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        if (opponent2Deck.isEmpty()) {
            opponent2Deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        MutableHand hand = getField(s.active, "hand");
        Card dailyScrum = new Card(Card.Type.DAILY_SCRUM, counter.getAndIncrement());
        hand.addToUnplayed(dailyScrum);

        int initialHandSize = s.active.getUnplayedCards().size();
        int initialBuys = s.active.numBuys();
        int initialActions = s.active.numActions();
        int initialOpponent1HandSize = opponent1.getUnplayedCards().size();
        int initialOpponent2HandSize = opponent2.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(dailyScrum));

        assertEquals(initialHandSize + 3, s.active.getUnplayedCards().size());
        assertEquals(initialBuys + 1, s.active.numBuys());
        assertEquals(initialActions - 1, s.active.numActions());
        assertEquals(initialOpponent1HandSize + 1, opponent1.getUnplayedCards().size());
        assertEquals(initialOpponent2HandSize + 1, opponent2.getUnplayedCards().size());
    }
}
