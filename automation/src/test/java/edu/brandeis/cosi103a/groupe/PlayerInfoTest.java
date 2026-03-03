package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;

/**
 * Comprehensive unit tests for the PlayerInfo class.
 * Tests cover initialization, card management, hand operations,
 * turn resource delegation, and equality.
 */
public class PlayerInfoTest {

    private PlayerInfo playerInfo;
    private MockPlayer mockPlayer;

    /**
     * Helper class - simple mock Player for testing
     */
    private static class MockPlayer implements Player {
        private String name;
        private Decision decisionToReturn;

        public MockPlayer(String name) {
            this.name = name;
            this.decisionToReturn = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        }

        public void setDecisionToReturn(Decision decision) {
            this.decisionToReturn = decision;
        }

        @Override
        public Decision makeDecision(GameState state, ImmutableList<Decision> validDecisions) {
            return decisionToReturn;
        }

        @Override
        public String getName() {
            return name;
        }
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
    @SuppressWarnings("unused")
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Before
    public void setUp() {
        mockPlayer = new MockPlayer("TestPlayer");
        playerInfo = new PlayerInfo(mockPlayer, new AtomicInteger(0));
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_InitializesDeckWithCorrectNumberOfCards() throws Exception {
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> hand = playerInfo.getUnplayedCards();
        
        // Total cards should be 10 (7 BITCOIN + 3 METHOD)
        // 5 in hand, rest in deck
        assertEquals("Should have 5 cards remaining in deck after drawing initial hand", 
                     5, deck.size());
        assertEquals("Should have 5 cards in initial hand", 5, hand.size());
    }

    @Test
    public void testConstructor_InitializesDeckWithCorrectCardTypes() throws Exception {
        // Need to count total BITCOINs and METHODs across deck and hand
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> hand = playerInfo.getUnplayedCards();
        
        int bitcoinCount = 0;
        int methodCount = 0;
        
        for (Card card : deck) {
            if (card.type() == Type.BITCOIN) bitcoinCount++;
            if (card.type() == Type.METHOD) methodCount++;
        }
        
        for (Card card : hand) {
            if (card.type() == Type.BITCOIN) bitcoinCount++;
            if (card.type() == Type.METHOD) methodCount++;
        }
        
        assertEquals("Should have 7 BITCOIN cards total", 7, bitcoinCount);
        assertEquals("Should have 3 METHOD cards total", 3, methodCount);
    }

    @Test
    public void testConstructor_InitializesEmptyDiscard() throws Exception {
        List<Card> discard = getPrivateField(playerInfo, "discard");
        assertTrue("Discard pile should be empty initially", discard.isEmpty());
    }

    @Test
    public void testConstructor_InitializesTurnInfoWithDefaults() {
        assertEquals("Should start with 1 action", 1, playerInfo.numActions());
        assertEquals("Should start with 0 money", 0, playerInfo.numMoney());
        assertEquals("Should start with 1 buy", 1, playerInfo.numBuys());
    }

    @Test
    public void testConstructor_AllCardIDsAreUnique() throws Exception {
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> hand = playerInfo.getUnplayedCards();
        
        // Collect all card IDs
        List<Integer> cardIds = new ArrayList<>();
        for (Card card : deck) {
            cardIds.add(card.id());
        }
        for (Card card : hand) {
            cardIds.add(card.id());
        }
        
        // Check all IDs are unique
        assertEquals("All 10 cards should have unique IDs", 10, cardIds.size());
        assertEquals("No duplicate IDs should exist", 
                     cardIds.size(), 
                     new java.util.HashSet<>(cardIds).size());
    }

    @Test
    public void testConstructor_CardIDsIncrementSequentially() throws Exception {
        AtomicInteger counter = new AtomicInteger(100);
        PlayerInfo testPlayer = new PlayerInfo(mockPlayer, counter);
        
        List<Card> deck = getPrivateField(testPlayer, "deck");
        List<Card> hand = testPlayer.getUnplayedCards();
        
        // Collect all card IDs
        List<Integer> cardIds = new ArrayList<>();
        for (Card card : deck) {
            cardIds.add(card.id());
        }
        for (Card card : hand) {
            cardIds.add(card.id());
        }
        
        // Sort to check sequential increment
        java.util.Collections.sort(cardIds);
        
        // IDs should start at 100 and go to 109
        assertEquals("First card ID should be 100", 100, (int)cardIds.get(0));
        assertEquals("Last card ID should be 109", 109, (int)cardIds.get(9));
    }

    @Test
    public void testConstructor_SharedCounterBetweenPlayers() throws Exception {
        AtomicInteger sharedCounter = new AtomicInteger(0);
        
        // Create two players with the same counter
        PlayerInfo player1 = new PlayerInfo(new MockPlayer("Player1"), sharedCounter);
        PlayerInfo player2 = new PlayerInfo(new MockPlayer("Player2"), sharedCounter);
        
        // Get all cards from both players
        List<Card> deck1 = getPrivateField(player1, "deck");
        List<Card> hand1 = player1.getUnplayedCards();
        List<Card> deck2 = getPrivateField(player2, "deck");
        List<Card> hand2 = player2.getUnplayedCards();
        
        // Collect all card IDs from both players
        List<Integer> allCardIds = new ArrayList<>();
        for (Card card : deck1) allCardIds.add(card.id());
        for (Card card : hand1) allCardIds.add(card.id());
        for (Card card : deck2) allCardIds.add(card.id());
        for (Card card : hand2) allCardIds.add(card.id());
        
        // All 20 cards (10 per player) should have unique IDs
        assertEquals("Should have 20 total cards", 20, allCardIds.size());
        assertEquals("All card IDs should be unique across both players", 
                     20, 
                     new java.util.HashSet<>(allCardIds).size());
        
        // Counter should have incremented to 20
        assertEquals("Shared counter should be at 20", 20, sharedCounter.get());
    }

    // ========== Player Delegation Tests ==========

    @Test
    public void testGetName_DelegatesToPlayer() {
        assertEquals("Should return player's name", "TestPlayer", playerInfo.getName());
    }

    @Test
    public void testMakeDecision_DelegatesToPlayer() {
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.BUY);
        mockPlayer.setDecisionToReturn(decision);
        
        ImmutableList<Decision> options = ImmutableList.of(decision);
        Decision result = playerInfo.makeDecision(null, options);
        
        assertEquals("Should return the decision from underlying player", decision, result);
    }

