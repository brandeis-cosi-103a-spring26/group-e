package edu.brandeis.cosi103a.groupe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.state.CardStacks;

/**
 * Integration tests for the ConsolePlayer class.
 * 
 * Since Renderer.renderGame() and Harness.getDecisionFromConsole() are tested
 * separately in their own unit test files, these tests focus on verifying that
 * ConsolePlayer correctly implements the Player interface, properly extends
 * AbstractPlayer, and that the integration points are correctly structured.
 * 
 * Note: Full end-to-end integration tests requiring live console input are
 * better suited for manual testing or system-level tests since ConsolePlayer
 * relies on Harness's static Scanner which cannot be easily mocked in unit tests.
 */
public class ConsolePlayerTest {
    
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    
    private ConsolePlayer player;
    private Card bitcoinCard;
    private Card methodCard;
    private Card frameworkCard;
    
    @Before
    public void setUp() {
        // Ensure output stream is clean before each test
        outputStream.reset();

        // Redirect System.out to capture console output
        System.setOut(new PrintStream(outputStream));
        
        player = new ConsolePlayer();
        
        // Create sample cards for testing
        bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        methodCard = new Card(Card.Type.METHOD, 2);
        frameworkCard = new Card(Card.Type.FRAMEWORK, 3);
    }
    
    @After
    public void tearDown() {
        // Restore original System.in and System.out
        System.setIn(originalIn);
        System.setOut(originalOut);
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Creates a mock GameState for testing.
     */
    private GameState createMockGameState(String playerName, GameState.TurnPhase phase) {
        Hand mockHand = new Hand(
            ImmutableList.of(), // played cards
            ImmutableList.of(bitcoinCard, methodCard) // unplayed cards
        );
        
        CardStacks mockShop = new CardStacks(ImmutableMap.of(
            Card.Type.BITCOIN, 50,
            Card.Type.FRAMEWORK, 8,
            Card.Type.METHOD, 10
        ));
        
        return new GameState(
            playerName,
            mockHand,
            phase,
            1, // numActions
            3, // spendableMoney
            1, // numBuys
            mockShop
        );
    }
    
    // ========== Player Interface Implementation Tests ==========
    
    @Test
    public void testImplementsPlayerInterface() {
        // Verify: ConsolePlayer implements the Player interface
        assertTrue("ConsolePlayer should implement Player interface", 
                   player instanceof Player);
    }
    
    @Test
    public void testExtendsAbstractPlayer() {
        // Verify: ConsolePlayer extends AbstractPlayer
        assertTrue("ConsolePlayer should extend AbstractPlayer", 
                   player instanceof AbstractPlayer);
    }
    
    @Test
    public void testGetName_ReturnsNonNull() {
        // Verify: getName returns a non-null value
        String name = player.getName();
        assertNotNull("Player name should not be null", name);
    }
    
    @Test
    public void testGetName_FollowsNamingConvention() {
        // Verify: Name follows the Player-X format from AbstractPlayer
        String name = player.getName();
        assertTrue("Name should follow Player-X format", 
                   name.matches("Player-\\d+"));
    }
    
    // ========== Method Signature Tests ==========
    
    @Test
    public void testMakeDecision_MethodExists() throws NoSuchMethodException {
        // Verify: makeDecision method exists with correct signature
        Method makeDecisionMethod = ConsolePlayer.class.getMethod(
            "makeDecision", 
            GameState.class, 
            ImmutableList.class
        );
        assertNotNull("makeDecision method should exist", makeDecisionMethod);
    }
    
    @Test
    public void testMakeDecision_ReturnsDecision() throws Exception {
        // Verify: makeDecision returns Decision type
        Method makeDecisionMethod = ConsolePlayer.class.getMethod(
            "makeDecision", 
            GameState.class, 
            ImmutableList.class
        );
        assertEquals("makeDecision should return Decision type", 
                     Decision.class, 
                     makeDecisionMethod.getReturnType());
    }
    
    // ========== Constructor Tests ==========
    
    @Test
    public void testConstructor_CreatesValidInstance() {
        // Verify: Constructor creates a valid ConsolePlayer instance
        ConsolePlayer newPlayer = new ConsolePlayer();
        assertNotNull("Constructor should create non-null instance", newPlayer);
        assertNotNull("New player should have a name", newPlayer.getName());
    }
    
    @Test
    public void testConstructor_MultipleInstances() {
        // Verify: Can create multiple independent instances
        ConsolePlayer p1 = new ConsolePlayer();
        ConsolePlayer p2 = new ConsolePlayer();
        ConsolePlayer p3 = new ConsolePlayer();
        
        assertNotNull("First instance should not be null", p1);
        assertNotNull("Second instance should not be null", p2);
        assertNotNull("Third instance should not be null", p3);
        
        // Each should have unique names
        assertNotEquals("Instances should have different names", 
                        p1.getName(), p2.getName());
        assertNotEquals("Instances should have different names", 
                        p2.getName(), p3.getName());
        assertNotEquals("Instances should have different names", 
                        p1.getName(), p3.getName());
    }
}
