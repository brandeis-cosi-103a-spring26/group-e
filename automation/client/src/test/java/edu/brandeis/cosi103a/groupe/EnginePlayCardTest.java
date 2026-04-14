package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * Tests for card playing mechanics, now exercised through {@link
 * TurnRunner#processPlayCardDecision} rather than via reflection on OurEngine private methods. This
 * gives better isolation and clearer failure messages.
 */
public class EnginePlayCardTest {

    // ========== Helper Methods ==========

    /** Sets up a standard 2-player game and returns (playerInfos, shop, runner). */
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
                        Card.Type.MONITORING,
                        Card.Type.DAILY_SCRUM);
            MutableCardStacks stacks = new MutableCardStacks(numPlayers, actionCards);
            shop = new ShopManager(stacks, counter);

            players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new PlayerInfo(new BigMoneyPlayer(), counter));
            }
            active = players.get(0);

            edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry registry =
                    new edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry(
                            List.of(
                                    new edu.brandeis.cosi103a.groupe.main.actions
                                            .CodeReviewEffect(),
                                    new edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect(),
                                    new edu.brandeis.cosi103a.groupe.main.actions
                                        .EvergreenTestEffect(),
                                    new edu.brandeis.cosi103a.groupe.main.actions
                                            .MonitoringEffect(),
                                    new edu.brandeis.cosi103a.groupe.main.actions
                                        .DailyScrumEffect()));
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

    // ========== processPlayCardDecision Tests ==========

    @Test
    public void testProcessPlayCardDecision_ActionCard_MovesToPlayed() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        // Add CODE_REVIEW to deck and draw it
        List<Card> deck = getField(s.active, "deck");
        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        deck.add(codeReview);
        s.active.drawCard();

        assertTrue("Card should be in unplayed", s.active.getUnplayedCards().contains(codeReview));

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        assertFalse(
                "Card should not be in unplayed", s.active.getUnplayedCards().contains(codeReview));
        MutableHand hand = getField(s.active, "hand");
        assertTrue("Card should be in played", hand.getPlayedCards().contains(codeReview));
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

        assertEquals("Initial money should be 0", 0, s.active.numMoney());

        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));

        assertEquals("Money should be 2", 2, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_ActionCard_DecrementsActions() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustActions(1); // now has 2 actions

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(codeReview);
        s.active.drawCard();

        assertEquals("Should start with 2 actions", 2, s.active.numActions());

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        // CODE_REVIEW: -1 for playing + 2 from effect = net +1
        assertEquals("Actions should be 3 (2 - 1 + 2)", 3, s.active.numActions());
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessPlayCardDecision_ActionCard_ZeroActions_ThrowsException()
            throws Throwable {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustActions(-1); // 0 actions

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        MutableHand hand = getField(s.active, "hand");
        hand.addToUnplayed(codeReview);

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));
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

        // fakeCard is NOT in hand — should throw
        s.runner.processPlayCardDecision(new PlayCardDecision(fakeCard));
    }

    // ========== Money Card Tests ==========

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
        assertEquals("Money should be 1", 1, s.active.numMoney());

        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));
        assertEquals("Money should be 3", 3, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_ActionCard_WithEmptyDeck_StillExecutes()
            throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustActions(1); // 2 actions

        // Empty deck and discard so draw fails silently
        List<Card> deck = getField(s.active, "deck");
        List<Card> discard = getField(s.active, "discard");
        deck.clear();
        discard.clear();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        MutableHand hand = getField(s.active, "hand");
        hand.addToUnplayed(codeReview);

        int initialActions = s.active.numActions();
        int initialHandSize = s.active.getUnplayedCards().size();

        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));

        assertEquals(
                "Should add actions even with empty deck",
                initialActions + 1,
                s.active.numActions());
        assertEquals(
                "Hand should decrease by 1 (played, nothing drawn)",
                initialHandSize - 1,
                s.active.getUnplayedCards().size());
    }

    @Test
    public void testProcessPlayCardDecision_EvergreenTestCard_DrawsTwoCards() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        PlayerInfo opponent = s.players.get(1);

        // Ensure active player's deck has 2 cards to draw
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

        // -1 played + 2 drawn = +1
        assertEquals(
                "Should draw 2 cards", initialHandSize + 1, s.active.getUnplayedCards().size());
        assertEquals(
                "Opponent discard should have one more card",
                initialOpponentDiscardSize + 1,
                opponentDiscard.size());
        Card gainedCard = opponentDiscard.get(opponentDiscard.size() - 1);
        assertEquals("Gained card should be a BUG", Card.Type.BUG, gainedCard.type());
    }

    @Test
    public void testProcessPlayCardDecision_Monitoring_DrawsTwoCards() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        MutableHand hand = getField(s.active, "hand");
        Card monitoring = new Card(Card.Type.MONITORING, counter.getAndIncrement());
        hand.addToUnplayed(monitoring);

        int initialHandSize = s.active.getUnplayedCards().size();
        int initialActions = s.active.numActions();

        s.runner.processPlayCardDecision(new PlayCardDecision(monitoring));

        assertEquals(
                "Monitoring should net +1 card in hand (-1 played, +2 drawn)",
                initialHandSize + 1,
                s.active.getUnplayedCards().size());
        assertEquals("Monitoring should consume one action", initialActions - 1, s.active.numActions());
    }

    @Test
    public void testProcessPlayCardDecision_EvergreenBlockedByMonitoring_LogsAndKeepsCardInHand()
            throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.runner.setCurrentTurnLog(new TurnLog(1, s.active.getDisplayName()));

        PlayerInfo opponent = s.players.get(1);
        AtomicInteger counter = getField(s.shop, "cardIdCounter");

        MutableHand activeHand = getField(s.active, "hand");
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        activeHand.addToUnplayed(evergreenTest);

        MutableHand opponentHand = getField(opponent, "hand");
        Card monitoring = new Card(Card.Type.MONITORING, counter.getAndIncrement());
        opponentHand.addToUnplayed(monitoring);

        List<Card> opponentDiscard = getField(opponent, "discard");
        int initialOpponentDiscardSize = opponentDiscard.size();

        s.runner.processPlayCardDecision(new PlayCardDecision(evergreenTest));

        assertEquals(
                "Opponent should be unaffected and gain no BUG",
                initialOpponentDiscardSize,
                opponentDiscard.size());
        assertTrue(
                "Monitoring used as reaction should remain in opponent hand",
                opponent.getUnplayedCards().contains(monitoring));

        TurnLog log = getField(s.runner, "currentTurnLog");
        assertTrue(
                "Turn log should mention attack was avoided by Monitoring",
                log.getEntries().stream().anyMatch(e -> e.contains("revealed Monitoring")));
    }

    @Test
    public void testProcessPlayCardDecision_DailyScrum_DrawsAndAddsBuyAndOthersDraw()
            throws Exception {
        Setup s = new Setup(3);
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

        assertEquals(
                "Active player should net +3 cards (-1 played, +4 drawn)",
                initialHandSize + 3,
                s.active.getUnplayedCards().size());
        assertEquals("Active player should gain +1 buy", initialBuys + 1, s.active.numBuys());
        assertEquals(
                "Playing Daily Scrum should consume exactly 1 action",
                initialActions - 1,
                s.active.numActions());
        assertEquals(
                "Opponent 1 should draw one card",
                initialOpponent1HandSize + 1,
                opponent1.getUnplayedCards().size());
        assertEquals(
                "Opponent 2 should draw one card",
                initialOpponent2HandSize + 1,
                opponent2.getUnplayedCards().size());
    }

    @Test
    public void testProcessPlayCardDecision_RefactorCard_TransitionsToGainPhase() throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card refactor = new Card(Card.Type.REFACTOR, counter.getAndIncrement());
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(refactor);
        hand.addToUnplayed(bitcoin);

        assertEquals(
                "Should start in ACTION phase", GameState.TurnPhase.ACTION, s.runner.getPhase());

        // BigMoneyPlayer handles trash/gain automatically (returns options.get(0))
        s.runner.processPlayCardDecision(new PlayCardDecision(refactor));

        assertEquals(
                "Should return to ACTION phase after REFACTOR",
                GameState.TurnPhase.ACTION,
                s.runner.getPhase());
    }

    @Test
    public void testProcessPlayCardDecision_MultipleActionCards_DecrementsActionsCorrectly()
            throws Exception {
        Setup s = new Setup(2);
        s.active.turnReset();
        s.active.adjustActions(1); // 2 actions

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        MutableHand hand = getField(s.active, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, counter.getAndIncrement());
        hand.addToUnplayed(codeReview);
        hand.addToUnplayed(evergreenTest);

        List<Card> deck = getField(s.active, "deck");
        while (deck.size() < 3) {
            deck.add(new Card(Card.Type.BITCOIN, counter.getAndIncrement()));
        }

        assertEquals("Should start with 2 actions", 2, s.active.numActions());

        // CODE_REVIEW: 2 - 1 + 2 = 3
        s.runner.processPlayCardDecision(new PlayCardDecision(codeReview));
        assertEquals("After CODE_REVIEW: should have 3 actions", 3, s.active.numActions());

        // EVERGREEN_TEST: 3 - 1 = 2
        s.runner.processPlayCardDecision(new PlayCardDecision(evergreenTest));
        assertEquals("After EVERGREEN_TEST: should have 2 actions", 2, s.active.numActions());
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

        // Second play of the same card — card is no longer in unplayed, should throw
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
        assertEquals("After BITCOIN", 1, s.active.numMoney());

        s.runner.processPlayCardDecision(new PlayCardDecision(ethereum));
        assertEquals("After ETHEREUM", 3, s.active.numMoney());

        s.runner.processPlayCardDecision(new PlayCardDecision(dogecoin));
        assertEquals("After DOGECOIN", 6, s.active.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_MoneyPhase_DoesNotRequireActions() throws Exception {
        Setup s = new Setup(2);
        s.runner.setPhase(GameState.TurnPhase.MONEY);
        s.active.turnReset();
        s.active.adjustActions(-1); // 0 actions

        assertEquals("Should have 0 actions", 0, s.active.numActions());

        AtomicInteger counter = getField(s.shop, "cardIdCounter");
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        List<Card> deck = getField(s.active, "deck");
        deck.add(bitcoin);
        s.active.drawCard();

        // Money card with 0 actions — should work fine
        s.runner.processPlayCardDecision(new PlayCardDecision(bitcoin));

        assertEquals("Money should be added", 1, s.active.numMoney());
    }
}