    // ========== Draw Card Tests ==========

    @Test
    public void testDrawCard_DrawsFromDeck() throws Exception {
        List<Card> deck = getPrivateField(playerInfo, "deck");
        int initialDeckSize = deck.size();
        int initialHandSize = playerInfo.getUnplayedCards().size();
        
        playerInfo.drawCard();
        
        assertEquals("Deck should have one less card", initialDeckSize - 1, deck.size());
        assertEquals("Hand should have one more card", initialHandSize + 1, playerInfo.getUnplayedCards().size());
    }

    @Test
    public void testDrawCard_ReshufflesDiscardWhenDeckIsEmpty() throws Exception {
        // Empty the deck
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> discard = getPrivateField(playerInfo, "discard");
        
        // Move all deck cards to discard
        while (!deck.isEmpty()) {
            discard.add(deck.remove(deck.size() - 1));
        }
        
        int discardSize = discard.size();
        assertTrue("Deck should be empty", deck.isEmpty());
        assertTrue("Discard should have cards", discardSize > 0);
        
        // Draw a card - should reshuffle discard into deck
        playerInfo.drawCard();
        
        assertTrue("Discard should be empty after reshuffling", discard.isEmpty());
        assertEquals("Deck should have discardSize - 1 cards", discardSize - 1, deck.size());
    }

    @Test
    public void testDrawCard_NoOpWhenDeckAndDiscardEmpty() throws Exception {
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> discard = getPrivateField(playerInfo, "discard");
        
        // Empty both deck and discard
        deck.clear();
        discard.clear();
        
        int handSize = playerInfo.getUnplayedCards().size();
        
        playerInfo.drawCard();
        
        // Hand size should remain the same
        assertEquals("Hand size should not change when both deck and discard are empty", 
                     handSize, playerInfo.getUnplayedCards().size());
    }

