package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
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
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN should have 60", 60, shop.getCount(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should have 40", 40, shop.getCount(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should have 30", 30, shop.getCount(Card.Type.DOGECOIN));
        assertEquals("METHOD should have 14", 14, shop.getCount(Card.Type.METHOD));
        assertEquals("MODULE should have 8", 8, shop.getCount(Card.Type.MODULE));
        assertEquals("FRAMEWORK should have 8", 8, shop.getCount(Card.Type.FRAMEWORK));
        assertEquals("BUG should have 20 (2 players * 10)", 20, shop.getCount(Card.Type.BUG));
        assertEquals("REFACTOR should have 10", 10, shop.getCount(Card.Type.REFACTOR));
        assertEquals("CODE_REVIEW should have 10", 10, shop.getCount(Card.Type.CODE_REVIEW));
        assertEquals("EVERGREEN_TEST should have 10", 10, shop.getCount(Card.Type.EVERGREEN_TEST));
    }

    @Test
    public void testConstructor_InitializesShopWithCorrectQuantities_FourPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BUG should have 40 (4 players * 10)", 40, shop.getCount(Card.Type.BUG));
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
        // Access internal shop map via reflection to set count to 0
        Map<Card.Type, Integer> shopMap = getPrivateField(shop, "shop");
        shopMap.put(Card.Type.FRAMEWORK, 0);

        assertTrue("Game should be over when FRAMEWORK cards are sold out", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_FrameworkNotInShop_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Access internal shop map via reflection to remove FRAMEWORK
        Map<Card.Type, Integer> shopMap = getPrivateField(shop, "shop");
        shopMap.remove(Card.Type.FRAMEWORK);

        assertTrue("Game should be over when FRAMEWORK type doesn't exist in shop",
                engine.isGameOver());
    }

    @Test
    public void testGetShopQuantity_CardExists_ReturnsQuantity() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN quantity should be 60", 60, shop.getCount(Card.Type.BITCOIN));
        assertEquals("ETHEREUM quantity should be 40", 40, shop.getCount(Card.Type.ETHEREUM));
        assertEquals("FRAMEWORK quantity should be 8", 8, shop.getCount(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetShopQuantity_CardQuantityZero_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Access internal shop map via reflection to set count to 0
        Map<Card.Type, Integer> shopMap = getPrivateField(shop, "shop");
        shopMap.put(Card.Type.FRAMEWORK, 0);

        assertEquals("Should return 0 for card with 0 quantity", 0,
                shop.getCount(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetShopQuantity_CardNotInShop_ReturnsZero() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Access internal shop map via reflection to remove card
        Map<Card.Type, Integer> shopMap = getPrivateField(shop, "shop");
        shopMap.remove(Card.Type.BITCOIN);

        assertEquals("Should return 0 for card not in shop", 0,
                shop.getCount(Card.Type.BITCOIN));
    }

    // ========== Game Mechanics Tests - drawCard ==========

    @Test
    public void testDrawCard_FromNonEmptyDeck_AddsToHand() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card card1 = new Card(Card.Type.BITCOIN, 1);
        Card card2 = new Card(Card.Type.METHOD, 2);
        deck.add(card1);
        deck.add(card2);

        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should have 1 card", 1, hand.size());
        assertEquals("Deck should have 1 card left", 1, deck.size());
        assertTrue("Hand should contain the drawn card", hand.contains(card2));
        assertEquals("Deck should contain the other card", card1, deck.get(0));
    }

    @Test
    public void testDrawCard_EmptyDeck_ReshufflesDiscard() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card card1 = new Card(Card.Type.BITCOIN, 1);
        Card card2 = new Card(Card.Type.METHOD, 2);
        Card card3 = new Card(Card.Type.ETHEREUM, 3);
        discard.add(card1);
        discard.add(card2);
        discard.add(card3);

        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should have 1 card", 1, hand.size());
        assertTrue("Discard should be empty after reshuffle", discard.isEmpty());
        assertEquals("Deck should have 2 cards remaining (3 - 1 drawn)", 2, deck.size());
    }

    @Test
    public void testDrawCard_EmptyDeckAndDiscard_NoChange() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should remain empty", 0, hand.size());
        assertTrue("Deck should remain empty", deck.isEmpty());
        assertTrue("Discard should remain empty", discard.isEmpty());
    }

    @Test
    public void testDrawCard_Reshuffle_ClearsDiscard() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        discard.add(new Card(Card.Type.BITCOIN, 1));
        discard.add(new Card(Card.Type.METHOD, 2));

        engine.drawCard(hand, deck, discard);

        assertTrue("Discard should be cleared after reshuffle", discard.isEmpty());
    }

    @Test
    public void testDrawCard_Reshuffle_ShufflesDeck() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add cards to discard in a specific order
        for (int i = 0; i < 10; i++) {
            discard.add(new Card(Card.Type.BITCOIN, i));
        }

        engine.drawCard(hand, deck, discard);

        // After shuffle and drawing one card, deck should have 9 cards
        assertEquals("Deck should have 9 cards after reshuffle and draw", 9, deck.size());
        // Note: We can't easily verify randomness, but we can verify the cards are
        // present
        assertFalse("Deck should not be empty after reshuffle", deck.isEmpty());
    }

    @Test
    public void testDrawCard_MultipleDrawsWithReshuffles() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Start with 3 cards in deck
        deck.add(new Card(Card.Type.BITCOIN, 1));
        deck.add(new Card(Card.Type.BITCOIN, 2));
        deck.add(new Card(Card.Type.BITCOIN, 3));

        // Draw 3 cards (empties deck)
        engine.drawCard(hand, deck, discard);
        engine.drawCard(hand, deck, discard);
        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should have 3 cards", 3, hand.size());
        assertTrue("Deck should be empty", deck.isEmpty());

        // Add cards to discard and draw again (triggers reshuffle)
        discard.add(new Card(Card.Type.METHOD, 4));
        discard.add(new Card(Card.Type.METHOD, 5));

        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should have 4 cards after reshuffle and draw", 4, hand.size());
        assertEquals("Deck should have 1 card after reshuffle and draw", 1, deck.size());
        assertTrue("Discard should be empty after reshuffle", discard.isEmpty());
    }

    @Test
    public void testDrawCard_DrawsFromEndOfDeck() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        Card firstCard = new Card(Card.Type.BITCOIN, 1);
        Card lastCard = new Card(Card.Type.METHOD, 2);
        deck.add(firstCard);
        deck.add(lastCard);

        engine.drawCard(hand, deck, discard);

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
    public void testDrawCard_HandAlreadyHasCards_AddsCorrectly() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add initial cards to hand
        Card initialCard = new Card(Card.Type.BITCOIN, 100);
        hand.addToUnplayed(initialCard);

        // Add cards to deck
        Card deckCard = new Card(Card.Type.METHOD, 101);
        deck.add(deckCard);

        engine.drawCard(hand, deck, discard);

        assertEquals("Hand should have 2 cards after draw", 2, hand.size());
        assertTrue("Hand should still contain initial card", hand.contains(initialCard));
        assertTrue("Hand should contain newly drawn card", hand.contains(deckCard));
    }

    @Test
    public void testDrawCard_ReshufflePreservesAllCards() {
        OurEngine engine = new OurEngine(twoPlayers);
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

        // Draw should reshuffle and draw one card
        engine.drawCard(hand, deck, discard);

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
        MutableCardStacks shop = getPrivateField(engine, "shop");
        // Access internal shop map via reflection to modify quantity
        Map<Card.Type, Integer> shopMap = getPrivateField(shop, "shop");

        // Modify shop quantity
        shopMap.put(Card.Type.BITCOIN, 25);

        assertEquals("Should return modified quantity", 25, shop.getCount(Card.Type.BITCOIN));
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
    public void testGetShopQuantity_AllCardTypes_ReturnCorrectInitialValues() throws Exception {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        // Verify all card types have correct initial quantities
        assertEquals(60, shop.getCount(Card.Type.BITCOIN));
        assertEquals(40, shop.getCount(Card.Type.ETHEREUM));
        assertEquals(30, shop.getCount(Card.Type.DOGECOIN));
        assertEquals(14, shop.getCount(Card.Type.METHOD));
        assertEquals(8, shop.getCount(Card.Type.MODULE));
        assertEquals(8, shop.getCount(Card.Type.FRAMEWORK));
        assertEquals(20, shop.getCount(Card.Type.BUG)); // 2 players * 10
        assertEquals(10, shop.getCount(Card.Type.REFACTOR));
        assertEquals(10, shop.getCount(Card.Type.CODE_REVIEW));
        assertEquals(10, shop.getCount(Card.Type.EVERGREEN_TEST));
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
    public void testDrawCard_MultipleSequentialDraws_MaintainsCorrectState() {
        OurEngine engine = new OurEngine(twoPlayers);
        MutableHand hand = new MutableHand();
        List<Card> deck = new ArrayList<>();
        List<Card> discard = new ArrayList<>();

        // Add 10 cards to deck
        for (int i = 0; i < 10; i++) {
            deck.add(new Card(Card.Type.BITCOIN, i));
        }

        // Draw 5 cards
        for (int i = 0; i < 5; i++) {
            engine.drawCard(hand, deck, discard);
        }

        assertEquals("Hand should have 5 cards", 5, hand.size());
        assertEquals("Deck should have 5 cards remaining", 5, deck.size());
        assertTrue("Discard should remain empty", discard.isEmpty());
    }
}
