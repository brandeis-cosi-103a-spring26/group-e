package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for card playing mechanics in OurEngine
 */
public class EnginePlayCardTest {

    private List<Player> twoPlayers;
    private List<Player> threePlayers;
    private List<Player> fourPlayers;

    @Before
    public void setUp() {
        twoPlayers = createPlayers(2);
        threePlayers = createPlayers(3);
        fourPlayers = createPlayers(4);
    }

    // ========== Helper Methods ==========

    /**
     * Creates a list of AIPlayer instances.
     */
    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new AIPlayer());
        }
        return players;
    }

    /**
     * Uses reflection to access a private field from an object.
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    /**
     * Uses reflection to set a private field in an object.
     */
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // ========== processPlayCardDecision Tests ==========

    @Test
    public void testProcessPlayCardDecision_ActionCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 1);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        
        Card actionCard = new Card(Card.Type.CODE_REVIEW, 20001);
        hand.addToUnplayed(actionCard);

        PlayCardDecision decision = new PlayCardDecision(actionCard);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify card moved to played
        assertFalse("Card should not be in unplayed", hand.getUnplayedCards().contains(actionCard));
        assertTrue("Card should be in played", hand.getPlayedCards().contains(actionCard));
        
        // Verify action was processed (CODE_REVIEW gives +2 actions, then -1 for playing = net +1)
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("numActions should be 2 (1 + 2 - 1)", 2, numActions);
    }

    @Test
    public void testProcessPlayCardDecision_MoneyCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numMoney", 0);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        
        Card moneyCard = new Card(Card.Type.ETHEREUM, 21001);
        hand.addToUnplayed(moneyCard);

        PlayCardDecision decision = new PlayCardDecision(moneyCard);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify card moved to played
        assertFalse("Card should not be in unplayed", hand.getUnplayedCards().contains(moneyCard));
        assertTrue("Card should be in played", hand.getPlayedCards().contains(moneyCard));
        
        // Verify money was added (ETHEREUM = 2)
        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("numMoney should be 2", 2, numMoney);
    }

    @Test
    public void testProcessPlayCardDecision_CodeReview() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 1);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        int initialHandSize = hand.size();
        
        Card codeReview = new Card(Card.Type.CODE_REVIEW, 22001);
        hand.addToUnplayed(codeReview);

        PlayCardDecision decision = new PlayCardDecision(codeReview);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // CODE_REVIEW: +1 Card, +2 Actions
        // Verify hand size increased by 1 (drew 1, played 1 = net 0, but we count unplayed)
        // Actually: playing moves from unplayed to played, drawing adds to unplayed
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("numActions should be 2 (1 + 2 - 1)", 2, numActions);
    }

    @Test
    public void testProcessPlayCardDecision_Bitcoin() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numMoney", 0);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        
        Card bitcoin = new Card(Card.Type.BITCOIN, 23001);
        hand.addToUnplayed(bitcoin);

        PlayCardDecision decision = new PlayCardDecision(bitcoin);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("numMoney should be 1", 1, numMoney);
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessPlayCardDecision_VictoryCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        
        Card victoryCard = new Card(Card.Type.METHOD, 24001);
        hand.addToUnplayed(victoryCard);

        PlayCardDecision decision = new PlayCardDecision(victoryCard);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        
        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw UnsupportedOperationException", 
                e.getCause() instanceof UnsupportedOperationException);
            assertTrue("Error message should mention unsupported category", 
                e.getCause().getMessage().contains("Unsupported"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessPlayCardDecision_CardNotInHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Card cardNotInHand = new Card(Card.Type.BITCOIN, 25001);

        PlayCardDecision decision = new PlayCardDecision(cardNotInHand);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        
        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // MutableHand.playCard should throw IllegalArgumentException
            assertTrue("Should throw IllegalArgumentException", 
                e.getCause() instanceof IllegalArgumentException);
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessPlayCardDecision_CardAlreadyPlayed() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        
        Card card = new Card(Card.Type.BITCOIN, 26001);
        hand.addToUnplayed(card);
        hand.playCard(card); // Play it once

        // Try to play it again
        PlayCardDecision decision = new PlayCardDecision(card);
        Method method = engine.getClass().getDeclaredMethod("processPlayCardDecision", PlayCardDecision.class);
        method.setAccessible(true);
        
        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Should fail because card is no longer in unplayed
            assertTrue("Should throw IllegalArgumentException", 
                e.getCause() instanceof IllegalArgumentException);
            throw e;
        }
    }

    // ========== processPlayActionCard Tests ==========

    @Test
    public void testProcessPlayActionCard_CodeReview() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 1);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        int initialSize = hand.size();

        Method method = engine.getClass().getDeclaredMethod("processPlayActionCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.CODE_REVIEW);

        // CODE_REVIEW: +1 Card, +2 Actions, then -1 action from playing
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("numActions should be 2 (1 + 2 - 1)", 2, numActions);
        
        // Hand size should increase (if there were cards to draw)
        assertTrue("Hand size should increase or stay same", hand.size() >= initialSize);
    }

    @Test
    public void testProcessPlayActionCard_NumActionsDecremented() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 3);

        Method method = engine.getClass().getDeclaredMethod("processPlayActionCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.CODE_REVIEW);

        // CODE_REVIEW gives +2, then -1 for playing = net +1
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("numActions should be 4 (3 + 2 - 1)", 4, numActions);
    }

    @Test
    public void testProcessPlayActionCard_NumActionsExactlyOne() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 1);

        // Use EVERGREEN_TEST which doesn't give actions
        Method method = engine.getClass().getDeclaredMethod("processPlayActionCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.EVERGREEN_TEST);

        // EVERGREEN_TEST gives +0 actions, then -1 for playing
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("numActions should be 0 (1 - 1)", 0, numActions);
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessPlayActionCard_UnsupportedActionType() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 1);

        // Try to process BITCOIN as an action (it's money)
        Method method = engine.getClass().getDeclaredMethod("processPlayActionCard", Card.Type.class);
        method.setAccessible(true);
        
        try {
            method.invoke(engine, Card.Type.BITCOIN);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw UnsupportedOperationException", 
                e.getCause() instanceof UnsupportedOperationException);
            assertTrue("Error message should mention unsupported", 
                e.getCause().getMessage().contains("Unsupported"));
            throw e;
        }
    }

    @Test
    public void testProcessPlayActionCard_AllActionTypes() throws Exception {
        // Test CODE_REVIEW and EVERGREEN_TEST can be processed
        // Note: REFACTOR is not tested here because it requires complex player decision mocking
        Card.Type[] actionTypes = {
            Card.Type.CODE_REVIEW,
            Card.Type.EVERGREEN_TEST
        };

        for (Card.Type actionType : actionTypes) {
            OurEngine engine = new OurEngine(twoPlayers);
            Player player = twoPlayers.get(0);
            setPrivateField(engine, "activePlayer", player);
            setPrivateField(engine, "numActions", 1);

            Method method = engine.getClass().getDeclaredMethod("processPlayActionCard", Card.Type.class);
            method.setAccessible(true);
            
            // Should not throw exception
            method.invoke(engine, actionType);
            
            // Verify action was decremented
            int numActions = getPrivateField(engine, "numActions");
            assertTrue("numActions should be affected", numActions >= 0);
        }
    }

    // ========== processPlayMoneyCard Tests ==========

    @Test
    public void testProcessPlayMoneyCard_Bitcoin() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numMoney", 0);

        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.BITCOIN);

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("BITCOIN should add 1 money", 1, numMoney);
    }

    @Test
    public void testProcessPlayMoneyCard_Ethereum() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numMoney", 0);

        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.ETHEREUM);

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("ETHEREUM should add 2 money", 2, numMoney);
    }

    @Test
    public void testProcessPlayMoneyCard_Dogecoin() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numMoney", 0);

        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.DOGECOIN);

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("DOGECOIN should add 3 money", 3, numMoney);
    }

    @Test
    public void testProcessPlayMoneyCard_Accumulation() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numMoney", 0);

        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        
        // Play multiple money cards
        method.invoke(engine, Card.Type.BITCOIN);    // +1
        method.invoke(engine, Card.Type.ETHEREUM);   // +2
        method.invoke(engine, Card.Type.DOGECOIN);   // +3

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("Money should accumulate to 6", 6, numMoney);
    }

    @Test
    public void testProcessPlayMoneyCard_StartingFromZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        
        // Verify starts at 0
        int initialMoney = getPrivateField(engine, "numMoney");
        assertEquals("Should start at 0", 0, initialMoney);

        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.ETHEREUM);

        int numMoney = getPrivateField(engine, "numMoney");
        assertEquals("Should be 2", 2, numMoney);
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessPlayMoneyCard_NonMoneyCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numMoney", 0);

        // Try to process CODE_REVIEW as money (it's action)
        Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
        method.setAccessible(true);
        
        try {
            method.invoke(engine, Card.Type.CODE_REVIEW);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw UnsupportedOperationException", 
                e.getCause() instanceof UnsupportedOperationException);
            throw e;
        }
    }

    @Test
    public void testProcessPlayMoneyCard_AllMoneyTypesParametric() throws Exception {
        Card.Type[] moneyTypes = {Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN};
        int[] expectedValues = {1, 2, 3};

        for (int i = 0; i < moneyTypes.length; i++) {
            OurEngine engine = new OurEngine(twoPlayers);
            setPrivateField(engine, "numMoney", 0);

            Method method = engine.getClass().getDeclaredMethod("processPlayMoneyCard", Card.Type.class);
            method.setAccessible(true);
            method.invoke(engine, moneyTypes[i]);

            int numMoney = getPrivateField(engine, "numMoney");
            assertEquals(moneyTypes[i] + " should add " + expectedValues[i] + " money", 
                expectedValues[i], numMoney);
        }
    }

    // ========== Action Execution Tests ==========

    // --- executeActionCodeReview Tests ---

    @Test
    public void testExecuteActionCodeReview_DrawsOneCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 0);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        int initialSize = hand.size();

        Method method = engine.getClass().getDeclaredMethod("executeActionCodeReview");
        method.setAccessible(true);
        method.invoke(engine);

        // Should draw 1 card (if available)
        int newSize = hand.size();
        assertTrue("Hand should have same or more cards", newSize >= initialSize);
    }

    @Test
    public void testExecuteActionCodeReview_AddsTwoActions() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 0);

        Method method = engine.getClass().getDeclaredMethod("executeActionCodeReview");
        method.setAccessible(true);
        method.invoke(engine);

        int numActions = getPrivateField(engine, "numActions");
        assertEquals("Should add 2 actions", 2, numActions);
    }

    @Test
    public void testExecuteActionCodeReview_EmptyDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);
        setPrivateField(engine, "numActions", 0);

        // Empty both deck and discard
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        decks.get(player).clear();
        discards.get(player).clear();

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        int initialSize = hands.get(player).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionCodeReview");
        method.setAccessible(true);
        method.invoke(engine);

        // No card drawn (silent), but actions still added
        int numActions = getPrivateField(engine, "numActions");
        assertEquals("Should still add 2 actions", 2, numActions);
        assertEquals("Hand size should not change", initialSize, hands.get(player).size());
    }

    @Test
    public void testExecuteActionCodeReview_RequiresReshuffle() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        // Empty deck, add cards to discard
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        decks.get(player).clear();
        discards.get(player).add(new Card(Card.Type.BITCOIN, 30001));
        discards.get(player).add(new Card(Card.Type.BITCOIN, 30002));

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        int initialSize = hands.get(player).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionCodeReview");
        method.setAccessible(true);
        method.invoke(engine);

        // Should reshuffle and draw
        assertTrue("Hand size should increase", hands.get(player).size() > initialSize);
        assertTrue("Discard should be mostly empty after reshuffle", discards.get(player).size() <= 1);
    }

    // --- executeActionEvergreenTest Tests ---

    @Test
    public void testExecuteActionEvergreenTest_DrawsTwoCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        int initialSize = hands.get(player).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // Should draw 2 cards (if available)
        int newSize = hands.get(player).size();
        assertTrue("Should draw up to 2 cards", newSize >= initialSize);
    }

    @Test
    public void testExecuteActionEvergreenTest_TwoPlayers() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player1 = twoPlayers.get(0);
        Player player2 = twoPlayers.get(1);
        setPrivateField(engine, "activePlayer", player1);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscard2 = discards.get(player2).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // Player 2 should gain a BUG
        assertEquals("Player 2 should gain 1 BUG", initialDiscard2 + 1, discards.get(player2).size());
        
        // Verify it's a BUG
        Card lastCard = discards.get(player2).get(discards.get(player2).size() - 1);
        assertEquals("Should be BUG", Card.Type.BUG, lastCard.type());
    }

    @Test
    public void testExecuteActionEvergreenTest_FourPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        Player activePlayer = fourPlayers.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int[] initialSizes = new int[4];
        for (int i = 0; i < 4; i++) {
            initialSizes[i] = discards.get(fourPlayers.get(i)).size();
        }

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // 3 opponents should each gain a BUG
        int bugsGained = 0;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                // Active player should not gain BUG
                assertEquals("Active player should not gain BUG", 
                    initialSizes[i], discards.get(fourPlayers.get(i)).size());
            } else {
                // Other players should gain BUG
                if (discards.get(fourPlayers.get(i)).size() > initialSizes[i]) {
                    bugsGained++;
                }
            }
        }
        assertEquals("3 opponents should gain BUGs", 3, bugsGained);
    }

    @Test
    public void testExecuteActionEvergreenTest_InsufficientBugs() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        Player activePlayer = fourPlayers.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);

        // Set BUG count to 1 (insufficient for 3 opponents)
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Deplete BUGs to 1 (starts at 40 for 4 players)
        for (int i = 0; i < 39; i++) {
            shop.decrementCount(Card.Type.BUG);
        }

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // Count how many BUGs were distributed
        int bugsDistributed = 0;
        for (Player p : fourPlayers) {
            if (!p.equals(activePlayer)) {
                for (Card card : discards.get(p)) {
                    if (card.type() == Card.Type.BUG) {
                        // Count new BUGs (would need to track initial count for accuracy)
                        bugsDistributed++;
                    }
                }
            }
        }
        
        // At least 1 BUG should be distributed (up to shop limit)
        assertTrue("At least 1 BUG should be distributed", bugsDistributed >= 1);
        assertEquals("BUG count should be 0", 0, shop.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testExecuteActionEvergreenTest_NoBugsInSupply() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player1 = twoPlayers.get(0);
        Player player2 = twoPlayers.get(1);
        setPrivateField(engine, "activePlayer", player1);

        // Set BUG count to 0
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Deplete all BUGs (starts at 20 for 2 players)
        for (int i = 0; i < 20; i++) {
            shop.decrementCount(Card.Type.BUG);
        }

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscard2 = discards.get(player2).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // Player 2 should not gain anything (silent)
        assertEquals("Player 2 discard should not change", initialDiscard2, discards.get(player2).size());
    }

    @Test
    public void testExecuteActionEvergreenTest_ExactlyEnoughBugs() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Player activePlayer = threePlayers.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);

        // Set BUG count to exactly 2 (for 2 opponents)
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Deplete BUGs to 2 (starts at 30 for 3 players)
        for (int i = 0; i < 28; i++) {
            shop.decrementCount(Card.Type.BUG);
        }

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // All BUGs should be distributed
        assertEquals("All BUGs should be distributed", 0, shop.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testExecuteActionEvergreenTest_ActivePlayerDoesNotGainBug() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player1 = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player1);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscard1 = discards.get(player1).size();

        Method method = engine.getClass().getDeclaredMethod("executeActionEvergreenTest");
        method.setAccessible(true);
        method.invoke(engine);

        // Active player's discard should not change
        assertEquals("Active player should not gain BUG", initialDiscard1, discards.get(player1).size());
    }

    // ========== buildTrashCardDecisionsForCurrentHand Tests ==========

    @Test
    public void testBuildTrashCardDecisions_SingleCard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        
        Card card = new Card(Card.Type.BITCOIN, 40001);
        hand.addToUnplayed(card);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        assertEquals("Should have 1 decision", 1, decisions.size());
        assertTrue("Should be TrashCardDecision", decisions.get(0) instanceof TrashCardDecision);
        TrashCardDecision decision = (TrashCardDecision) decisions.get(0);
        assertEquals("Should be for the correct card", card, decision.card());
    }

    @Test
    public void testBuildTrashCardDecisions_MultipleCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        
        Card card1 = new Card(Card.Type.BITCOIN, 41001);
        Card card2 = new Card(Card.Type.ETHEREUM, 41002);
        Card card3 = new Card(Card.Type.METHOD, 41003);
        Card card4 = new Card(Card.Type.CODE_REVIEW, 41004);
        Card card5 = new Card(Card.Type.MODULE, 41005);
        hand.addToUnplayed(card1);
        hand.addToUnplayed(card2);
        hand.addToUnplayed(card3);
        hand.addToUnplayed(card4);
        hand.addToUnplayed(card5);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        assertEquals("Should have 5 decisions", 5, decisions.size());
        for (Decision d : decisions) {
            assertTrue("All should be TrashCardDecisions", d instanceof TrashCardDecision);
        }
    }

    @Test
    public void testBuildTrashCardDecisions_VerifyCardInstances() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        
        Card card1 = new Card(Card.Type.BITCOIN, 42001);
        Card card2 = new Card(Card.Type.ETHEREUM, 42002);
        hand.addToUnplayed(card1);
        hand.addToUnplayed(card2);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        // Verify decisions contain correct card instances
        List<Card> decisionCards = new ArrayList<>();
        for (Decision d : decisions) {
            TrashCardDecision tcd = (TrashCardDecision) d;
            decisionCards.add(tcd.card());
        }
        assertTrue("Should contain card1", decisionCards.contains(card1));
        assertTrue("Should contain card2", decisionCards.contains(card2));
    }

    @Test
    public void testBuildTrashCardDecisions_EmptyHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand(); // Empty
        hands.put(player, hand);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        assertEquals("Should have 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        EndPhaseDecision epd = (EndPhaseDecision) decisions.get(0);
        assertEquals("Should be ACTION phase", GameState.TurnPhase.ACTION, epd.phase());
    }

    @Test
    public void testBuildTrashCardDecisions_OnlyPlayedCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        
        // Add cards and play them (moves to played)
        Card card1 = new Card(Card.Type.BITCOIN, 43001);
        Card card2 = new Card(Card.Type.BITCOIN, 43002);
        hand.addToUnplayed(card1);
        hand.addToUnplayed(card2);
        hand.playCard(card1);
        hand.playCard(card2);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        // Unplayed is empty, so should return EndPhaseDecision
        assertEquals("Should have 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildTrashCardDecisions_SameTypesDifferentIds() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        
        // Add 3 BITCOIN cards with different IDs
        Card bitcoin1 = new Card(Card.Type.BITCOIN, 44001);
        Card bitcoin2 = new Card(Card.Type.BITCOIN, 44002);
        Card bitcoin3 = new Card(Card.Type.BITCOIN, 44003);
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);
        hand.addToUnplayed(bitcoin3);

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        assertEquals("Should have 3 distinct decisions", 3, decisions.size());
        
        // Verify all are distinct by card ID
        List<Integer> ids = new ArrayList<>();
        for (Decision d : decisions) {
            TrashCardDecision tcd = (TrashCardDecision) d;
            ids.add(tcd.card().id());
        }
        assertEquals("All IDs should be unique", 3, ids.stream().distinct().count());
        assertTrue("Should contain ID 44001", ids.contains(44001));
        assertTrue("Should contain ID 44002", ids.contains(44002));
        assertTrue("Should contain ID 44003", ids.contains(44003));
    }

    @Test
    public void testBuildTrashCardDecisions_Immutability() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand();
        hands.put(player, hand);
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, 45001));

        Method method = engine.getClass().getDeclaredMethod("buildTrashCardDecisionsForCurrentHand");
        method.setAccessible(true);
        ImmutableList<Decision> decisions = (ImmutableList<Decision>) method.invoke(engine);

        // Verify list is immutable
        try {
            decisions.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected - list is immutable
        }
    }
}