    // ========== Play Card Tests ==========

    @Test
    public void testPlayCard_RemovesFromUnplayed() {
        List<Card> unplayed = playerInfo.getUnplayedCards();
        assertFalse("Should have cards in hand", unplayed.isEmpty());
        
        Card cardToPlay = unplayed.get(0);
        int initialSize = unplayed.size();
        
        playerInfo.playCard(cardToPlay);
        
        List<Card> afterPlay = playerInfo.getUnplayedCards();
        assertEquals("Should have one fewer unplayed card", initialSize - 1, afterPlay.size());
        assertFalse("Played card should not be in unplayed list", afterPlay.contains(cardToPlay));
    }

    // ========== Cleanup Tests ==========

    @Test
    public void testCleanup_DiscardsAllCards() throws Exception {
        int initialDiscardSize = 0; // Discard is empty initially
        
        // Play a card to have both played and unplayed cards
        Card cardToPlay = playerInfo.getUnplayedCards().get(0);
        playerInfo.playCard(cardToPlay);
        
        playerInfo.cleanup();
        
        List<Card> discard = getPrivateField(playerInfo, "discard");
        // All 5 cards from hand should now be in discard
        assertEquals("All 5 cards should be discarded", initialDiscardSize + 5, discard.size());
    }

    @Test
    public void testCleanup_DrawsNewHandOfFive() {
        playerInfo.cleanup();
        
        assertEquals("Should have 5 cards in new hand", 5, playerInfo.getUnplayedCards().size());
    }

    @Test
    public void testCleanup_MultipleTurns() throws Exception {
        List<Card> deck = getPrivateField(playerInfo, "deck");
        
        // First cleanup
        playerInfo.cleanup();
        assertEquals("Should have 5 cards after first cleanup", 5, playerInfo.getUnplayedCards().size());
        
        // Second cleanup - deck should be empty, reshuffle happens
        assertTrue("Deck should be empty after drawing 10 cards", deck.isEmpty());
        
        playerInfo.cleanup();
        assertEquals("Should have 5 cards after second cleanup", 5, playerInfo.getUnplayedCards().size());
    }

    // ========== Gain Card Tests ==========

    @Test
    public void testGainCard_AddsToDiscard() throws Exception {
        List<Card> discard = getPrivateField(playerInfo, "discard");
        int initialSize = discard.size();
        
        Card newCard = new Card(Type.ETHEREUM, 100);
        playerInfo.gainCard(newCard);
        
        assertEquals("Discard should have one more card", initialSize + 1, discard.size());
        assertTrue("New card should be in discard", discard.contains(newCard));
    }

    @Test
    public void testGainCard_MultipleCards() throws Exception {
        List<Card> discard = getPrivateField(playerInfo, "discard");
        
        Card card1 = new Card(Type.ETHEREUM, 101);
        Card card2 = new Card(Type.DOGECOIN, 102);
        
        playerInfo.gainCard(card1);
        playerInfo.gainCard(card2);
        
        assertTrue("First card should be in discard", discard.contains(card1));
        assertTrue("Second card should be in discard", discard.contains(card2));
    }

    // ========== Trash Card Tests ==========

    @Test
    public void testTrashCard_RemovesFromGame() throws Exception {
        List<Card> unplayed = playerInfo.getUnplayedCards();
        Card cardToTrash = unplayed.get(0);
        int initialSize = unplayed.size();
        
        boolean result = playerInfo.trashCard(cardToTrash);
        
        assertTrue("Trash should succeed", result);
        assertEquals("Hand should have one fewer card", initialSize - 1, playerInfo.getUnplayedCards().size());
        assertFalse("Trashed card should not be in hand", playerInfo.getUnplayedCards().contains(cardToTrash));
        
        // Verify card is truly removed from the game - not in deck or discard
        List<Card> deck = getPrivateField(playerInfo, "deck");
        List<Card> discard = getPrivateField(playerInfo, "discard");
        assertFalse("Trashed card should not be in deck", deck.contains(cardToTrash));
        assertFalse("Trashed card should not be in discard", discard.contains(cardToTrash));
    }

