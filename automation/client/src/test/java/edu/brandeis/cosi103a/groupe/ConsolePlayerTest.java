package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the ConsolePlayer class.
 *
 * <p>Since Renderer.renderGame() and Harness.getDecisionFromConsole() are tested separately in
 * their own unit test files, these tests focus on verifying that ConsolePlayer correctly implements
 * the Player interface and that properly extends AbstractPlayer.
 *
 * <p>Note: Full end-to-end integration tests requiring live console input are better suited for
 * manual testing or system-level tests since ConsolePlayer relies on Harness's static Scanner which
 * cannot be easily mocked in unit tests.
 */
public class ConsolePlayerTest {

    private ConsolePlayer player;

    @Before
    public void setUp() {
        player = new ConsolePlayer();
    }

    // ========== Player Interface Implementation Tests ==========

    @Test
    public void testImplementsPlayerInterface() {
        // Verify: ConsolePlayer implements the Player interface
        assertTrue("ConsolePlayer should implement Player interface", player instanceof Player);
    }

    @Test
    public void testExtendsAbstractPlayer() {
        // Verify: ConsolePlayer extends AbstractPlayer
        assertTrue("ConsolePlayer should extend AbstractPlayer", player instanceof AbstractPlayer);
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
        assertTrue("Name should follow Player-X format", name.matches("Player-\\d+"));
    }

    // ========== Method Signature Tests ==========

    @Test
    public void testMakeDecision_MethodExists() throws NoSuchMethodException {
        // Verify: makeDecision method exists with correct signature
        Method makeDecisionMethod =
                ConsolePlayer.class.getMethod("makeDecision", GameState.class, ImmutableList.class, java.util.Optional.class);
        assertNotNull("makeDecision method should exist", makeDecisionMethod);
    }

    @Test
    public void testMakeDecision_ReturnsDecision() throws Exception {
        // Verify: makeDecision returns Decision type
        Method makeDecisionMethod =
                ConsolePlayer.class.getMethod("makeDecision", GameState.class, ImmutableList.class, java.util.Optional.class);
        assertEquals(
                "makeDecision should return Decision type",
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
        assertNotEquals("Instances should have different names", p1.getName(), p2.getName());
        assertNotEquals("Instances should have different names", p2.getName(), p3.getName());
        assertNotEquals("Instances should have different names", p1.getName(), p3.getName());
    }

    // ========== Auto-Skip Behavior Tests ==========

    @Test
    public void testMakeDecision_ActionPhaseNoActions_AutoReturnsEndPhase() {
        // When it's the ACTION phase with 0 available actions and the only option
        // is EndPhaseDecision, ConsolePlayer should return it immediately without input.
        GameState state =
                new GameState(
                        player.getName(),
                        new Hand(ImmutableList.<Card>of(), ImmutableList.<Card>of()),
                        GameState.TurnPhase.ACTION,
                        0, // 0 available actions
                        0,
                        1,
                        new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 30)));
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should auto-return EndPhaseDecision when no actions remain", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhaseNoBuys_AutoReturnsEndPhase() {
        // When it's the BUY phase with 0 available buys and the only option
        // is EndPhaseDecision, ConsolePlayer should return it immediately without input.
        GameState state =
                new GameState(
                        player.getName(),
                        new Hand(ImmutableList.<Card>of(), ImmutableList.<Card>of()),
                        GameState.TurnPhase.BUY,
                        0,
                        0,
                        0, // 0 available buys
                        new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 30)));
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should auto-return EndPhaseDecision when no buys remain", endPhase, result);
    }
}
