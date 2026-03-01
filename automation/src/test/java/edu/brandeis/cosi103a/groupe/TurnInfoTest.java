package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the TurnInfo class.
 * Tests cover constructor defaults, increment/decrement operations, 
 * guard conditions, and reset functionality.
 */
public class TurnInfoTest {

    private TurnInfo turnInfo;

    @Before
    public void setUp() {
        turnInfo = new TurnInfo();
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_InitializesDefaultActions() {
        assertEquals("Default actions should be 1", 1, turnInfo.getNumActions());
    }

    @Test
    public void testConstructor_InitializesDefaultMoney() {
        assertEquals("Default money should be 0", 0, turnInfo.getNumMoney());
    }

    @Test
    public void testConstructor_InitializesDefaultBuys() {
        assertEquals("Default buys should be 1", 1, turnInfo.getNumBuys());
    }

    // ========== Actions Tests ==========

    @Test
    public void testIncrementActions_ByOne() {
        turnInfo.incrementActions(1);
        assertEquals("Actions should be 2 after incrementing by 1", 2, turnInfo.getNumActions());
    }

    @Test
    public void testIncrementActions_ByMultiple() {
        turnInfo.incrementActions(3);
        assertEquals("Actions should be 4 after incrementing by 3", 4, turnInfo.getNumActions());
    }

    @Test
    public void testIncrementActions_Sequential() {
        turnInfo.incrementActions(2);
        turnInfo.incrementActions(1);
        assertEquals("Actions should accumulate to 4", 4, turnInfo.getNumActions());
    }

    @Test
    public void testDecrementActions_FromDefault() {
        turnInfo.decrementActions();
        assertEquals("Actions should be 0 after decrementing from 1", 0, turnInfo.getNumActions());
    }

    @Test
    public void testDecrementActions_GuardsAgainstNegative() {
        turnInfo.decrementActions(); // 1 -> 0
        turnInfo.decrementActions(); // Should stay at 0
        assertEquals("Actions should not go below 0", 0, turnInfo.getNumActions());
    }

    @Test
    public void testDecrementActions_AfterIncrement() {
        turnInfo.incrementActions(3);
        turnInfo.decrementActions();
        turnInfo.decrementActions();
        assertEquals("Actions should be 2 after increment by 3 and two decrements", 2, turnInfo.getNumActions());
    }

    // ========== Money Tests ==========

    @Test
    public void testIncrementMoney_ByOne() {
        turnInfo.incrementMoney(1);
        assertEquals("Money should be 1 after incrementing by 1", 1, turnInfo.getNumMoney());
    }

    @Test
    public void testIncrementMoney_ByMultiple() {
        turnInfo.incrementMoney(5);
        assertEquals("Money should be 5 after incrementing by 5", 5, turnInfo.getNumMoney());
    }

    @Test
    public void testIncrementMoney_Sequential() {
        turnInfo.incrementMoney(3);
        turnInfo.incrementMoney(2);
        assertEquals("Money should accumulate to 5", 5, turnInfo.getNumMoney());
    }

    @Test
    public void testDecrementMoney_WithSufficientFunds() {
        turnInfo.incrementMoney(10);
        turnInfo.decrementMoney(6);
        assertEquals("Money should be 4 after decrementing 6 from 10", 4, turnInfo.getNumMoney());
    }

    @Test
    public void testDecrementMoney_WithInsufficientFunds() {
        turnInfo.incrementMoney(3);
        turnInfo.decrementMoney(5);
        assertEquals("Money should remain at 3 when trying to decrement more than available", 3, turnInfo.getNumMoney());
    }

    @Test
    public void testDecrementMoney_ExactAmount() {
        turnInfo.incrementMoney(8);
        turnInfo.decrementMoney(8);
        assertEquals("Money should be 0 after decrementing exact amount", 0, turnInfo.getNumMoney());
    }

    @Test
    public void testDecrementMoney_FromZero() {
        turnInfo.decrementMoney(1);
        assertEquals("Money should remain at 0 when decrementing from 0", 0, turnInfo.getNumMoney());
    }

    // ========== Buys Tests ==========

    @Test
    public void testIncrementBuys_Once() {
        turnInfo.incrementBuys();
        assertEquals("Buys should be 2 after one increment", 2, turnInfo.getNumBuys());
    }

    @Test
    public void testIncrementBuys_Multiple() {
        turnInfo.incrementBuys();
        turnInfo.incrementBuys();
        turnInfo.incrementBuys();
        assertEquals("Buys should be 4 after three increments", 4, turnInfo.getNumBuys());
    }

    @Test
    public void testDecrementBuys_FromDefault() {
        turnInfo.decrementBuys();
        assertEquals("Buys should be 0 after decrementing from 1", 0, turnInfo.getNumBuys());
    }

    @Test
    public void testDecrementBuys_GuardsAgainstNegative() {
        turnInfo.decrementBuys(); // 1 -> 0
        turnInfo.decrementBuys(); // Should stay at 0
        assertEquals("Buys should not go below 0", 0, turnInfo.getNumBuys());
    }

    @Test
    public void testDecrementBuys_AfterIncrement() {
        turnInfo.incrementBuys();
        turnInfo.incrementBuys();
        turnInfo.decrementBuys();
        assertEquals("Buys should be 2 after two increments and one decrement", 2, turnInfo.getNumBuys());
    }

    // ========== Reset Tests ==========

    @Test
    public void testReset_ActionsReturnToDefault() {
        turnInfo.incrementActions(5);
        turnInfo.reset();
        assertEquals("Actions should be 1 after reset", 1, turnInfo.getNumActions());
    }

    @Test
    public void testReset_MoneyReturnsToDefault() {
        turnInfo.incrementMoney(10);
        turnInfo.reset();
        assertEquals("Money should be 0 after reset", 0, turnInfo.getNumMoney());
    }

    @Test
    public void testReset_BuysReturnToDefault() {
        turnInfo.incrementBuys();
        turnInfo.incrementBuys();
        turnInfo.reset();
        assertEquals("Buys should be 1 after reset", 1, turnInfo.getNumBuys());
    }

    @Test
    public void testReset_AllFieldsResetTogether() {
        turnInfo.incrementActions(3);
        turnInfo.incrementMoney(8);
        turnInfo.incrementBuys();
        turnInfo.reset();
        assertEquals("Actions should be 1 after reset", 1, turnInfo.getNumActions());
        assertEquals("Money should be 0 after reset", 0, turnInfo.getNumMoney());
        assertEquals("Buys should be 1 after reset", 1, turnInfo.getNumBuys());
    }

    @Test
    public void testReset_FromDecrementedValues() {
        turnInfo.decrementActions();
        turnInfo.decrementMoney(1);
        turnInfo.decrementBuys();
        turnInfo.reset();
        assertEquals("Actions should be 1 after reset", 1, turnInfo.getNumActions());
        assertEquals("Money should be 0 after reset", 0, turnInfo.getNumMoney());
        assertEquals("Buys should be 1 after reset", 1, turnInfo.getNumBuys());
    }

    // ========== Integration Tests ==========

    @Test
    public void testTypicalTurnSequence() {
        // Start of turn - default values
        assertEquals("Turn starts with 1 action", 1, turnInfo.getNumActions());
        
        // Play CODE_REVIEW action card: +2 actions, costs 1 action
        turnInfo.decrementActions();
        turnInfo.incrementActions(2);
        assertEquals("Should have 2 actions after playing CODE_REVIEW", 2, turnInfo.getNumActions());
        
        // Play money cards: BITCOIN (+1), ETHEREUM (+2)
        turnInfo.incrementMoney(1);
        turnInfo.incrementMoney(2);
        assertEquals("Should have 3 money total", 3, turnInfo.getNumMoney());
        
        // Buy a card for 3 money
        turnInfo.decrementMoney(3);
        turnInfo.decrementBuys();
        assertEquals("Should have 0 money after purchase", 0, turnInfo.getNumMoney());
        assertEquals("Should have 0 buys after purchase", 0, turnInfo.getNumBuys());
    }

    @Test
    public void testMultipleTurnsWithReset() {
        // Turn 1
        turnInfo.incrementMoney(5);
        turnInfo.decrementBuys();
        
        // Reset for Turn 2
        turnInfo.reset();
        assertEquals("Turn 2 should start with default actions", 1, turnInfo.getNumActions());
        assertEquals("Turn 2 should start with default money", 0, turnInfo.getNumMoney());
        assertEquals("Turn 2 should start with default buys", 1, turnInfo.getNumBuys());
        
        // Turn 2 operations
        turnInfo.incrementMoney(3);
        assertEquals("Turn 2 money should accumulate independently", 3, turnInfo.getNumMoney());
    }
}
