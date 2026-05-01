package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.UnitTestEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class UnitTestEffectTest extends ActionTestSupport {

    private static class EffectChoosingPlayer implements Player {
        private final String name;
        private final ChooseEffectDecision.Effect target;
        ImmutableList<Decision> lastOptions;

        EffectChoosingPlayer(String name, ChooseEffectDecision.Effect target) {
            this.name = name;
            this.target = target;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            lastOptions = options;
            for (Decision option : options) {
                if (option instanceof ChooseEffectDecision choose && choose.effect() == target) {
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
    public void testUnitTest_ChoosePlusTwoActions_AddsNetOneActionAfterPlay() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.UNIT_TEST), List.of(new UnitTestEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card unitTest = new Card(Card.Type.UNIT_TEST, counter.getAndIncrement());
        hand.addToUnplayed(unitTest);

        EffectChoosingPlayer chooser =
                new EffectChoosingPlayer(
                        "ChooseActions", ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_ACTIONS);
        setField(s.active, "player", chooser);

        int actionsBefore = s.active.numActions();
        int moneyBefore = s.active.numMoney();

        s.runner.processPlayCardDecision(new PlayCardDecision(unitTest));

        assertEquals(actionsBefore + 1, s.active.numActions());
        assertEquals(moneyBefore, s.active.numMoney());
    }

    @Test
    public void testUnitTest_ChoosePlusTwoMoney_AddsTwoMoney() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.UNIT_TEST), List.of(new UnitTestEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card unitTest = new Card(Card.Type.UNIT_TEST, counter.getAndIncrement());
        hand.addToUnplayed(unitTest);

        EffectChoosingPlayer chooser =
                new EffectChoosingPlayer(
                        "ChooseMoney", ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_MONEY);
        setField(s.active, "player", chooser);

        int moneyBefore = s.active.numMoney();

        s.runner.processPlayCardDecision(new PlayCardDecision(unitTest));

        assertEquals(moneyBefore + 2, s.active.numMoney());
    }

    @Test
    public void testUnitTest_ChoosePlusTwoCards_DrawsTwoCards() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.UNIT_TEST), List.of(new UnitTestEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card unitTest = new Card(Card.Type.UNIT_TEST, counter.getAndIncrement());
        hand.addToUnplayed(unitTest);
        deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));

        EffectChoosingPlayer chooser =
                new EffectChoosingPlayer(
                        "ChooseCards", ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_CARDS);
        setField(s.active, "player", chooser);

        s.runner.processPlayCardDecision(new PlayCardDecision(unitTest));

        assertEquals(2, s.active.getUnplayedCards().size());
        assertEquals(0, deck.size());
    }

    @Test
    public void testUnitTest_OffersExactlyThreeChooseEffectOptions() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.UNIT_TEST), List.of(new UnitTestEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card unitTest = new Card(Card.Type.UNIT_TEST, counter.getAndIncrement());
        hand.addToUnplayed(unitTest);

        EffectChoosingPlayer chooser =
                new EffectChoosingPlayer(
                        "InspectOptions", ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_MONEY);
        setField(s.active, "player", chooser);

        s.runner.processPlayCardDecision(new PlayCardDecision(unitTest));

        assertNotNull(chooser.lastOptions);
        assertEquals(3, chooser.lastOptions.size());
        assertEquals(
                3,
                chooser.lastOptions.stream()
                        .filter(option -> option instanceof ChooseEffectDecision)
                        .count());
        assertFalse(
                chooser.lastOptions.stream()
                        .anyMatch(option -> !(option instanceof ChooseEffectDecision)));
    }
}
