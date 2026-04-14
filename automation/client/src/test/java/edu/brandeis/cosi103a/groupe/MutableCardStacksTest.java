package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the MutableCardStacks class. Tests initialization, card count
 * management, and shop queries.
 */
public class MutableCardStacksTest {

    private ImmutableSet<Card.Type> standardActionCards;
    private ImmutableSet<Card.Type> singleActionCard;
    private ImmutableSet<Card.Type> emptyActionCards;

    @Before
    public void setUp() {
        standardActionCards =
                ImmutableSet.of(
                Card.Type.CODE_REVIEW,
                Card.Type.REFACTOR,
                Card.Type.EVERGREEN_TEST,
                Card.Type.DAILY_SCRUM);

        singleActionCard = ImmutableSet.of(Card.Type.CODE_REVIEW);
        emptyActionCards = ImmutableSet.of();
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_TwoPlayers_InitializesCorrectly() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        // Verify treasure cards
        assertEquals("BITCOIN should be 60", 60, stacks.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should be 40", 40, stacks.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should be 30", 30, stacks.getNumAvailable(Card.Type.DOGECOIN));

        // Verify victory cards
        assertEquals("METHOD should be 14", 14, stacks.getNumAvailable(Card.Type.METHOD));
        assertEquals("MODULE should be 8", 8, stacks.getNumAvailable(Card.Type.MODULE));
        assertEquals(
                "FRAMEWORK should be 8 for 2 players",
                8,
                stacks.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals("BUG should be 20 (2*10)", 20, stacks.getNumAvailable(Card.Type.BUG));

        // Verify action cards
        assertEquals("CODE_REVIEW should be 10", 10, stacks.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("REFACTOR should be 10", 10, stacks.getNumAvailable(Card.Type.REFACTOR));
        assertEquals(
                "EVERGREEN_TEST should be 10",
                10,
                stacks.getNumAvailable(Card.Type.EVERGREEN_TEST));
        assertEquals(
            "DAILY_SCRUM should be 10", 10, stacks.getNumAvailable(Card.Type.DAILY_SCRUM));
    }

    @Test
    public void testConstructor_ThreePlayers_InitializesCorrectly() {
        MutableCardStacks stacks = new MutableCardStacks(3, standardActionCards);

        assertEquals(
                "FRAMEWORK should be 12 for 3 players",
                12,
                stacks.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals("BUG should be 30 (3*10)", 30, stacks.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testConstructor_FourPlayers_InitializesCorrectly() {
        MutableCardStacks stacks = new MutableCardStacks(4, standardActionCards);

        assertEquals(
                "FRAMEWORK should be 12 for 4 players",
                12,
                stacks.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals("BUG should be 40 (4*10)", 40, stacks.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testConstructor_SingleActionCard_OnlyThatCardInitialized() {
        MutableCardStacks stacks = new MutableCardStacks(2, singleActionCard);

        assertEquals("CODE_REVIEW should be 10", 10, stacks.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals(
                "REFACTOR should be 0 (not included)",
                0,
                stacks.getNumAvailable(Card.Type.REFACTOR));
        assertEquals(
                "EVERGREEN_TEST should be 0 (not included)",
                0,
                stacks.getNumAvailable(Card.Type.EVERGREEN_TEST));
        assertEquals(
            "DAILY_SCRUM should be 0 (not included)",
            0,
            stacks.getNumAvailable(Card.Type.DAILY_SCRUM));
    }

    @Test
    public void testConstructor_NoActionCards_OnlyTreasuresAndVictories() {
        MutableCardStacks stacks = new MutableCardStacks(2, emptyActionCards);

        // Verify treasures still exist
        assertEquals("BITCOIN should be 60", 60, stacks.getNumAvailable(Card.Type.BITCOIN));

        // Verify no action cards
        assertEquals("CODE_REVIEW should be 0", 0, stacks.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("REFACTOR should be 0", 0, stacks.getNumAvailable(Card.Type.REFACTOR));
        assertEquals(
                "EVERGREEN_TEST should be 0", 0, stacks.getNumAvailable(Card.Type.EVERGREEN_TEST));
        assertEquals("DAILY_SCRUM should be 0", 0, stacks.getNumAvailable(Card.Type.DAILY_SCRUM));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_InvalidNumPlayers_TooLow() {
        new MutableCardStacks(1, standardActionCards);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_InvalidNumPlayers_TooHigh() {
        new MutableCardStacks(5, standardActionCards);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullActionCardsForGame() {
        new MutableCardStacks(2, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_NullElementInActionCardsForGame() {
        // ImmutableSet.of() raises the NullPointerException if null is included, so we expect that
        // instead of IllegalArgumentException
        ImmutableSet<Card.Type> invalidActionCards = ImmutableSet.of(Card.Type.CODE_REVIEW, null);
        new MutableCardStacks(2, invalidActionCards);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NonActionTypeInActionCardsForGame() {
        ImmutableSet<Card.Type> invalidActionCards =
                ImmutableSet.of(Card.Type.CODE_REVIEW, Card.Type.BITCOIN);
        new MutableCardStacks(2, invalidActionCards);
    }

    // ========== getCount Tests ==========

    @Test
    public void testGetCount_ExistingCard_ReturnsCorrectCount() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        assertEquals("BITCOIN count should be 60", 60, stacks.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("FRAMEWORK count should be 8", 8, stacks.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetCount_NonExistentCard_ReturnsZero() {
        MutableCardStacks stacks = new MutableCardStacks(2, emptyActionCards);

        assertEquals(
                "Non-existent action card should return 0",
                0,
                stacks.getNumAvailable(Card.Type.CODE_REVIEW));
    }

    @Test
    public void testGetCount_AfterDecrement_ReturnsUpdatedCount() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        stacks.decrementCount(Card.Type.BITCOIN);
        assertEquals(
                "BITCOIN should be 59 after decrement",
                59,
                stacks.getNumAvailable(Card.Type.BITCOIN));
    }

    // ========== decrementCount Tests ==========

    @Test
    public void testDecrementCount_ExistingCardWithStock_ReturnsTrue() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        assertTrue("Decrement should succeed", stacks.decrementCount(Card.Type.BITCOIN));
        assertEquals("Count should be decremented", 59, stacks.getNumAvailable(Card.Type.BITCOIN));
    }

    @Test
    public void testDecrementCount_MultipleDecrements_ReducesCountCorrectly() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        for (int i = 0; i < 5; i++) {
            assertTrue(
                    "Decrement " + (i + 1) + " should succeed",
                    stacks.decrementCount(Card.Type.ETHEREUM));
        }

        assertEquals(
                "ETHEREUM should be 35 after 5 decrements",
                35,
                stacks.getNumAvailable(Card.Type.ETHEREUM));
    }

    @Test
    public void testDecrementCount_ToZero_ThenFailsOnNext() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        // Decrement CODE_REVIEW from 10 to 0
        for (int i = 0; i < 10; i++) {
            assertTrue(
                    "Decrement " + (i + 1) + " should succeed",
                    stacks.decrementCount(Card.Type.CODE_REVIEW));
        }

        assertEquals("CODE_REVIEW should be 0", 0, stacks.getNumAvailable(Card.Type.CODE_REVIEW));

        // Next decrement should fail
        assertFalse(
                "Decrement should fail when count is 0",
                stacks.decrementCount(Card.Type.CODE_REVIEW));
        assertEquals("Count should remain 0", 0, stacks.getNumAvailable(Card.Type.CODE_REVIEW));
    }

    @Test
    public void testDecrementCount_NonExistentCard_ReturnsFalse() {
        MutableCardStacks stacks = new MutableCardStacks(2, emptyActionCards);

        assertFalse(
                "Decrement should fail for non-existent card",
                stacks.decrementCount(Card.Type.CODE_REVIEW));
    }

    @Test
    public void testDecrementCount_DoesNotAffectOtherCards() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        int bitcoinBefore = stacks.getNumAvailable(Card.Type.BITCOIN);
        int ethereumBefore = stacks.getNumAvailable(Card.Type.ETHEREUM);

        stacks.decrementCount(Card.Type.DOGECOIN);

        assertEquals(
                "BITCOIN should be unchanged",
                bitcoinBefore,
                stacks.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(
                "ETHEREUM should be unchanged",
                ethereumBefore,
                stacks.getNumAvailable(Card.Type.ETHEREUM));
    }

    // ========== availableCardsForCost Tests ==========

    @Test
    public void testAvailableCardsForCost_Zero_OnlyIncludesZeroCostCards() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(0);

        // Should only include cards with cost 0
        assertTrue("Should include BITCOIN (cost 0)", available.contains(Card.Type.BITCOIN));
        assertFalse("Should not include ETHEREUM (cost 3)", available.contains(Card.Type.ETHEREUM));
        assertFalse("Should not include DOGECOIN (cost 6)", available.contains(Card.Type.DOGECOIN));
        assertTrue("Size should be > 0", available.size() > 0);
    }

    @Test
    public void testAvailableCardsForCost_Three_IncludesUpToCostThree() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(3);

        // Should include cards with cost <= 3
        assertTrue("Should include BITCOIN (0)", available.contains(Card.Type.BITCOIN));
        assertTrue("Should include ETHEREUM (3)", available.contains(Card.Type.ETHEREUM));
        assertTrue("Should include METHOD (2)", available.contains(Card.Type.METHOD));
        assertTrue("Should include CODE_REVIEW (3)", available.contains(Card.Type.CODE_REVIEW));

        // Should NOT include cards with cost > 3
        assertFalse("Should not include DOGECOIN (6)", available.contains(Card.Type.DOGECOIN));
        assertFalse("Should not include MODULE (5)", available.contains(Card.Type.MODULE));
    }

    @Test
    public void testAvailableCardsForCost_Eight_IncludesAllCards() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(8);

        // Highest cost card is FRAMEWORK at 8
        assertTrue("Should include FRAMEWORK (8)", available.contains(Card.Type.FRAMEWORK));
        assertTrue("Should include BITCOIN (0)", available.contains(Card.Type.BITCOIN));
        assertTrue("Should include DOGECOIN (6)", available.contains(Card.Type.DOGECOIN));
    }

    @Test
    public void testAvailableCardsForCost_ExcludesCardsWithZeroCount() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        // Deplete BITCOIN
        for (int i = 0; i < 60; i++) {
            stacks.decrementCount(Card.Type.BITCOIN);
        }

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(8);

        assertFalse("Should not include BITCOIN (count 0)", available.contains(Card.Type.BITCOIN));
        assertTrue("Should still include ETHEREUM", available.contains(Card.Type.ETHEREUM));
    }

    @Test
    public void testAvailableCardsForCost_NegativeCost_ReturnsEmpty() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(-1);

        assertTrue("Should return empty set for negative cost", available.isEmpty());
    }

    @Test
    public void testAvailableCardsForCost_VeryHighCost_ReturnsAllAvailableCards() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(999);

        // Should include all cards with count > 0
        assertEquals("Should include all 11 card types", 11, available.size());
    }

    @Test
    public void testAvailableCardsForCost_AfterDepletion_UpdatesCorrectly() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        ImmutableSet<Card.Type> beforeDepletion = stacks.availableCardsForCost(5);
        int sizeBefore = beforeDepletion.size();

        // Deplete MODULE (cost 5)
        for (int i = 0; i < 8; i++) {
            stacks.decrementCount(Card.Type.MODULE);
        }

        ImmutableSet<Card.Type> afterDepletion = stacks.availableCardsForCost(5);

        assertEquals("Should have one fewer card available", sizeBefore - 1, afterDepletion.size());
        assertFalse(
                "Should not include depleted MODULE", afterDepletion.contains(Card.Type.MODULE));
    }

    // ========== toImmutableCardStacks Tests ==========

    @Test
    public void testToImmutableCardStacks_ReturnsNonNull() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        // Capture expected counts from the mutable stacks before snapshot
        int expectedBitcoin = stacks.getNumAvailable(Card.Type.BITCOIN);
        int expectedMethod = stacks.getNumAvailable(Card.Type.METHOD);

        CardStacks immutable = stacks.toImmutableCardStacks();

        assertNotNull("Should return non-null CardStacks", immutable);

        // Verify that the snapshot has the same counts as the mutable source
        assertEquals(
                "Snapshot should have same BITCOIN count as mutable stacks",
                expectedBitcoin,
                immutable.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(
                "Snapshot should have same METHOD count as mutable stacks",
                expectedMethod,
                immutable.getNumAvailable(Card.Type.METHOD));
    }

    @Test
    public void testToImmutableCardStacks_AfterModifications_ReturnsNonNull() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        // Modify some counts
        stacks.decrementCount(Card.Type.BITCOIN);
        stacks.decrementCount(Card.Type.BITCOIN);
        stacks.decrementCount(Card.Type.FRAMEWORK);

        // Capture expected counts after modifications
        int expectedBitcoin = stacks.getNumAvailable(Card.Type.BITCOIN);
        int expectedFramework = stacks.getNumAvailable(Card.Type.FRAMEWORK);

        CardStacks immutable = stacks.toImmutableCardStacks();

        assertNotNull("Should return non-null CardStacks after modifications", immutable);

        // Verify that the snapshot reflects the modified counts
        assertEquals(
                "Snapshot should have updated BITCOIN count",
                expectedBitcoin,
                immutable.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(
                "Snapshot should have updated FRAMEWORK count",
                expectedFramework,
                immutable.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testToImmutableCardStacks_MultipleCalls_ReturnsNonNull() {
        MutableCardStacks stacks = new MutableCardStacks(2, standardActionCards);

        CardStacks first = stacks.toImmutableCardStacks();
        assertNotNull("First call should return non-null", first);

        // Record a value from the first snapshot to verify immutability later
        int firstBitcoin = first.getNumAvailable(Card.Type.BITCOIN);

        // Modify mutable stacks
        stacks.decrementCount(Card.Type.BITCOIN);

        CardStacks second = stacks.toImmutableCardStacks();
        assertNotNull("Second call should return non-null", second);

        // Second snapshot should match the current mutable state
        assertEquals(
                "Second snapshot should match mutable BITCOIN count",
                stacks.getNumAvailable(Card.Type.BITCOIN),
                second.getNumAvailable(Card.Type.BITCOIN));

        // First snapshot should remain unchanged (immutability)
        assertEquals(
                "First snapshot should remain unchanged after mutable stacks are modified",
                firstBitcoin,
                first.getNumAvailable(Card.Type.BITCOIN));

        // And the two snapshots should differ when the underlying mutable stacks change
        assertNotEquals(
                "First and second snapshots should differ after a decrement",
                first.getNumAvailable(Card.Type.BITCOIN),
                second.getNumAvailable(Card.Type.BITCOIN));
    }

    // ========== Edge Cases and Integration Tests ==========

    @Test
    public void testMixedOperations_MaintainsConsistency() {
        MutableCardStacks stacks = new MutableCardStacks(3, standardActionCards);

        // Perform various operations
        stacks.decrementCount(Card.Type.BITCOIN);
        stacks.decrementCount(Card.Type.BITCOIN);
        stacks.decrementCount(Card.Type.METHOD);

        // Verify consistency
        assertEquals("BITCOIN should be 58", 58, stacks.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("METHOD should be 13", 13, stacks.getNumAvailable(Card.Type.METHOD));

        ImmutableSet<Card.Type> available = stacks.availableCardsForCost(2);
        assertTrue("BITCOIN should be available", available.contains(Card.Type.BITCOIN));
        assertTrue("METHOD should be available", available.contains(Card.Type.METHOD));
    }

    @Test
    public void testDifferentPlayerCounts_AffectOnlyScalingCards() {
        MutableCardStacks twoPlayer = new MutableCardStacks(2, standardActionCards);
        MutableCardStacks fourPlayer = new MutableCardStacks(4, standardActionCards);

        // Cards that scale with player count
        assertEquals("2-player BUG should be 20", 20, twoPlayer.getNumAvailable(Card.Type.BUG));
        assertEquals("4-player BUG should be 40", 40, fourPlayer.getNumAvailable(Card.Type.BUG));
        assertEquals(
                "2-player FRAMEWORK should be 8",
                8,
                twoPlayer.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals(
                "4-player FRAMEWORK should be 12",
                12,
                fourPlayer.getNumAvailable(Card.Type.FRAMEWORK));

        // Cards that don't scale
        assertEquals(
                "BITCOIN should be same for both",
                twoPlayer.getNumAvailable(Card.Type.BITCOIN),
                fourPlayer.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(
                "METHOD should be same for both",
                twoPlayer.getNumAvailable(Card.Type.METHOD),
                fourPlayer.getNumAvailable(Card.Type.METHOD));
    }

    @Test
    public void testDifferentActionCardSets_OnlyIncludeSpecified() {
        MutableCardStacks withCodeReview =
                new MutableCardStacks(2, ImmutableSet.of(Card.Type.CODE_REVIEW));
        MutableCardStacks withRefactor =
                new MutableCardStacks(2, ImmutableSet.of(Card.Type.REFACTOR));

        assertEquals(
                "CODE_REVIEW should be 10 in first",
                10,
                withCodeReview.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals(
                "REFACTOR should be 0 in first",
                0,
                withCodeReview.getNumAvailable(Card.Type.REFACTOR));

        assertEquals(
                "CODE_REVIEW should be 0 in second",
                0,
                withRefactor.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals(
                "REFACTOR should be 10 in second",
                10,
                withRefactor.getNumAvailable(Card.Type.REFACTOR));
    }
}
