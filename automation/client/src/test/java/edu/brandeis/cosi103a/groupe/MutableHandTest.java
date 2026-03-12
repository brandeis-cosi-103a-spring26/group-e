package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;

import static org.junit.Assert.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.Hand;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Exhaustive unit tests for the MutableHand class. Tests all public methods with normal cases, edge
 * cases, and error conditions.
 */
public class MutableHandTest {

    private MutableHand hand;
    private Card bitcoin1;
    private Card bitcoin2;
    private Card method1;
    private Card ethereum1;
    private Card dogecoin1;

    @Before
    public void setUp() {
        hand = new MutableHand();
        bitcoin1 = new Card(Card.Type.BITCOIN, 1);
        bitcoin2 = new Card(Card.Type.BITCOIN, 2);
        method1 = new Card(Card.Type.METHOD, 3);
        ethereum1 = new Card(Card.Type.ETHEREUM, 4);
        dogecoin1 = new Card(Card.Type.DOGECOIN, 5);
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_CreatesEmptyHand() {
        MutableHand newHand = new MutableHand();
        assertTrue("New hand should be empty", newHand.isEmpty());
        assertEquals("New hand should have size 0", 0, newHand.size());
        assertTrue("Unplayed cards should be empty", newHand.getUnplayedCards().isEmpty());
        assertTrue("Played cards should be empty", newHand.getPlayedCards().isEmpty());
    }

    // ========== addToUnplayed Tests ==========

    @Test
    public void testAddToUnplayed_SingleCard() {
        hand.addToUnplayed(bitcoin1);
        assertEquals("Hand should have 1 card", 1, hand.size());
        assertTrue("Hand should contain the card", hand.contains(bitcoin1));
        assertEquals("Unplayed cards should have 1 card", 1, hand.getUnplayedCards().size());
        assertEquals("Played cards should be empty", 0, hand.getPlayedCards().size());
    }

    @Test
    public void testAddToUnplayed_MultipleCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);