    @Test
    public void testTrashCard_ReturnsFalseForNonExistentCard() {
        Card nonExistentCard = new Card(Type.ETHEREUM, 999);
        
        boolean result = playerInfo.trashCard(nonExistentCard);
        
        assertFalse("Trash should fail for card not in hand", result);
    }

    // ========== Score Tests ==========

    @Test
    public void testGetScore_InitiallyStartingMethodScore() {
        assertEquals("Score should be 3 from starting METHOD cards", 3, playerInfo.getScore());
    }

    @Test
    public void testGainCard_VictoryCardIncreasesScore() {
        int initialScore = playerInfo.getScore();
        
        Card method = new Card(Type.METHOD, 1000);
        playerInfo.gainCard(method);
        
        assertEquals("Score should increase by 1 for METHOD card", initialScore + 1, playerInfo.getScore());
        
        Card module = new Card(Type.MODULE, 1001);
        playerInfo.gainCard(module);
        
        assertEquals("Score should increase by 3 for MODULE card", initialScore + 1 + 3, playerInfo.getScore());
        
        Card framework = new Card(Type.FRAMEWORK, 1002);
        playerInfo.gainCard(framework);
        
        assertEquals("Score should increase by 6 for FRAMEWORK card", initialScore + 1 + 3 + 6, playerInfo.getScore());
    }

    @Test
    public void testGainCard_NonVictoryCardDoesNotChangeScore() {
        int initialScore = playerInfo.getScore();
        
        Card bitcoin = new Card(Type.BITCOIN, 2000);
        playerInfo.gainCard(bitcoin);
        
        assertEquals("Score should not change for BITCOIN", initialScore, playerInfo.getScore());
        
        Card ethereum = new Card(Type.ETHEREUM, 2001);
        playerInfo.gainCard(ethereum);
        
        assertEquals("Score should not change for ETHEREUM", initialScore, playerInfo.getScore());
        
        Card dogecoin = new Card(Type.DOGECOIN, 2002);
        playerInfo.gainCard(dogecoin);
        
        assertEquals("Score should not change for DOGECOIN", initialScore, playerInfo.getScore());
    }

    @Test
    public void testTrashCard_VictoryCardDecreasesScore() throws Exception {
        // First gain some victory cards so we have them in hand to trash
        Card method = new Card(Type.METHOD, 3000);
        Card module = new Card(Type.MODULE, 3001);
        
        // Add cards to deck and draw them
        List<Card> deck = getPrivateField(playerInfo, "deck");
        deck.add(method);
        deck.add(module);
        playerInfo.drawCard();
        playerInfo.drawCard();
        
        int initialScore = playerInfo.getScore();
        
        // Trash METHOD (1 point)
        playerInfo.trashCard(method);
        assertEquals("Score should decrease by 1 for METHOD card", initialScore - 1, playerInfo.getScore());
        
        // Trash MODULE (3 points)
        playerInfo.trashCard(module);
        assertEquals("Score should decrease by 3 for MODULE card", initialScore - 1 - 3, playerInfo.getScore());
    }

    @Test
    public void testTrashCard_NonVictoryCardDoesNotChangeScore() throws Exception {
        // Add non-victory cards to deck and draw them
        Card bitcoin = new Card(Type.BITCOIN, 4000);
        Card ethereum = new Card(Type.ETHEREUM, 4001);
        
        List<Card> deck = getPrivateField(playerInfo, "deck");
        deck.add(bitcoin);
        deck.add(ethereum);
        playerInfo.drawCard();
        playerInfo.drawCard();
        
        int initialScore = playerInfo.getScore();
        
        // Trash BITCOIN
        playerInfo.trashCard(bitcoin);
        assertEquals("Score should not change when trashing BITCOIN", initialScore, playerInfo.getScore());
        
        // Trash ETHEREUM
        playerInfo.trashCard(ethereum);
        assertEquals("Score should not change when trashing ETHEREUM", initialScore, playerInfo.getScore());
    }

