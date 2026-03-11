package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * Comprehensive tests for card playing mechanics in OurEngine. Focuses on detailed scenarios for
 * processPlayCardDecision, action card execution, and edge cases for card interactions.
 */
public class EnginePlayCardTest {

    // ========== Helper Methods ==========

    /** Creates a list of BigMoneyPlayer instances. */
    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new BigMoneyPlayer());
        }
        return players;
    }

    /** Uses reflection to access a private field from an object. */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    /** Uses reflection to set a private field in an object. */
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /** Uses reflection to invoke a private method. */
    private Object invokePrivateMethod(
            Object obj, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    // ========== processPlayCardDecision Tests ==========

    @Test
    public void testProcessPlayCardDecision_ActionCard_MovesToPlayed() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Add an action card to hand
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(codeReview);
        activePlayer.drawCard();

        // Verify card is in unplayed
        assertTrue(
                "Card should be in unplayed", activePlayer.getUnplayedCards().contains(codeReview));

        // Play the card
        PlayCardDecision decision = new PlayCardDecision(codeReview);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // Verify card moved to played
        assertFalse(
                "Card should not be in unplayed",
                activePlayer.getUnplayedCards().contains(codeReview));
        MutableHand hand = getPrivateField(activePlayer, "hand");
        assertTrue("Card should be in played", hand.getPlayedCards().contains(codeReview));
    }

    @Test
    public void testProcessPlayCardDecision_MoneyCard_AddsCorrectValue() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Set initial money to 0
        activePlayer.turnReset();
        activePlayer.adjustMoney(-activePlayer.numMoney());

        // Add ETHEREUM (value 2) to hand
        Card ethereum = new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(ethereum);
        activePlayer.drawCard();

        assertEquals("Initial money should be 0", 0, activePlayer.numMoney());

        // Play the money card
        PlayCardDecision decision = new PlayCardDecision(ethereum);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        assertEquals("Money should be 2", 2, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_ActionCard_DecrementsActions() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Set to 2 actions
        activePlayer.turnReset();
        activePlayer.adjustActions(1);
        assertEquals("Should start with 2 actions", 2, activePlayer.numActions());

        // Add CODE_REVIEW to hand (gives +2 actions)
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(codeReview);
        activePlayer.drawCard();

        // Play the card
        PlayCardDecision decision = new PlayCardDecision(codeReview);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // CODE_REVIEW: -1 for playing + 2 from effect = net +1
        assertEquals("Actions should be 3 (2 - 1 + 2)", 3, activePlayer.numActions());
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessPlayCardDecision_ActionCard_ZeroActions_ThrowsException()
            throws Throwable {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Set to 0 actions
        activePlayer.turnReset();
        activePlayer.adjustActions(-1);
        assertEquals("Should have 0 actions", 0, activePlayer.numActions());

        // Add action card to hand
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(codeReview);
        activePlayer.drawCard();

        // Try to play the card - should throw
        PlayCardDecision decision = new PlayCardDecision(codeReview);
        try {
            invokePrivateMethod(
                    engine,
                    "processPlayCardDecision",
                    new Class<?>[] {PlayCardDecision.class},
                    decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProcessPlayCardDecision_VictoryCard_ThrowsException() throws Throwable {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Add victory card to hand
        Card method = new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(method);
        activePlayer.drawCard();

        // Try to play victory card - should throw
        PlayCardDecision decision = new PlayCardDecision(method);
        try {
            invokePrivateMethod(
                    engine,
                    "processPlayCardDecision",
                    new Class<?>[] {PlayCardDecision.class},
                    decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessPlayCardDecision_CardNotInHand_ThrowsException() throws Throwable {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Create a card that's not in the player's hand
        Card fakeCard = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());

        // Try to play card not in hand - should throw
        PlayCardDecision decision = new PlayCardDecision(fakeCard);
        try {
            invokePrivateMethod(
                    engine,
                    "processPlayCardDecision",
                    new Class<?>[] {PlayCardDecision.class},
                    decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    // ========== Money Card Playing Tests ==========

    @Test
    public void testProcessPlayCardDecision_Bitcoin_AddsOneMoney() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        activePlayer.turnReset();
        activePlayer.adjustMoney(-activePlayer.numMoney());

        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(bitcoin);
        activePlayer.drawCard();

        PlayCardDecision decision = new PlayCardDecision(bitcoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        assertEquals("Money should be 1", 1, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_Dogecoin_AddsThreeMoney() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        activePlayer.turnReset();
        activePlayer.adjustMoney(-activePlayer.numMoney());

        Card dogecoin = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(dogecoin);
        activePlayer.drawCard();

        PlayCardDecision decision = new PlayCardDecision(dogecoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        assertEquals("Money should be 3", 3, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_MultipleMoneyCards_Accumulates() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        activePlayer.turnReset();
        activePlayer.adjustMoney(-activePlayer.numMoney());

        // Add multiple money cards
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(ethereum);
        deck.add(bitcoin);
        activePlayer.drawCard();
        activePlayer.drawCard();

        // Play BITCOIN (1)
        PlayCardDecision decision1 = new PlayCardDecision(bitcoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision1);
        assertEquals("Money should be 1", 1, activePlayer.numMoney());

        // Play ETHEREUM (2)
        PlayCardDecision decision2 = new PlayCardDecision(ethereum);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision2);
        assertEquals("Money should be 3", 3, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_ActionCard_WithEmptyDeck_StillExecutes()
            throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Empty the deck and discard so card draw will fail
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        deck.clear();
        discard.clear();

        // Give player actions
        activePlayer.turnReset();
        activePlayer.adjustActions(1);

        // Add CODE_REVIEW to hand (draws 1 card, adds 2 actions)
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(codeReview);

        int initialActions = activePlayer.numActions();
        int initialHandSize = activePlayer.getUnplayedCards().size();

        // Play CODE_REVIEW - should still add actions even if draw fails
        PlayCardDecision decision = new PlayCardDecision(codeReview);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // Should add 2 actions and decrement 1 for playing = net +1
        assertEquals(
                "Should add actions even with empty deck",
                initialActions + 1,
                activePlayer.numActions());
        // Hand size should decrease by 1 (card played, nothing drawn)
        assertEquals(
                "Hand should decrease by 1",
                initialHandSize - 1,
                activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testProcessPlayCardDecision_EvergreenTestCard_DrawsTwoCards() throws Exception {
        // Create fresh players to verify BUG distribution
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        PlayerInfo opponent = players.get(1);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Ensure deck has enough cards
        List<Card> deck = getPrivateField(activePlayer, "deck");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        // Give player an action
        activePlayer.turnReset();

        // Add EVERGREEN_TEST to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(evergreenTest);

        int initialHandSize = activePlayer.getUnplayedCards().size();
        List<Card> opponentDiscard = getPrivateField(opponent, "discard");
        int initialOpponentDiscardSize = opponentDiscard.size();

        // Play EVERGREEN_TEST (draws 2 cards, gives BUG to opponent)
        PlayCardDecision decision = new PlayCardDecision(evergreenTest);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // Should draw 2 cards: -1 for playing, +2 for drawing = +1
        assertEquals(
                "Should draw 2 cards", initialHandSize + 1, activePlayer.getUnplayedCards().size());
        // Opponent should gain a BUG
        assertEquals(
                "Opponent should gain a BUG",
                initialOpponentDiscardSize + 1,
                opponentDiscard.size());
    }

    @Test
    public void testProcessPlayCardDecision_RefactorCard_TransitionsToGainPhase() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Give player an action
        activePlayer.turnReset();

        // Add REFACTOR and a card to trash to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card refactor = new Card(Card.Type.REFACTOR, cardIdCounter.getAndIncrement());
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(refactor);
        hand.addToUnplayed(bitcoin);

        GameState.TurnPhase initialPhase = getPrivateField(engine, "phase");
        assertEquals("Should start in ACTION phase", GameState.TurnPhase.ACTION, initialPhase);

        // Play REFACTOR (will trash a card and gain a card, temporarily entering GAIN phase)
        PlayCardDecision decision = new PlayCardDecision(refactor);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // Phase should return to ACTION after REFACTOR completes
        GameState.TurnPhase finalPhase = getPrivateField(engine, "phase");
        assertEquals(
                "Should return to ACTION phase after REFACTOR",
                GameState.TurnPhase.ACTION,
                finalPhase);
    }

    @Test
    public void testProcessPlayCardDecision_MultipleActionCards_DecrementsActionsCorrectly()
            throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Start with 2 actions
        activePlayer.turnReset();
        activePlayer.adjustActions(1);
        assertEquals("Should start with 2 actions", 2, activePlayer.numActions());

        // Add CODE_REVIEW and EVERGREEN_TEST to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        Card evergreenTest = new Card(Card.Type.EVERGREEN_TEST, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(codeReview);
        hand.addToUnplayed(evergreenTest);

        // Ensure deck has cards for drawing
        List<Card> deck = getPrivateField(activePlayer, "deck");
        while (deck.size() < 3) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        // Play CODE_REVIEW: 2 actions -1 for playing +2 from effect = 3 actions
        PlayCardDecision decision1 = new PlayCardDecision(codeReview);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision1);
        assertEquals("After CODE_REVIEW: should have 3 actions", 3, activePlayer.numActions());

        // Play EVERGREEN_TEST: 3 actions -1 for playing +0 from effect = 2 actions
        PlayCardDecision decision2 = new PlayCardDecision(evergreenTest);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision2);
        assertEquals("After EVERGREEN_TEST: should have 2 actions", 2, activePlayer.numActions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessPlayCardDecision_PlayedCard_CannotBePlayedAgain() throws Throwable {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Add BITCOIN to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoin);

        // Play the card once
        PlayCardDecision decision = new PlayCardDecision(bitcoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        // Try to play the same card again - should throw IllegalArgumentException
        // because the card is no longer in unplayed cards
        try {
            invokePrivateMethod(
                    engine,
                    "processPlayCardDecision",
                    new Class<?>[] {PlayCardDecision.class},
                    decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testProcessPlayCardDecision_AllThreeMoneyTypes_Accumulate() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Reset money to 0
        activePlayer.turnReset();
        activePlayer.adjustMoney(-activePlayer.numMoney());

        // Add all three money types to hand
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement());
        Card dogecoin = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(dogecoin);
        deck.add(ethereum);
        deck.add(bitcoin);
        activePlayer.drawCard();
        activePlayer.drawCard();
        activePlayer.drawCard();

        // Play BITCOIN (1)
        PlayCardDecision decision1 = new PlayCardDecision(bitcoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision1);
        assertEquals("After BITCOIN", 1, activePlayer.numMoney());

        // Play ETHEREUM (2)
        PlayCardDecision decision2 = new PlayCardDecision(ethereum);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision2);
        assertEquals("After ETHEREUM", 3, activePlayer.numMoney());

        // Play DOGECOIN (3)
        PlayCardDecision decision3 = new PlayCardDecision(dogecoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision3);
        assertEquals("After DOGECOIN", 6, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayCardDecision_MoneyPhase_DoesNotRequireActions() throws Exception {
        // Create fresh players
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Set actions to 0 (money cards don't require actions)
        activePlayer.turnReset();
        activePlayer.adjustActions(-1);
        assertEquals("Should have 0 actions", 0, activePlayer.numActions());

        // Add BITCOIN to hand
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        List<Card> deck = getPrivateField(activePlayer, "deck");
        deck.add(bitcoin);
        activePlayer.drawCard();

        // Play money card with 0 actions - should work fine
        PlayCardDecision decision = new PlayCardDecision(bitcoin);
        invokePrivateMethod(
                engine,
                "processPlayCardDecision",
                new Class<?>[] {PlayCardDecision.class},
                decision);

        assertEquals("Money should be added", 1, activePlayer.numMoney());
    }

    // ========== Placeholder for additional tests ==========
    // Note: Can expand with more tests later if needed.
}