        assertEquals("Hand should have 3 cards", 3, hand.size());
        assertTrue("Hand should contain bitcoin1", hand.contains(bitcoin1));
        assertTrue("Hand should contain method1", hand.contains(method1));
        assertTrue("Hand should contain ethereum1", hand.contains(ethereum1));
        assertEquals("Unplayed cards should have 3 cards", 3, hand.getUnplayedCards().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddToUnplayed_NullCard_ThrowsException() {
        hand.addToUnplayed(null);
    }

    @Test
    public void testAddToUnplayed_DuplicateCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin1);
        assertEquals("Hand should have 2 cards (duplicates allowed)", 2, hand.size());
    }

    // ========== playCard Tests ==========

    @Test
    public void testPlayCard_MovesCardFromUnplayedToPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);

        assertEquals("Hand should still have 1 card", 1, hand.size());
        assertEquals("Unplayed cards should be empty", 0, hand.getUnplayedCards().size());
        assertEquals("Played cards should have 1 card", 1, hand.getPlayedCards().size());
        assertTrue("Card should be in played cards", hand.getPlayedCards().contains(bitcoin1));
    }

    @Test
    public void testPlayCard_MultipleCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);

        hand.playCard(bitcoin1);
        hand.playCard(ethereum1);

        assertEquals("Hand should have 3 cards total", 3, hand.size());
        assertEquals("Unplayed cards should have 1 card", 1, hand.getUnplayedCards().size());
        assertEquals("Played cards should have 2 cards", 2, hand.getPlayedCards().size());
        assertTrue("method1 should still be unplayed", hand.getUnplayedCards().contains(method1));
        assertTrue("bitcoin1 should be played", hand.getPlayedCards().contains(bitcoin1));
        assertTrue("ethereum1 should be played", hand.getPlayedCards().contains(ethereum1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlayCard_CardNotInHand_ThrowsException() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(method1); // method1 was never added
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlayCard_AlreadyPlayedCard_ThrowsException() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        hand.playCard(bitcoin1); // Already played
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlayCard_EmptyHand_ThrowsException() {
        hand.playCard(bitcoin1);
    }

    // ========== getUnplayedCards Tests ==========

    @Test
    public void testGetUnplayedCards_ReturnsImmutableCopy() {
        hand.addToUnplayed(bitcoin1);
        List<Card> unplayed = hand.getUnplayedCards();

        assertEquals("Should have 1 unplayed card", 1, unplayed.size());

        // Verify it's a copy by trying to modify it
        try {
            unplayed.add(method1);
            fail("Should throw UnsupportedOperationException for immutable list");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
        }
    }

    @Test
    public void testGetUnplayedCards_EmptyHand() {
        List<Card> unplayed = hand.getUnplayedCards();
        assertTrue("Unplayed cards should be empty", unplayed.isEmpty());
    }

    @Test
    public void testGetUnplayedCards_AfterPlayingCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        List<Card> unplayed = hand.getUnplayedCards();
        assertEquals("Should have 1 unplayed card", 1, unplayed.size());
        assertTrue("Should contain method1", unplayed.contains(method1));
        assertFalse("Should not contain bitcoin1", unplayed.contains(bitcoin1));
    }

    // ========== getPlayedCards Tests ==========

    @Test
    public void testGetPlayedCards_ReturnsImmutableCopy() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        List<Card> played = hand.getPlayedCards();

        assertEquals("Should have 1 played card", 1, played.size());

        // Verify it's a copy by trying to modify it
        try {
            played.add(method1);
            fail("Should throw UnsupportedOperationException for immutable list");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
        }
    }

    @Test
    public void testGetPlayedCards_EmptyWhenNothingPlayed() {
        hand.addToUnplayed(bitcoin1);
        List<Card> played = hand.getPlayedCards();
        assertTrue("Played cards should be empty", played.isEmpty());
    }

    // ========== getAllCards Tests ==========

    @Test
    public void testGetAllCards_CombinesBothLists() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        List<Card> allCards = hand.getAllCards();
        assertEquals("Should have 2 total cards", 2, allCards.size());
        assertTrue("Should contain bitcoin1", allCards.contains(bitcoin1));
        assertTrue("Should contain method1", allCards.contains(method1));
    }

    @Test
    public void testGetAllCards_EmptyHand() {
        List<Card> allCards = hand.getAllCards();
        assertTrue("All cards should be empty", allCards.isEmpty());
    }

    @Test
    public void testGetAllCards_OnlyUnplayed() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);

        List<Card> allCards = hand.getAllCards();
        assertEquals("Should have 2 cards", 2, allCards.size());
    }

    @Test
    public void testGetAllCards_OnlyPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);
        hand.playCard(method1);

        List<Card> allCards = hand.getAllCards();
        assertEquals("Should have 2 cards", 2, allCards.size());
    }

    // ========== toImmutableHand Tests ==========

    @Test
    public void testToImmutableHand_CreatesCorrectHand() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        Hand immutableHand = hand.toImmutableHand();

        assertNotNull("Immutable hand should not be null", immutableHand);
        assertEquals("Unplayed cards should match", 1, immutableHand.unplayedCards().size());
        assertEquals("Played cards should match", 1, immutableHand.playedCards().size());
        assertTrue(
                "Should contain method1 in unplayed",
                immutableHand.unplayedCards().contains(method1));
        assertTrue(
                "Should contain bitcoin1 in played",
                immutableHand.playedCards().contains(bitcoin1));
    }

    @Test
    public void testToImmutableHand_EmptyHand() {
        Hand immutableHand = hand.toImmutableHand();

        assertNotNull("Immutable hand should not be null", immutableHand);
        assertTrue("Unplayed cards should be empty", immutableHand.unplayedCards().isEmpty());
        assertTrue("Played cards should be empty", immutableHand.playedCards().isEmpty());
    }

    @Test
    public void testToImmutableHand_DoesNotAffectMutableHand() {
        hand.addToUnplayed(bitcoin1);
        Hand immutableHand = hand.toImmutableHand();

        // Modify the mutable hand
        hand.addToUnplayed(method1);

        // Immutable hand should not change
        assertEquals(
                "Immutable hand should still have 1 unplayed card",
                1,
                immutableHand.unplayedCards().size());
        assertEquals(
                "Mutable hand should have 2 unplayed cards", 2, hand.getUnplayedCards().size());
    }

    // ========== removeFromUnplayed Tests ==========

    @Test
    public void testRemoveFromUnplayed_SuccessfulRemoval() {
        hand.addToUnplayed(bitcoin1);
        boolean removed = hand.removeFromUnplayed(bitcoin1);

        assertTrue("Should return true on successful removal", removed);
        assertFalse("Hand should not contain bitcoin1", hand.contains(bitcoin1));
        assertEquals("Hand should be empty", 0, hand.size());
    }

    @Test
    public void testRemoveFromUnplayed_CardNotPresent() {
        hand.addToUnplayed(bitcoin1);
        boolean removed = hand.removeFromUnplayed(method1);

        assertFalse("Should return false when card not found", removed);
        assertEquals("Hand should still have 1 card", 1, hand.size());
    }

    @Test
    public void testRemoveFromUnplayed_EmptyHand() {
        boolean removed = hand.removeFromUnplayed(bitcoin1);
        assertFalse("Should return false on empty hand", removed);
    }

    @Test
    public void testRemoveFromUnplayed_DoesNotRemoveFromPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);

        boolean removed = hand.removeFromUnplayed(bitcoin1);

        assertFalse("Should return false (card is in played, not unplayed)", removed);
        assertEquals("Hand should still have 1 card", 1, hand.size());
        assertTrue(
                "Card should still be in played cards", hand.getPlayedCards().contains(bitcoin1));
    }

    @Test
    public void testRemoveFromUnplayed_WithDuplicates() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin1);

        boolean removed = hand.removeFromUnplayed(bitcoin1);

        assertTrue("Should remove one instance", removed);
        assertEquals("Hand should still have 1 card", 1, hand.size());
        assertTrue("Hand should still contain bitcoin1", hand.contains(bitcoin1));
    }

    // ========== removeFromPlayed Tests ==========

    @Test
    public void testRemoveFromPlayed_SuccessfulRemoval() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        boolean removed = hand.removeFromPlayed(bitcoin1);

        assertTrue("Should return true on successful removal", removed);
        assertFalse("Hand should not contain bitcoin1", hand.contains(bitcoin1));
        assertEquals("Hand should be empty", 0, hand.size());
    }

    @Test
    public void testRemoveFromPlayed_CardNotPresent() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        boolean removed = hand.removeFromPlayed(method1);

        assertFalse("Should return false when card not found", removed);
        assertEquals("Hand should still have 1 card", 1, hand.size());
    }

    @Test
    public void testRemoveFromPlayed_EmptyHand() {
        boolean removed = hand.removeFromPlayed(bitcoin1);
        assertFalse("Should return false on empty hand", removed);
    }

    @Test
    public void testRemoveFromPlayed_DoesNotRemoveFromUnplayed() {
        hand.addToUnplayed(bitcoin1);

        boolean removed = hand.removeFromPlayed(bitcoin1);

        assertFalse("Should return false (card is in unplayed, not played)", removed);
        assertEquals("Hand should still have 1 card", 1, hand.size());
        assertTrue(
                "Card should still be in unplayed cards",
                hand.getUnplayedCards().contains(bitcoin1));
    }

    // ========== moveAllCardsTo Tests ==========

    @Test
    public void testMoveAllCardsTo_MovesAllCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        List<Card> destination = new ArrayList<>();
        hand.moveAllCardsTo(destination);

        assertEquals("Destination should have 2 cards", 2, destination.size());
        assertTrue("Destination should contain bitcoin1", destination.contains(bitcoin1));
        assertTrue("Destination should contain method1", destination.contains(method1));
        assertTrue("Hand should be empty", hand.isEmpty());
        assertEquals("Hand size should be 0", 0, hand.size());
    }

    @Test
    public void testMoveAllCardsTo_EmptyHand() {
        List<Card> destination = new ArrayList<>();
        hand.moveAllCardsTo(destination);

        assertTrue("Destination should be empty", destination.isEmpty());
        assertTrue("Hand should be empty", hand.isEmpty());
    }

    @Test
    public void testMoveAllCardsTo_AppendsToExistingList() {
        hand.addToUnplayed(bitcoin1);

        List<Card> destination = new ArrayList<>();
        destination.add(ethereum1);

        hand.moveAllCardsTo(destination);

        assertEquals("Destination should have 2 cards", 2, destination.size());
        assertTrue("Destination should contain ethereum1", destination.contains(ethereum1));
        assertTrue("Destination should contain bitcoin1", destination.contains(bitcoin1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveAllCardsTo_NullDestination_ThrowsException() {
        hand.addToUnplayed(bitcoin1);
        hand.moveAllCardsTo(null);
    }

    @Test
    public void testMoveAllCardsTo_ClearsUnplayedAndPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);
        hand.playCard(bitcoin1);
        hand.playCard(method1);

        List<Card> destination = new ArrayList<>();
        hand.moveAllCardsTo(destination);

        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
        assertEquals("Played should be empty", 0, hand.getPlayedCards().size());
        assertEquals("Destination should have all 3 cards", 3, destination.size());
    }

    // ========== playAllCards Tests ==========

    @Test
    public void testPlayAllCards_MovesAllUnplayedToPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);

        hand.playAllCards();

        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
        assertEquals("Played should have 3 cards", 3, hand.getPlayedCards().size());
        assertEquals("Total size should be 3", 3, hand.size());
        assertTrue("bitcoin1 should be played", hand.getPlayedCards().contains(bitcoin1));
        assertTrue("method1 should be played", hand.getPlayedCards().contains(method1));
        assertTrue("ethereum1 should be played", hand.getPlayedCards().contains(ethereum1));
    }

    @Test
    public void testPlayAllCards_EmptyHand() {
        hand.playAllCards();
        assertTrue("Hand should still be empty", hand.isEmpty());
    }

    @Test
    public void testPlayAllCards_WithExistingPlayedCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        hand.playAllCards();

        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
        assertEquals("Played should have 2 cards", 2, hand.getPlayedCards().size());
    }

    @Test
    public void testPlayAllCards_NoUnplayedCards() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);

        hand.playAllCards();

        assertEquals("Played should still have 1 card", 1, hand.getPlayedCards().size());
        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
    }

    // ========== clear Tests ==========

    @Test
    public void testClear_RemovesAllCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        hand.clear();

        assertTrue("Hand should be empty", hand.isEmpty());
        assertEquals("Size should be 0", 0, hand.size());
        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
        assertEquals("Played should be empty", 0, hand.getPlayedCards().size());
    }

    @Test
    public void testClear_EmptyHand() {
        hand.clear();
        assertTrue("Hand should still be empty", hand.isEmpty());
    }

    @Test
    public void testClear_CanAddCardsAfter() {
        hand.addToUnplayed(bitcoin1);
        hand.clear();
        hand.addToUnplayed(method1);

        assertEquals("Should have 1 card", 1, hand.size());
        assertTrue("Should contain method1", hand.contains(method1));
    }

    // ========== size Tests ==========

    @Test
    public void testSize_EmptyHand() {
        assertEquals("Empty hand should have size 0", 0, hand.size());
    }

    @Test
    public void testSize_OnlyUnplayedCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        assertEquals("Should have size 2", 2, hand.size());
    }

    @Test
    public void testSize_OnlyPlayedCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);
        hand.playCard(method1);
        assertEquals("Should have size 2", 2, hand.size());
    }

    @Test
    public void testSize_MixedCards() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);
        hand.playCard(bitcoin1);

        assertEquals("Should have size 3", 3, hand.size());
    }

    @Test
    public void testSize_AfterRemoval() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.removeFromUnplayed(bitcoin1);

        assertEquals("Should have size 1", 1, hand.size());
    }

    // ========== isEmpty Tests ==========

    @Test
    public void testIsEmpty_NewHand() {
        assertTrue("New hand should be empty", hand.isEmpty());
    }

    @Test
    public void testIsEmpty_AfterAddingCards() {
        hand.addToUnplayed(bitcoin1);
        assertFalse("Hand with cards should not be empty", hand.isEmpty());
    }

    @Test
    public void testIsEmpty_AfterPlayingCards() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        assertFalse("Hand with played cards should not be empty", hand.isEmpty());
    }

    @Test
    public void testIsEmpty_AfterClear() {
        hand.addToUnplayed(bitcoin1);
        hand.clear();
        assertTrue("Hand should be empty after clear", hand.isEmpty());
    }

    @Test
    public void testIsEmpty_AfterMoveAllCardsTo() {
        hand.addToUnplayed(bitcoin1);
        List<Card> destination = new ArrayList<>();
        hand.moveAllCardsTo(destination);
        assertTrue("Hand should be empty after moveAllCardsTo", hand.isEmpty());
    }

    @Test
    public void testIsEmpty_AfterRemovingAllCards() {
        hand.addToUnplayed(bitcoin1);
        hand.removeFromUnplayed(bitcoin1);
        assertTrue("Hand should be empty after removing all cards", hand.isEmpty());
    }

    // ========== contains Tests ==========

    @Test
    public void testContains_CardInUnplayed() {
        hand.addToUnplayed(bitcoin1);
        assertTrue("Should contain card in unplayed", hand.contains(bitcoin1));
    }

    @Test
    public void testContains_CardInPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.playCard(bitcoin1);
        assertTrue("Should contain card in played", hand.contains(bitcoin1));
    }

    @Test
    public void testContains_CardNotInHand() {
        hand.addToUnplayed(bitcoin1);
        assertFalse("Should not contain card not in hand", hand.contains(method1));
    }

    @Test
    public void testContains_EmptyHand() {
        assertFalse("Empty hand should not contain any card", hand.contains(bitcoin1));
    }

    @Test
    public void testContains_AfterRemoval() {
        hand.addToUnplayed(bitcoin1);
        hand.removeFromUnplayed(bitcoin1);
        assertFalse("Should not contain removed card", hand.contains(bitcoin1));
    }

    @Test
    public void testContains_NullCard() {
        assertFalse("Should return false for null card", hand.contains(null));
    }

    // ========== Integration Tests ==========

    @Test
    public void testComplexScenario_MultipleOperations() {
        // Add several cards
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);
        hand.addToUnplayed(dogecoin1);

        assertEquals("Should have 5 cards", 5, hand.size());

        // Play some cards
        hand.playCard(bitcoin1);
        hand.playCard(method1);

        assertEquals("Should still have 5 cards", 5, hand.size());
        assertEquals("Should have 3 unplayed", 3, hand.getUnplayedCards().size());
        assertEquals("Should have 2 played", 2, hand.getPlayedCards().size());

        // Remove a card from unplayed
        hand.removeFromUnplayed(ethereum1);

        assertEquals("Should have 4 cards", 4, hand.size());

        // Play all remaining unplayed cards
        hand.playAllCards();

        assertEquals("Should have 0 unplayed", 0, hand.getUnplayedCards().size());
        assertEquals("Should have 4 played", 4, hand.getPlayedCards().size());

        // Move all to destination
        List<Card> discard = new ArrayList<>();
        hand.moveAllCardsTo(discard);

        assertTrue("Hand should be empty", hand.isEmpty());
        assertEquals("Discard should have 4 cards", 4, discard.size());
    }

    @Test
    public void testConversionBetweenMutableAndImmutable() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.playCard(bitcoin1);

        Hand immutable1 = hand.toImmutableHand();

        // Continue modifying mutable hand
        hand.addToUnplayed(ethereum1);
        hand.playCard(method1);

        Hand immutable2 = hand.toImmutableHand();

        // Verify immutable1 is unchanged
        assertEquals(
                "First immutable should have 1 unplayed", 1, immutable1.unplayedCards().size());
        assertEquals("First immutable should have 1 played", 1, immutable1.playedCards().size());

        // Verify immutable2 reflects new state
        assertEquals(
                "Second immutable should have 1 unplayed", 1, immutable2.unplayedCards().size());
        assertEquals("Second immutable should have 2 played", 2, immutable2.playedCards().size());
    }

    @Test
    public void testCardOrdering_PreservedInLists() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);

        List<Card> unplayed = hand.getUnplayedCards();

        assertEquals("First card should be bitcoin1", bitcoin1, unplayed.get(0));
        assertEquals("Second card should be method1", method1, unplayed.get(1));
        assertEquals("Third card should be ethereum1", ethereum1, unplayed.get(2));
    }

    @Test
    public void testPlayCard_OrderingInPlayedList() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(method1);
        hand.addToUnplayed(ethereum1);

        hand.playCard(method1);
        hand.playCard(bitcoin1);

        List<Card> played = hand.getPlayedCards();

        assertEquals("First played should be method1", method1, played.get(0));
        assertEquals("Second played should be bitcoin1", bitcoin1, played.get(1));
    }

    // ========== Same Card Type, Different ID Tests ==========

    @Test
    public void testSameCardTypeDifferentId_BothInUnplayed() {
        hand.addToUnplayed(bitcoin1); // BITCOIN with ID 1
        hand.addToUnplayed(bitcoin2); // BITCOIN with ID 2

        assertEquals("Should have 2 distinct cards", 2, hand.size());
        assertTrue("Should contain bitcoin1", hand.contains(bitcoin1));
        assertTrue("Should contain bitcoin2", hand.contains(bitcoin2));

        List<Card> unplayed = hand.getUnplayedCards();
        assertEquals("Unplayed should have 2 cards", 2, unplayed.size());
        assertTrue("Unplayed should contain bitcoin1", unplayed.contains(bitcoin1));
        assertTrue("Unplayed should contain bitcoin2", unplayed.contains(bitcoin2));
    }

    @Test
    public void testSameCardTypeDifferentId_PlaySpecificCard() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);

        hand.playCard(bitcoin1); // Play only bitcoin1

        assertTrue("bitcoin1 should be in played", hand.getPlayedCards().contains(bitcoin1));
        assertFalse(
                "bitcoin1 should not be in unplayed", hand.getUnplayedCards().contains(bitcoin1));
        assertTrue(
                "bitcoin2 should still be in unplayed", hand.getUnplayedCards().contains(bitcoin2));
        assertFalse("bitcoin2 should not be in played", hand.getPlayedCards().contains(bitcoin2));
        assertEquals("Should still have 2 cards total", 2, hand.size());
    }

    @Test
    public void testSameCardTypeDifferentId_RemoveSpecificCard() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);

        boolean removed = hand.removeFromUnplayed(bitcoin1);

        assertTrue("Should successfully remove bitcoin1", removed);
        assertFalse("bitcoin1 should not be in hand", hand.contains(bitcoin1));
        assertTrue("bitcoin2 should still be in hand", hand.contains(bitcoin2));
        assertEquals("Should have 1 card remaining", 1, hand.size());
    }

    @Test
    public void testSameCardTypeDifferentId_BothPlayed() {
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);

        hand.playCard(bitcoin1);
        hand.playCard(bitcoin2);

        assertEquals("Both should be in played", 2, hand.getPlayedCards().size());
        assertEquals("Unplayed should be empty", 0, hand.getUnplayedCards().size());
        assertTrue("bitcoin1 should be in played", hand.getPlayedCards().contains(bitcoin1));
        assertTrue("bitcoin2 should be in played", hand.getPlayedCards().contains(bitcoin2));
    }

    @Test
    public void testSameCardTypeDifferentId_ComplexIntegrationScenario() {
        // Setup: Add multiple BITCOIN cards with different IDs and other card types
        Card bitcoin3 = new Card(Card.Type.BITCOIN, 10);
        Card bitcoin4 = new Card(Card.Type.BITCOIN, 20);
        Card method2 = new Card(Card.Type.METHOD, 15);

        hand.addToUnplayed(bitcoin1); // BITCOIN ID:1
        hand.addToUnplayed(method1); // METHOD ID:3
        hand.addToUnplayed(bitcoin2); // BITCOIN ID:2
        hand.addToUnplayed(ethereum1); // ETHEREUM ID:4
        hand.addToUnplayed(bitcoin3); // BITCOIN ID:10
        hand.addToUnplayed(method2); // METHOD ID:15
        hand.addToUnplayed(bitcoin4); // BITCOIN ID:20

        assertEquals("Should have 7 cards total", 7, hand.size());
        assertEquals("All should be unplayed", 7, hand.getUnplayedCards().size());

        // Play specific BITCOIN cards
        hand.playCard(bitcoin1);
        hand.playCard(bitcoin3);

        assertEquals("Should have 5 unplayed", 5, hand.getUnplayedCards().size());
        assertEquals("Should have 2 played", 2, hand.getPlayedCards().size());
        assertTrue("bitcoin1 should be played", hand.getPlayedCards().contains(bitcoin1));
        assertTrue("bitcoin3 should be played", hand.getPlayedCards().contains(bitcoin3));
        assertTrue("bitcoin2 should still be unplayed", hand.getUnplayedCards().contains(bitcoin2));
        assertTrue("bitcoin4 should still be unplayed", hand.getUnplayedCards().contains(bitcoin4));

        // Remove a specific BITCOIN from unplayed
        boolean removed = hand.removeFromUnplayed(bitcoin2);
        assertTrue("bitcoin2 should be removed", removed);
        assertEquals("Should have 6 cards total", 6, hand.size());
        assertFalse("bitcoin2 should not be in hand", hand.contains(bitcoin2));
        assertTrue("bitcoin4 should still be in hand", hand.contains(bitcoin4));

        // Remove a specific BITCOIN from played
        removed = hand.removeFromPlayed(bitcoin1);
        assertTrue("bitcoin1 should be removed from played", removed);
        assertEquals("Should have 5 cards total", 5, hand.size());
        assertFalse("bitcoin1 should not be in hand", hand.contains(bitcoin1));
        assertTrue("bitcoin3 should still be in played", hand.getPlayedCards().contains(bitcoin3));

        // Play the remaining BITCOIN card
        hand.playCard(bitcoin4);
        assertTrue("bitcoin4 should now be played", hand.getPlayedCards().contains(bitcoin4));
        assertEquals(
                "Should have 2 played BITCOINs",
                2,
                hand.getPlayedCards().stream().filter(c -> c.type() == Card.Type.BITCOIN).count());

        // Verify all cards are correctly positioned
        List<Card> allCards = hand.getAllCards();
        assertEquals("Should have 5 cards total", 5, allCards.size());
        assertTrue("Should contain method1", allCards.contains(method1));
        assertTrue("Should contain ethereum1", allCards.contains(ethereum1));
        assertTrue("Should contain method2", allCards.contains(method2));
        assertTrue("Should contain bitcoin3", allCards.contains(bitcoin3));
        assertTrue("Should contain bitcoin4", allCards.contains(bitcoin4));

        // Verify removed cards are not present
        assertFalse("Should not contain bitcoin1", allCards.contains(bitcoin1));
        assertFalse("Should not contain bitcoin2", allCards.contains(bitcoin2));

        // Play all remaining cards
        hand.playAllCards();
        assertEquals("All 5 cards should be played", 5, hand.getPlayedCards().size());
        assertEquals("No cards should be unplayed", 0, hand.getUnplayedCards().size());

        // Convert to immutable and verify
        Hand immutableHand = hand.toImmutableHand();
        assertEquals("Immutable should have 5 played", 5, immutableHand.playedCards().size());
        assertEquals("Immutable should have 0 unplayed", 0, immutableHand.unplayedCards().size());

        // Move all to discard pile
        List<Card> discardPile = new ArrayList<>();
        hand.moveAllCardsTo(discardPile);
        assertTrue("Hand should be empty", hand.isEmpty());
        assertEquals("Discard pile should have 5 cards", 5, discardPile.size());

        // Verify the specific cards in discard
        assertTrue("Discard should contain bitcoin3", discardPile.contains(bitcoin3));
        assertTrue("Discard should contain bitcoin4", discardPile.contains(bitcoin4));
        assertFalse("Discard should not contain bitcoin1", discardPile.contains(bitcoin1));
        assertFalse("Discard should not contain bitcoin2", discardPile.contains(bitcoin2));
    }
}
