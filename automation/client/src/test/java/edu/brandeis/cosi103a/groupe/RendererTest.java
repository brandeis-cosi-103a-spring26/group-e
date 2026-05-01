package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Renderer class. Tests console output by capturing System.out and verifying
 * content.
 */
public class RendererTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        // Redirect System.out to our stream for testing
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() {
        // Restore original System.out after each test
        System.setOut(originalOut);
    }

    // ========== Player Name Tests ==========

    @Test
    public void testRenderGame_DisplaysPlayerName() {
        GameState state = createTestGameState("Alice", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should contain player name 'Alice'", output.contains("Player: Alice"));
    }

    @Test
    public void testRenderGame_DisplaysDifferentPlayerNames() {
        String[] playerNames = {"Player 1", "Bob", "TestUser123", "AI_Player"};

        for (String name : playerNames) {
            outputStream.reset(); // Clear previous output
            GameState state = createTestGameState(name, GameState.TurnPhase.ACTION);

            Renderer.renderGame(state);

            String output = outputStream.toString();
            assertTrue(
                    "Output should contain player name: " + name,
                    output.contains("Player: " + name));
        }
    }

    // ========== Phase Display Tests ==========

    @Test
    public void testRenderGame_DisplaysActionPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Action phase", output.contains("Phase: Action"));
    }

    @Test
    public void testRenderGame_DisplaysBuyPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.BUY);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Buy phase", output.contains("Phase: Buy"));
    }

    @Test
    public void testRenderGame_DisplaysMoneyPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.MONEY);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Money phase", output.contains("Phase: Money"));
    }

    @Test
    public void testRenderGame_DisplaysCleanupPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.CLEANUP);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Cleanup phase", output.contains("Phase: Cleanup"));
    }

    @Test
    public void testRenderGame_DisplaysGainPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.GAIN);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Gain phase", output.contains("Phase: Gain"));
    }

    @Test
    public void testRenderGame_DisplaysDiscardPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.DISCARD);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Discard phase", output.contains("Phase: Discard"));
    }

    @Test
    public void testRenderGame_DisplaysReactionPhase() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.REACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Output should show Reaction phase", output.contains("Phase: Reaction"));
    }

    @Test
    public void testRenderGame_AllPhases_ContainPhaseLabel() {
        GameState.TurnPhase[] phases = {
            GameState.TurnPhase.ACTION,
            GameState.TurnPhase.BUY,
            GameState.TurnPhase.MONEY,
            GameState.TurnPhase.CLEANUP,
            GameState.TurnPhase.GAIN,
            GameState.TurnPhase.DISCARD,
            GameState.TurnPhase.REACTION
        };

        for (GameState.TurnPhase phase : phases) {
            outputStream.reset();
            GameState state = createTestGameState("Player1", phase);

            Renderer.renderGame(state);

            String output = outputStream.toString();
            assertTrue("Output should contain 'Phase:' for " + phase, output.contains("Phase:"));
        }
    }

    // ========== Hand Display Tests ==========

    @Test
    public void testRenderGame_DisplaysAllCardsInHand() {
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card ethereum = new Card(Card.Type.ETHEREUM, 2);
        Card method = new Card(Card.Type.METHOD, 3);
        Card codeReview = new Card(Card.Type.CODE_REVIEW, 4);

        Hand hand =
                new Hand(
                        ImmutableList.of(), // No played cards
                        ImmutableList.of(bitcoin, ethereum, method, codeReview) // Unplayed cards
                        );

        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.ACTION,
                        1,
                        3,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display Bitcoin", output.contains("Bitcoin"));
        assertTrue("Should display Ethereum", output.contains("Ethereum"));
        assertTrue("Should display Method", output.contains("Method"));
        assertTrue("Should display Code Review", output.contains("Code Review"));
    }

    @Test
    public void testRenderGame_DisplaysMultipleCardsOfSameType() {
        Card bitcoin1 = new Card(Card.Type.BITCOIN, 1);
        Card bitcoin2 = new Card(Card.Type.BITCOIN, 2);
        Card bitcoin3 = new Card(Card.Type.BITCOIN, 3);

        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(bitcoin1, bitcoin2, bitcoin3));

        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.ACTION,
                        1,
                        3,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Count occurrences of "Bitcoin" in hand section
        int count = countOccurrences(output, "Bitcoin");
        assertTrue("Should display Bitcoin multiple times (at least 3 in hand)", count >= 3);
    }

    @Test
    public void testRenderGame_EmptyHand_ShowsNone() {
        Hand emptyHand = new Hand(ImmutableList.of(), ImmutableList.of());

        GameState state =
                new GameState(
                        "Player1",
                        emptyHand,
                        GameState.TurnPhase.BUY,
                        0,
                        0,
                        0,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display 'Your Hand' section", output.contains("Your Hand"));
        assertTrue("Should indicate empty categories with (none)", output.contains("(none)"));
    }

    @Test
    public void testRenderGame_DisplaysHandSection() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display 'Your Hand' section header", output.contains("Your Hand"));
    }

    @Test
    public void testRenderGame_DisplaysCardsFromAllCategories() {
        Card codeReview = new Card(Card.Type.CODE_REVIEW, 1); // action card
        Card bitcoin = new Card(Card.Type.BITCOIN, 2); // treasure card
        Card ethereum = new Card(Card.Type.ETHEREUM, 3); // treasure card
        Card method = new Card(Card.Type.METHOD, 4); // victory card

        Hand mixedHand =
                new Hand(
                        ImmutableList.of(),
                        ImmutableList.of(method, bitcoin, ethereum, codeReview));

        GameState state =
                new GameState(
                        "Player1",
                        mixedHand,
                        GameState.TurnPhase.ACTION,
                        1,
                        3,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display action card (Code Review)", output.contains("Code Review"));
        assertTrue("Should display treasure card (Bitcoin)", output.contains("Bitcoin"));
        assertTrue("Should display treasure card (Ethereum)", output.contains("Ethereum"));
        assertTrue("Should display victory card (Method)", output.contains("Method"));
    }

    // ========== Played Cards Tests ==========

    @Test
    public void testRenderGame_DisplaysPlayedCards() {
        Card method = new Card(Card.Type.METHOD, 1);
        Card refactor = new Card(Card.Type.REFACTOR, 2);

        Hand handWithPlayed =
                new Hand(
                        ImmutableList.of(method, refactor), // Played cards
                        ImmutableList.of() // No unplayed cards
                        );

        GameState state =
                new GameState(
                        "Player1",
                        handWithPlayed,
                        GameState.TurnPhase.BUY,
                        0,
                        3,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should show played cards section", output.contains("Played Cards"));
        assertTrue("Should list Method card", output.contains("Method"));
        assertTrue("Should list Refactor card", output.contains("Refactor"));
    }

    @Test
    public void testRenderGame_NoPlayedCards_ShowsNone() {
        Hand handNoPlayed =
                new Hand(
                        ImmutableList.of(), // No played cards
                        ImmutableList.of(new Card(Card.Type.BITCOIN, 1)));

        GameState state =
                new GameState(
                        "Player1",
                        handNoPlayed,
                        GameState.TurnPhase.ACTION,
                        1,
                        0,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should show played cards section", output.contains("Played Cards"));
        assertTrue("Should indicate no played cards", output.contains("(none)"));
    }

    // ========== Available Cards (Supply) Tests ==========

    @Test
    public void testRenderGame_DisplaysAvailableCardsSection() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display 'Available Cards' section", output.contains("Available Cards"));
    }

    @Test
    public void testRenderGame_DisplaysSupplyPiles() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Check that supply cards appear (these are in the CardStacks)
        assertTrue("Should list Method in supply", output.contains("Method"));
        assertTrue("Should list Bitcoin in supply", output.contains("Bitcoin"));
    }

    @Test
    public void testRenderGame_DisplaysSupplyCardCounts() {
        CardStacks supply =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.METHOD, 12,
                                Card.Type.BITCOIN, 30));

        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state =
                new GameState("Player1", hand, GameState.TurnPhase.ACTION, 1, 0, 1, supply);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Should show counts like "12 Method" and "30 Bitcoin"
        assertTrue(
                "Should display Method count",
                output.contains("12 Method") || output.contains("Method: 12"));
        assertTrue(
                "Should display Bitcoin count",
                output.contains("30 Bitcoin") || output.contains("Bitcoin: 30"));
    }

    // ========== Status Bar Tests ==========

    @Test
    public void testRenderGame_DisplaysStatusBar() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display 'Actions:' label", output.contains("Actions:"));
        assertTrue("Should display 'Buys:' label", output.contains("Buys:"));
        assertTrue("Should display 'Money:' label", output.contains("Money:"));
    }

    @Test
    public void testRenderGame_DisplaysCorrectActionCount() {
        GameState state =
                new GameState(
                        "Player1",
                        new Hand(ImmutableList.of(), ImmutableList.of()),
                        GameState.TurnPhase.ACTION,
                        5, // 5 actions
                        0,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display '5' actions", output.contains("Actions: 5"));
    }

    @Test
    public void testRenderGame_DisplaysCorrectBuyCount() {
        GameState state =
                new GameState(
                        "Player1",
                        new Hand(ImmutableList.of(), ImmutableList.of()),
                        GameState.TurnPhase.BUY,
                        0,
                        0,
                        3, // 3 buys
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display '3' buys", output.contains("Buys: 3"));
    }

    @Test
    public void testRenderGame_DisplaysCorrectMoneyCount() {
        GameState state =
                new GameState(
                        "Player1",
                        new Hand(ImmutableList.of(), ImmutableList.of()),
                        GameState.TurnPhase.MONEY,
                        0,
                        7, // 7 money
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display '7' money", output.contains("Money: 7"));
    }

    @Test
    public void testRenderGame_DisplaysZeroValues() {
        GameState state =
                new GameState(
                        "Player1",
                        new Hand(ImmutableList.of(), ImmutableList.of()),
                        GameState.TurnPhase.CLEANUP,
                        0, // 0 actions
                        0, // 0 money
                        0, // 0 buys
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display '0' for actions", output.contains("Actions: 0"));
        assertTrue("Should display '0' for buys", output.contains("Buys: 0"));
        assertTrue("Should display '0' for money", output.contains("Money: 0"));
    }

    // ========== Welcome Message Tests ==========

    @Test
    public void testRenderGame_DisplaysWelcomeMessage() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue(
                "Should display welcome message",
                output.contains("Welcome to Automation: The Game"));
    }

    // ========== Edge Cases ==========

    @Test
    public void testRenderGame_LargeHandSize() {
        // Create a hand with many cards
        ImmutableList.Builder<Card> cardsBuilder = ImmutableList.builder();
        for (int i = 0; i < 20; i++) {
            cardsBuilder.add(new Card(Card.Type.BITCOIN, i));
        }

        Hand largeHand = new Hand(ImmutableList.of(), cardsBuilder.build());
        GameState state =
                new GameState(
                        "Player1",
                        largeHand,
                        GameState.TurnPhase.ACTION,
                        1,
                        0,
                        1,
                        createTestCardStacks());

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Should not crash and should display hand section
        assertTrue(
                "Should display hand section even with large hand", output.contains("Your Hand"));
    }

    @Test
    public void testRenderGame_LongPlayerName() {
        String longName = "VeryLongPlayerNameThatExceedsNormalLength123456";
        GameState state = createTestGameState(longName, GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should handle long player names", output.contains("Player: " + longName));
    }

    @Test
    public void testRenderGame_EmptySupply() {
        CardStacks emptySupply = new CardStacks(ImmutableMap.of());
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state =
                new GameState("Player1", hand, GameState.TurnPhase.ACTION, 1, 0, 1, emptySupply);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Should not crash with empty supply
        assertTrue(
                "Should display available cards section even when empty",
                output.contains("Available Cards"));
    }

    // ========== renderGameWithDecisions Tests ==========

    @Test
    public void testRenderGameWithDecisions_EmptyDecisions_SameAsRenderGame() {
        // renderGameWithDecisions with no decisions should produce identical structural output
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGameWithDecisions(state, ImmutableList.of());
        String output = outputStream.toString();

        assertTrue("Should still display player name", output.contains("Player: Player1"));
        assertTrue("Should still display phase", output.contains("Phase: Action"));
        assertTrue("Should still display Available Cards", output.contains("Available Cards"));
        assertTrue("Should still display Your Hand", output.contains("Your Hand"));
        assertFalse("Should not show any * markers with no decisions", output.contains("*"));
    }

    @Test
    public void testRenderGameWithDecisions_BuyDecision_MarksSupplyCard() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.BUY);
        ImmutableList<Decision> decisions = ImmutableList.of(new BuyDecision(Card.Type.BITCOIN));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertTrue(
                "Output should contain * marker for actionable supply card", output.contains("*"));
    }

    @Test
    public void testRenderGameWithDecisions_NoBuyDecisionForCard_NoMarker() {
        // Only Ethereum is buyable; Bitcoin is in supply but not in decisions
        CardStacks supply =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.ETHEREUM, 10));
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState("Player1", hand, GameState.TurnPhase.BUY, 0, 0, 1, supply);
        ImmutableList<Decision> decisions = ImmutableList.of(new BuyDecision(Card.Type.ETHEREUM));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        // There should be exactly one * (for Ethereum), not two
        assertEquals("Only one supply card should be marked", 1, countOccurrences(output, "*"));
    }

    @Test
    public void testRenderGameWithDecisions_PlayCardDecision_MarksHandCard() {
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card ethereum = new Card(Card.Type.ETHEREUM, 2);
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(bitcoin, ethereum));
        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.MONEY,
                        0,
                        0,
                        1,
                        createTestCardStacks());
        // Only bitcoin is playable
        ImmutableList<Decision> decisions = ImmutableList.of(new PlayCardDecision(bitcoin));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertTrue("Output should contain * marker for actionable hand card", output.contains("*"));
        assertEquals("Exactly one hand card should be marked", 1, countOccurrences(output, "*"));
    }

    @Test
    public void testRenderGameWithDecisions_GainCardDecision_MarksSupplyCard() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.GAIN);
        ImmutableList<Decision> decisions =
                ImmutableList.of(new GainCardDecision(Card.Type.METHOD));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertTrue("Output should contain * marker for gainable supply card", output.contains("*"));
    }

    @Test
    public void testRenderGameWithDecisions_TrashCardDecision_MarksHandCard() {
        Card refactor = new Card(Card.Type.REFACTOR, 1);
        Card bitcoin = new Card(Card.Type.BITCOIN, 2);
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(refactor, bitcoin));
        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.ACTION,
                        1,
                        0,
                        1,
                        createTestCardStacks());
        ImmutableList<Decision> decisions = ImmutableList.of(new TrashCardDecision(bitcoin));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertTrue("Output should contain * marker for trashable hand card", output.contains("*"));
        assertEquals("Exactly one hand card should be marked", 1, countOccurrences(output, "*"));
    }

    @Test
    public void testRenderGameWithDecisions_DiscardCardDecision_MarksHandCard() {
        Card refactor = new Card(Card.Type.REFACTOR, 1);
        Card bitcoin = new Card(Card.Type.BITCOIN, 2);
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(refactor, bitcoin));
        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.DISCARD,
                        1,
                        0,
                        1,
                        createTestCardStacks());
        ImmutableList<Decision> decisions = ImmutableList.of(new DiscardCardDecision(bitcoin));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertTrue(
                "Output should contain * marker for discardable hand card", output.contains("*"));
        assertEquals("Exactly one hand card should be marked", 1, countOccurrences(output, "*"));
    }

    @Test
    public void testRenderGameWithDecisions_MultipleDecisions_MarksAllActionableCards() {
        Card bitcoin1 = new Card(Card.Type.BITCOIN, 1);
        Card bitcoin2 = new Card(Card.Type.BITCOIN, 2);
        Card method = new Card(Card.Type.METHOD, 3);
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(bitcoin1, bitcoin2, method));
        GameState state =
                new GameState(
                        "Player1",
                        hand,
                        GameState.TurnPhase.MONEY,
                        0,
                        0,
                        1,
                        createTestCardStacks());
        ImmutableList<Decision> decisions =
                ImmutableList.of(new PlayCardDecision(bitcoin1), new PlayCardDecision(bitcoin2));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertEquals("Both playable cards should be marked", 2, countOccurrences(output, "*"));
    }

    @Test
    public void testRenderGameWithDecisions_EndPhaseDecision_NoMarkerAdded() {
        // EndPhaseDecision should not cause any * markers
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        ImmutableList<Decision> decisions =
                ImmutableList.of(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        Renderer.renderGameWithDecisions(state, decisions);

        String output = outputStream.toString();
        assertFalse("EndPhaseDecision should not produce any * markers", output.contains("*"));
    }

    @Test
    public void testRenderGameWithDecisions_DisplaysEventLogPanel() {
        outputStream.reset();
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        ImmutableList<EventNotif> events =
                ImmutableList.of(new EventNotif(state, new EndTurnEvent()));

        Renderer.renderGameWithDecisions(state, ImmutableList.of(), events);

        String output = outputStream.toString();
        assertTrue("Should show turn log header", output.contains("Turn Log"));
        assertTrue("Should show turn header", output.contains("Turn 1 - Player1"));
        assertTrue("Should show event description", output.contains("End turn"));
    }

    // ========== Supply Column Header & Cost Tests ==========

    @Test
    public void testRenderGame_DisplaysColumnHeaders() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display ACTIONS column header", output.contains("ACTIONS"));
        assertTrue("Should display TREASURES column header", output.contains("TREASURES"));
        assertTrue("Should display VICTORIES column header", output.contains("VICTORIES"));
    }

    @Test
    public void testRenderGame_SupplyDisplaysCardCosts() {
        CardStacks supply =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.METHOD, 12, // costs $2
                                Card.Type.BITCOIN, 30 // costs $0
                                ));
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state =
                new GameState("Player1", hand, GameState.TurnPhase.ACTION, 1, 0, 1, supply);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        // Costs are displayed as ($N) after the card name
        assertTrue("Should display cost for Method ($2)", output.contains("$2"));
        assertTrue("Should display cost for Bitcoin ($0)", output.contains("$0"));
    }

    @Test
    public void testRenderGame_SupplyDisplaysDiscountedCosts() {
        CardStacks supply =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.METHOD, 12, // costs $2
                                Card.Type.BITCOIN, 30 // costs $0
                                ));
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state = new GameState("Player1", hand, GameState.TurnPhase.BUY, 1, 0, 1, supply);

        Renderer.renderGameWithDecisions(state, ImmutableList.of(), 1);

        String output = outputStream.toString();
        assertTrue(
                "Should display discounted cost for Method ($1)", output.contains("Method ($1)"));
        assertTrue("Should still display Bitcoin at $0", output.contains("Bitcoin ($0)"));
    }

    @Test
    public void testRenderGame_BugCardInSupply_Renders() {
        CardStacks supply = new CardStacks(ImmutableMap.of(Card.Type.BUG, 20));
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of());
        GameState state =
                new GameState("Player1", hand, GameState.TurnPhase.ACTION, 1, 0, 1, supply);

        Renderer.renderGame(state);

        String output = outputStream.toString();
        assertTrue("Should display Bug card in supply", output.contains("Bug"));
        assertTrue("Should display Bug's count", output.contains("20 Bug"));
    }

    // ========== renderGameDebugMode Tests ==========

    @Test
    public void testRenderGameDebugMode_DisplaysDebugHeaders() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        BigMoneyPlayer player = new BigMoneyPlayer();
        DebugGameState debugState =
                new DebugGameState(
                        state,
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())),
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())));

        Renderer.renderGameDebugMode(debugState);

        String output = outputStream.toString();
        assertTrue("Should display debug deck header", output.contains("DEBUG MODE: Player Decks"));
        assertTrue(
                "Should display debug discard header",
                output.contains("DEBUG MODE: Player Discards"));
    }

    @Test
    public void testRenderGameDebugMode_DisplaysPlayerDecks() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        BigMoneyPlayer player = new BigMoneyPlayer();
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card method = new Card(Card.Type.METHOD, 2);
        DebugGameState debugState =
                new DebugGameState(
                        state,
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of(bitcoin, method))),
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())));

        Renderer.renderGameDebugMode(debugState);

        String output = outputStream.toString();
        assertTrue(
                "Should display player name in deck section",
                output.contains(player.getName() + "'s deck:"));
        // Both cards should appear somewhere in the output (also in supply/hand from renderGame)
        assertTrue("Should display Bitcoin in debug output", output.contains("Bitcoin"));
        assertTrue("Should display Method in debug output", output.contains("Method"));
    }

    @Test
    public void testRenderGameDebugMode_DisplaysPlayerDiscards() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        BigMoneyPlayer player = new BigMoneyPlayer();
        Card refactor = new Card(Card.Type.REFACTOR, 1);
        DebugGameState debugState =
                new DebugGameState(
                        state,
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())),
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of(refactor))));

        Renderer.renderGameDebugMode(debugState);

        String output = outputStream.toString();
        assertTrue(
                "Should display player name in discard section",
                output.contains(player.getName() + "'s discard pile:"));
        assertTrue("Should display Refactor in discard section", output.contains("Refactor"));
    }

    @Test
    public void testRenderGameDebugMode_EmptyDecksAndDiscards() {
        GameState state = createTestGameState("Player1", GameState.TurnPhase.ACTION);
        BigMoneyPlayer player = new BigMoneyPlayer();
        DebugGameState debugState =
                new DebugGameState(
                        state,
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())),
                        ImmutableMap.<Player, List<Card>>of(
                                player, Objects.requireNonNull(ImmutableList.of())));

        Renderer.renderGameDebugMode(debugState);

        String output = outputStream.toString();
        // Headers must appear even with empty collections
        assertTrue("Should display debug headers with empty decks", output.contains("DEBUG MODE"));
        // No "  - CardName" lines should appear for the debug sections
        assertFalse(
                "Should not show dash-prefixed card lines when debug decks are empty",
                output.contains("\n  - "));
    }

    // ========== Helper Methods ==========

    /**
     * Creates a basic test GameState with specified player name and phase. Includes a simple hand
     * and supply for testing.
     */
    private GameState createTestGameState(String playerName, GameState.TurnPhase phase) {
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card method = new Card(Card.Type.METHOD, 2);

        Hand hand =
                new Hand(
                        ImmutableList.<Card>of(), // No played cards
                        ImmutableList.of(bitcoin, method) // Two unplayed cards
                        );

        return new GameState(
                playerName,
                hand,
                phase,
                1, // availableActions
                3, // spendableMoney
                1, // availableBuys
                createTestCardStacks());
    }

    /** Creates a basic CardStacks for testing with a variety of cards. */
    private CardStacks createTestCardStacks() {
        return new CardStacks(
                ImmutableMap.of(
                        Card.Type.METHOD, 12,
                        Card.Type.MODULE, 12,
                        Card.Type.FRAMEWORK, 12,
                        Card.Type.BITCOIN, 30,
                        Card.Type.ETHEREUM, 40,
                        Card.Type.DOGECOIN, 30,
                        Card.Type.CODE_REVIEW, 8,
                        Card.Type.EVERGREEN_TEST, 8,
                        Card.Type.REFACTOR, 8));
    }

    /** Helper method to count occurrences of a substring in a string. */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
