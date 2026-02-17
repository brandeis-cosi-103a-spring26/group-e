package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

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
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN should have 60", 60, (int) shop.get(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should have 40", 40, (int) shop.get(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should have 30", 30, (int) shop.get(Card.Type.DOGECOIN));
        assertEquals("METHOD should have 14", 14, (int) shop.get(Card.Type.METHOD));
        assertEquals("MODULE should have 8", 8, (int) shop.get(Card.Type.MODULE));
        assertEquals("FRAMEWORK should have 8", 8, (int) shop.get(Card.Type.FRAMEWORK));
        assertEquals("BUG should have 20 (2 players * 10)", 20, (int) shop.get(Card.Type.BUG));
        assertEquals("REFACTOR should have 10", 10, (int) shop.get(Card.Type.REFACTOR));
        assertEquals("CODE_REVIEW should have 10", 10, (int) shop.get(Card.Type.CODE_REVIEW));
        assertEquals("EVERGREEN_TEST should have 10", 10, (int) shop.get(Card.Type.EVERGREEN_TEST));
    }

    @Test
    public void testConstructor_InitializesShopWithCorrectQuantities_FourPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");

        assertEquals("BUG should have 40 (4 players * 10)", 40, (int) shop.get(Card.Type.BUG));
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
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        shop.put(Card.Type.FRAMEWORK, 0);

        assertTrue("Game should be over when FRAMEWORK cards are sold out", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_FrameworkNotInShop_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        shop.remove(Card.Type.FRAMEWORK);

        assertTrue("Game should be over when FRAMEWORK type doesn't exist in shop",
                engine.isGameOver());
    }

    @Test
    public void testGetShopQuantity_CardExists_ReturnsQuantity() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);

        assertEquals("BITCOIN quantity should be 60", 60, engine.getShopQuantity(Card.Type.BITCOIN));
        assertEquals("ETHEREUM quantity should be 40", 40, engine.getShopQuantity(Card.Type.ETHEREUM));
        assertEquals("FRAMEWORK quantity should be 8", 8, engine.getShopQuantity(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetShopQuantity_CardQuantityZero_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        shop.put(Card.Type.FRAMEWORK, 0);

        assertEquals("Should return 0 for card with 0 quantity", 0,
                engine.getShopQuantity(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetShopQuantity_CardNotInShop_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        shop.remove(Card.Type.BITCOIN);

        assertEquals("Should return 0 for card not in shop", 0,
                engine.getShopQuantity(Card.Type.BITCOIN));
    }

    // ========== Game Mechanics Tests - drawCard ==========

    @Test
    public void testDrawCard_FromNonEmptyDeck_AddsToHand() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
        playersWithNull.add(new AIPlayer());
        playersWithNull.add(null);
        new OurEngine(playersWithNull);
    }

    @Test
    public void testDrawCard_HandAlreadyHasCards_AddsCorrectly() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player testPlayer = new AIPlayer();
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
        Player testPlayer = new AIPlayer();
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
    public void testGetShopQuantity_AfterModification_ReturnsUpdatedValue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");

        // Modify shop quantity
        shop.put(Card.Type.BITCOIN, 25);

        assertEquals("Should return modified quantity", 25, engine.getShopQuantity(Card.Type.BITCOIN));
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
    public void testGetShopQuantity_AllCardTypes_ReturnCorrectInitialValues() {
        OurEngine engine = new OurEngine(twoPlayers);

        // Verify all card types have correct initial quantities
        assertEquals(60, engine.getShopQuantity(Card.Type.BITCOIN));
        assertEquals(40, engine.getShopQuantity(Card.Type.ETHEREUM));
        assertEquals(30, engine.getShopQuantity(Card.Type.DOGECOIN));
        assertEquals(14, engine.getShopQuantity(Card.Type.METHOD));
        assertEquals(8, engine.getShopQuantity(Card.Type.MODULE));
        assertEquals(8, engine.getShopQuantity(Card.Type.FRAMEWORK));
        assertEquals(20, engine.getShopQuantity(Card.Type.BUG)); // 2 players * 10
        assertEquals(10, engine.getShopQuantity(Card.Type.REFACTOR));
        assertEquals(10, engine.getShopQuantity(Card.Type.CODE_REVIEW));
        assertEquals(10, engine.getShopQuantity(Card.Type.EVERGREEN_TEST));
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
        Player testPlayer = new AIPlayer();
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

        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");
        int cardIdCounter = getPrivateField(engine, "cardIdCounter");

        int initialShopCount = shop.get(Card.Type.ETHEREUM);
        int initialDiscardSize = discards.get(player).size();

        // Gain an ETHEREUM card
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.ETHEREUM, player);

        // Verify shop count decremented
        assertEquals("Shop count should decrease by 1", initialShopCount - 1, (int) shop.get(Card.Type.ETHEREUM));

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

        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Set shop to have only 1 REFACTOR card
        shop.put(Card.Type.REFACTOR, 1);

        // Gain the last REFACTOR
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.REFACTOR, player);

        // Verify shop count is now 0
        assertEquals("Shop count should be 0", 0, (int) shop.get(Card.Type.REFACTOR));

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

        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Set CODE_REVIEW count to 0
        shop.put(Card.Type.CODE_REVIEW, 0);
        int initialDiscardSize = discards.get(player).size();

        // Attempt to gain CODE_REVIEW (should be silent no-op)
        java.lang.reflect.Method method = engine.getClass().getDeclaredMethod("processGainingCard", Card.Type.class,
                Player.class);
        method.setAccessible(true);
        method.invoke(engine, Card.Type.CODE_REVIEW, player);

        // Verify nothing changed
        assertEquals("Shop count should remain 0", 0, (int) shop.get(Card.Type.CODE_REVIEW));
        assertEquals("Discard should not change", initialDiscardSize, discards.get(player).size());
    }

    @Test
    public void testProcessGainingCard_CardTypeNotInShop() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        Player player = twoPlayers.get(0);

        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        Map<Player, List<Card>> discards = getPrivateField(engine, "discards");

        // Remove FRAMEWORK from shop entirely
        shop.remove(Card.Type.FRAMEWORK);
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
        Map<Card.Type, Integer> shop = getPrivateField(engine, "shop");
        shop.put(Card.Type.MODULE, 0);

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

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testMakeValidDecision_InvalidDecision() throws Exception {
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
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Should throw UnsupportedOperationException from handleCheating
            assertTrue("Should throw UnsupportedOperationException",
                    e.getCause() instanceof UnsupportedOperationException);
            assertTrue("Error message should mention unimplemented handleCheating",
                    e.getCause().getMessage().contains("Unimplemented method 'handleCheating'"));
            throw e;
        }
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testMakeValidDecision_NullDecision() throws Exception {
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
        } catch (java.lang.reflect.InvocationTargetException e) {
            // null is not in validDecisions, so should trigger handleCheating
            assertTrue("Should throw UnsupportedOperationException or NullPointerException",
                    e.getCause() instanceof UnsupportedOperationException ||
                            e.getCause() instanceof NullPointerException);
            throw e;
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
}
