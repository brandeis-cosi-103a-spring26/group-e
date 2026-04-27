package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.MergeConflictEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MergeConflictEffectTest extends ActionTestSupport {

    private static class TrashChoosingPlayer implements Player {
        private final String name;
        private final Card.Type cardTypeToTrash;
        boolean sawEndPhaseOption = false;

        TrashChoosingPlayer(String name, Card.Type cardTypeToTrash) {
            this.name = name;
            this.cardTypeToTrash = cardTypeToTrash;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            for (Decision option : options) {
                if (option instanceof EndPhaseDecision) {
                    sawEndPhaseOption = true;
                }
                if (option instanceof TrashCardDecision trash
                        && trash.card().type() == cardTypeToTrash) {
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
    public void testMergeConflict_TrashesSelectedCard_DrawsCardsEqualToCost() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.MERGE_CONFLICT),
                        List.of(new MergeConflictEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card mergeConflict = new Card(Card.Type.MERGE_CONFLICT, counter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, counter.getAndIncrement());
        hand.addToUnplayed(mergeConflict);
        hand.addToUnplayed(ethereum);

        for (int i = 0; i < 10; i++) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        TrashChoosingPlayer chooser = new TrashChoosingPlayer("TrashEthereum", Card.Type.ETHEREUM);
        setField(s.active, "player", chooser);

        int deckBefore = deck.size();
        int expectedDraws = ethereum.cost();

        s.runner.processPlayCardDecision(new PlayCardDecision(mergeConflict));

        assertEquals(expectedDraws, s.active.getUnplayedCards().size());
        assertEquals(deckBefore - expectedDraws, deck.size());
        assertFalse(
                s.active.getUnplayedCards().stream()
                        .anyMatch(card -> card.type() == Card.Type.ETHEREUM));
        assertFalse(chooser.sawEndPhaseOption);
    }

    @Test
    public void testMergeConflict_NoTrashTargets_ReturnsImmediately() throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.MERGE_CONFLICT),
                        List.of(new MergeConflictEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card mergeConflict = new Card(Card.Type.MERGE_CONFLICT, counter.getAndIncrement());
        hand.addToUnplayed(mergeConflict);

        TrashChoosingPlayer chooser = new TrashChoosingPlayer("NoTargets", Card.Type.BITCOIN);
        setField(s.active, "player", chooser);

        s.runner.processPlayCardDecision(new PlayCardDecision(mergeConflict));

        assertEquals(0, s.active.getUnplayedCards().size());
        assertEquals(0, deck.size());
        assertFalse(chooser.sawEndPhaseOption);
    }

    @Test
    public void testMergeConflict_TrashesSelectedCard_UsesThatCardCostForDrawCount()
            throws Exception {
        Setup s =
                new Setup(
                        2,
                        ImmutableSet.of(Card.Type.MERGE_CONFLICT),
                        List.of(new MergeConflictEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card mergeConflict = new Card(Card.Type.MERGE_CONFLICT, counter.getAndIncrement());
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(mergeConflict);
        hand.addToUnplayed(bitcoin);

        for (int i = 0; i < 10; i++) {
            deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        }

        TrashChoosingPlayer chooser = new TrashChoosingPlayer("TrashBitcoin", Card.Type.BITCOIN);
        setField(s.active, "player", chooser);

        int deckBefore = deck.size();
        int expectedDraws = bitcoin.cost();

        s.runner.processPlayCardDecision(new PlayCardDecision(mergeConflict));

        assertEquals(deckBefore - expectedDraws, deck.size());
        assertEquals(expectedDraws, s.active.getUnplayedCards().size());
    }
}
