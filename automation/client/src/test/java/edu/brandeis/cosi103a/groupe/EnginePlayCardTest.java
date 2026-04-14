package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.assertEquals;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.main.TurnRunner;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/** Tests for money/victory card play routing through TurnRunner.processPlayCardDecision. */
public class EnginePlayCardTest {

    private static class Setup {
        final List<PlayerInfo> players;
        final PlayerInfo active;
        final ShopManager shop;
        final TurnRunner runner;

        Setup(int numPlayers) throws Exception {
            AtomicInteger counter = new AtomicInteger(1);
            com.google.common.collect.ImmutableSet<Card.Type> actionCards =
                    com.google.common.collect.ImmutableSet.of();
            MutableCardStacks stacks = new MutableCardStacks(numPlayers, actionCards);
            shop = new ShopManager(stacks, counter);

            players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new PlayerInfo(new BigMoneyPlayer(), counter));
            }
            active = players.get(0);

            ActionEffectRegistry registry = new ActionEffectRegistry(List.of());
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

    @Test
    public void testProcessPlayCardDecision_MoneyCard_AddsCorrectValue() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustMoney(-s.active.numMoney());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card ethereum = new Card(Card.Type.ETHEREUM, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(ethereum);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));

        assertEquals("Money should be 2", 2, s.active.numMoney());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProcessPlayCardDecision_VictoryCard_ThrowsException() throws Throwable {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card method = new Card(Card.Type.METHOD, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(method);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(method));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessPlayCardDecision_CardNotInHand_ThrowsException() throws Throwable {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card fakeCard = new Card(Card.Type.BITCOIN, counter.getAndIncrement());

        s.runner.processPlayCardDecision(new PlayCardDecision(fakeCard));
    }

    @Test
    public void testProcessPlayCardDecision_Bitcoin_AddsOneMoney() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustMoney(-s.active.numMoney());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(bitcoin);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));

        assertEquals("Money should be 1", 1, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_Dogecoin_AddsThreeMoney() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustMoney(-s.active.numMoney());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card dogecoin = new Card(Card.Type.DOGECOIN, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(dogecoin);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(dogecoin));

        assertEquals("Money should be 3", 3, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_MultipleMoneyCards_Accumulates() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustMoney(-s.active.numMoney());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(ethereum);
        deck.add(bitcoin);
        s.active.drawCard();
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));
        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));

        assertEquals("Money should be 3", 3, s.active.numMoney());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessPlayCardDecision_PlayedCard_CannotBePlayedAgain() throws Throwable {
        Setup s = new Setup(2);
        s.runner.setPhase(GameState.TurnPhase.MONEY);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(bitcoin);

        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));
        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));
    }

    @Test
    public void testProcessPlayCardDecision_AllThreeMoneyTypes_Accumulate() throws Exception {
        Setup s = new Setup(2);
        s.runner.setPhase(GameState.TurnPhase.MONEY);
        s.active.turnReset();
        s.active.adjustMoney(-s.active.numMoney());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, counter.getAndIncrement());
        Card dogecoin = new Card(Card.Type.DOGECOIN, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(dogecoin);
        deck.add(ethereum);
        deck.add(bitcoin);
        s.active.drawCard();
        s.active.drawCard();
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));
        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));
        s.runner.processPlayCardDecision(new PlayCardDecision(dogecoin));

        assertEquals("After DOGECOIN", 6, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_MoneyPhase_DoesNotRequireActions() throws Exception {
        Setup s = new Setup(2);
        s.runner.setPhase(GameState.TurnPhase.MONEY);
        s.active.turnReset();
        s.active.adjustActions(-1);

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(bitcoin);
        s.active.drawCard();

        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));

        assertEquals("Money should be added", 1, s.active.numMoney());
    }
}
