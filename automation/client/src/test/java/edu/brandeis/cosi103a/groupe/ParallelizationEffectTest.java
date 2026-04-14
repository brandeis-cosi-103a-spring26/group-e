package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.main.TurnRunner;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ParallelizationEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * Unit tests for {@link ParallelizationEffect}.
 *
 * <p>Each test uses a self-contained {@link Setup} that mirrors {@code EnginePlayCardTest.Setup}
 * but includes {@code PARALLELIZATION} in both the registry and the supply.
 */
public class ParallelizationEffectTest {

    // ========== Setup ==========

    private static class Setup {
        final List<PlayerInfo> players;
        final PlayerInfo active;
        final ShopManager shop;
        final TurnRunner runner;

        Setup(int numPlayers) throws Exception {
            AtomicInteger counter = new AtomicInteger(1);
            com.google.common.collect.ImmutableSet<Card.Type> actionCards =
                    com.google.common.collect.ImmutableSet.of(
                            Card.Type.CODE_REVIEW,
                            Card.Type.REFACTOR,
                            Card.Type.EVERGREEN_TEST,
                            Card.Type.PARALLELIZATION);
            MutableCardStacks stacks = new MutableCardStacks(numPlayers, actionCards);
            shop = new ShopManager(stacks, counter);

            players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new PlayerInfo(new BigMoneyPlayer(), counter));
            }
            active = players.get(0);

            ParallelizationEffect parallelization = new ParallelizationEffect();
            ActionEffectRegistry registry =
                    new ActionEffectRegistry(
                            List.of(
                                    new CodeReviewEffect(),
                                    new RefactorEffect(),
                                    new EvergreenTestEffect(),
                                    parallelization));
            parallelization.setRegistry(registry);

            runner = new TurnRunner(players, shop, registry);
            runner.setActivePlayer(active);
            runner.setPhase(GameState.TurnPhase.ACTION);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    // ========== Tests ==========

    /**
     * Parallelizing CODE_REVIEW should fire its effect twice: +1 Card and +2 Actions per
     * execution → net +2 cards drawn and +4 actions added (before the -1 for playing
     * Parallelization itself).
     */
    @Test
    public void testParallelization_DoubleCodeReview_DrawsTwoExtraCards() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        // Ensure deck has at least 2 cards to draw
        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        // Give the player a Parallelization and a Code Review in hand
        MutableHand hand = getField(s.active, "hand");
        Card parallelization =
                new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(codeReview);

        // Record state before playing Parallelization
        // Starting: Parallelization + Code Review in unplayed = hand size before
        int unplayedBefore = s.active.getUnplayedCards().size();
        int actionsBefore = s.active.numActions(); // 1 from turnReset

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        // Parallelization: -1 action (playing it) + Code Review fires twice (+2 actions each)
        // Net actions: 1 - 1 + 2 + 2 = 4
        assertEquals("Actions should be 4 after double Code Review", 4, s.active.numActions());

        // Hand: -1 (Parallelization played) -1 (Code Review moved to played) +1 +1 (draws)
        // Net unplayed change = 0
        assertEquals(
                "Unplayed count unchanged: -1 parallelization -1 code_review +2 draws",
                unplayedBefore,
                s.active.getUnplayedCards().size());
    }

    /**
     * Parallelizing EVERGREEN_TEST should fire its attack effect twice, giving each opponent
     * exactly 2 Bug cards.
     */
    @Test
    public void testParallelization_DoubleEvergreenTest_EachPlayerGetsTwoBugs()
            throws Exception {
        Setup s = new Setup(3); // active + 2 opponents
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        Card parallelization =
                new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(evergreenTest);

        // Capture opponent discard sizes before
        PlayerInfo opponent1 = s.players.get(1);
        PlayerInfo opponent2 = s.players.get(2);
        List<Card> discard1 = getField(opponent1, "discard");
        List<Card> discard2 = getField(opponent2, "discard");
        int before1 = discard1.size();
        int before2 = discard2.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertEquals("Opponent 1 should receive 2 Bugs", before1 + 2, discard1.size());
        assertEquals("Opponent 2 should receive 2 Bugs", before2 + 2, discard2.size());

        // All gained cards should be Bugs
        for (int i = before1; i < discard1.size(); i++) {
            assertEquals("Should be a Bug", Card.Type.BUG, discard1.get(i).type());
        }
        for (int i = before2; i < discard2.size(); i++) {
            assertEquals("Should be a Bug", Card.Type.BUG, discard2.get(i).type());
        }
    }

    /**
     * If no other action card is in hand when Parallelization is played, the effect should be a
     * silent no-op: no exception, no state change beyond the card moving to played.
     */
    @Test
    public void testParallelization_NoOtherActionCard_DoesNothing() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand hand = getField(s.active, "hand");
        Card parallelization =
                new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);

        // Remove any other action cards that may already be in unplayed
        List<Card> toRemove = new ArrayList<>();
        for (Card c : s.active.getUnplayedCards()) {
            if (c.category() == Card.Type.Category.ACTION
                    && !c.equals(parallelization)) {
                toRemove.add(c);
            }
        }
        for (Card c : toRemove) {
            s.active.trashCard(c);
        }

        int actionsBefore = s.active.numActions();
        int unplayedBefore = s.active.getUnplayedCards().size();

        // Should not throw
        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        // Only 1 action consumed (for playing Parallelization); effect does nothing
        assertEquals(
                "Only 1 action consumed for playing parallelization",
                actionsBefore - 1,
                s.active.numActions());
        // Parallelization moved from unplayed to played → 1 fewer unplayed
        assertEquals(
                "Parallelization moved to played",
                unplayedBefore - 1,
                s.active.getUnplayedCards().size());
    }

    /**
     * Playing Parallelization should consume exactly 1 action token (the cost of Parallelization
     * itself). The doubled card's executions do not consume additional actions.
     */
    @Test
    public void testParallelization_OneActionUsed() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        // Give extra actions so we can measure the exact delta clearly
        s.active.adjustActions(3); // now 4 actions

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card parallelization =
                new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        // Use EVERGREEN_TEST because it adds no actions — makes the count easy to reason about
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(evergreenTest);

        int actionsBefore = s.active.numActions(); // 4

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        // Playing Parallelization costs 1 action. EvergreenTest has no +action effect.
        // Expected: 4 - 1 = 3
        assertEquals(
                "Only Parallelization's own action cost should be deducted",
                actionsBefore - 1,
                s.active.numActions());
    }

    /**
     * After Parallelization is played, the chosen target card should be in the played zone (not
     * unplayed), regardless of the number of times its effect fires.
     */
    @Test
    public void testParallelization_ChosenCardMovedToPlayed() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card parallelization =
                new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(parallelization);
        hand.addToUnplayed(evergreenTest);

        s.runner.processPlayCardDecision(new PlayCardDecision(parallelization));

        assertFalse(
                "chosen card should not be in unplayed",
                s.active.getUnplayedCards().contains(evergreenTest));
        MutableHand finalHand = getField(s.active, "hand");
        assertTrue(
                "chosen card should be in the played zone",
                finalHand.getPlayedCards().contains(evergreenTest));
    }
}