    @Test
    public void testScore_MultipleGainsAndTrashes() throws Exception {
        int initialScore = playerInfo.getScore();
        
        // Gain victory cards
        Card method1 = new Card(Type.METHOD, 5000);
        Card module1 = new Card(Type.MODULE, 5001);
        playerInfo.gainCard(method1);  // +1
        playerInfo.gainCard(module1);  // +3
        
        assertEquals("Score should be +4 after gains", initialScore + 4, playerInfo.getScore());
        
        // Gain non-victory card (should not affect score)
        Card bitcoin = new Card(Type.BITCOIN, 5002);
        playerInfo.gainCard(bitcoin);
        assertEquals("Score should still be +4 after gaining treasure", initialScore + 4, playerInfo.getScore());
        
        // Add a victory card to hand and trash it
        List<Card> deck = getPrivateField(playerInfo, "deck");
        Card method2 = new Card(Type.METHOD, 5003);
        deck.add(method2);
        playerInfo.drawCard();
        
        playerInfo.trashCard(method2);  // -1
        assertEquals("Score should be +3 after trashing METHOD", initialScore + 3, playerInfo.getScore());
        
        // Add and trash a non-victory card (should not affect score)
        Card ethereum = new Card(Type.ETHEREUM, 5004);
        deck.add(ethereum);
        playerInfo.drawCard();
        playerInfo.trashCard(ethereum);
        
        assertEquals("Score should still be +3 after trashing treasure", initialScore + 3, playerInfo.getScore());
    }

    // ========== Get Cards in Hand Tests ==========

    @Test
    public void testGetUnplayedCards_ReturnsAllUnplayed() {
        List<Card> unplayed = playerInfo.getUnplayedCards();
        assertEquals("Should have 5 unplayed cards initially", 5, unplayed.size());
    }

    @Test
    public void testGetUnplayedCards_AfterPlayingCard() {
        Card cardToPlay = playerInfo.getUnplayedCards().get(0);
        playerInfo.playCard(cardToPlay);
        
        List<Card> unplayed = playerInfo.getUnplayedCards();
        assertEquals("Should have 4 unplayed cards after playing one", 4, unplayed.size());
    }

    @Test
    public void testGetActionsInHand_FiltersActionCards() throws Exception {
        // Clear hand and add specific cards
        MutableHand hand = getPrivateField(playerInfo, "hand");
        List<Card> unplayedCards = getPrivateField(hand, "unplayedCards");
        unplayedCards.clear();
        
        hand.addToUnplayed(new Card(Type.REFACTOR, 200));     // ACTION
        hand.addToUnplayed(new Card(Type.CODE_REVIEW, 201));  // ACTION
        hand.addToUnplayed(new Card(Type.BITCOIN, 202));      // MONEY
        hand.addToUnplayed(new Card(Type.METHOD, 203));       // VICTORY
        
        List<Card> actions = playerInfo.getActionsInHand();
        
        assertEquals("Should have 2 action cards", 2, actions.size());
        for (Card card : actions) {
            assertEquals("All cards should be ACTION category", 
                        Card.Type.Category.ACTION, card.getCategory());
        }
    }

    @Test
    public void testGetActionsInHand_EmptyWhenNoActions() throws Exception {
        MutableHand hand = getPrivateField(playerInfo, "hand");
        List<Card> unplayedCards = getPrivateField(hand, "unplayedCards");
        unplayedCards.clear();
        
        hand.addToUnplayed(new Card(Type.BITCOIN, 300));
        hand.addToUnplayed(new Card(Type.METHOD, 301));
        
        List<Card> actions = playerInfo.getActionsInHand();
        
        assertTrue("Should have no action cards", actions.isEmpty());
    }

    @Test
    public void testGetMoneysInHand_FiltersMoneyCards() throws Exception {
        MutableHand hand = getPrivateField(playerInfo, "hand");
        List<Card> unplayedCards = getPrivateField(hand, "unplayedCards");
        unplayedCards.clear();
        
        hand.addToUnplayed(new Card(Type.BITCOIN, 400));      // MONEY
        hand.addToUnplayed(new Card(Type.ETHEREUM, 401));     // MONEY
        hand.addToUnplayed(new Card(Type.REFACTOR, 402));     // ACTION
        hand.addToUnplayed(new Card(Type.METHOD, 403));       // VICTORY
        
        List<Card> moneys = playerInfo.getMoneysInHand();
        
        assertEquals("Should have 2 money cards", 2, moneys.size());
        for (Card card : moneys) {
            assertEquals("All cards should be MONEY category", 
                        Card.Type.Category.MONEY, card.getCategory());
        }
    }

