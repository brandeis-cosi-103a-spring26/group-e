package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the OurEngine class.
 * Tests constructor validation, game mechanics, game state queries, and
 * orchestration logic.
 */
public class OurEngineTest {

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
            players.add(new BigMoneyPlayer());
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

    // ========== Constructor Validation Tests ==========

    @Test
    public void testConstructor_TwoPlayers_Success() {
        OurEngine engine = new OurEngine(twoPlayers);
        assertNotNull("Engine should be created", engine);
    }

    @Test
    public void testConstructor_ThreePlayers_Success() {
        OurEngine engine = new OurEngine(threePlayers);
        assertNotNull("Engine should be created", engine);
    }

    @Test
    public void testConstructor_FourPlayers_Success() {
        OurEngine engine = new OurEngine(fourPlayers);
        assertNotNull("Engine should be created", engine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_OnePlayer_ThrowsException() {
        new OurEngine(createPlayers(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroPlayers_ThrowsException() {
        new OurEngine(createPlayers(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_FivePlayers_ThrowsException() {
        new OurEngine(createPlayers(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullPlayersList_ThrowsException() {
        new OurEngine(null);
    }

    @Test
    public void testConstructor_InitializesPhaseToAction() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        GameState.TurnPhase phase = getPrivateField(engine, "phase");
        assertEquals("Initial phase should be ACTION", GameState.TurnPhase.ACTION, phase);
    }

    @Test
    public void testConstructor_InitializesActivePlayerCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        List<Player> players = getPrivateField(engine, "players");

        // Active player should be the last player (so getNextPlayer returns first)
        assertEquals("Active player should be last player initially",
                players.get(players.size() - 1), activePlayer);
    }

    @Test
    public void testConstructor_CreatesHandsForAllPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        assertEquals("Should have hands for all players", 3, hands.size());
        for (Player p : threePlayers) {
            assertTrue("Should have hand for player " + p.getName(), hands.containsKey(p));
            assertNotNull("Hand should not be null", hands.get(p));
        }
    }

    @Test
    public void testConstructor_CreatesDecksForAllPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");

        assertEquals("Should have decks for all players", 3, decks.size());
        for (Player p : threePlayers) {
            assertTrue("Should have deck for player " + p.getName(), decks.containsKey(p));
            assertNotNull("Deck should not be null", decks.get(p));
        }
    }

    @Test
    public void testConstructor_CreatesDiscardsForAllPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        assertEquals("Should have discards for all players", 3, discards.size());
        for (Player p : threePlayers) {
            assertTrue("Should have discard for player " + p.getName(), discards.containsKey(p));
            assertNotNull("Discard should not be null", discards.get(p));
        }
    }

    @Test
    public void testConstructor_DrawsFiveCardsPerPlayer() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        for (Player p : twoPlayers) {
            MutableHand hand = hands.get(p);
            assertEquals("Each player should have 5 cards in hand", 5, hand.size());
        }
    }

    @Test
    public void testConstructor_EachPlayerHasFiveRemainingCardsInDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");

        for (Player p : twoPlayers) {
            List<Card> deck = decks.get(p);
            assertEquals("Each player should have 5 cards remaining in deck (10 total - 5 drawn)",
                    5, deck.size());
        }
    }

    @Test
    public void testConstructor_VerifyStartingCardsComposition() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");

        for (Player p : twoPlayers) {
            // Combine hand and deck to verify total composition
            List<Card> allCards = new ArrayList<>(decks.get(p));
            allCards.addAll(hands.get(p).getUnplayedCards());

            assertEquals("Each player should have 10 total cards", 10, allCards.size());

            // Count card types
            int bitcoinCount = 0;
            int methodCount = 0;
            for (Card card : allCards) {
                if (card.type() == Card.Type.BITCOIN)
                    bitcoinCount++;
                else if (card.type() == Card.Type.METHOD)
                    methodCount++;
            }

            assertEquals("Each player should have 7 BITCOIN cards", 7, bitcoinCount);
            assertEquals("Each player should have 3 METHOD cards", 3, methodCount);
        }
    }

    @Test
    public void testConstructor_VerifyDiscardsStartEmpty() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        for (Player p : twoPlayers) {
            assertTrue("Discard pile should start empty for " + p.getName(),
                    discards.get(p).isEmpty());
        }
    }

    @Test
    public void testConstructor_VerifyCardIdUniqueness() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");

        List<Integer> allIds = new ArrayList<>();
        for (Player p : twoPlayers) {
            for (Card card : hands.get(p).getUnplayedCards()) {
                allIds.add(card.id());
            }
            for (Card card : decks.get(p)) {
                allIds.add(card.id());
            }
        }

        // Check for unique IDs
        long uniqueCount = allIds.stream().distinct().count();
        assertEquals("All cards should have unique IDs", allIds.size(), uniqueCount);
    }

    @Test
    public void testConstructor_InitializesShopWithCorrectQuantities_TwoPlayers() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN should have 60", 60, shop.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should have 40", 40, shop.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should have 30", 30, shop.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals("METHOD should have 14", 14, shop.getNumAvailable(Card.Type.METHOD));
        assertEquals("MODULE should have 8", 8, shop.getNumAvailable(Card.Type.MODULE));
        assertEquals("FRAMEWORK should have 8", 8, shop.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals("BUG should have 20 (2 players * 10)", 20, shop.getNumAvailable(Card.Type.BUG));
        assertEquals("REFACTOR should have 10", 10, shop.getNumAvailable(Card.Type.REFACTOR));
        assertEquals("CODE_REVIEW should have 10", 10, shop.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("EVERGREEN_TEST should have 10", 10, shop.getNumAvailable(Card.Type.EVERGREEN_TEST));
    }

    @Test
    public void testConstructor_InitializesShopWithCorrectQuantities_FourPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BUG should have 40 (4 players * 10)", 40, shop.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testConstructor_InitializesNumActions() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        int numActions = getPrivateField(engine, "numActions");
        // numActions should be 0 initially (set in play() method before first turn)
        assertEquals("numActions should be initialized to 0", 0, numActions);
    }

    @Test
    public void testConstructor_InitializesNumBuys() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        int numBuys = getPrivateField(engine, "numBuys");
        // numBuys should be 0 initially (set in play() method before first turn)
        assertEquals("numBuys should be initialized to 0", 0, numBuys);
    }

