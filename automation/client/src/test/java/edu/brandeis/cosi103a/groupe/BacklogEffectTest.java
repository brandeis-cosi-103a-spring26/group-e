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
import edu.brandeis.cosi103a.groupe.main.actions.BacklogEffect;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * Unit tests for {@link BacklogEffect}.
 */
public class BacklogEffectTest {

    // ========== Setup ==========

    private static class Setup {
        final List<PlayerInfo> players;
        final PlayerInfo active;
        final ShopManager shop;
        final TurnRunner runner;

        Setup(int numPlayers) throws Exception {
            AtomicInteger counter = new AtomicInteger(1);
            com.google.common.collect.ImmutableSet<Card.Type> actionCards =
                    com.google.common.collect.ImmutableSet.of(Card.Type.BACKLOG);
            MutableCardStacks stacks = new MutableCardStacks(numPlayers, actionCards);
            shop = new ShopManager(stacks, counter);

            players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new PlayerInfo(new BigMoneyPlayer(), counter));
            }
            active = players.get(0);

            BacklogEffect backlog = new BacklogEffect();
            ActionEffectRegistry registry = new ActionEffectRegistry(List.of(backlog));

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

    @Test
    public void testBacklog_EmptyHand_GivesActionAndDrawsNothing() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        // Clear hand entirely
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card backlog = new Card(Card.Type.BACKLOG, counter.getAndIncrement());
        hand.addToUnplayed(backlog);

        int actionsBefore = s.active.numActions();
        int unplayedBefore = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(backlog));

        // +1 action -1 from playing action = no net change
        assertEquals("Actions remain the same", actionsBefore, s.active.numActions());
        // Played backlog, so hand is empty, player discards 0, draws 0.
        assertEquals("Unplayed hand should be empty", 0, s.active.getUnplayedCards().size());
        assertTrue("Chosen card should be in played", hand.getPlayedCards().contains(backlog));
    }

    @Test
    public void testBacklog_NonEmptyHand_DiscardsAndDraws() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        // Give the player a Backlog and two extra cards in hand
        MutableHand hand = getField(s.active, "hand");
        hand.clear();

        Card backlog = new Card(Card.Type.BACKLOG, counter.getAndIncrement());
        Card bitCoin1 = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        Card bitCoin2 = new Card(Card.Type.BITCOIN, counter.getAndIncrement());

        hand.addToUnplayed(backlog);
        hand.addToUnplayed(bitCoin1);
        hand.addToUnplayed(bitCoin2);

        // Ensure deck has enough to draw
        List<Card> deck = getField(s.active, "deck");
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));
        deck.add(new Card(Card.Type.ETHEREUM, counter.getAndIncrement()));

        int actionsBefore = s.active.numActions();
        
        // Before playing, unplayed has 3 cards (Backlog + 2 Bitcoins)
        s.runner.processPlayCardDecision(new PlayCardDecision(backlog));

        // Net actions change: +1 (backlog) -1 (playing action) = 0
        assertEquals("Actions unchanged", actionsBefore, s.active.numActions());
        
        // BigMoneyPlayer defaults to selecting options.get(0).
        // Since buildDiscardDecisions adds DiscardCardDecisions first, 
        // it will discard all cards in unplayed hand.
        // It discarded two Bitcoins -> drew 2 cards. Hand should have 2 newly drawn cards.
        assertEquals("Hand has 2 newly drawn cards", 2, s.active.getUnplayedCards().size());
        
        // Verify those 2 Bitcoins got moved to the discard pile
        List<Card> discardPile = getField(s.active, "discard");
        assertTrue("Discard pile contains bitcoin 1", discardPile.contains(bitCoin1));
        assertTrue("Discard pile contains bitcoin 2", discardPile.contains(bitCoin2));
    }

}
