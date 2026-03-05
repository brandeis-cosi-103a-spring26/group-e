package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the TurnInfo class.
 * Tests resource management (actions, money, buys) for a game turn.
 */
public class TurnInfoTest {

    private TurnInfo turnInfo;

    @Before
    public void setUp() {
        turnInfo = new TurnInfo();
    }

    // ===== Constructor Test =====

    @Test
    public void testDefaultConstructor_InitializesCorrectly() {
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(1, turnInfo.getNumBuys());
    }

    // ===== Actions Tests =====

    @Test
    public void testAdjustActions_IncreaseByOne() {
        turnInfo.adjustActions(1);
        assertEquals(2, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_IncreaseByLargeAmount() {
        turnInfo.adjustActions(5);
        assertEquals(6, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_DecreaseByOneWithSufficientActions() {
        turnInfo.adjustActions(2); // Now have 3 actions
        turnInfo.adjustActions(-1);
        assertEquals(2, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_DecreaseByExactAmount() {
        turnInfo.adjustActions(4); // Now have 5 actions
        turnInfo.adjustActions(-5);
        assertEquals(0, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_DecreaseWithInsufficientActions() {
        turnInfo.adjustActions(-2); // Try to subtract 2 from 1, should cap at 0
        assertEquals(0, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_DecreaseFromZero() {
        turnInfo.adjustActions(-1); // Use the 1 action
        turnInfo.adjustActions(-1); // Try to go negative
        assertEquals(0, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_MultipleIncrementsAndDecrements() {
        turnInfo.adjustActions(2);  // 1 -> 3
        turnInfo.adjustActions(3);  // 3 -> 6
        turnInfo.adjustActions(-2); // 6 -> 4
        turnInfo.adjustActions(-1); // 4 -> 3
        assertEquals(3, turnInfo.getNumActions());
    }

    @Test
    public void testAdjustActions_ZeroAdjustment() {
        turnInfo.adjustActions(0);
        assertEquals(1, turnInfo.getNumActions());
    }

    // ===== Money Tests =====

    @Test
    public void testAdjustMoney_IncreaseByOne() {
        turnInfo.adjustMoney(1);
        assertEquals(1, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_IncreaseByLargeAmount() {
        turnInfo.adjustMoney(10);
        assertEquals(10, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_DecreaseWithSufficientFunds() {
        turnInfo.adjustMoney(7);
        turnInfo.adjustMoney(-3);
        assertEquals(4, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_DecreaseByExactAmount() {
        turnInfo.adjustMoney(5);
        turnInfo.adjustMoney(-5);
        assertEquals(0, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_DecreaseWithInsufficientFunds() {
        turnInfo.adjustMoney(3);
        turnInfo.adjustMoney(-5); // Try to subtract 5 from 3
        assertEquals(3, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_DecreaseFromZero() {
        turnInfo.adjustMoney(-1); // Try to go negative from 0
        assertEquals(0, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_MultipleIncrementsAndDecrements() {
        turnInfo.adjustMoney(2);  // 0 -> 2
        turnInfo.adjustMoney(3);  // 2 -> 5
        turnInfo.adjustMoney(-1); // 5 -> 4
        turnInfo.adjustMoney(2);  // 4 -> 6
        turnInfo.adjustMoney(-3); // 6 -> 3
        assertEquals(3, turnInfo.getNumMoney());
    }

    @Test
    public void testAdjustMoney_ZeroAdjustment() {
        turnInfo.adjustMoney(5);
        turnInfo.adjustMoney(0);
        assertEquals(5, turnInfo.getNumMoney());
    }

    // ===== Buys Tests =====

    @Test
    public void testAdjustBuys_IncreaseByOne() {
        turnInfo.adjustBuys(1);
        assertEquals(2, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_IncreaseByLargeAmount() {
        turnInfo.adjustBuys(4);
        assertEquals(5, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_DecreaseByOneWithSufficientBuys() {
        turnInfo.adjustBuys(2); // Now have 3 buys
        turnInfo.adjustBuys(-1);
        assertEquals(2, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_DecreaseByExactAmount() {
        turnInfo.adjustBuys(3); // Now have 4 buys
        turnInfo.adjustBuys(-4);
        assertEquals(0, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_DecreaseWithInsufficientBuys() {
        turnInfo.adjustBuys(-2); // Try to subtract 2 from 1, should cap at 0
        assertEquals(0, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_DecreaseFromZero() {
        turnInfo.adjustBuys(-1); // Use the 1 buy
        turnInfo.adjustBuys(-1); // Try to go negative
        assertEquals(0, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_MultipleIncrementsAndDecrements() {
        turnInfo.adjustBuys(2);  // 1 -> 3
        turnInfo.adjustBuys(1);  // 3 -> 4
        turnInfo.adjustBuys(-2); // 4 -> 2
        turnInfo.adjustBuys(-1); // 2 -> 1
        assertEquals(1, turnInfo.getNumBuys());
    }

    @Test
    public void testAdjustBuys_ZeroAdjustment() {
        turnInfo.adjustBuys(0);
        assertEquals(1, turnInfo.getNumBuys());
    }

    // ===== Reset Tests =====

    @Test
    public void testReset_ResetsActionsToOne() {
        turnInfo.adjustActions(5);
        turnInfo.reset();
        assertEquals(1, turnInfo.getNumActions());
    }

    @Test
    public void testReset_ResetsMoneyToZero() {
        turnInfo.adjustMoney(10);
        turnInfo.reset();
        assertEquals(0, turnInfo.getNumMoney());
    }

    @Test
    public void testReset_ResetsBuysToOne() {
        turnInfo.adjustBuys(3);
        turnInfo.reset();
        assertEquals(1, turnInfo.getNumBuys());
    }

    @Test
    public void testReset_ResetsAllFieldsSimultaneously() {
        turnInfo.adjustActions(3);
        turnInfo.adjustMoney(7);
        turnInfo.adjustBuys(2);
        
        turnInfo.reset();
        
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(1, turnInfo.getNumBuys());
    }

    @Test
    public void testReset_CanBeCalledMultipleTimes() {
        turnInfo.adjustMoney(5);
        turnInfo.reset();
        assertEquals(0, turnInfo.getNumMoney());
        
        turnInfo.adjustMoney(3);
        turnInfo.reset();
        assertEquals(0, turnInfo.getNumMoney());
    }

    @Test
    public void testReset_FromDefaultState() {
        turnInfo.reset(); // Reset without any changes
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(1, turnInfo.getNumBuys());
    }

    // ===== Integration/Scenario Tests =====

    @Test
    public void testScenario_SimpleActionPhase() {
        // Play an action card that gives +2 actions
        turnInfo.adjustActions(-1); // Use 1 action to play the card
        turnInfo.adjustActions(2);  // Card gives +2 actions
        assertEquals(2, turnInfo.getNumActions());
    }

    @Test
    public void testScenario_MoneyPhaseWithMoneyCards() {
        // Play 3 Bitcoin (1 coin each) and 2 Ethereum (2 coins each)
        turnInfo.adjustMoney(1);
        turnInfo.adjustMoney(1);
        turnInfo.adjustMoney(1);
        turnInfo.adjustMoney(2);
        turnInfo.adjustMoney(2);
        assertEquals(7, turnInfo.getNumMoney());
    }

    @Test
    public void testScenario_BuyPhaseWithPurchase() {
        // Have 5 money and 1 buy, purchase a 3-cost card
        turnInfo.adjustMoney(5);
        turnInfo.adjustMoney(-3);
        turnInfo.adjustBuys(-1);
        
        assertEquals(2, turnInfo.getNumMoney());
        assertEquals(0, turnInfo.getNumBuys());
    }

    @Test
    public void testScenario_CompleteTypicalTurn() {
        // Action phase: play action that gives +1 action, +1 buy
        turnInfo.adjustActions(-1);
        turnInfo.adjustActions(1);
        turnInfo.adjustBuys(1);
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(2, turnInfo.getNumBuys());
        
        // Money phase: play 4 coins worth of money
        turnInfo.adjustMoney(4);
        assertEquals(4, turnInfo.getNumMoney());
        
        // Buy phase: buy two 2-cost cards
        turnInfo.adjustMoney(-2);
        turnInfo.adjustBuys(-1);
        turnInfo.adjustMoney(-2);
        turnInfo.adjustBuys(-1);
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(0, turnInfo.getNumBuys());
    }

    @Test
    public void testScenario_TurnWithInsufficientResourceAttempts() {
        // Try to play action without actions
        turnInfo.adjustActions(-1); // Use the 1 action
        turnInfo.adjustActions(-1); // Try to use another - should fail
        assertEquals(0, turnInfo.getNumActions());
        
        // Try to buy without buys
        turnInfo.adjustBuys(-1); // Use the 1 buy
        turnInfo.adjustBuys(-1); // Try to buy again - should fail
        assertEquals(0, turnInfo.getNumBuys());
        
        // Try to spend money you don't have
        turnInfo.adjustMoney(-1); // Try to spend from 0 - should fail
        assertEquals(0, turnInfo.getNumMoney());
    }

    @Test
    public void testScenario_MultipleTurnsCycle() {
        // Turn 1: Simple turn
        turnInfo.adjustActions(-1);
        turnInfo.adjustMoney(3);
        turnInfo.adjustMoney(-3);
        turnInfo.adjustBuys(-1);
        
        // Reset for turn 2
        turnInfo.reset();
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(1, turnInfo.getNumBuys());
        
        // Turn 2: More complex
        turnInfo.adjustMoney(5);
        turnInfo.adjustMoney(-2);
        
        // Reset for turn 3
        turnInfo.reset();
        assertEquals(1, turnInfo.getNumActions());
        assertEquals(0, turnInfo.getNumMoney());
        assertEquals(1, turnInfo.getNumBuys());
    }

}