    @Test
    public void testConstructor_InitializesNumMoney() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        int numMoney = getPrivateField(engine, "numMoney");
        // numMoney should be 0 initially (set in play() method before first turn)
        assertEquals("numMoney should be initialized to 0", 0, numMoney);
    }

    // ========== Game State Query Tests ==========

    @Test
    public void testIsGameOver_FrameworkInStock_ReturnsFalse() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        assertFalse("Game should not be over when FRAMEWORK cards remain", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_FrameworkSoldOut_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Deplete FRAMEWORK by repeatedly calling decrementCount
        for (int i = 0; i < 8; i++) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        assertTrue("Game should be over when FRAMEWORK cards are sold out", engine.isGameOver());
    }

    @Test
    public void testMutableCardStacks_GetNumAvailable_CardExists_ReturnsQuantity() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN quantity should be 60", 60, shop.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("ETHEREUM quantity should be 40", 40, shop.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals("FRAMEWORK quantity should be 8", 8, shop.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testMutableCardStacks_GetNumAvailable_CardQuantityZero_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Deplete FRAMEWORK by repeatedly calling decrementCount
        for (int i = 0; i < 8; i++) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        assertEquals("Should return 0 for card with 0 quantity", 0,
                shop.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testMutableCardStacks_GetNumAvailable_SoldOut_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Deplete FRAMEWORK by repeatedly calling decrementCount
        for (int i = 0; i < 8; i++) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        assertEquals("Should return 0 for sold out card", 0,
                shop.getNumAvailable(Card.Type.FRAMEWORK));
    }

    // ========== Game Mechanics Tests - drawCard ==========

    @Test
    public void testDrawCard_FromNonEmptyDeck_AddsToHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card card1 = new Card(Card.Type.BITCOIN, 1);
        Card card2 = new Card(Card.Type.METHOD, 2);
        deck.add(card1);
        deck.add(card2);

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should have 1 card", 1, hand.size());
        assertEquals("Deck should have 1 card left", 1, deck.size());
        assertTrue("Hand should contain the drawn card", hand.contains(card2));
        assertEquals("Deck should contain the other card", card1, deck.get(0));
    }

    @Test
    public void testDrawCard_EmptyDeck_ReshufflesDiscard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card card1 = new Card(Card.Type.BITCOIN, 1);
        Card card2 = new Card(Card.Type.METHOD, 2);
        Card card3 = new Card(Card.Type.ETHEREUM, 3);
        discard.add(card1);
        discard.add(card2);
        discard.add(card3);

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should have 1 card", 1, hand.size());
        assertTrue("Discard should be empty after reshuffle", discard.isEmpty());
        assertEquals("Deck should have 2 cards remaining (3 - 1 drawn)", 2, deck.size());
    }

    @Test
    public void testDrawCard_EmptyDeckAndDiscard_NoChange() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should remain empty", 0, hand.size());
        assertTrue("Deck should remain empty", deck.isEmpty());
        assertTrue("Discard should remain empty", discard.isEmpty());
    }

    @Test
    public void testDrawCard_Reshuffle_ClearsDiscard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        discard.add(new Card(Card.Type.BITCOIN, 1));
        discard.add(new Card(Card.Type.METHOD, 2));

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertTrue("Discard should be cleared after reshuffle", discard.isEmpty());
    }

    @Test
    public void testDrawCard_Reshuffle_ShufflesDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add cards to discard in a specific order
        for (int i = 0; i < 10; i++) {
            discard.add(new Card(Card.Type.BITCOIN, i));
        }

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        // After shuffle and drawing one card, deck should have 9 cards
        assertEquals("Deck should have 9 cards after reshuffle and draw", 9, deck.size());
        // Note: We can't easily verify randomness, but we can verify the cards are
        // present
        assertFalse("Deck should not be empty after reshuffle", deck.isEmpty());
    }

    @Test
    public void testDrawCard_MultipleDrawsWithReshuffles() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Start with 3 cards in deck
        deck.add(new Card(Card.Type.BITCOIN, 1));
        deck.add(new Card(Card.Type.BITCOIN, 2));
        deck.add(new Card(Card.Type.BITCOIN, 3));

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);

        // Draw 3 cards (empties deck)
        drawCard.invoke(engine, testPlayer);
        drawCard.invoke(engine, testPlayer);
        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should have 3 cards", 3, hand.size());
        assertTrue("Deck should be empty", deck.isEmpty());

        // Add cards to discard and draw again (triggers reshuffle)
        discard.add(new Card(Card.Type.METHOD, 4));
        discard.add(new Card(Card.Type.METHOD, 5));

        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should have 4 cards after reshuffle and draw", 4, hand.size());
        assertEquals("Deck should have 1 card after reshuffle and draw", 1, deck.size());
        assertTrue("Discard should be empty after reshuffle", discard.isEmpty());
    }

    @Test
    public void testDrawCard_DrawsFromEndOfDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card firstCard = new Card(Card.Type.BITCOIN, 1);
        Card lastCard = new Card(Card.Type.METHOD, 2);
        deck.add(firstCard);
        deck.add(lastCard);

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertTrue("Should draw the last card in the deck", hand.contains(lastCard));
        assertFalse("Should not draw the first card", hand.contains(firstCard));
        assertEquals("First card should remain in deck", firstCard, deck.get(0));
    }

    // ========== Game Mechanics Tests - getNextPlayer ==========

    @Test
    public void testGetNextPlayer_TwoPlayers_CircularRotation() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        List<Player> players = getPrivateField(engine, "players");

        Player first = engine.getNextPlayer();
        assertEquals("First call should return first player", players.get(0), first);

        // Set activePlayer to first
        setPrivateField(engine, "activePlayer", first);

        Player second = engine.getNextPlayer();
        assertEquals("Second call should return second player", players.get(1), second);

        // Set activePlayer to second
        setPrivateField(engine, "activePlayer", second);

        Player third = engine.getNextPlayer();
        assertEquals("Third call should rotate back to first player", players.get(0), third);
    }

    @Test
    public void testGetNextPlayer_ThreePlayers_CircularRotation() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");

        Player first = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", first);

        Player second = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", second);

        Player third = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", third);

        Player fourth = engine.getNextPlayer();

        assertEquals("Should rotate back to first player after full rotation",
                players.get(0), fourth);
    }

    @Test
    public void testGetNextPlayer_FourPlayers_CircularRotation() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        List<Player> players = getPrivateField(engine, "players");

        // Rotate through all 4 players
        for (int i = 0; i < 4; i++) {
            Player current = engine.getNextPlayer();
            assertEquals("Should get player " + i, players.get(i), current);
            setPrivateField(engine, "activePlayer", current);
        }

        // Should rotate back to first
        Player fifth = engine.getNextPlayer();
        assertEquals("Should rotate back to first player", players.get(0), fifth);
    }

    @Test
    public void testGetNextPlayer_MultipleRotations_ContinuesCircling() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        List<Player> players = getPrivateField(engine, "players");

        // Test 10 rotations (5 full cycles)
        for (int i = 0; i < 10; i++) {
            Player current = engine.getNextPlayer();
            assertEquals("Rotation " + i + " should return correct player",
                    players.get(i % 2), current);
            setPrivateField(engine, "activePlayer", current);
        }
    }

    // ========== Additional Edge Case Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullPlayerInList_ThrowsException() {
        List<Player> playersWithNull = new ArrayList<>();
        playersWithNull.add(new BigMoneyPlayer());
        playersWithNull.add(null);
        new OurEngine(playersWithNull);
    }

    @Test
    public void testDrawCard_HandAlreadyHasCards_AddsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add initial cards to hand
        Card initialCard = new Card(Card.Type.BITCOIN, 100);
        hand.addToUnplayed(initialCard);

        // Add cards to deck
        Card deckCard = new Card(Card.Type.METHOD, 101);
        deck.add(deckCard);

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        assertEquals("Hand should have 2 cards after draw", 2, hand.size());
        assertTrue("Hand should still contain initial card", hand.contains(initialCard));
        assertTrue("Hand should contain newly drawn card", hand.contains(deckCard));
    }

    @Test
    public void testDrawCard_ReshufflePreservesAllCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add 5 cards to discard
        List<Card> originalCards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Card card = new Card(Card.Type.BITCOIN, i);
            discard.add(card);
            originalCards.add(card);
        }

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);
        drawCard.invoke(engine, testPlayer);

        // Verify all cards are accounted for
        List<Card> allCards = new ArrayList<>(deck);
        allCards.addAll(hand.getUnplayedCards());

        assertEquals("Total cards should be preserved", 5, allCards.size());
        assertTrue("Discard should be empty after reshuffle", discard.isEmpty());

        // Verify all original cards are still in the system
        for (Card original : originalCards) {
            boolean found = false;
            for (Card card : allCards) {
                if (card.id() == original.id()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Original card " + original.id() + " should be preserved", found);
        }
    }

    @Test
    public void testMutableCardStacks_GetNumAvailable_AfterModification_ReturnsUpdatedValue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Deplete BITCOIN from 60 to 25 by calling decrementCount 35 times
        for (int i = 0; i < 35; i++) {
            shop.decrementCount(Card.Type.BITCOIN);
        }

        assertEquals("Should return modified quantity", 25, shop.getNumAvailable(Card.Type.BITCOIN));
    }

    @Test
    public void testConstructor_PlayerListOrder_IsPreserved() throws Exception {
        List<Player> orderedPlayers = createPlayers(3);
        OurEngine engine = new OurEngine(orderedPlayers);
        List<Player> enginePlayers = getPrivateField(engine, "players");

        // Verify order is preserved
        for (int i = 0; i < orderedPlayers.size(); i++) {
            assertEquals("Player at position " + i + " should match",
                    orderedPlayers.get(i), enginePlayers.get(i));
        }
    }

    @Test
    public void testIsGameOver_MultipleCalls_ConsistentResult() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        boolean firstCall = engine.isGameOver();
        boolean secondCall = engine.isGameOver();

        assertEquals("Multiple calls should return same result", firstCall, secondCall);
    }

    @Test
    public void testMutableCardStacks_GetNumAvailable_AllCardTypes_ReturnCorrectInitialValues() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Verify all card types have correct initial quantities
        assertEquals(60, shop.getNumAvailable(Card.Type.BITCOIN));
        assertEquals(40, shop.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals(30, shop.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals(14, shop.getNumAvailable(Card.Type.METHOD));
        assertEquals(8, shop.getNumAvailable(Card.Type.MODULE));
        assertEquals(8, shop.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals(20, shop.getNumAvailable(Card.Type.BUG)); // 2 players * 10
        assertEquals(10, shop.getNumAvailable(Card.Type.REFACTOR));
        assertEquals(10, shop.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals(10, shop.getNumAvailable(Card.Type.EVERGREEN_TEST));
    }

    @Test
    public void testConstructor_CardIdCounter_GeneratesUniqueIdsAcrossPlayers() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        int cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Verify counter is incremented properly (should be at least 20 for 2 players *
        // 10 cards)
        assertTrue("Card ID counter should be incremented beyond initial cards",
                cardIdCounter > 20);

        // Verify the highest ID is one less than the counter
        int maxId = 0;
        for (Player p : twoPlayers) {
            for (Card card : hands.get(p).getUnplayedCards()) {
                maxId = Math.max(maxId, card.id());
            }
            for (Card card : decks.get(p)) {
                maxId = Math.max(maxId, card.id());
            }
        }
        assertEquals("Highest card ID should be one less than counter",
                cardIdCounter - 1, maxId);
    }

    @Test
    public void testDrawCard_MultipleSequentialDraws_MaintainsCorrectState() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new BigMoneyPlayer();
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add 10 cards to deck
        for (int i = 0; i < 10; i++) {
            deck.add(new Card(Card.Type.BITCOIN, i));
        }

        // Set up engine's internal state
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        hands.put(testPlayer, hand);
        decks.put(testPlayer, deck);
        discards.put(testPlayer, discard);

        // Call drawCard using reflection
        java.lang.reflect.Method drawCard = engine.getClass().getDeclaredMethod("drawCard", Player.class);
        drawCard.setAccessible(true);

        // Draw 5 cards
        for (int i = 0; i < 5; i++) {
            drawCard.invoke(engine, testPlayer);
        }

        assertEquals("Hand should have 5 cards", 5, hand.size());
        assertEquals("Deck should have 5 cards remaining", 5, deck.size());
        assertTrue("Discard should remain empty", discard.isEmpty());
    }

    // ========== Decision Building Tests - buildActionDecisions ==========

    @Test
    public void testBuildActionDecisions_NoActionsAvailable_OnlyEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numActions", 0);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions");

        assertEquals("Should have exactly 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision",
                decisions.get(0) instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);

        edu.brandeis.cosi.atg.decisions.EndPhaseDecision endDecision = (edu.brandeis.cosi.atg.decisions.EndPhaseDecision) decisions
                .get(0);
        assertEquals("Should end ACTION phase", GameState.TurnPhase.ACTION, endDecision.phase());
    }

    @Test
    public void testBuildActionDecisions_HasActionsButNoActionCards_OnlyEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear hand and add only money cards
        MutableHand hand = hands.get(activePlayer);
        hand.clear();
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, 1));
        hand.addToUnplayed(new Card(Card.Type.ETHEREUM, 2));

        setPrivateField(engine, "numActions", 1);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions");

        assertEquals("Should have exactly 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision",
                decisions.get(0) instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);
    }

    @Test
    public void testBuildActionDecisions_HasActionsAndActionCards_IncludesPlayCardDecisions() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        // Get the first actual player from the players list
        List<Player> players = getPrivateField(engine, "players");
        Player firstPlayer = players.get(0);
        setPrivateField(engine, "activePlayer", firstPlayer);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear hand and add action cards (REFACTOR and CODE_REVIEW are ACTION
        // category)
        MutableHand hand = hands.get(firstPlayer);
        hand.clear();
        Card refactor = new Card(Card.Type.REFACTOR, 1);
        Card codeReview = new Card(Card.Type.CODE_REVIEW, 2);
        hand.addToUnplayed(refactor);
        hand.addToUnplayed(codeReview);
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, 3)); // Add a money card too

        setPrivateField(engine, "numActions", 2);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions");

        // Should have 2 PlayCardDecisions + 1 EndPhaseDecision = 3
        assertEquals("Should have 3 decisions (2 play + 1 end)", 3, decisions.size());

        // Count decision types
        int playCount = 0;
        int endCount = 0;
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.PlayCardDecision) {
                playCount++;
            } else if (d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision) {
                endCount++;
            }
        }

        assertEquals("Should have 2 PlayCardDecisions", 2, playCount);
        assertEquals("Should have 1 EndPhaseDecision", 1, endCount);
    }

    @Test
    public void testBuildActionDecisions_AlwaysIncludesEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        // Get the first actual player from the players list
        List<Player> players = getPrivateField(engine, "players");
        Player firstPlayer = players.get(0);
        setPrivateField(engine, "activePlayer", firstPlayer);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Setup with multiple action cards (REFACTOR and CODE_REVIEW are ACTION
        // category)
        MutableHand hand = hands.get(firstPlayer);
        hand.clear();
        for (int i = 0; i < 3; i++) {
            hand.addToUnplayed(new Card(Card.Type.REFACTOR, i));
        }

        setPrivateField(engine, "numActions", 5);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions");

        // Verify EndPhaseDecision is present
        boolean hasEndPhase = decisions.stream()
                .anyMatch(d -> d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);
        assertTrue("Should always include EndPhaseDecision", hasEndPhase);
    }

    // ========== Decision Building Tests - buildMoneyDecisions ==========

    @Test
    public void testBuildMoneyDecisions_NoMoneyCards_OnlyEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear hand and add only action cards
        MutableHand hand = hands.get(activePlayer);
        hand.clear();
        hand.addToUnplayed(new Card(Card.Type.METHOD, 1));
        hand.addToUnplayed(new Card(Card.Type.MODULE, 2));

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions");

        assertEquals("Should have exactly 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision",
                decisions.get(0) instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);

        edu.brandeis.cosi.atg.decisions.EndPhaseDecision endDecision = (edu.brandeis.cosi.atg.decisions.EndPhaseDecision) decisions
                .get(0);
        assertEquals("Should end MONEY phase", GameState.TurnPhase.MONEY, endDecision.phase());
    }

    @Test
    public void testBuildMoneyDecisions_HasMoneyCards_IncludesPlayCardDecisions() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear hand and add money cards
        MutableHand hand = hands.get(activePlayer);
        hand.clear();
        Card bitcoin = new Card(Card.Type.BITCOIN, 1);
        Card ethereum = new Card(Card.Type.ETHEREUM, 2);
        Card dogecoin = new Card(Card.Type.DOGECOIN, 3);
        hand.addToUnplayed(bitcoin);
        hand.addToUnplayed(ethereum);
        hand.addToUnplayed(dogecoin);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions");

        // Should have 3 PlayCardDecisions + 1 EndPhaseDecision = 4
        assertEquals("Should have 4 decisions (3 play + 1 end)", 4, decisions.size());

        // Count decision types
        int playCount = 0;
        int endCount = 0;
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.PlayCardDecision) {
                playCount++;
            } else if (d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision) {
                endCount++;
            }
        }

        assertEquals("Should have 3 PlayCardDecisions", 3, playCount);
        assertEquals("Should have 1 EndPhaseDecision", 1, endCount);
    }

    @Test
    public void testBuildMoneyDecisions_MixedCards_OnlyIncludesMoneyCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Add mix of money and non-money cards
        MutableHand hand = hands.get(activePlayer);
        hand.clear();
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, 1));
        hand.addToUnplayed(new Card(Card.Type.METHOD, 2)); // Action, not money
        hand.addToUnplayed(new Card(Card.Type.ETHEREUM, 3));
        hand.addToUnplayed(new Card(Card.Type.BUG, 4)); // Victory, not money

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions");

        // Should have 2 PlayCardDecisions (for BITCOIN and ETHEREUM) + 1
        // EndPhaseDecision = 3
        assertEquals("Should have 3 decisions (2 play + 1 end)", 3, decisions.size());

        // Verify only money cards are included
        int playCount = 0;
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.PlayCardDecision) {
                playCount++;
                edu.brandeis.cosi.atg.decisions.PlayCardDecision playDecision = (edu.brandeis.cosi.atg.decisions.PlayCardDecision) d;
                assertEquals("PlayCard should be for money category",
                        Card.Type.Category.MONEY, playDecision.card().getCategory());
            }
        }
        assertEquals("Should have exactly 2 PlayCardDecisions", 2, playCount);
    }

    @Test
    public void testBuildMoneyDecisions_AlwaysIncludesEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player activePlayer = getPrivateField(engine, "activePlayer");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Add money cards
        MutableHand hand = hands.get(activePlayer);
        hand.clear();
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, 1));
        hand.addToUnplayed(new Card(Card.Type.ETHEREUM, 2));

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions");

        // Verify EndPhaseDecision is present
        boolean hasEndPhase = decisions.stream()
                .anyMatch(d -> d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);
        assertTrue("Should always include EndPhaseDecision", hasEndPhase);
    }

    // ========== Decision Building Tests - buildBuyDecisions ==========

    @Test
    public void testBuildBuyDecisions_NoBuysAvailable_OnlyEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 0);
        setPrivateField(engine, "numMoney", 10);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        assertEquals("Should have exactly 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision",
                decisions.get(0) instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);

        edu.brandeis.cosi.atg.decisions.EndPhaseDecision endDecision = (edu.brandeis.cosi.atg.decisions.EndPhaseDecision) decisions
                .get(0);
        assertEquals("Should end BUY phase", GameState.TurnPhase.BUY, endDecision.phase());
    }

    @Test
    public void testBuildBuyDecisions_HasBuysButNoMoney_OnlyEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 0);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // With 0 money, can still buy BITCOIN (cost 0), ETHEREUM (cost 3), and DOGECOIN
        // (cost 6) are too expensive
        // So expect: BuyDecision for BITCOIN + EndPhaseDecision
        assertTrue("Should have at least EndPhaseDecision", decisions.size() >= 1);

        // Verify BITCOIN is affordable and included
        boolean hasBitcoin = false;
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
                edu.brandeis.cosi.atg.decisions.BuyDecision buyDecision = (edu.brandeis.cosi.atg.decisions.BuyDecision) d;
                assertTrue("All buy options should be affordable with 0 money",
                        buyDecision.cardType().cost() <= 0);
                if (buyDecision.cardType() == Card.Type.BITCOIN) {
                    hasBitcoin = true;
                }
            }
        }
        assertTrue("Should include BITCOIN (cost 0) when numMoney is 0", hasBitcoin);
    }

    @Test
    public void testBuildBuyDecisions_HasBuysAndMoney_IncludesAffordableCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 3); // Can afford cards costing 0-3

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Verify we have BuyDecisions for affordable cards
        boolean hasBuyDecisions = decisions.stream()
                .anyMatch(d -> d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision);
        assertTrue("Should include BuyDecisions for affordable cards", hasBuyDecisions);

        // Verify all BuyDecisions are for cards we can afford
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
                edu.brandeis.cosi.atg.decisions.BuyDecision buyDecision = (edu.brandeis.cosi.atg.decisions.BuyDecision) d;
                assertTrue("Card should be affordable", buyDecision.cardType().cost() <= 3);
            }
        }
    }

    @Test
    public void testBuildBuyDecisions_OnlyIncludesCardsInStock() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Deplete BITCOIN stock completely (from 60 to 0)
        for (int i = 0; i < 60; i++) {
            shop.decrementCount(Card.Type.BITCOIN);
        }

        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 10); // Can afford anything

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Verify no BuyDecision for BITCOIN
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
                edu.brandeis.cosi.atg.decisions.BuyDecision buyDecision = (edu.brandeis.cosi.atg.decisions.BuyDecision) d;
                assertNotEquals("Should not include out-of-stock BITCOIN",
                        Card.Type.BITCOIN, buyDecision.cardType());
            }
        }
    }

    @Test
    public void testBuildBuyDecisions_ExcludesExpensiveCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 2); // Can only afford cards costing 0-2

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Verify no BuyDecision for expensive cards
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
                edu.brandeis.cosi.atg.decisions.BuyDecision buyDecision = (edu.brandeis.cosi.atg.decisions.BuyDecision) d;
                assertTrue("Should not include cards more expensive than available money",
                        buyDecision.cardType().cost() <= 2);
            }
        }
    }

    @Test
    public void testBuildBuyDecisions_AlwaysIncludesEndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 2);
        setPrivateField(engine, "numMoney", 5);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Verify EndPhaseDecision is present
        boolean hasEndPhase = decisions.stream()
                .anyMatch(d -> d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);
        assertTrue("Should always include EndPhaseDecision", hasEndPhase);
    }

    @Test
    public void testBuildBuyDecisions_WithZeroMoney_OnlyEndPhase() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 0);

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Can only afford cards with cost 0, but those might not exist or be in stock
        // At minimum should have EndPhaseDecision
        assertTrue("Should have at least 1 decision (EndPhase)", decisions.size() >= 1);

        boolean hasEndPhase = decisions.stream()
                .anyMatch(d -> d instanceof edu.brandeis.cosi.atg.decisions.EndPhaseDecision);
        assertTrue("Should include EndPhaseDecision", hasEndPhase);
    }

    @Test
    public void testBuildBuyDecisions_VerifyBitcoinInDecisionsWhenAffordable() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "numBuys", 1);
        setPrivateField(engine, "numMoney", 1); // BITCOIN costs 0, so affordable

        List<edu.brandeis.cosi.atg.decisions.Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions");

        // Verify BITCOIN is included (cost 0)
        boolean hasBitcoin = false;
        for (edu.brandeis.cosi.atg.decisions.Decision d : decisions) {
            if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
                edu.brandeis.cosi.atg.decisions.BuyDecision buyDecision = (edu.brandeis.cosi.atg.decisions.BuyDecision) d;
                if (buyDecision.cardType() == Card.Type.BITCOIN) {
                    hasBitcoin = true;
                    break;
                }
            }
        }
        assertTrue("Should include BITCOIN when affordable", hasBitcoin);
    }

    // ========== Helper Method for Reflection ==========

    /**
     * Invokes a private method using reflection.
     */
    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(obj, args);
    }

    // ========== processTrashCardDecision Tests ==========

    @Test
    public void testProcessTrashCardDecision_CardExistsInHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        // Get the hand and add a specific card
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);
        Card cardToTrash = new Card(Card.Type.BITCOIN, 1001);
        hand.addToUnplayed(cardToTrash);
        int initialSize = hand.size();

        // Create and process decision
        TrashCardDecision decision = new TrashCardDecision(cardToTrash);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify card was removed
        assertEquals("Hand size should decrease by 1", initialSize - 1, hand.size());
        assertFalse("Card should not be in unplayed cards", hand.getUnplayedCards().contains(cardToTrash));
    }

    @Test
    public void testProcessTrashCardDecision_MultipleCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);

        // Add specific cards
        Card card1 = new Card(Card.Type.BITCOIN, 2001);
        Card card2 = new Card(Card.Type.ETHEREUM, 2002);
        Card card3 = new Card(Card.Type.METHOD, 2003);
        hand.addToUnplayed(card1);
        hand.addToUnplayed(card2);
        hand.addToUnplayed(card3);

        // Trash the middle card
        TrashCardDecision decision = new TrashCardDecision(card2);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify only card2 was removed
        assertTrue("Card1 should still be in hand", hand.getUnplayedCards().contains(card1));
        assertFalse("Card2 should not be in hand", hand.getUnplayedCards().contains(card2));
        assertTrue("Card3 should still be in hand", hand.getUnplayedCards().contains(card3));
    }

    @Test
    public void testProcessTrashCardDecision_LastCardInHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand(); // Start with empty hand
        hands.put(player, hand);

        Card onlyCard = new Card(Card.Type.BITCOIN, 3001);
        hand.addToUnplayed(onlyCard);

        // Trash the only card
        TrashCardDecision decision = new TrashCardDecision(onlyCard);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify hand is now empty
        assertEquals("Hand should be empty", 0, hand.size());
        assertTrue("Unplayed cards should be empty", hand.getUnplayedCards().isEmpty());
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessTrashCardDecision_CardNotInHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);

        // Create a card that's not in the hand
        Card cardNotInHand = new Card(Card.Type.BITCOIN, 4001);

        // Attempt to trash it - should throw IllegalStateException
        TrashCardDecision decision = new TrashCardDecision(cardNotInHand);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Verify it's the right exception
            assertTrue("Should throw IllegalStateException", e.getCause() instanceof IllegalStateException);
            assertTrue("Error message should mention card not found",
                    e.getCause().getMessage().contains("not found"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessTrashCardDecision_CardInPlayedNotUnplayed() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = hands.get(player);

        // Add card to unplayed, then play it
        Card card = new Card(Card.Type.CODE_REVIEW, 5001);
        hand.addToUnplayed(card);
        hand.playCard(card); // Moves to played cards

        // Attempt to trash it - should fail since it's in played, not unplayed
        TrashCardDecision decision = new TrashCardDecision(card);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalStateException", e.getCause() instanceof IllegalStateException);
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessTrashCardDecision_EmptyHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        MutableHand hand = new MutableHand(); // Empty hand
        hands.put(player, hand);

        Card card = new Card(Card.Type.BITCOIN, 6001);

        // Attempt to trash from empty hand
        TrashCardDecision decision = new TrashCardDecision(card);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalStateException", e.getCause() instanceof IllegalStateException);
            throw e;
        }
    }

    @Test
    public void testProcessTrashCardDecision_CardIdVsType() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);
        setPrivateField(engine, "activePlayer", player);

        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        // Replace with empty hand for controlled testing
        MutableHand hand = new MutableHand();
        hands.put(player, hand);

        // Add two BITCOIN cards with different IDs
        Card bitcoin1 = new Card(Card.Type.BITCOIN, 7001);
        Card bitcoin2 = new Card(Card.Type.BITCOIN, 7002);
        hand.addToUnplayed(bitcoin1);
        hand.addToUnplayed(bitcoin2);

        // Trash bitcoin1 specifically
        TrashCardDecision decision = new TrashCardDecision(bitcoin1);
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processTrashCardDecision",
                TrashCardDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        // Verify only bitcoin1 was removed, bitcoin2 remains
        assertFalse("Bitcoin1 should be removed", hand.getUnplayedCards().contains(bitcoin1));
        assertTrue("Bitcoin2 should remain", hand.getUnplayedCards().contains(bitcoin2));
        assertEquals("Should have exactly 1 card left", 1, hand.getUnplayedCards().size());
    }

    // ========== processGainingCard Tests ==========

    @Test
    public void testProcessGainingCard_NormalGain() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        MutableCardStacks shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int cardIdCounter = getPrivateField(engine, "cardIdCounter");

        int initialShopCount = shop.getNumAvailable(Card.Type.ETHEREUM);
        int initialDiscardSize = discards.get(player).size();

        // Gain an ETHEREUM card
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.ETHEREUM, player);

        // Verify shop count decremented
        assertEquals("Shop count should decrease by 1", initialShopCount - 1, shop.getNumAvailable(Card.Type.ETHEREUM));

        // Verify card added to discard
        assertEquals("Discard size should increase by 1", initialDiscardSize + 1, discards.get(player).size());

        // Verify card is ETHEREUM and has unique ID
        Card gainedCard = discards.get(player).get(discards.get(player).size() - 1);
        assertEquals("Gained card should be ETHEREUM", Card.Type.ETHEREUM, gainedCard.type());
        assertEquals("Card should have correct ID", cardIdCounter, gainedCard.id());

        // Verify cardIdCounter was incremented
        int newCardIdCounter = getPrivateField(engine, "cardIdCounter");
        assertEquals("Card ID counter should increment", cardIdCounter + 1, newCardIdCounter);
    }

    @Test
    public void testProcessGainingCard_LastCardOfType() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        MutableCardStacks shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Deplete REFACTOR to have only 1 card (starts at 10)
        for (int i = 0; i < 9; i++) {
            shop.decrementCount(Card.Type.REFACTOR);
        }

        // Gain the last REFACTOR
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.REFACTOR, player);

        // Verify shop count is now 0
        assertEquals("Shop count should be 0", 0, shop.getNumAvailable(Card.Type.REFACTOR));

        // Verify card was still added to discard
        boolean foundRefactor = false;
        for (Card card : discards.get(player)) {
            if (card.type() == Card.Type.REFACTOR) {
                foundRefactor = true;
                break;
            }
        }
        assertTrue("REFACTOR should be in discard", foundRefactor);
    }

    @Test
    public void testProcessGainingCard_MultipleGainsSameType() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscardSize = discards.get(player).size();

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);

        // Gain 3 BITCOIN cards
        method.invoke(engine, Card.Type.BITCOIN, player);
        method.invoke(engine, Card.Type.BITCOIN, player);
        method.invoke(engine, Card.Type.BITCOIN, player);

        // Verify 3 cards added
        assertEquals("Three cards should be added", initialDiscardSize + 3, discards.get(player).size());

        // Verify all have unique IDs
        List<Integer> ids = new ArrayList<>();
        int bitcoinCount = 0;
        for (Card card : discards.get(player)) {
            if (card.type() == Card.Type.BITCOIN) {
                ids.add(card.id());
                bitcoinCount++;
            }
        }

        // Check that we have unique IDs for the gained BITCOINs (may have started with
        // some)
        long uniqueIdCount = ids.stream().distinct().count();
        assertEquals("All BITCOIN cards should have unique IDs", ids.size(), uniqueIdCount);
    }

    @Test
    public void testProcessGainingCard_DifferentPlayers() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player1 = twoPlayers.get(0);
        Player player2 = twoPlayers.get(1);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscard1 = discards.get(player1).size();
        int initialDiscard2 = discards.get(player2).size();

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);

        // Player 1 gains METHOD, Player 2 gains MODULE
        method.invoke(engine, Card.Type.METHOD, player1);
        method.invoke(engine, Card.Type.MODULE, player2);

        // Verify correct discard piles were modified
        assertEquals("Player 1 discard should increase by 1", initialDiscard1 + 1, discards.get(player1).size());
        assertEquals("Player 2 discard should increase by 1", initialDiscard2 + 1, discards.get(player2).size());

        // Verify correct card types
        Card player1LastCard = discards.get(player1).get(discards.get(player1).size() - 1);
        Card player2LastCard = discards.get(player2).get(discards.get(player2).size() - 1);
        assertEquals("Player 1 should gain METHOD", Card.Type.METHOD, player1LastCard.type());
        assertEquals("Player 2 should gain MODULE", Card.Type.MODULE, player2LastCard.type());
    }

    @Test
    public void testProcessGainingCard_ShopEmpty() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        MutableCardStacks shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Deplete all CODE_REVIEW cards (starts at 10)
        for (int i = 0; i < 10; i++) {
            shop.decrementCount(Card.Type.CODE_REVIEW);
        }
        int initialDiscardSize = discards.get(player).size();

        // Attempt to gain CODE_REVIEW (should be silent no-op)
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.CODE_REVIEW, player);

        // Verify nothing changed
        assertEquals("Shop count should remain 0", 0, shop.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("Discard should not change", initialDiscardSize, discards.get(player).size());
    }

    @Test
    public void testProcessGainingCard_CardTypeNotInShop() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        MutableCardStacks shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Deplete all FRAMEWORK cards (starts at 8 for 2 players)
        for (int i = 0; i < 8; i++) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }
        int initialDiscardSize = discards.get(player).size();

        // Attempt to gain FRAMEWORK (should be silent no-op)
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.FRAMEWORK, player);

        // Verify discard didn't change
        assertEquals("Discard should not change", initialDiscardSize, discards.get(player).size());
    }

    @Test
    public void testProcessGainingCard_AllCardTypes() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        Player player = fourPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int initialDiscardSize = discards.get(player).size();

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);

        // Test all card types
        Card.Type[] allTypes = {
                Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN,
                Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
                Card.Type.BUG, Card.Type.CODE_REVIEW, Card.Type.REFACTOR, Card.Type.EVERGREEN_TEST
        };

        for (Card.Type type : allTypes) {
            method.invoke(engine, type, player);
        }

        // Verify 10 cards were gained (one of each type)
        assertEquals("Should gain 10 cards", initialDiscardSize + 10, discards.get(player).size());
    }

    // ========== executeCleanupPhase Tests ==========

    @Test
    public void testExecuteCleanupPhase_DiscardsAllCardsAndDrawsFive() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = twoPlayers.get(0);

        // Set up engine state
        setPrivateField(engine, "activePlayer", testPlayer);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        MutableHand hand = hands.get(testPlayer);
        List<Card> deck = decks.get(testPlayer);
        List<Card> discard = discards.get(testPlayer);

        // Capture initial state
        int initialHandSize = hand.size();
        int initialDeckSize = deck.size();
        int initialDiscardSize = discard.size();
        List<Card> cardsInHandBefore = new ArrayList<>(hand.getUnplayedCards());

        // Invoke executeCleanupPhase using reflection
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("executeCleanupPhase");
        method.setAccessible(true);
        method.invoke(engine);

        // Verify all cards were discarded and new hand drawn
        assertEquals("Hand should have exactly 5 cards after cleanup", 5, hand.size());
        assertEquals("Discard should contain previous hand cards",
                initialDiscardSize + initialHandSize, discard.size());
        assertEquals("Deck should have 5 fewer cards",
                initialDeckSize - 5, deck.size());

        // Verify the old cards are in discard
        for (Card card : cardsInHandBefore) {
            assertTrue("Old hand card should be in discard pile", discard.contains(card));
        }

        // Verify the new cards are from the deck (not the old hand)
        List<Card> cardsInHandAfter = hand.getUnplayedCards();
        for (Card card : cardsInHandAfter) {
            assertFalse("New hand should not contain old cards", cardsInHandBefore.contains(card));
        }
    }

    @Test
    public void testExecuteCleanupPhase_WithEmptyDeck_ShufflesDiscardIntoDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = twoPlayers.get(0);

        setPrivateField(engine, "activePlayer", testPlayer);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        MutableHand hand = hands.get(testPlayer);
        List<Card> deck = decks.get(testPlayer);
        List<Card> discard = discards.get(testPlayer);

        // Clear deck and put many cards in discard to simulate mid-game state
        Card extraCard1 = new Card(Card.Type.METHOD, 9001);
        Card extraCard2 = new Card(Card.Type.BITCOIN, 9002);
        Card extraCard3 = new Card(Card.Type.ETHEREUM, 9003);
        discard.addAll(deck);
        deck.clear();
        discard.add(extraCard1);
        discard.add(extraCard2);
        discard.add(extraCard3);

        int totalCardsBeforeCleanup = hand.size() + discard.size();

        // Invoke executeCleanupPhase
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("executeCleanupPhase");
        method.setAccessible(true);
        method.invoke(engine);

        // Verify hand has 5 cards
        assertEquals("Hand should have exactly 5 cards", 5, hand.size());

        // Verify total cards preserved (some in hand, rest in deck or discard)
        int totalCardsAfterCleanup = hand.size() + deck.size() + discard.size();
        assertEquals("Total cards should be preserved", totalCardsBeforeCleanup, totalCardsAfterCleanup);
    }

    @Test
    public void testExecuteCleanupPhase_WithPlayedCards_DiscardsAll() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = twoPlayers.get(0);

        setPrivateField(engine, "activePlayer", testPlayer);
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        MutableHand hand = hands.get(testPlayer);
        List<Card> discard = discards.get(testPlayer);

        // Simulate playing some cards during the turn
        List<Card> unplayedCards = new ArrayList<>(hand.getUnplayedCards());
        if (!unplayedCards.isEmpty()) {
            Card cardToPlay = unplayedCards.get(0);
            hand.playCard(cardToPlay);
        }

        int totalCardsInHand = hand.size(); // Both played and unplayed
        int initialDiscardSize = discard.size();

        // Invoke executeCleanupPhase
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("executeCleanupPhase");
        method.setAccessible(true);
        method.invoke(engine);

        // Verify all cards (played and unplayed) were discarded
        assertEquals("All cards should be discarded including played ones",
                initialDiscardSize + totalCardsInHand, discard.size());
        assertEquals("Hand should have exactly 5 new cards", 5, hand.size());
    }

    // ========== processEndPhaseDecision Tests ==========

    @Test
    public void testProcessEndPhaseDecision_ActionToMoney_TransitionsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from ACTION to MONEY",
                GameState.TurnPhase.MONEY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_MoneyToBuy_TransitionsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from MONEY to BUY",
                GameState.TurnPhase.BUY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_BuyToCleanup_TransitionsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.BUY);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.BUY);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);
        method.invoke(engine, decision);

        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from BUY to CLEANUP",
                GameState.TurnPhase.CLEANUP, newPhase);
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessEndPhaseDecision_PhaseMismatch_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);

        // Try to end MONEY phase while in ACTION phase
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw IllegalArgumentException for phase mismatch");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalArgumentException",
                    e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention phase mismatch",
                    e.getCause().getMessage().contains("does not match"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessEndPhaseDecision_CleanupPhase_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.CLEANUP);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.CLEANUP);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw UnsupportedOperationException for CLEANUP phase");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw UnsupportedOperationException",
                    e.getCause() instanceof UnsupportedOperationException);
            assertTrue("Error message should mention cannot end phase",
                    e.getCause().getMessage().contains("Cannot end phase"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessEndPhaseDecision_GainPhase_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.GAIN);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.GAIN);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processEndPhaseDecision", EndPhaseDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw UnsupportedOperationException for GAIN phase");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw UnsupportedOperationException",
                    e.getCause() instanceof UnsupportedOperationException);
            throw e;
        }
    }

    // ========== processBuyDecision Tests ==========

    // Note: The valid purchase test is commented out because it relies on
    // processGainingCard being fully implemented to verify the shop and discard
    // updates. Once processGainingCard is complete, we can uncomment this test and
    // verify those state changes as well.

    // @Test
    // public void testProcessBuyDecision_ValidPurchase_UpdatesStateCorrectly()
    // throws Exception {
    // OurEngine engine = new OurEngine(twoPlayers);
    // Player testPlayer = twoPlayers.get(0);

    // setPrivateField(engine, "activePlayer", testPlayer);
    // setPrivateField(engine, "numMoney", 5);
    // setPrivateField(engine, "numBuys", 1);

    // Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
    // Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");

    // int initialDiscardSize = discards.get(testPlayer).size();
    // int initialShopQuantity = shop.get(Card.Type.MODULE);
    // int initialMoney = 5;
    // int initialBuys = 1;

    // // MODULE costs 4
    // BuyDecision decision = new BuyDecision(Card.Type.MODULE);

    // java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
    // "processBuyDecision", BuyDecision.class);
    // method.setAccessible(true);
    // method.invoke(engine, decision);

    // // Verify state updates
    // int newMoney = getPrivateField(engine, "numMoney");
    // int newBuys = getPrivateField(engine, "numBuys");

    // assertEquals("Money should be reduced by card cost",
    // initialMoney - Card.Type.MODULE.cost(), newMoney);
    // assertEquals("Buys should be decremented by 1", initialBuys - 1, newBuys);

    // // Note: Can't verify shop/discard directly since processGainingCard is not
    // yet
    // // implemented
    // // These assertions would fail until processGainingCard is complete
    // // assertEquals("Shop quantity should decrease", initialShopQuantity - 1,
    // (int)
    // // shop.get(Card.Type.MODULE));
    // // assertEquals("Card should be added to discard", initialDiscardSize + 1,
    // // discards.get(testPlayer).size());
    // }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessBuyDecision_InsufficientMoney_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        setPrivateField(engine, "activePlayer", twoPlayers.get(0));
        setPrivateField(engine, "numMoney", 2); // Not enough for MODULE (costs 5)
        setPrivateField(engine, "numBuys", 1);

        BuyDecision decision = new BuyDecision(Card.Type.MODULE);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processBuyDecision", BuyDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw IllegalStateException for insufficient money");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalStateException",
                    e.getCause() instanceof IllegalStateException);
            assertTrue("Error message should mention not enough money",
                    e.getCause().getMessage().toLowerCase().contains("not enough money"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessBuyDecision_NoBuysRemaining_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        setPrivateField(engine, "activePlayer", twoPlayers.get(0));
        setPrivateField(engine, "numMoney", 10);
        setPrivateField(engine, "numBuys", 0); // No buys remaining

        BuyDecision decision = new BuyDecision(Card.Type.BITCOIN);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processBuyDecision", BuyDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw IllegalStateException for no buys remaining");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalStateException",
                    e.getCause() instanceof IllegalStateException);
            assertTrue("Error message should mention no buys",
                    e.getCause().getMessage().toLowerCase().contains("no buys"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testProcessBuyDecision_CardOutOfStock_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        setPrivateField(engine, "activePlayer", twoPlayers.get(0));
        setPrivateField(engine, "numMoney", 10);
        setPrivateField(engine, "numBuys", 1);

        // Set MODULE to out of stock
        MutableCardStacks shop = getPrivateField(engine, "shop");
        for (int i = 0; i < 8; i++) {
            shop.decrementCount(Card.Type.MODULE);
        }

        BuyDecision decision = new BuyDecision(Card.Type.MODULE);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
                "processBuyDecision", BuyDecision.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, decision);
            fail("Should throw IllegalStateException for out of stock card");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Should throw IllegalStateException",
                    e.getCause() instanceof IllegalStateException);
            assertTrue("Error message should mention out of stock",
                    e.getCause().getMessage().toLowerCase().contains("out of stock"));
            throw e;
        }
    }

    // NOTE: The following test is commented out because it relies on
    // processGainingCard being fully implemented to verify the shop and discard
    // updates. Once processGainingCard is complete, we can uncomment this test and
    // verify those state changes as well.

    // @Test
    // public void testProcessBuyDecision_BuyingMultipleCards_DecrementsCorrectly()
    // throws Exception {
    // OurEngine engine = new OurEngine(twoPlayers);

    // setPrivateField(engine, "activePlayer", twoPlayers.get(0));
    // setPrivateField(engine, "numMoney", 10);
    // setPrivateField(engine, "numBuys", 3);

    // // First purchase: BITCOIN (costs 0)
    // BuyDecision decision1 = new BuyDecision(Card.Type.BITCOIN);
    // java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
    // "processBuyDecision", BuyDecision.class);
    // method.setAccessible(true);
    // method.invoke(engine, decision1);

    // int money1 = getPrivateField(engine, "numMoney");
    // int buys1 = getPrivateField(engine, "numBuys");
    // assertEquals("Money should be 10 after buying BITCOIN", 10, money1);
    // assertEquals("Buys should be 2 after first purchase", 2, buys1);

    // // Second purchase: ETHEREUM (costs 3)
    // BuyDecision decision2 = new BuyDecision(Card.Type.ETHEREUM);
    // method.invoke(engine, decision2);

    // int money2 = getPrivateField(engine, "numMoney");
    // int buys2 = getPrivateField(engine, "numBuys");
    // assertEquals("Money should be 7 after buying ETHEREUM", 7, money2);
    // assertEquals("Buys should be 1 after second purchase", 1, buys2);
    // }

    // @Test
    // public void testProcessBuyDecision_ExactMoney_SucceedsWithZeroRemaining()
    // throws Exception {
    // OurEngine engine = new OurEngine(twoPlayers);

    // setPrivateField(engine, "activePlayer", twoPlayers.get(0));
    // setPrivateField(engine, "numMoney", 5); // Exactly MODULE cost
    // setPrivateField(engine, "numBuys", 1);

    // BuyDecision decision = new BuyDecision(Card.Type.MODULE);

    // java.lang.reflect.Method method = engine.getClass().getDeclaredMethod(
    // "processBuyDecision", BuyDecision.class);
    // method.setAccessible(true);
    // method.invoke(engine, decision);

    // int newMoney = getPrivateField(engine, "numMoney");
    // assertEquals("Should have 0 money after exact purchase", 0, newMoney);
    // }

    // ========== makeValidDecision Tests ==========

    /**
     * Mock Player that returns a predetermined decision
     */
    private static class MockPlayer implements Player {
        private Decision decisionToReturn;

        public MockPlayer(Decision decision) {
            this.decisionToReturn = decision;
        }

        @Override
        public Decision makeDecision(GameState state, ImmutableList<Decision> validDecisions) {
            return decisionToReturn;
        }

        @Override
        public String getName() {
            return "MockPlayer";
        }
    }

    // Uncomment after implementation of validateDecision

    @Test
    public void testMakeValidDecision_ValidSingleDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        EndPhaseDecision validDecision = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> validDecisions = ImmutableList.of(validDecision);

        MockPlayer mockPlayer = new MockPlayer(validDecision);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);
        Decision result = (Decision) method.invoke(engine, mockPlayer,
                validDecisions);

        assertEquals("Should return the valid decision", validDecision, result);
    }

    @Test
    public void testMakeValidDecision_ValidFromMultiple() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        Card card1 = new Card(Card.Type.BITCOIN, 10001);
        Card card2 = new Card(Card.Type.ETHEREUM, 10002);
        Card card3 = new Card(Card.Type.METHOD, 10003);

        PlayCardDecision decision1 = new PlayCardDecision(card1);
        PlayCardDecision decision2 = new PlayCardDecision(card2);
        PlayCardDecision decision3 = new PlayCardDecision(card3);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> validDecisions = ImmutableList.of(decision1,
                decision2, decision3, endPhase);

        // Mock player chooses decision2
        MockPlayer mockPlayer = new MockPlayer(decision2);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);
        Decision result = (Decision) method.invoke(engine, mockPlayer,
                validDecisions);

        assertEquals("Should return the selected decision", decision2, result);
    }

    @Test
    public void testMakeValidDecision_EndPhaseDecision() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        Card card = new Card(Card.Type.BITCOIN, 11001);
        PlayCardDecision decision = new PlayCardDecision(card);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> validDecisions = ImmutableList.of(decision,
                endPhase);

        // Mock player chooses to end phase
        MockPlayer mockPlayer = new MockPlayer(endPhase);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);
        Decision result = (Decision) method.invoke(engine, mockPlayer,
                validDecisions);

        assertEquals("Should return EndPhaseDecision", endPhase, result);
    }

    @Test
    public void testMakeValidDecision_InvalidDecision_ThrowsPlayerViolationException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        Card validCard = new Card(Card.Type.BITCOIN, 12001);
        Card invalidCard = new Card(Card.Type.ETHEREUM, 12002);

        PlayCardDecision validDecision = new PlayCardDecision(validCard);
        PlayCardDecision invalidDecision = new PlayCardDecision(invalidCard);

        ImmutableList<Decision> validDecisions = ImmutableList.of(validDecision);

        // Mock player returns invalid decision
        MockPlayer mockPlayer = new MockPlayer(invalidDecision);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, mockPlayer, validDecisions);
            fail("Should throw PlayerViolationException for invalid decision");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Should throw PlayerViolationException from makeValidDecision
            assertTrue("Should throw PlayerViolationException",
                    e.getCause() instanceof edu.brandeis.cosi.atg.engine.PlayerViolationException);
            assertTrue("Error message should mention invalid decision",
                    e.getCause().getMessage().toLowerCase().contains("invalid"));
        }
    }

    @Test
    public void testMakeValidDecision_NullDecision_ThrowsPlayerViolationException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        Card card = new Card(Card.Type.BITCOIN, 13001);
        PlayCardDecision validDecision = new PlayCardDecision(card);
        ImmutableList<Decision> validDecisions = ImmutableList.of(validDecision);

        // Mock player returns null
        MockPlayer mockPlayer = new MockPlayer(null);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);

        try {
            method.invoke(engine, mockPlayer, validDecisions);
            fail("Should throw PlayerViolationException for null decision");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // null is not in validDecisions, so should throw PlayerViolationException
            assertTrue("Should throw PlayerViolationException",
                    e.getCause() instanceof edu.brandeis.cosi.atg.engine.PlayerViolationException);
        }
    }

    @Test
    public void testMakeValidDecision_EmptyValidList() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        ImmutableList<Decision> emptyDecisions = ImmutableList.of();

        // Mock player returns null (can't make valid decision from empty list)
        MockPlayer mockPlayer = new MockPlayer(null);

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("makeValidDecision", Player.class,
                ImmutableList.class);
        method.setAccessible(true);

        // This edge case documents current behavior - likely triggers handleCheating
        try {
            method.invoke(engine, mockPlayer, emptyDecisions);
            fail("Should throw exception when valid decisions list is empty");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Expected - documents that empty list triggers exception
            assertTrue("Should throw some exception", e.getCause() != null);
        }
    }

    // ========== Player Disqualification Tests ==========

    @Test
    public void testConstructor_InitializesDisqualifiedPlayersMap() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        assertNotNull("disqualifiedPlayers should be initialized", disqualifiedPlayers);
        assertEquals("Should have entries for all players", 2, disqualifiedPlayers.size());

        for (Player p : twoPlayers) {
            assertTrue("Should have entry for player", disqualifiedPlayers.containsKey(p));
            assertFalse("All players should start non-disqualified", disqualifiedPlayers.get(p));
        }
    }

    @Test
    public void testHandleCheating_MarksPlayerAsDisqualified() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player cheater = twoPlayers.get(0);

        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");
        assertFalse("Player should start non-disqualified", disqualifiedPlayers.get(cheater));

        // Call handleCheating using reflection
        java.lang.reflect.Method handleCheating = engine.getClass()
                .getDeclaredMethod("handleCheating", Player.class,
                        edu.brandeis.cosi.atg.engine.PlayerViolationException.class);
        handleCheating.setAccessible(true);
        handleCheating.invoke(engine, cheater, new edu.brandeis.cosi.atg.engine.PlayerViolationException("test"));

        assertTrue("Player should be disqualified after cheating", disqualifiedPlayers.get(cheater));
    }

    @Test
    public void testHandleCheating_DoesNotAffectOtherPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Player cheater = threePlayers.get(1);
        Player innocent1 = threePlayers.get(0);
        Player innocent2 = threePlayers.get(2);

        // Call handleCheating using reflection
        java.lang.reflect.Method handleCheating = engine.getClass()
                .getDeclaredMethod("handleCheating", Player.class,
                        edu.brandeis.cosi.atg.engine.PlayerViolationException.class);
        handleCheating.setAccessible(true);
        handleCheating.invoke(engine, cheater, new edu.brandeis.cosi.atg.engine.PlayerViolationException("test"));

        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");
        assertTrue("Cheater should be disqualified", disqualifiedPlayers.get(cheater));
        assertFalse("Other player 1 should not be disqualified", disqualifiedPlayers.get(innocent1));
        assertFalse("Other player 2 should not be disqualified", disqualifiedPlayers.get(innocent2));
    }

    @Test
    public void testGetNextPlayer_SkipsDisqualifiedPlayer() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify player at index 1
        disqualifiedPlayers.put(players.get(1), true);

        // Set active player to player 0
        setPrivateField(engine, "activePlayer", players.get(0));

        Player next = engine.getNextPlayer();
        assertEquals("Should skip disqualified player 1 and return player 2", players.get(2), next);
    }

    @Test
    public void testGetNextPlayer_SkipsMultipleDisqualifiedPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify players at indices 1 and 2
        disqualifiedPlayers.put(players.get(1), true);
        disqualifiedPlayers.put(players.get(2), true);

        // Set active player to player 0
        setPrivateField(engine, "activePlayer", players.get(0));

        Player next = engine.getNextPlayer();
        assertEquals("Should skip both disqualified players and return player 3", players.get(3), next);
    }

    @Test
    public void testGetNextPlayer_WrapsAroundAndSkipsDisqualified() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify player at index 0
        disqualifiedPlayers.put(players.get(0), true);

        // Set active player to player 2 (last player)
        setPrivateField(engine, "activePlayer", players.get(2));

        Player next = engine.getNextPlayer();
        assertEquals("Should wrap around and skip disqualified player 0, returning player 1",
                players.get(1), next);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNextPlayer_AllButOneDisqualified_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify all but one player
        disqualifiedPlayers.put(players.get(0), true);
        disqualifiedPlayers.put(players.get(1), true);

        // Set active player to the only non-disqualified player
        setPrivateField(engine, "activePlayer", players.get(2));

        // This should throw IllegalStateException
        engine.getNextPlayer();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNextPlayer_AllDisqualified_ThrowsException() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify all players
        disqualifiedPlayers.put(players.get(0), true);
        disqualifiedPlayers.put(players.get(1), true);

        setPrivateField(engine, "activePlayer", players.get(0));

        // This should throw IllegalStateException
        engine.getNextPlayer();
    }

    @Test
    public void testIsGameOver_AllButOneDisqualified_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify all but one player
        disqualifiedPlayers.put(players.get(0), true);
        disqualifiedPlayers.put(players.get(1), true);

        assertTrue("Game should be over when all but one player is disqualified", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_AllDisqualified_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify all players
        disqualifiedPlayers.put(players.get(0), true);
        disqualifiedPlayers.put(players.get(1), true);

        assertTrue("Game should be over when all players are disqualified", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_NoPlayersDisqualified_ReturnsFalse() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        assertFalse("Game should not be over when no players are disqualified", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_OnePlayerDisqualified_ReturnsFalse() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");

        // Disqualify one player (leaving two active)
        disqualifiedPlayers.put(players.get(0), true);

        assertFalse("Game should not be over when two or more players remain", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_StillTrueWhenBothConditionsActive() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<Player> players = getPrivateField(engine, "players");
        Map<Player, Boolean> disqualifiedPlayers = getPrivateField(engine, "disqualifiedPlayers");
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Disqualify one player (game should continue with 2 players)
        disqualifiedPlayers.put(players.get(0), true);

        // But sell out FRAMEWORK
        while (shop.getNumAvailable(Card.Type.FRAMEWORK) > 0) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        assertTrue("Game should be over when FRAMEWORK is sold out", engine.isGameOver());
    }

    // ========== Calculate Player Score Tests ==========

    @Test
    public void testCalculatePlayerScore_OnlyVictoryCardsInDiscard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Verify piles are empty after clearing
        assertEquals("Hand should be empty after clear", 0, hands.get(player).size());
        assertEquals("Deck should be empty after clear", 0, decks.get(player).size());
        assertEquals("Discard should be empty after clear", 0, discards.get(player).size());

        // Add only victory cards to discard
        discards.get(player).add(new Card(Card.Type.METHOD, 1)); // 1 point
        discards.get(player).add(new Card(Card.Type.MODULE, 2)); // 3 points
        discards.get(player).add(new Card(Card.Type.FRAMEWORK, 3)); // 6 points

        // Verify we added exactly 3 cards
        assertEquals("Should have 3 cards in discard before score calculation", 3, discards.get(player).size());

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        // Debug: Check what's actually in discard after scoring
        int actualDiscardSize = discards.get(player).size();
        int victoryPoints = 0;
        for (Card card : discards.get(player)) {
            if (card.getCategory() == Card.Type.Category.VICTORY) {
                victoryPoints += card.value();
            }
        }

        // Assert discard size first to see if extra cards were added
        assertEquals("Discard should have exactly 3 cards", 3, actualDiscardSize);
        // Then assert the score matches our calculation
        assertEquals("Victory points should match our calculation", 10, victoryPoints);
        assertEquals("Score should be sum of victory points (1 + 3 + 6)", 10, score);
    }

    @Test
    public void testCalculatePlayerScore_VictoryCardsInHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add victory cards to hand
        hands.get(player).addToUnplayed(new Card(Card.Type.METHOD, 1)); // 1 point
        hands.get(player).addToUnplayed(new Card(Card.Type.FRAMEWORK, 2)); // 6 points

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be sum of victory points (1 + 6)", 7, score);
        assertEquals("Hand should be empty after scoring", 0, hands.get(player).size());
        assertEquals("All cards should be moved to discard", 2, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_VictoryCardsInDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add victory cards to deck
        decks.get(player).add(new Card(Card.Type.METHOD, 1)); // 1 point
        decks.get(player).add(new Card(Card.Type.MODULE, 2)); // 3 points

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be sum of victory points (1 + 3)", 4, score);
        assertEquals("Deck should be empty after scoring", 0, decks.get(player).size());
        assertEquals("All cards should be moved to discard", 2, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_VictoryCardsAcrossAllLocations() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add victory cards to all locations
        discards.get(player).add(new Card(Card.Type.METHOD, 1)); // 1 point
        hands.get(player).addToUnplayed(new Card(Card.Type.MODULE, 2)); // 3 points
        decks.get(player).add(new Card(Card.Type.FRAMEWORK, 3)); // 6 points

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be sum of all victory points (1 + 3 + 6)", 10, score);
        assertEquals("Hand should be empty after scoring", 0, hands.get(player).size());
        assertEquals("Deck should be empty after scoring", 0, decks.get(player).size());
        assertEquals("All cards should be in discard", 3, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_MixedCardsOnlyCountsVictory() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add mixed card types
        discards.get(player).add(new Card(Card.Type.BITCOIN, 1)); // Money, 0 points
        discards.get(player).add(new Card(Card.Type.METHOD, 2)); // Victory, 1 point
        hands.get(player).addToUnplayed(new Card(Card.Type.REFACTOR, 3)); // Action, 0 points
        hands.get(player).addToUnplayed(new Card(Card.Type.MODULE, 4)); // Victory, 3 points
        decks.get(player).add(new Card(Card.Type.ETHEREUM, 5)); // Money, 0 points
        decks.get(player).add(new Card(Card.Type.FRAMEWORK, 6)); // Victory, 6 points

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should only count victory cards (1 + 3 + 6)", 10, score);
        assertEquals("Hand should be empty after scoring", 0, hands.get(player).size());
        assertEquals("Deck should be empty after scoring", 0, decks.get(player).size());
        assertEquals("All 6 cards should be in discard", 6, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_NoVictoryCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add only non-victory cards
        discards.get(player).add(new Card(Card.Type.BITCOIN, 1));
        hands.get(player).addToUnplayed(new Card(Card.Type.REFACTOR, 2));
        decks.get(player).add(new Card(Card.Type.ETHEREUM, 3));

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be 0 with no victory cards", 0, score);
        assertEquals("All 3 cards should be in discard", 3, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_EmptyPiles() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be 0 with empty piles", 0, score);
        assertEquals("Discard should remain empty", 0, discards.get(player).size());
    }

    @Test
    public void testCalculatePlayerScore_MultipleMethodCards() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        discards.get(player).clear();
        decks.get(player).clear();
        hands.get(player).clear();

        // Add multiple METHOD cards (1 point each)
        for (int i = 0; i < 5; i++) {
            discards.get(player).add(new Card(Card.Type.METHOD, i));
        }

        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("calculatePlayerScore", Player.class);
        method.setAccessible(true);
        int score = (int) method.invoke(engine, player);

        assertEquals("Score should be 5 (5 x 1 point)", 5, score);
    }

    // ========== Print Game Result Tests ==========

    @Test
    public void testPrintGameResult_DisplaysGameOverHeader() {
        // Create a simple game result
        Card methodCard = new Card(Card.Type.METHOD, 1);
        ImmutableList<Card> deck = ImmutableList.of(methodCard);
        PlayerResult pr = new PlayerResult("Player-1", 1, deck);
        GameResult result = new GameResult(ImmutableList.of(pr));

        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();
            assertTrue("Should display 'Game Over!'", output.contains("Game Over!"));
            assertTrue("Should display header separator",
                    output.contains("═══════════════════════════════════════════════"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintGameResult_DisplaysPlayerNameAndScore() {
        Card methodCard = new Card(Card.Type.METHOD, 1);
        ImmutableList<Card> deck = ImmutableList.of(methodCard);
        PlayerResult pr = new PlayerResult("TestPlayer", 15, deck);
        GameResult result = new GameResult(ImmutableList.of(pr));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();
            assertTrue("Should display player name", output.contains("TestPlayer"));
            assertTrue("Should display score", output.contains("scored 15"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintGameResult_DisplaysAllPlayersAndCards() {
        Card methodCard = new Card(Card.Type.METHOD, 1);
        Card module = new Card(Card.Type.MODULE, 2);
        Card bitcoin = new Card(Card.Type.BITCOIN, 3);

        PlayerResult pr1 = new PlayerResult("Player1", 10, ImmutableList.of(methodCard, bitcoin));
        PlayerResult pr2 = new PlayerResult("Player2", 5, ImmutableList.of(module));
        GameResult result = new GameResult(ImmutableList.of(pr1, pr2));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();

            // Check both players are displayed
            assertTrue("Should display Player1", output.contains("Player1"));
            assertTrue("Should display Player2", output.contains("Player2"));
            assertTrue("Should display Player1 score", output.contains("scored 10"));
            assertTrue("Should display Player2 score", output.contains("scored 5"));

            // Check cards are displayed
            assertTrue("Should mention cards in discard pile", output.contains("cards in their discard pile"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintGameResult_DisplaysMultipleCards() {
        Card methodCard = new Card(Card.Type.METHOD, 1);
        Card module = new Card(Card.Type.MODULE, 2);
        Card framework = new Card(Card.Type.FRAMEWORK, 3);

        PlayerResult pr = new PlayerResult("Player1", 8, ImmutableList.of(methodCard, module, framework));
        GameResult result = new GameResult(ImmutableList.of(pr));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();

            // All cards should appear in output
            assertTrue("Should list Method card", output.contains("Method"));
            assertTrue("Should list Module card", output.contains("Module"));
            assertTrue("Should list Framework card", output.contains("Framework"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintGameResult_EmptyDeck() {
        PlayerResult pr = new PlayerResult("Player1", 0, ImmutableList.of());
        GameResult result = new GameResult(ImmutableList.of(pr));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();
            assertTrue("Should still display player info", output.contains("Player1"));
            assertTrue("Should display score of 0", output.contains("scored 0"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintGameResult_FourPlayers() {
        Card card1 = new Card(Card.Type.METHOD, 1);
        PlayerResult pr1 = new PlayerResult("Alice", 20, ImmutableList.of(card1));
        PlayerResult pr2 = new PlayerResult("Bob", 15, ImmutableList.of(card1));
        PlayerResult pr3 = new PlayerResult("Charlie", 10, ImmutableList.of(card1));
        PlayerResult pr4 = new PlayerResult("Diana", 5, ImmutableList.of(card1));

        GameResult result = new GameResult(ImmutableList.of(pr1, pr2, pr3, pr4));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Renderer.printGameResult(result);

            String output = outputStream.toString();

            // All players should be displayed
            assertTrue("Should display Alice", output.contains("Alice"));
            assertTrue("Should display Bob", output.contains("Bob"));
            assertTrue("Should display Charlie", output.contains("Charlie"));
            assertTrue("Should display Diana", output.contains("Diana"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ========== End Game Tests ==========

    @Test
    public void testEndGame_ReturnsGameResult() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        // Set up minimal game state
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        for (Player p : twoPlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
        }

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("endGame");
            method.setAccessible(true);
            GameResult result = (GameResult) method.invoke(engine);

            assertNotNull("Should return a GameResult", result);
            assertEquals("Should have results for 2 players", 2, result.playerResults().size());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEndGame_SortsPlayersByScoreDescending() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player1 = twoPlayers.get(0);
        Player player2 = twoPlayers.get(1);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        for (Player p : twoPlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
        }

        // Give player1 lower score (1 point)
        discards.get(player1).add(new Card(Card.Type.METHOD, 1));

        // Give player2 higher score (6 points)
        discards.get(player2).add(new Card(Card.Type.FRAMEWORK, 2));

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("endGame");
            method.setAccessible(true);
            GameResult result = (GameResult) method.invoke(engine);

            // Results should be sorted by score descending (player2 first)
            assertEquals("First player should be the one with higher score",
                    player2.getName(), result.playerResults().get(0).playerName());
            assertEquals("First player score should be 6",
                    6, result.playerResults().get(0).score());
            assertEquals("Second player should be the one with lower score",
                    player1.getName(), result.playerResults().get(1).playerName());
            assertEquals("Second player score should be 1",
                    1, result.playerResults().get(1).score());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEndGame_ThreePlayers_SortsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        Player player1 = threePlayers.get(0);
        Player player2 = threePlayers.get(1);
        Player player3 = threePlayers.get(2);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        for (Player p : threePlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
        }

        // Give different scores: player1=3, player2=6, player3=1
        discards.get(player1).add(new Card(Card.Type.MODULE, 1)); // 3 points
        discards.get(player2).add(new Card(Card.Type.FRAMEWORK, 2)); // 6 points
        discards.get(player3).add(new Card(Card.Type.METHOD, 3)); // 1 point

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("endGame");
            method.setAccessible(true);
            GameResult result = (GameResult) method.invoke(engine);

            // Should be sorted: player2 (6), player1 (3), player3 (1)
            assertEquals("First should be player2 with 6 points",
                    player2.getName(), result.playerResults().get(0).playerName());
            assertEquals("First player score should be 6",
                    6, result.playerResults().get(0).score());
            assertEquals("Second should be player1 with 3 points",
                    player1.getName(), result.playerResults().get(1).playerName());
            assertEquals("Second player score should be 3",
                    3, result.playerResults().get(1).score());
            assertEquals("Third should be player3 with 1 point",
                    player3.getName(), result.playerResults().get(2).playerName());
            assertEquals("Third player score should be 1",
                    1, result.playerResults().get(2).score());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEndGame_MovesAllCardsToDiscard() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        for (Player p : twoPlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
        }

        // Add cards to different locations
        hands.get(player).addToUnplayed(new Card(Card.Type.METHOD, 1));
        decks.get(player).add(new Card(Card.Type.MODULE, 2));
        discards.get(player).add(new Card(Card.Type.BITCOIN, 3));

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("endGame");
            method.setAccessible(true);
            GameResult result = (GameResult) method.invoke(engine);

            // After endGame, all cards should be in discard
            assertEquals("Hand should be empty", 0, hands.get(player).size());
            assertEquals("Deck should be empty", 0, decks.get(player).size());
            assertEquals("All 3 cards should be in discard", 3, discards.get(player).size());

            // GameResult should have all cards in endingDeck
            PlayerResult pr = result.playerResults().stream()
                    .filter(p -> p.playerName().equals(player.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Should find player result", pr);
            assertEquals("PlayerResult should have all 3 cards", 3, pr.endingDeck().size());
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEndGame_PlayerResultContainsCorrectDeck() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles
        for (Player p : twoPlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
        }

        // Add specific cards
        Card method = new Card(Card.Type.METHOD, 100);
        Card module = new Card(Card.Type.MODULE, 101);
        discards.get(player).add(method);
        decks.get(player).add(module);

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method endGameMethod = engine.getClass().getDeclaredMethod("endGame");
            endGameMethod.setAccessible(true);
            GameResult result = (GameResult) endGameMethod.invoke(engine);

            // Find this player's result
            PlayerResult pr = result.playerResults().stream()
                    .filter(p -> p.playerName().equals(player.getName()))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Should find player result", pr);
            assertEquals("Should have 2 cards in ending deck", 2, pr.endingDeck().size());

            // Verify the cards are present (they should be in discard after
            // calculatePlayerScore)
            assertTrue("Should contain method card",
                    pr.endingDeck().stream().anyMatch(c -> c.id() == 100));
            assertTrue("Should contain module card",
                    pr.endingDeck().stream().anyMatch(c -> c.id() == 101));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testEndGame_ZeroScoresHandledCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        Map<Player, List<Card>> decks = getPrivateField(engine, "decks");
        Map<Player, MutableHand> hands = getPrivateField(engine, "hands");

        // Clear all piles and add only non-victory cards
        for (Player p : twoPlayers) {
            discards.get(p).clear();
            decks.get(p).clear();
            hands.get(p).clear();
            // Add only money cards (no victory points)
            discards.get(p).add(new Card(Card.Type.BITCOIN, p.hashCode()));
        }

        // Suppress output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("endGame");
            method.setAccessible(true);
            GameResult result = (GameResult) method.invoke(engine);

            // Both players should have 0 score
            for (PlayerResult pr : result.playerResults()) {
                assertEquals("All players should have 0 score", 0, pr.score());
            }
        } finally {
            System.setOut(originalOut);
        }
    }

    // ========== Output Suppression Helpers ==========

    private PrintStream savedOut;

    private void suppressOutput() {
        savedOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    private void restoreOutput() {
        if (savedOut != null) {
            System.setOut(savedOut);
            savedOut = null;
        }
    }

    // ========== play() Integration Tests ==========

    /**
     * A player that always returns null from makeDecision, triggering
     * PlayerViolationException and disqualification.
     */
    private static class CheaterPlayer implements Player {
        @Override
        public String getName() {
            return "Cheater";
        }

        @Override
        public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
            return null;
        }
    }

    @Test
    public void testPlay_TwoPlayers_ReturnsNonNullGameResult() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertNotNull("play() should return a non-null GameResult", result);
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_ThreePlayers_ReturnsNonNullGameResult() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertNotNull("play() should return a non-null GameResult", result);
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_FourPlayers_ReturnsNonNullGameResult() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertNotNull("play() should return a non-null GameResult", result);
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_ResultHasCorrectPlayerCount() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertEquals("GameResult should contain the same number of players as input",
                    twoPlayers.size(), result.playerResults().size());
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_ResultSortedByScoreDescending() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            List<PlayerResult> results = result.playerResults();
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue("Results should be sorted in descending order by score",
                        results.get(i).score() >= results.get(i + 1).score());
            }
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_ResultPlayerNamesMatchInputPlayers() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        suppressOutput();
        try {
            GameResult result = engine.play();
            List<String> resultNames = new ArrayList<>();
            for (PlayerResult pr : result.playerResults()) {
                resultNames.add(pr.playerName());
            }
            for (Player p : twoPlayers) {
                assertTrue("All input player names should appear in the result",
                        resultNames.contains(p.getName()));
            }
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_CheatingPlayer_GameStillCompletes() throws Exception {
        // When one player cheats, they are disqualified and the game ends immediately
        // since only one qualified player remains
        List<Player> players = new ArrayList<>();
        players.add(new CheaterPlayer());
        players.add(new AIPlayer());

        OurEngine engine = new OurEngine(players);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertNotNull("Game should still return a result when a player cheats", result);
            assertEquals("Result should still contain all players", 2, result.playerResults().size());
        } finally {
            restoreOutput();
        }
    }

    @Test
    public void testPlay_CheatingPlayer_DoesNotCauseExceptionInCaller() {
        // PlayerViolationException should be handled internally; play() should not
        // propagate it to the caller
        List<Player> players = new ArrayList<>();
        players.add(new AIPlayer());
        players.add(new CheaterPlayer());

        OurEngine engine = new OurEngine(players);
        suppressOutput();
        try {
            GameResult result = engine.play();
            assertNotNull("play() should not propagate PlayerViolationException", result);
        } catch (Exception e) {
            fail("play() should not throw an exception when a player cheats: " + e.getMessage());
        } finally {
            restoreOutput();
        }
    }
}