    @Test
    public void testGetMoneysInHand_EmptyWhenNoMoney() throws Exception {
        MutableHand hand = getPrivateField(playerInfo, "hand");
        List<Card> unplayedCards = getPrivateField(hand, "unplayedCards");
        unplayedCards.clear();
        
        hand.addToUnplayed(new Card(Type.REFACTOR, 500));
        hand.addToUnplayed(new Card(Type.METHOD, 501));
        
        List<Card> moneys = playerInfo.getMoneysInHand();
        
        assertTrue("Should have no money cards", moneys.isEmpty());
    }

    // ========== Immutable Hand Tests ==========

    @Test
    public void testGetImmutableHand_ReturnsHand() {
        Hand immutableHand = playerInfo.getImmutableHand();
        assertNotNull("Should return a hand", immutableHand);
    }

    @Test
    public void testGetImmutableHand_ContainsCorrectCards() {
        Hand immutableHand = playerInfo.getImmutableHand();
        assertEquals("Immutable hand should have same unplayed count as mutable", 
                     playerInfo.getUnplayedCards().size(), 
                     immutableHand.unplayedCards().size());
    }

    // ========== Turn Resource Delegation Tests ==========

    @Test
    public void testNumActions_DelegatesToTurnInfo() {
        assertEquals("Should return 1 action by default", 1, playerInfo.numActions());
    }

    @Test
    public void testAdjustActions_PositiveAmount_DelegatesToTurnInfo() {
        playerInfo.adjustActions(2);
        assertEquals("Should have 3 actions after adjusting by +2", 3, playerInfo.numActions());
    }

    @Test
    public void testAdjustActions_NegativeAmount_DelegatesToTurnInfo() {
        playerInfo.adjustActions(5);
        playerInfo.adjustActions(-2);
        assertEquals("Should have 4 actions after adjusting by -2", 4, playerInfo.numActions());
    }

    @Test
    public void testNumMoney_DelegatesToTurnInfo() {
        assertEquals("Should return 0 money by default", 0, playerInfo.numMoney());
    }

    @Test
    public void testAdjustMoney_PositiveAmount_DelegatesToTurnInfo() {
        playerInfo.adjustMoney(5);
        assertEquals("Should have 5 money after adjusting by +5", 5, playerInfo.numMoney());
    }

    @Test
    public void testAdjustMoney_NegativeAmount_DelegatesToTurnInfo() {
        playerInfo.adjustMoney(10);
        playerInfo.adjustMoney(-3);
        assertEquals("Should have 7 money after adjusting by +10 and -3", 7, playerInfo.numMoney());
    }

    @Test
    public void testNumBuys_DelegatesToTurnInfo() {
        assertEquals("Should return 1 buy by default", 1, playerInfo.numBuys());
    }

    @Test
    public void testAdjustBuys_PositiveAmount_DelegatesToTurnInfo() {
        playerInfo.adjustBuys(4);
        assertEquals("Should have 5 buys after adjusting by +4", 5, playerInfo.numBuys());
    }

    @Test
    public void testAdjustBuys_NegativeAmount_DelegatesToTurnInfo() {
        playerInfo.adjustBuys(5);
        playerInfo.adjustBuys(-2);
        assertEquals("Should have 4 buys after adjusting by -2", 4, playerInfo.numBuys());
    }

    @Test
    public void testTurnReset_DelegatesToTurnInfo() {
        playerInfo.adjustActions(5);
        playerInfo.adjustMoney(10);
        playerInfo.adjustBuys(2);
        
        playerInfo.turnReset();
        
        assertEquals("Actions should reset to 1", 1, playerInfo.numActions());
        assertEquals("Money should reset to 0", 0, playerInfo.numMoney());
        assertEquals("Buys should reset to 1", 1, playerInfo.numBuys());
    }

