package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.main.actions.TechDebtEffect;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class TechDebtEffectTest extends ActionTestSupport {

    private static class TrackingDiscardPlayer implements Player {
        private final String name;
        int promptCount = 0;
        int discardPhasePromptCount = 0;
        boolean sawEndPhaseDecision = false;

        TrackingDiscardPlayer(String name) {
            this.name = name;
        }

        @Override
        public Decision makeDecision(
                edu.brandeis.cosi.atg.state.GameState state,
                ImmutableList<Decision> options,
                Optional<Event> reason) {
            promptCount++;
            sawEndPhaseDecision =
                    options.stream().anyMatch(option -> option instanceof EndPhaseDecision);
            for (Decision option : options) {
                if (option instanceof DiscardCardDecision) {
                    if (state.phase() == GameState.TurnPhase.DISCARD) {
                        discardPhasePromptCount++;
                    }
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
    public void testTechDebt_NoEmptyPiles_AppliesBonuses() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.TECH_DEBT), List.of(new TechDebtEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");

        hand.clear();
        deck.clear();
        discard.clear();

        Card techDebt = new Card(Card.Type.TECH_DEBT, counter.getAndIncrement());
        hand.addToUnplayed(techDebt);
        deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));

        int initialHandSize = s.active.getUnplayedCards().size();
        int initialActions = s.active.numActions();
        int initialMoney = s.active.numMoney();

        s.runner.processPlayCardDecision(new PlayCardDecision(techDebt));

        assertEquals(initialHandSize, s.active.getUnplayedCards().size());
        assertEquals(initialActions, s.active.numActions());
        assertEquals(initialMoney + 1, s.active.numMoney());
    }

    @Test
    public void testTechDebt_DiscardsPerEmptyPile_WithoutEndPhaseOption() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.TECH_DEBT), List.of(new TechDebtEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");
        MutableCardStacks stacks = getField(s.shop, "stacks");

        hand.clear();
        deck.clear();
        discard.clear();

        Card techDebt = new Card(Card.Type.TECH_DEBT, counter.getAndIncrement());
        hand.addToUnplayed(techDebt);
        for (int i = 0; i < 4; i++) {
            hand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));

        while (stacks.getNumAvailable(Card.Type.ETHEREUM) > 0) {
            stacks.decrementCount(Card.Type.ETHEREUM);
        }
        while (stacks.getNumAvailable(Card.Type.MODULE) > 0) {
            stacks.decrementCount(Card.Type.MODULE);
        }

        TrackingDiscardPlayer tracker = new TrackingDiscardPlayer("Tracker");
        setField(s.active, "player", tracker);

        int initialHandSize = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(techDebt));

        assertEquals(initialHandSize - 2, s.active.getUnplayedCards().size());
        assertEquals(2, tracker.promptCount);
        assertEquals(2, tracker.discardPhasePromptCount);
        assertFalse(tracker.sawEndPhaseDecision);
    }

    @Test
    public void testTechDebt_Shortfall_DiscardsAllAvailableAndStops() throws Exception {
        Setup s = new Setup(2, ImmutableSet.of(Card.Type.TECH_DEBT), List.of(new TechDebtEffect()));
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");
        MutableCardStacks stacks = getField(s.shop, "stacks");

        hand.clear();
        deck.clear();
        discard.clear();

        Card techDebt = new Card(Card.Type.TECH_DEBT, counter.getAndIncrement());
        hand.addToUnplayed(techDebt);
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));

        while (stacks.getNumAvailable(Card.Type.ETHEREUM) > 0) {
            stacks.decrementCount(Card.Type.ETHEREUM);
        }
        while (stacks.getNumAvailable(Card.Type.MODULE) > 0) {
            stacks.decrementCount(Card.Type.MODULE);
        }
        while (stacks.getNumAvailable(Card.Type.DOGECOIN) > 0) {
            stacks.decrementCount(Card.Type.DOGECOIN);
        }

        s.runner.processPlayCardDecision(new PlayCardDecision(techDebt));

        assertEquals(0, s.active.getUnplayedCards().size());
    }
}