    // ========== Equals Tests ==========

    @Test
    public void testEquals_SameObject() {
        assertTrue("PlayerInfo should equal itself", playerInfo.equals(playerInfo));
    }

    @Test
    public void testEquals_SamePlayer() {
        PlayerInfo other = new PlayerInfo(mockPlayer, new AtomicInteger(100));
        assertTrue("PlayerInfo with same underlying player should be equal", playerInfo.equals(other));
    }

    @Test
    public void testEquals_DifferentPlayer() {
        MockPlayer otherPlayer = new MockPlayer("OtherPlayer");
        PlayerInfo other = new PlayerInfo(otherPlayer, new AtomicInteger(100));
        assertFalse("PlayerInfo with different players should not be equal", playerInfo.equals(other));
    }

    @Test
    public void testEquals_Null() {
        assertFalse("PlayerInfo should not equal null", playerInfo.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        assertFalse("PlayerInfo should not equal a string", playerInfo.equals("not a PlayerInfo"));
    }

    @Test
    public void testEquals_ConsistentAfterStateChanges() {
        PlayerInfo other = new PlayerInfo(mockPlayer, new AtomicInteger(100));
        
        // Initially equal
        assertTrue("Should be equal initially", playerInfo.equals(other));
        
        // Modify state (TurnInfo, hand, etc.)
        playerInfo.adjustActions(5);
        playerInfo.adjustMoney(10);
        playerInfo.adjustBuys(2);
        
        // Still equal because equality is based only on Player
        assertTrue("Should still be equal after state changes", playerInfo.equals(other));
    }

    // ========== HashCode Tests ==========

    @Test
    public void testHashCode_Consistency() {
        int hash1 = playerInfo.hashCode();
        int hash2 = playerInfo.hashCode();
        assertEquals("hashCode should be consistent across multiple calls", hash1, hash2);
    }

    @Test
    public void testHashCode_EqualObjectsHaveSameHashCode() {
        PlayerInfo other = new PlayerInfo(mockPlayer, new AtomicInteger(100));
        
        assertTrue("Objects should be equal", playerInfo.equals(other));
        assertEquals("Equal objects must have the same hashCode", 
                     playerInfo.hashCode(), other.hashCode());
    }

    @Test
    public void testHashCode_ConsistentAfterStateChanges() {
        int initialHash = playerInfo.hashCode();
        
        // Modify state (TurnInfo, hand, etc.)
        playerInfo.adjustActions(5);
        playerInfo.adjustMoney(10);
        playerInfo.adjustBuys(2);
        
        int hashAfterChanges = playerInfo.hashCode();
        assertEquals("hashCode should remain consistent after state changes", 
                     initialHash, hashAfterChanges);
    }

    @Test
    public void testHashCode_WorksInHashSet() {
        java.util.Set<PlayerInfo> set = new java.util.HashSet<>();
        
        set.add(playerInfo);
        assertTrue("Set should contain playerInfo", set.contains(playerInfo));
        
        // Create another PlayerInfo with same player
        PlayerInfo duplicate = new PlayerInfo(mockPlayer, new AtomicInteger(200));
        assertTrue("Set should contain duplicate (same player)", set.contains(duplicate));
        
        // Adding duplicate should not increase size
        set.add(duplicate);
        assertEquals("Set should still have 1 element (duplicates not added)", 1, set.size());
    }

    @Test
    public void testHashCode_WorksInHashMap() {
        java.util.Map<PlayerInfo, String> map = new java.util.HashMap<>();
        
        map.put(playerInfo, "Original");
        assertEquals("Map should contain the value", "Original", map.get(playerInfo));
        
        // Create another PlayerInfo with same player
        PlayerInfo duplicate = new PlayerInfo(mockPlayer, new AtomicInteger(200));
        assertEquals("Map should find value using duplicate key", "Original", map.get(duplicate));
        
        // Overwriting with duplicate should replace, not add
        map.put(duplicate, "Replaced");
        assertEquals("Map should still have 1 entry", 1, map.size());
        assertEquals("Value should be replaced", "Replaced", map.get(playerInfo));
    }

    @Test
    public void testHashCode_DifferentPlayersInHashSet() {
        java.util.Set<PlayerInfo> set = new java.util.HashSet<>();
        
        MockPlayer player1 = new MockPlayer("Player1");
        MockPlayer player2 = new MockPlayer("Player2");
        
        PlayerInfo info1 = new PlayerInfo(player1, new AtomicInteger(0));
        PlayerInfo info2 = new PlayerInfo(player2, new AtomicInteger(100));
        
        set.add(info1);
        set.add(info2);
        
        assertEquals("Set should contain 2 different PlayerInfo objects", 2, set.size());
        assertTrue("Set should contain info1", set.contains(info1));
        assertTrue("Set should contain info2", set.contains(info2));
    }

    // ========== Integration Tests ==========

    @Test
    public void testCompleteGameTurn() throws Exception {
        // Simulate a complete turn
        List<Card> discard = getPrivateField(playerInfo, "discard");
        
        // Play some cards
        List<Card> unplayed = playerInfo.getUnplayedCards();
        if (!unplayed.isEmpty()) {
            playerInfo.playCard(unplayed.get(0));
        }
        
        // Gain a card
        Card gainedCard = new Card(Type.ETHEREUM, 600);
        playerInfo.gainCard(gainedCard);
        assertTrue("Gained card should be in discard", discard.contains(gainedCard));
        
        // Modify turn resources
        playerInfo.adjustMoney(5);
        playerInfo.adjustBuys(-1);
        
        // Cleanup
        playerInfo.cleanup();
        
        assertEquals("Should have 5 cards in new hand", 5, playerInfo.getUnplayedCards().size());
        
        // Gained card should still be in discard
        assertTrue("Gained card should remain in discard after cleanup", discard.contains(gainedCard));
    }

    @Test
    public void testMultipleTurnsWithGainedCards() throws Exception {
        List<Card> discard = getPrivateField(playerInfo, "discard");
        List<Card> deck = getPrivateField(playerInfo, "deck");
        
        // Turn 1: Gain a card and cleanup
        Card newCard = new Card(Type.CODE_REVIEW, 700);
        playerInfo.gainCard(newCard);
        playerInfo.cleanup();
        
        // After cleanup, deck is empty, gained card is in discard
        assertTrue("Deck should be empty", deck.isEmpty());
        assertTrue("Gained card should be in discard", discard.contains(newCard));
        
        // Turn 2: Cleanup again - should reshuffle discard (including gained card) into deck
        playerInfo.cleanup();
        
        // The gained card should eventually appear in hand or deck
        boolean foundNewCard = false;
        for (Card card : playerInfo.getUnplayedCards()) {
            if (card.equals(newCard)) {
                foundNewCard = true;
                break;
            }
        }
        if (!foundNewCard) {
            for (Card card : deck) {
                if (card.equals(newCard)) {
                    foundNewCard = true;
                    break;
                }
            }
        }
        
        assertTrue("Gained card should eventually be in deck or hand after reshuffling", foundNewCard);
    }

    // ========== Disqualification Tests ==========

    @Test
    public void testIsDisqualified_InitiallyFalse() {
        assertFalse("Player should not be disqualified initially", playerInfo.isDisqualified());
    }

    @Test
    public void testForfeit_SetsDisqualifiedToTrue() {
        playerInfo.forfeit();
        assertTrue("Player should be disqualified after forfeit", playerInfo.isDisqualified());
    }

    @Test
    public void testForfeit_CanBeCalledMultipleTimes() {
        playerInfo.forfeit();
        playerInfo.forfeit();
        assertTrue("Player should remain disqualified", playerInfo.isDisqualified());
    }

    @Test
    public void testDisqualification_DoesNotAffectOtherState() throws Exception {
        // Set up some state
        playerInfo.adjustMoney(5);
        playerInfo.gainCard(new Card(Type.ETHEREUM, 5000));
        
        playerInfo.forfeit();
        
        // State should be unchanged
        assertEquals("Money should be unchanged by disqualification", 5, playerInfo.numMoney());
        assertTrue("Player should still be disqualified", playerInfo.isDisqualified());
    }

}
