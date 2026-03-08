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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the OurEngine class.
 * Tests constructor validation, game mechanics, decision processing,
 * action card execution, and game orchestration logic.
 */
public class OurEngineTest {

    private List<Player> threePlayers;
    private List<Player> fourPlayers;

    @Before
    public void setUp() {
        threePlayers = createPlayers(3);
        fourPlayers = createPlayers(4);
    }

    // ========== Helper Methods ==========

    /**
     * Creates a list of BigMoneyPlayer instances.
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

    /**
     * Uses reflection to invoke a private method.
     */
    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(Object obj, String methodName, Class<?>[] paramTypes, Object... args) 
            throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(obj, args);
    }

    // ========== Constructor Validation Tests ==========

    @Test
    public void testConstructor_TwoPlayers_Success() {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        assertNotNull("Engine should be created successfully", engine);
    }

    @Test
    public void testConstructor_ThreePlayers_Success() {
        OurEngine engine = new OurEngine(threePlayers);
        assertNotNull("Engine should be created successfully", engine);
    }

    @Test
    public void testConstructor_FourPlayers_Success() {
        OurEngine engine = new OurEngine(fourPlayers);
        assertNotNull("Engine should be created successfully", engine);
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

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ListContainsNullPlayer_ThrowsException() {
        List<Player> playersWithNull = new ArrayList<>();
        playersWithNull.add(new BigMoneyPlayer());
        playersWithNull.add(null);
        new OurEngine(playersWithNull);
    }

    @Test
    public void testConstructor_InitializesPhaseToAction() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        GameState.TurnPhase phase = getPrivateField(engine, "phase");
        assertEquals("Initial phase should be ACTION", GameState.TurnPhase.ACTION, phase);
    }

    @Test
    public void testConstructor_CreatesPlayerInfoForAllPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> playerInfos = getPrivateField(engine, "players");
        
        assertEquals("Should have PlayerInfo instances for all players", 3, playerInfos.size());
        
        for (int i = 0; i < threePlayers.size(); i++) {
            PlayerInfo info = playerInfos.get(i);
            assertNotNull("PlayerInfo should not be null", info);
            assertEquals("PlayerInfo should wrap the correct player", 
                threePlayers.get(i).getName(), info.getName());
        }
    }

    @Test
    public void testConstructor_InitializesActivePlayerToLastPlayer() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        PlayerInfo activePlayer = getPrivateField(engine, "activePlayer");
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // activePlayer should be last so getNextPlayer() returns first
        assertEquals("Active player should be last player initially",
            players.get(players.size() - 1), activePlayer);
    }

    @Test
    public void testConstructor_EachPlayerStartsWithFiveCardHand() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        for (PlayerInfo playerInfo : players) {
            assertEquals("Each player should start with 5 cards in hand",
                5, playerInfo.getUnplayedCards().size());
        }
    }

    @Test
    public void testConstructor_EachPlayerStartsWithFiveCardsInDeck() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        for (PlayerInfo playerInfo : players) {
            // Each player starts with 10 cards total: 5 in hand, 5 in deck
            // Use reflection to get deck size
            List<Card> deck = getPrivateField(playerInfo, "deck");
            assertEquals("Each player should have 5 cards remaining in deck",
                5, deck.size());
        }
    }

    @Test
    public void testConstructor_VerifyStartingCardsComposition() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        for (PlayerInfo playerInfo : players) {
            // Get all cards (hand + deck)
            List<Card> hand = playerInfo.getUnplayedCards();
            List<Card> deck = getPrivateField(playerInfo, "deck");
            
            List<Card> allCards = new ArrayList<>(hand);
            allCards.addAll(deck);
            
            assertEquals("Each player should have 10 total cards", 10, allCards.size());
            
            // Count card types
            int bitcoinCount = 0;
            int methodCount = 0;
            for (Card card : allCards) {
                if (card.type() == Card.Type.BITCOIN) {
                    bitcoinCount++;
                } else if (card.type() == Card.Type.METHOD) {
                    methodCount++;
                }
            }
            
            assertEquals("Each player should have 7 BITCOIN cards", 7, bitcoinCount);
            assertEquals("Each player should have 3 METHOD cards", 3, methodCount);
        }
    }

    @Test
    public void testConstructor_VerifyCardIdUniqueness() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        List<Integer> allIds = new ArrayList<>();
        for (PlayerInfo playerInfo : players) {
            for (Card card : playerInfo.getUnplayedCards()) {
                allIds.add(card.id());
            }
            List<Card> deck = getPrivateField(playerInfo, "deck");
            for (Card card : deck) {
                allIds.add(card.id());
            }
        }
        
        // Check for unique IDs
        long uniqueCount = allIds.stream().distinct().count();
        assertEquals("All cards should have unique IDs", allIds.size(), uniqueCount);
    }

    @Test
    public void testConstructor_InitializesShopCorrectly_TwoPlayers() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        assertEquals("BITCOIN should have 60", 60, shop.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should have 40", 40, shop.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should have 30", 30, shop.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals("METHOD should have 14 (2 players * 7 starting)", 14, shop.getNumAvailable(Card.Type.METHOD));
        assertEquals("MODULE should have 8", 8, shop.getNumAvailable(Card.Type.MODULE));
        assertEquals("FRAMEWORK should have 8", 8, shop.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals("BUG should have 20 (2 players * 10)", 20, shop.getNumAvailable(Card.Type.BUG));
        assertEquals("CODE_REVIEW should have 10", 10, shop.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("REFACTOR should have 10", 10, shop.getNumAvailable(Card.Type.REFACTOR));
        assertEquals("EVERGREEN_TEST should have 10", 10, shop.getNumAvailable(Card.Type.EVERGREEN_TEST));
    }

    @Test
    public void testConstructor_InitializesShopCorrectly_FourPlayers() throws Exception {
        OurEngine engine = new OurEngine(fourPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        assertEquals("BUG should have 40 (4 players * 10)", 40, shop.getNumAvailable(Card.Type.BUG));
        assertEquals("METHOD should have 14 (constant, not player-dependent)", 14, shop.getNumAvailable(Card.Type.METHOD));
        assertEquals("FRAMEWORK should have 12 for 4 players", 12, shop.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testConstructor_InitializesCardIdCounter() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        assertNotNull("Card ID counter should be initialized", cardIdCounter);
        // After creating 20 starting cards (2 players * 10 cards each), ID should be at least 21
        assertTrue("Card ID counter should be > 20 after creating starting cards", 
            cardIdCounter.get() > 20);
    }

    @Test
    public void testConstructor_InitializesTurnNumber() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        int turnNumber = getPrivateField(engine, "turnNumber");
        
        assertEquals("Turn number should start at 0", 0, turnNumber);
    }

    @Test
    public void testConstructor_AllCardsHaveUniqueIDs() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Collect all card IDs from all players' decks, discards, and hands
        Set<Integer> allCardIds = new HashSet<>();
        for (PlayerInfo player : players) {
            List<Card> deck = getPrivateField(player, "deck");
            List<Card> discard = getPrivateField(player, "discard");
            MutableHand hand = getPrivateField(player, "hand");
            
            // Add all card IDs to the set
            for (Card card : deck) {
                boolean wasNew = allCardIds.add(card.id());
                assertTrue("Card ID " + card.id() + " should be unique (found duplicate in deck)", wasNew);
            }
            for (Card card : discard) {
                boolean wasNew = allCardIds.add(card.id());
                assertTrue("Card ID " + card.id() + " should be unique (found duplicate in discard)", wasNew);
            }
            for (Card card : hand.getAllCards()) {
                boolean wasNew = allCardIds.add(card.id());
                assertTrue("Card ID " + card.id() + " should be unique (found duplicate in hand)", wasNew);
            }
        }
        
        // For 2 players, we should have 20 unique cards (10 per player)
        assertEquals("Should have 20 unique cards for 2 players", 20, allCardIds.size());
    }

    // ========== getNextPlayer Tests ==========

    @Test
    public void testGetNextPlayer_TwoPlayers_RotatesCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // First call should return first player
        PlayerInfo firstPlayer = engine.getNextPlayer();
        assertEquals("First call should return first player", players.get(0), firstPlayer);
        
        // Set active player and get next
        setPrivateField(engine, "activePlayer", firstPlayer);
        PlayerInfo secondPlayer = engine.getNextPlayer();
        assertEquals("Second call should return second player", players.get(1), secondPlayer);
        
        // Set active player and get next (should wrap around)
        setPrivateField(engine, "activePlayer", secondPlayer);
        PlayerInfo wrappedPlayer = engine.getNextPlayer();
        assertEquals("Should wrap around to first player", players.get(0), wrappedPlayer);
    }

    @Test
    public void testGetNextPlayer_ThreePlayers_RotatesCorrectly() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        PlayerInfo player1 = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", player1);
        
        PlayerInfo player2 = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", player2);
        
        PlayerInfo player3 = engine.getNextPlayer();
        setPrivateField(engine, "activePlayer", player3);
        
        PlayerInfo wrappedPlayer = engine.getNextPlayer();
        assertEquals("Should wrap around to first player", players.get(0), wrappedPlayer);
    }

    @Test
    public void testGetNextPlayer_SkipsDisqualifiedPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Disqualify the second player
        players.get(1).forfeit();
        
        PlayerInfo player1 = engine.getNextPlayer();
        assertEquals("Should get first player", players.get(0), player1);
        
        setPrivateField(engine, "activePlayer", player1);
        PlayerInfo nextPlayer = engine.getNextPlayer();
        
        // Should skip player 2 (disqualified) and go to player 3
        assertEquals("Should skip disqualified player", players.get(2), nextPlayer);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNextPlayer_AllButOneDisqualified_ThrowsException() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Disqualify one player (leaving only one)
        players.get(0).forfeit();
        
        // This should throw IllegalStateException
        engine.getNextPlayer();
    }

    // ========== isGameOver Tests ==========

    @Test
    public void testIsGameOver_InitialState_ReturnsFalse() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        assertFalse("Game should not be over initially", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_FrameworksSoldOut_ReturnsTrue() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        // Sell out all FRAMEWORKs
        int frameworkCount = shop.getNumAvailable(Card.Type.FRAMEWORK);
        for (int i = 0; i < frameworkCount; i++) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }
        
        assertTrue("Game should be over when FRAMEWORKs are sold out", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_AllButOnePlayerDisqualified_ReturnsTrue() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Disqualify all but one player
        players.get(0).forfeit();
        players.get(1).forfeit();
        
        assertTrue("Game should be over when all but one player is disqualified", 
            engine.isGameOver());
    }

    // ========== buildGameState Tests ==========

    @Test
    public void testBuildGameState_CorrectActivePlayerName() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Set first player as active
        PlayerInfo firstPlayer = players.get(0);
        setPrivateField(engine, "activePlayer", firstPlayer);
        
        GameState gameState = invokePrivateMethod(engine, "buildGameState", new Class<?>[]{});
        
        assertEquals("GameState should have correct active player name",
            firstPlayer.getName(), gameState.currentPlayerName());
    }

    @Test
    public void testBuildGameState_CorrectPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.BUY);
        
        GameState gameState = invokePrivateMethod(engine, "buildGameState", new Class<?>[]{});
        
        assertEquals("GameState should reflect current phase",
            GameState.TurnPhase.BUY, gameState.phase());
    }

    @Test
    public void testBuildGameState_CorrectTurnResources() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Set turn resources (turnReset already gave 1 action and 1 buy)
        activePlayer.adjustActions(2); // Now has 3 actions total
        activePlayer.adjustMoney(5); // Now has 5 money
        // Don't adjust buys - turnReset already gave 1
        
        GameState gameState = invokePrivateMethod(engine, "buildGameState", new Class<?>[]{});
        
        // turnReset() sets actions to 1, then we adjusted by +2, so total is 3
        assertEquals("GameState should have correct actions", 3, gameState.availableActions());
        assertEquals("GameState should have correct money", 5, gameState.spendableMoney());
        assertEquals("GameState should have correct buys", 1, gameState.availableBuys());
    }

    @Test
    public void testBuildGameState_ContainsShopState() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        setPrivateField(engine, "activePlayer", players.get(0));
        
        GameState gameState = invokePrivateMethod(engine, "buildGameState", new Class<?>[]{});
        
        assertNotNull("GameState should have shop", gameState.buyableCards());
        assertTrue("Shop should contain BITCOIN", 
            gameState.buyableCards().getNumAvailable(Card.Type.BITCOIN) > 0);
    }

    // ========== buildActionDecisions Tests ==========

    @Test
    public void testBuildActionDecisions_NoActions_OnlyEndPhaseDecision() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        
        // Ensure player has no actions
        activePlayer.adjustActions(0);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions", 
            new Class<?>[]{});
        
        assertEquals("Should have only one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        assertEquals("Should be ACTION phase", 
            GameState.TurnPhase.ACTION, ((EndPhaseDecision) decisions.get(0)).phase());
    }

    @Test
    public void testBuildActionDecisions_HasActionsButNoActionCards_OnlyEndPhaseDecision() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player actions but ensure hand has no action cards (default starting hand)
        activePlayer.adjustActions(3);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions", 
            new Class<?>[]{});
        
        // Should only have EndPhaseDecision since starting hand has no action cards
        assertEquals("Should have only EndPhaseDecision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildActionDecisions_HasActionsAndActionCards_IncludesPlayCardDecisions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Reset turn to get clean state, then give player actions
        activePlayer.turnReset();
        activePlayer.adjustActions(2); // Now has 3 actions total (1 base + 2)
        
        // Get the MutableHand and directly add an action card
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(codeReview);
        
        // Verify the CODE_REVIEW is actually in hand
        List<Card> unplayed = activePlayer.getUnplayedCards();
        long actionCardCount = unplayed.stream()
            .filter(c -> c.getCategory() == Card.Type.Category.ACTION)
            .count();
        assertTrue("Should have at least one action card in hand", actionCardCount > 0);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildActionDecisions", 
            new Class<?>[]{});
        
        // Should have PlayCardDecision(s) for action cards in hand + EndPhaseDecision
        // When player has actions and action cards, should get PlayCardDecision + EndPhaseDecision
        assertTrue("Should have at least 2 decisions", decisions.size() >= 2);
        
        boolean hasPlayCardDecision = decisions.stream()
            .anyMatch(d -> d instanceof PlayCardDecision);
        assertTrue("Should have at least one PlayCardDecision", hasPlayCardDecision);
        
        boolean hasEndPhaseDecision = decisions.stream()
            .anyMatch(d -> d instanceof EndPhaseDecision);
        assertTrue("Should have EndPhaseDecision", hasEndPhaseDecision);
    }

    // ========== buildMoneyDecisions Tests ==========

    @Test
    public void testBuildMoneyDecisions_NoMoneyCards_OnlyEndPhaseDecision() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Remove all money cards from hand (play them all first)
        List<Card> moneyCards = new ArrayList<>(activePlayer.getMoneysInHand());
        for (Card card : moneyCards) {
            activePlayer.playCard(card);
        }
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions", 
            new Class<?>[]{});
        
        // Should only have EndPhaseDecision
        boolean hasOnlyEndPhase = decisions.size() == 1 && 
            decisions.get(0) instanceof EndPhaseDecision;
        assertTrue("Should have only EndPhaseDecision when no money cards", hasOnlyEndPhase);
    }

    @Test
    public void testBuildMoneyDecisions_HasMoneyCards_IncludesPlayCardDecisions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions", 
            new Class<?>[]{});
        
        // Starting hand should have some money cards (BITCOINs)
        assertTrue("Should have multiple decisions", decisions.size() > 1);
        
        boolean hasPlayCardDecision = decisions.stream()
            .anyMatch(d -> d instanceof PlayCardDecision);
        assertTrue("Should have PlayCardDecision for money cards", hasPlayCardDecision);
        
        boolean hasEndPhaseDecision = decisions.stream()
            .anyMatch(d -> d instanceof EndPhaseDecision);
        assertTrue("Should have EndPhaseDecision", hasEndPhaseDecision);
    }

    // ========== buildBuyDecisions Tests ==========

    @Test
    public void testBuildBuyDecisions_NoBuys_OnlyEndPhaseDecision() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Set buys to exactly 0 (turnReset gives 1, so adjust by -1)
        activePlayer.turnReset();
        activePlayer.adjustBuys(-1);
        assertEquals("Should have 0 buys", 0, activePlayer.numBuys());
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        // buildBuyDecisions returns EndPhaseDecision when numBuys <= 0
        assertEquals("Should have only one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildBuyDecisions_HasBuysAndMoney_IncludesBuyDecisions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player buys and money
        activePlayer.adjustBuys(2);
        activePlayer.adjustMoney(5);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        // Should have BuyDecisions for cards costing <= 5
        assertTrue("Should have multiple decisions", decisions.size() > 1);
        
        boolean hasBuyDecision = decisions.stream()
            .anyMatch(d -> d instanceof BuyDecision);
        assertTrue("Should have BuyDecision", hasBuyDecision);
        
        boolean hasEndPhaseDecision = decisions.stream()
            .anyMatch(d -> d instanceof EndPhaseDecision);
        assertTrue("Should have EndPhaseDecision", hasEndPhaseDecision);
    }

    @Test
    public void testBuildBuyDecisions_OnlyIncludesAffordableCards() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player exactly 3 money
        activePlayer.adjustBuys(1);
        activePlayer.adjustMoney(3);
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        for (Decision decision : decisions) {
            if (decision instanceof BuyDecision) {
                BuyDecision buyDecision = (BuyDecision) decision;
                assertTrue("Should only include cards costing <= 3",
                    buyDecision.cardType().cost() <= 3);
            }
        }
    }

    // ========== validateDecision Tests ==========

    @Test
    public void testValidateDecision_ValidDecision_ReturnsTrue() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> validDecisions = ImmutableList.of(decision);
        
        boolean result = invokePrivateMethod(engine, "validateDecision", 
            new Class<?>[]{Decision.class, ImmutableList.class}, decision, validDecisions);
        
        assertTrue("Valid decision should return true", result);
    }

    @Test
    public void testValidateDecision_InvalidDecision_ReturnsFalse() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        
        EndPhaseDecision decision1 = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        EndPhaseDecision decision2 = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> validDecisions = ImmutableList.of(decision1);
        
        boolean result = invokePrivateMethod(engine, "validateDecision", 
            new Class<?>[]{Decision.class, ImmutableList.class}, decision2, validDecisions);
        
        assertFalse("Invalid decision should return false", result);
    }

    @Test
    public void testValidateDecision_NullDecision_ReturnsFalse() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        
        ImmutableList<Decision> validDecisions = ImmutableList.of(
            new EndPhaseDecision(GameState.TurnPhase.ACTION));
        
        boolean result = invokePrivateMethod(engine, "validateDecision", 
            new Class<?>[]{Decision.class, ImmutableList.class}, null, validDecisions);
        
        assertFalse("Null decision should return false", result);
    }

    // ========== processEndPhaseDecision Tests ==========

    @Test
    public void testProcessEndPhaseDecision_ActionToMoney_ChangesPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Phase should change from ACTION to MONEY",
            GameState.TurnPhase.MONEY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_MoneyToBuy_ChangesPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Phase should change from MONEY to BUY",
            GameState.TurnPhase.BUY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_BuyToCleanup_ChangesPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.BUY);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.BUY);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Phase should change from BUY to CLEANUP",
            GameState.TurnPhase.CLEANUP, newPhase);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessEndPhaseDecision_MismatchedPhase_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        
        // Try to end BUY phase when in ACTION phase
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.BUY);
        
        try {
            invokePrivateMethod(engine, "processEndPhaseDecision", 
                new Class<?>[]{EndPhaseDecision.class}, decision);
        } catch (Exception e) {
            throw e.getCause(); // Unwrap reflection exception
        }
    }

    // ========== processPlayCardDecision Tests ==========

    @Test
    public void testProcessPlayCardDecision_ActionCard_MovesToPlayedCards() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Give player an action first (turnReset gives 1, we add 1 more = 2 total)
        activePlayer.turnReset();
        activePlayer.adjustActions(1);
        
        // Get the MutableHand and directly add an action card
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(codeReview);
        
        // Get the actual card object from the hand
        List<Card> unplayedCards = activePlayer.getUnplayedCards();
        Card actualCard = unplayedCards.stream()
            .filter(c -> c.type() == Card.Type.CODE_REVIEW)
            .findFirst()
            .orElseThrow(() -> new AssertionError("CODE_REVIEW not found in hand"));
        
        int initialUnplayedCount = unplayedCards.size();
        
        PlayCardDecision decision = new PlayCardDecision(actualCard);
        invokePrivateMethod(engine, "processPlayCardDecision", 
            new Class<?>[]{PlayCardDecision.class}, decision);
        
        // CODE_REVIEW effect: +1 Card, +2 Actions
        // So unplayed count: starts at 6, -1 for playing, +1 for drawing = 6
        int newUnplayedCount = activePlayer.getUnplayedCards().size();
        assertEquals("Card count should stay same (played one, drew one)", initialUnplayedCount, newUnplayedCount);
    }

    @Test
    public void testProcessPlayCardDecision_MoneyCard_AddsMoneyValue() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        
        // Find a BITCOIN card in hand
        List<Card> moneyCards = activePlayer.getMoneysInHand();
        Card bitcoin = moneyCards.stream()
            .filter(c -> c.type() == Card.Type.BITCOIN)
            .findFirst()
            .orElse(null);
        assertNotNull("Should have BITCOIN in hand", bitcoin);
        
        int initialMoney = activePlayer.numMoney();
        
        PlayCardDecision decision = new PlayCardDecision(bitcoin);
        invokePrivateMethod(engine, "processPlayCardDecision", 
            new Class<?>[]{PlayCardDecision.class}, decision);
        
        int newMoney = activePlayer.numMoney();
        assertEquals("Money should increase by 1 for BITCOIN", initialMoney + 1, newMoney);
    }

    @Test
    public void testProcessPlayCardDecision_ActionCardWithZeroActions_ThrowsException() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Reset turn which gives 1 action, then set actions to 0
        activePlayer.turnReset();
        activePlayer.adjustActions(-1); // Now has 0 actions
        
        assertEquals("Should have 0 actions", 0, activePlayer.numActions());
        
        // Add an action card to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(codeReview);
        
        // Get the actual card from hand
        List<Card> unplayedCards = activePlayer.getUnplayedCards();
        Card actualCard = unplayedCards.stream()
            .filter(c -> c.type() == Card.Type.CODE_REVIEW)
            .findFirst()
            .orElseThrow(() -> new AssertionError("CODE_REVIEW not found in hand"));
        
        // Try to play action card with 0 actions - should throw IllegalStateException
        PlayCardDecision decision = new PlayCardDecision(actualCard);
        try {
            invokePrivateMethod(engine, "processPlayCardDecision", 
                new Class<?>[]{PlayCardDecision.class}, decision);
            fail("Expected IllegalStateException to be thrown");
        } catch (Exception e) {
            // Unwrap InvocationTargetException if present
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            assertTrue("Should throw IllegalStateException", cause instanceof IllegalStateException);
            assertTrue("Error message should mention no actions", 
                cause.getMessage().contains("No actions remaining"));
        }
    }

    // ========== processBuyDecision Tests ==========

    @Test
    public void testProcessBuyDecision_ValidBuy_DecrementsBuysAndMoney() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player money and buys
        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);
        
        int initialMoney = activePlayer.numMoney();
        int initialBuys = activePlayer.numBuys();
        
        // Buy a BITCOIN (costs 0)
        BuyDecision decision = new BuyDecision(Card.Type.BITCOIN);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, decision);
        
        assertEquals("Money should decrease by card cost", 
            initialMoney - Card.Type.BITCOIN.cost(), activePlayer.numMoney());
        assertEquals("Buys should decrease by 1", initialBuys - 1, activePlayer.numBuys());
    }

    @Test
    public void testProcessBuyDecision_ValidBuy_AddsCardToDiscard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);
        
        List<Card> discard = getPrivateField(activePlayer, "discard");
        int initialDiscardSize = discard.size();
        
        BuyDecision decision = new BuyDecision(Card.Type.ETHEREUM);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, decision);
        
        assertEquals("Discard should gain one card", initialDiscardSize + 1, discard.size());
    }

    @Test
    public void testProcessBuyDecision_ValidBuy_DecrementsShop() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);
        
        int initialShopCount = shop.getNumAvailable(Card.Type.ETHEREUM);
        
        BuyDecision decision = new BuyDecision(Card.Type.ETHEREUM);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, decision);
        
        assertEquals("Shop should decrease by 1", 
            initialShopCount - 1, shop.getNumAvailable(Card.Type.ETHEREUM));
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_InsufficientMoney_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player buys but not enough money
        activePlayer.adjustMoney(1);
        activePlayer.adjustBuys(1);
        
        // Try to buy FRAMEWORK (costs 8)
        BuyDecision decision = new BuyDecision(Card.Type.FRAMEWORK);
        
        try {
            invokePrivateMethod(engine, "processBuyDecision", 
                new Class<?>[]{BuyDecision.class}, decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_NoBuysRemaining_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player money but no buys (turnReset gives 1 buy, so adjust by -1)
        activePlayer.turnReset();
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(-1);
        assertEquals("Should have 0 buys", 0, activePlayer.numBuys());
        
        BuyDecision decision = new BuyDecision(Card.Type.BITCOIN);
        
        try {
            invokePrivateMethod(engine, "processBuyDecision", 
                new Class<?>[]{BuyDecision.class}, decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_CardOutOfStock_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(1);
        
        // Sell out all FRAMEWORKs
        while (shop.getNumAvailable(Card.Type.FRAMEWORK) > 0) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }
        
        BuyDecision decision = new BuyDecision(Card.Type.FRAMEWORK);
        
        try {
            invokePrivateMethod(engine, "processBuyDecision", 
                new Class<?>[]{BuyDecision.class}, decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    // ========== processTrashCardDecision Tests ==========

    @Test
    public void testProcessTrashCardDecision_ValidTrash_RemovesCard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Get a card from hand
        List<Card> unplayedCards = activePlayer.getUnplayedCards();
        Card cardToTrash = unplayedCards.get(0);
        
        int initialHandSize = unplayedCards.size();
        
        TrashCardDecision decision = new TrashCardDecision(cardToTrash);
        invokePrivateMethod(engine, "processTrashCardDecision", 
            new Class<?>[]{TrashCardDecision.class}, decision);
        
        int newHandSize = activePlayer.getUnplayedCards().size();
        assertEquals("Hand should lose one card", initialHandSize - 1, newHandSize);
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessTrashCardDecision_CardNotInHand_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Create a card that's not in the player's hand
        Card fakeCard = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        
        TrashCardDecision decision = new TrashCardDecision(fakeCard);
        
        try {
            invokePrivateMethod(engine, "processTrashCardDecision", 
                new Class<?>[]{TrashCardDecision.class}, decision);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    // ========== processGainingCard Tests ==========

    @Test
    public void testProcessGainingCard_ValidGain_AddsCardToDiscard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        List<Card> discard = getPrivateField(activePlayer, "discard");
        int initialDiscardSize = discard.size();
        
        boolean result = invokePrivateMethod(engine, "processGainingCard", 
            new Class<?>[]{Card.Type.class, PlayerInfo.class}, 
            Card.Type.BITCOIN, activePlayer);
        
        assertTrue("Gaining should succeed", result);
        assertEquals("Discard should gain one card", initialDiscardSize + 1, discard.size());
    }

    @Test
    public void testProcessGainingCard_ValidGain_DecrementsShop() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        int initialShopCount = shop.getNumAvailable(Card.Type.BITCOIN);
        
        invokePrivateMethod(engine, "processGainingCard", 
            new Class<?>[]{Card.Type.class, PlayerInfo.class}, 
            Card.Type.BITCOIN, activePlayer);
        
        assertEquals("Shop should decrease by 1", 
            initialShopCount - 1, shop.getNumAvailable(Card.Type.BITCOIN));
    }

    @Test
    public void testProcessGainingCard_CardNotAvailable_ReturnsFalse() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        
        // Sell out all FRAMEWORKs
        while (shop.getNumAvailable(Card.Type.FRAMEWORK) > 0) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }
        
        boolean result = invokePrivateMethod(engine, "processGainingCard", 
            new Class<?>[]{Card.Type.class, PlayerInfo.class}, 
            Card.Type.FRAMEWORK, activePlayer);
        
        assertFalse("Gaining should fail when card unavailable", result);
    }

    // ========== Action Card Execution Tests ==========

    @Test
    public void testExecuteActionCodeReview_DrawsCard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Ensure deck has cards
        List<Card> deck = getPrivateField(activePlayer, "deck");
        if (deck.isEmpty()) {
            // Add a card to deck if empty
            AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }
        
        int initialHandSize = activePlayer.getUnplayedCards().size();
        
        invokePrivateMethod(engine, "executeActionCodeReview", new Class<?>[]{});
        
        int newHandSize = activePlayer.getUnplayedCards().size();
        assertEquals("Should draw 1 card", initialHandSize + 1, newHandSize);
    }

    @Test
    public void testExecuteActionCodeReview_AddsTwoActions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        int initialActions = activePlayer.numActions();
        
        invokePrivateMethod(engine, "executeActionCodeReview", new Class<?>[]{});
        
        int newActions = activePlayer.numActions();
        assertEquals("Should add 2 actions", initialActions + 2, newActions);
    }

    @Test
    public void testExecuteActionEvergreenTest_DrawsTwoCards() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Ensure deck has enough cards
        List<Card> deck = getPrivateField(activePlayer, "deck");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }
        
        int initialHandSize = activePlayer.getUnplayedCards().size();
        
        invokePrivateMethod(engine, "executeActionEvergreenTest", new Class<?>[]{});
        
        int newHandSize = activePlayer.getUnplayedCards().size();
        assertEquals("Should draw 2 cards", initialHandSize + 2, newHandSize);
    }

    @Test
    public void testExecuteActionEvergreenTest_GivesBugToOtherPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Get initial discard sizes of other players
        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        List<Card> player3Discard = getPrivateField(players.get(2), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();
        int initialPlayer3DiscardSize = player3Discard.size();
        
        invokePrivateMethod(engine, "executeActionEvergreenTest", new Class<?>[]{});
        
        assertEquals("Player 2 should gain a BUG", 
            initialPlayer2DiscardSize + 1, player2Discard.size());
        assertEquals("Player 3 should gain a BUG", 
            initialPlayer3DiscardSize + 1, player3Discard.size());
    }

    @Test
    public void testExecuteActionEvergreenTest_SkipsDisqualifiedPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Disqualify player 2
        players.get(1).forfeit();
        
        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        List<Card> player3Discard = getPrivateField(players.get(2), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();
        int initialPlayer3DiscardSize = player3Discard.size();
        
        invokePrivateMethod(engine, "executeActionEvergreenTest", new Class<?>[]{});
        
        assertEquals("Disqualified player should not gain BUG", 
            initialPlayer2DiscardSize, player2Discard.size());
        assertEquals("Active player 3 should gain a BUG", 
            initialPlayer3DiscardSize + 1, player3Discard.size());
    }

    // ========== Player Disqualification Tests ==========

    @Test
    public void testHandleCheating_DisqualifiesPlayer() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo cheater = players.get(0);
        
        assertFalse("Player should not be disqualified initially", cheater.isDisqualified());
        
        invokePrivateMethod(engine, "handleCheating", 
            new Class<?>[]{PlayerInfo.class, edu.brandeis.cosi.atg.engine.PlayerViolationException.class},
            cheater, new edu.brandeis.cosi.atg.engine.PlayerViolationException("test violation"));
        
        assertTrue("Player should be disqualified after cheating", cheater.isDisqualified());
    }

    // ========== endGame Tests ==========

    @Test
    public void testEndGame_ReturnsGameResult() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        setPrivateField(engine, "activePlayer", players.get(0));
        
        GameResult result = invokePrivateMethod(engine, "endGame", new Class<?>[]{});
        
        assertNotNull("Should return a GameResult", result);
    }

    @Test
    public void testEndGame_IncludesAllPlayers() throws Exception {
        OurEngine engine = new OurEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        setPrivateField(engine, "activePlayer", players.get(0));
        
        GameResult result = invokePrivateMethod(engine, "endGame", new Class<?>[]{});
        
        assertEquals("Should include all players in result", 3, result.playerResults().size());
    }

    @Test
    public void testEndGame_SortsPlayersByScore() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        
        // Give player 2 a higher score (add victory cards)
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        for (int i = 0; i < 3; i++) {
            players.get(1).gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        }
        
        GameResult result = invokePrivateMethod(engine, "endGame", new Class<?>[]{});
        
        // First player in results should have higher score
        PlayerResult first = result.playerResults().get(0);
        PlayerResult second = result.playerResults().get(1);
        
        assertTrue("Results should be sorted by score descending", 
            first.score() >= second.score());
    }

    // ========== Integration-Style Tests ==========

    @Test
    public void testPlay_GameCompletesSuccessfully() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        GameResult result = engine.play();
        
        assertNotNull("Should return a game result", result);
        assertEquals("Should have results for both players", 2, result.playerResults().size());
    }

    @Test
    public void testPlay_AllPlayersHaveFinalDecks() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        GameResult result = engine.play();
        
        for (PlayerResult pr : result.playerResults()) {
            assertNotNull("Each player should have a final deck", pr.endingDeck());
            assertFalse("Final deck should not be empty", pr.endingDeck().isEmpty());
        }
    }

    @Test
    public void testGetNumPlayers_ReturnsCorrectCount() {
        OurEngine engine = new OurEngine(threePlayers);
        assertEquals("Should return correct player count", 3, engine.getNumPlayers());
    }

    @Test
    public void testGetTurnLogs_ReturnsNonNullList() {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<TurnLog> logs = engine.getTurnLogs();
        
        assertNotNull("Turn logs should not be null", logs);
    }

    // ========== buildTrashCardDecisionsForCurrentHand Tests ==========

    @Test
    public void testBuildTrashCardDecisions_EmptyHand_ReturnsEndPhaseDecision() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Empty the hand
        List<Card> allCards = new ArrayList<>(activePlayer.getUnplayedCards());
        for (Card card : allCards) {
            activePlayer.playCard(card);
        }
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, 
            "buildTrashCardDecisionsForCurrentHand", new Class<?>[]{});
        
        assertEquals("Should have one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildTrashCardDecisions_NonEmptyHand_ReturnsTrashDecisions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        int handSize = activePlayer.getUnplayedCards().size();
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, 
            "buildTrashCardDecisionsForCurrentHand", new Class<?>[]{});
        
        assertEquals("Should have TrashCardDecision for each card in hand", 
            handSize, decisions.size());
        
        for (Decision decision : decisions) {
            assertTrue("All decisions should be TrashCardDecision", 
                decision instanceof TrashCardDecision);
        }
    }

    // ========== processPlayActionCard Tests ==========

    @Test(expected = IllegalStateException.class)
    public void testProcessPlayActionCard_NoActionsRemaining_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Set actions to exactly 0 (turnReset gives 1, so adjust by -1)
        activePlayer.turnReset();
        activePlayer.adjustActions(-1);
        assertEquals("Should have 0 actions", 0, activePlayer.numActions());
        
        try {
            invokePrivateMethod(engine, "processPlayActionCard", 
                new Class<?>[]{Card.Type.class}, Card.Type.CODE_REVIEW);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testProcessPlayActionCard_ValidAction_DecrementsActions() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player actions
        activePlayer.adjustActions(3);
        int initialActions = activePlayer.numActions();
        
        invokePrivateMethod(engine, "processPlayActionCard", 
            new Class<?>[]{Card.Type.class}, Card.Type.CODE_REVIEW);
        
        // CODE_REVIEW gives +2 actions, but we should decrement by 1 at the end
        // So net effect is +1 action (starts with 3, +2 from card effect, -1 from playing = 4)
        int expectedActions = initialActions + 2 - 1;
        assertEquals("Actions should be correctly adjusted", 
            expectedActions, activePlayer.numActions());
    }

    // ========== processPlayMoneyCard Tests ==========

    @Test
    public void testProcessPlayMoneyCard_Bitcoin_AddsOneMoney() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        int initialMoney = activePlayer.numMoney();
        
        invokePrivateMethod(engine, "processPlayMoneyCard", 
            new Class<?>[]{Card.Type.class}, Card.Type.BITCOIN);
        
        assertEquals("BITCOIN should add 1 money", initialMoney + 1, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayMoneyCard_Ethereum_AddsTwoMoney() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        int initialMoney = activePlayer.numMoney();
        
        invokePrivateMethod(engine, "processPlayMoneyCard", 
            new Class<?>[]{Card.Type.class}, Card.Type.ETHEREUM);
        
        assertEquals("ETHEREUM should add 2 money", initialMoney + 2, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayMoneyCard_Dogecoin_AddsThreeMoney() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        int initialMoney = activePlayer.numMoney();
        
        invokePrivateMethod(engine, "processPlayMoneyCard", 
            new Class<?>[]{Card.Type.class}, Card.Type.DOGECOIN);
        
        assertEquals("DOGECOIN should add 3 money", initialMoney + 3, activePlayer.numMoney());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProcessPlayMoneyCard_NonMoneyCard_ThrowsException() throws Throwable {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        setPrivateField(engine, "activePlayer", players.get(0));
        
        try {
            invokePrivateMethod(engine, "processPlayMoneyCard", 
                new Class<?>[]{Card.Type.class}, Card.Type.CODE_REVIEW);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    // ========== executeActionRefactor Tests ==========

    @Test
    public void testExecuteActionRefactor_TrashAndGain_Success() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Get the hand and add a BITCOIN to trash (cost 3)
        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card bitcoinToTrash = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoinToTrash);
        
        int initialHandSize = activePlayer.getUnplayedCards().size();
        
        // Execute REFACTOR - it will prompt for trash, then gain
        // BigMoneyPlayer should make valid decisions
        invokePrivateMethod(engine, "executeActionRefactor", new Class<?>[]{});
        
        // After trash and gain, hand size should be same (removed 1, gained 1 to discard)
        // But card was gained to discard, not hand
        int finalHandSize = activePlayer.getUnplayedCards().size();
        assertEquals("Hand should have one less card after trashing", initialHandSize - 1, finalHandSize);
    }

    @Test
    public void testExecuteActionRefactor_NoCardsInHand_ReturnsEarly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        
        // Clear the hand completely
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        
        // Execute REFACTOR with empty hand - should return early without error
        invokePrivateMethod(engine, "executeActionRefactor", new Class<?>[]{});
        
        // Verify hand is still empty and no exception was thrown
        assertEquals("Hand should still be empty", 0, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionRefactor_CostCalculation_AllowsCardsUpToCostPlusTwo() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Add a DOGECOIN (cost 0) to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card dogecoin = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(dogecoin);
        
        // Execute REFACTOR - trash DOGECOIN (cost 0), can gain cards costing up to 2
        // BigMoneyPlayer should gain a card
        invokePrivateMethod(engine, "executeActionRefactor", new Class<?>[]{});
        
        // Verify a card was gained from the shop
        List<Card> discard = getPrivateField(activePlayer, "discard");
        assertTrue("Should have gained a card to discard", discard.size() > 0);
    }

    @Test
    public void testExecuteActionRefactor_TrashExpensiveCard_CanGainExpensiveReplacement() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Add a FRAMEWORK (cost 8) to hand
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card framework = new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(framework);
        
        // Execute REFACTOR - trash FRAMEWORK (cost 8), can gain cards costing up to 10
        invokePrivateMethod(engine, "executeActionRefactor", new Class<?>[]{});
        
        // Verify the FRAMEWORK was trashed (not in hand or discard)
        List<Card> discard = getPrivateField(activePlayer, "discard");
        boolean frameworkInDiscard = discard.stream().anyMatch(c -> c.type() == Card.Type.FRAMEWORK);
        assertFalse("Trashed FRAMEWORK should not be in discard", frameworkInDiscard);
        
        // Should have gained something
        assertTrue("Should have gained a card", discard.size() > 0);
    }

    @Test
    public void testExecuteActionRefactor_PhasesTransition_GainPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Add a card to trash
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoin);
        
        // Execute REFACTOR
        invokePrivateMethod(engine, "executeActionRefactor", new Class<?>[]{});
        
        // After REFACTOR completes, phase should be back to ACTION
        GameState.TurnPhase currentPhase = getPrivateField(engine, "phase");
        assertEquals("Phase should return to ACTION after REFACTOR", GameState.TurnPhase.ACTION, currentPhase);
    }

    // ========== Card Drawing Edge Case Tests ==========

    @Test
    public void testDrawCard_EmptyDeck_ReshufflesDiscard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Empty the deck and put cards in discard
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        deck.clear();
        
        // Add 3 cards to discard
        discard.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        discard.add(new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement()));
        discard.add(new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement()));
        
        int discardSizeBefore = discard.size();
        
        // Draw a card - should reshuffle discard into deck
        activePlayer.drawCard();
        
        // Discard should now be empty, deck should have remaining cards
        assertEquals("Discard should be empty after reshuffle", 0, discard.size());
        assertEquals("Deck should have remaining cards", discardSizeBefore - 1, deck.size());
    }

    @Test
    public void testDrawCard_BothDeckAndDiscardEmpty_NoCardDrawn() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        
        // Empty both deck and discard
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        deck.clear();
        discard.clear();
        
        int handSizeBefore = activePlayer.getUnplayedCards().size();
        
        // Try to draw - should not crash, just not add a card
        activePlayer.drawCard();
        
        int handSizeAfter = activePlayer.getUnplayedCards().size();
        assertEquals("Hand size should not change when no cards available", handSizeBefore, handSizeAfter);
    }

    @Test
    public void testDrawMultipleCards_InsufficientCards_DrawsWhatAvailable() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Set up deck with only 1 card, discard with 1 card
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        deck.clear();
        discard.clear();
        
        deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        discard.add(new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement()));
        
        int handSizeBefore = activePlayer.getUnplayedCards().size();
        
        // Try to draw 3 cards (like EVERGREEN_TEST would)
        activePlayer.drawCard();
        activePlayer.drawCard();
        activePlayer.drawCard(); // This one finds no cards
        
        int handSizeAfter = activePlayer.getUnplayedCards().size();
        // Should have drawn only 2 cards (1 from deck, 1 after reshuffle from discard)
        assertEquals("Should draw only available cards", handSizeBefore + 2, handSizeAfter);
    }

    @Test
    public void testDrawCard_ReshufflePreservesCards_NoDuplication() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Empty deck, put specific cards in discard
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        deck.clear();
        discard.clear();
        
        Card card1 = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        Card card2 = new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement());
        Card card3 = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        discard.add(card1);
        discard.add(card2);
        discard.add(card3);
        
        // Draw all 3 cards
        activePlayer.drawCard();
        activePlayer.drawCard();
        activePlayer.drawCard();
        
        // Verify hand has exactly 3 new cards (plus whatever was there before)
        List<Card> unplayed = activePlayer.getUnplayedCards();
        long bitcoinCount = unplayed.stream().filter(c -> c.id() == card1.id()).count();
        long ethereumCount = unplayed.stream().filter(c -> c.id() == card2.id()).count();
        long dogecoinCount = unplayed.stream().filter(c -> c.id() == card3.id()).count();
        
        assertEquals("Should have drawn BITCOIN exactly once", 1, bitcoinCount);
        assertEquals("Should have drawn ETHEREUM exactly once", 1, ethereumCount);
        assertEquals("Should have drawn DOGECOIN exactly once", 1, dogecoinCount);
    }

    // ========== Money Phase Edge Case Tests ==========

    @Test
    public void testMoneyPhase_PlayAllMoneyCardsSequentially_AccumulatesMoney() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Clear hand and add specific money cards
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        activePlayer.turnReset();
        
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement());
        Card dogecoin = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoin);
        hand.addToUnplayed(ethereum);
        hand.addToUnplayed(dogecoin);
        
        // Play all money cards
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.BITCOIN);
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.ETHEREUM);
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.DOGECOIN);
        
        // Total money should be 1 + 2 + 3 = 6
        int totalMoney = activePlayer.numMoney();
        assertEquals("Money should accumulate correctly", 6, totalMoney);
    }

    @Test
    public void testMoneyPhase_MultipleSameTypeCards_EachAddsValue() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        
        activePlayer.turnReset();
        
        // Play 3 DOGECOINs (3 money each)
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.DOGECOIN);
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.DOGECOIN);
        invokePrivateMethod(engine, "processPlayMoneyCard", new Class<?>[]{Card.Type.class}, Card.Type.DOGECOIN);
        
        // Total money should be 3 + 3 + 3 = 9
        assertEquals("Multiple cards of same type should accumulate", 9, activePlayer.numMoney());
    }

    @Test
    public void testBuildMoneyDecisions_AllCardsAreMoney_IncludesAllPlusEndPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Clear hand and add only money cards
        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        
        hand.addToUnplayed(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        hand.addToUnplayed(new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement()));
        hand.addToUnplayed(new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement()));
        
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildMoneyDecisions", 
            new Class<?>[]{});
        
        // Should have 3 PlayCardDecisions + 1 EndPhaseDecision = 4 total
        assertEquals("Should have decision for each money card plus EndPhase", 4, decisions.size());
        
        long playCardCount = decisions.stream()
            .filter(d -> d instanceof PlayCardDecision)
            .count();
        assertEquals("Should have 3 PlayCardDecisions", 3, playCardCount);
    }

    // ========== Score Calculation Tests ==========

    @Test
    public void testEndGame_ScoreCalculation_MethodCardsCountedCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points from 3 METHOD cards", 3, initialScore);
        
        // Gain 2 more METHOD cards (should add 2 more points)
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        
        // Score should now be 5 (3 starting + 2 gained)
        assertEquals("5 METHOD cards should give 5 points", 5, player.getScore());
    }

    @Test
    public void testEndGame_ScoreCalculation_ModuleCardsCountedCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);
        
        // Gain 3 MODULE cards (3 VP each = 9 VP)
        for (int i = 0; i < 3; i++) {
            player.gainCard(new Card(Card.Type.MODULE, cardIdCounter.getAndIncrement()));
        }
        
        // Score should now be 12 (3 starting + 9 from MODULEs)
        assertEquals("3 MODULE cards should add 9 points", 12, player.getScore());
    }

    @Test
    public void testEndGame_ScoreCalculation_OnlyTreasureCards_ZeroPoints() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);
        
        // Gain only treasure cards (should not change score)
        player.gainCard(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement()));
        
        // Score should still be 3 (treasure cards have 0 VP)
        assertEquals("Treasure cards should not add points", 3, player.getScore());
    }

    @Test
    public void testEndGame_ScoreCalculation_BugCardsPenalty() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);
        
        // Add 3 METHOD cards (3 VP) and 2 BUG cards (-2 VP) = net +1 VP
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.BUG, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.BUG, cardIdCounter.getAndIncrement()));
        
        // Score should now be 4 (3 starting + 3 from METHODs - 2 from BUGs)
        assertEquals("BUG cards should subtract points correctly", 4, player.getScore());
    }

    @Test
    public void testEndGame_ScoreCalculation_FrameworkCardsCountedCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);
        
        // Gain 2 FRAMEWORK cards (6 VP each = 12 VP)
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        
        // Score should now be 15 (3 starting + 12 from FRAMEWORKs)
        assertEquals("2 FRAMEWORK cards should add 12 points", 15, player.getScore());
    }

    @Test
    public void testEndGame_ScoreCalculation_MixedVictoryCardsWithFramework() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        
        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);
        
        // Gain mixed victory cards: 1 METHOD (1 VP), 2 FRAMEWORK (12 VP), 1 MODULE (3 VP) = 16 VP total
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.MODULE, cardIdCounter.getAndIncrement()));
        
        // Score should now be 19 (3 starting + 16 gained)
        assertEquals("Mixed cards with FRAMEWORKs should sum correctly", 19, player.getScore());
    }

    // ========== Phase Transition Tests ==========

    @Test
    public void testProcessEndPhaseDecision_ActionToMoney_TransitionsCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from ACTION to MONEY", GameState.TurnPhase.MONEY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_MoneyToBuy_TransitionsCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.MONEY);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from MONEY to BUY", GameState.TurnPhase.BUY, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_BuyToCleanup_TransitionsCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        setPrivateField(engine, "phase", GameState.TurnPhase.BUY);
        
        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.BUY);
        
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, decision);
        
        GameState.TurnPhase newPhase = getPrivateField(engine, "phase");
        assertEquals("Should transition from BUY to CLEANUP", GameState.TurnPhase.CLEANUP, newPhase);
    }

    @Test
    public void testProcessEndPhaseDecision_AllPhases_CompleteSequence() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        
        // Start at ACTION phase
        setPrivateField(engine, "phase", GameState.TurnPhase.ACTION);
        EndPhaseDecision actionEnd = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, actionEnd);
        assertEquals("After ACTION phase", GameState.TurnPhase.MONEY, 
            (GameState.TurnPhase) getPrivateField(engine, "phase"));
        
        // MONEY to BUY
        EndPhaseDecision moneyEnd = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, moneyEnd);
        assertEquals("After MONEY phase", GameState.TurnPhase.BUY, 
            (GameState.TurnPhase) getPrivateField(engine, "phase"));
        
        // BUY to CLEANUP
        EndPhaseDecision buyEnd = new EndPhaseDecision(GameState.TurnPhase.BUY);
        invokePrivateMethod(engine, "processEndPhaseDecision", 
            new Class<?>[]{EndPhaseDecision.class}, buyEnd);
        assertEquals("After BUY phase", GameState.TurnPhase.CLEANUP, 
            (GameState.TurnPhase) getPrivateField(engine, "phase"));
    }

    // ========== Multiple Sequential Buys Tests ==========

    @Test
    public void testProcessBuyDecision_MultipleBuys_DecrementsCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Give player money and multiple buys
        activePlayer.turnReset();
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(1); // Total 2 buys (1 from reset + 1)
        
        assertEquals("Should start with 2 buys", 2, activePlayer.numBuys());
        assertEquals("Should start with 10 money", 10, activePlayer.numMoney());
        
        // First buy: ETHEREUM (cost 3)
        BuyDecision firstBuy = new BuyDecision(Card.Type.ETHEREUM);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, firstBuy);
        
        assertEquals("Should have 1 buy remaining", 1, activePlayer.numBuys());
        assertEquals("Should have 7 money remaining", 7, activePlayer.numMoney());
        
        // Second buy: BITCOIN (cost 1)
        BuyDecision secondBuy = new BuyDecision(Card.Type.BITCOIN);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, secondBuy);
        
        assertEquals("Should have 0 buys remaining", 0, activePlayer.numBuys());
        assertEquals("Should have 7 money remaining", 7, activePlayer.numMoney());
    }

    @Test
    public void testBuildBuyDecisions_AfterFirstBuy_RebuildsProperly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Setup: 2 buys, 5 money
        activePlayer.turnReset();
        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1); // Total 2 buys
        
        // Build decisions before first buy
        ImmutableList<Decision> decisionsBeforeBuy = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        long buyOptionsBeforeBuy = decisionsBeforeBuy.stream()
            .filter(d -> d instanceof BuyDecision)
            .count();
        assertTrue("Should have buy options with 5 money", buyOptionsBeforeBuy > 0);
        
        // Make a buy (ETHEREUM costs 2)
        BuyDecision buy = new BuyDecision(Card.Type.ETHEREUM);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, buy);
        
        // Build decisions after first buy
        ImmutableList<Decision> decisionsAfterBuy = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        // Should still have decisions (still has 1 buy and 3 money remaining)
        assertTrue("Should still have buy options", decisionsAfterBuy.size() > 1);
        boolean hasEndPhase = decisionsAfterBuy.stream()
            .anyMatch(d -> d instanceof EndPhaseDecision);
        assertTrue("Should always have EndPhaseDecision", hasEndPhase);
    }

    @Test
    public void testProcessBuyDecision_ExhaustAllBuys_DecisionsOnlyShowEndPhase() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        setPrivateField(engine, "activePlayer", activePlayer);
        
        // Setup: exactly 1 buy, plenty of money
        activePlayer.turnReset(); // Gives 1 buy
        activePlayer.adjustMoney(10);
        
        // Use the only buy
        BuyDecision buy = new BuyDecision(Card.Type.BITCOIN);
        invokePrivateMethod(engine, "processBuyDecision", 
            new Class<?>[]{BuyDecision.class}, buy);
        
        assertEquals("Should have 0 buys remaining", 0, activePlayer.numBuys());
        
        // Build decisions after exhausting buys
        ImmutableList<Decision> decisions = invokePrivateMethod(engine, "buildBuyDecisions", 
            new Class<?>[]{});
        
        assertEquals("Should only have EndPhaseDecision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    // ========== Cleanup Phase Tests ==========

    @Test
    public void testCleanup_DiscardsAllCards_DrawsFiveNew() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        
        // Get initial state
        List<Card> initialHandCards = new ArrayList<>(activePlayer.getUnplayedCards());
        
        // Perform cleanup
        activePlayer.cleanup();
        
        // Verify hand has exactly 5 cards
        assertEquals("Hand should have exactly 5 cards after cleanup", 5, activePlayer.getUnplayedCards().size());
        
        // Verify old cards are in discard
        List<Card> currentDiscard = getPrivateField(activePlayer, "discard");
        for (Card oldCard : initialHandCards) {
            assertTrue("Old hand cards should be in discard", currentDiscard.contains(oldCard));
        }
        
        // Verify new hand has different cards
        List<Card> newHandCards = activePlayer.getUnplayedCards();
        for (Card newCard : newHandCards) {
            assertFalse("New hand should not contain old cards", initialHandCards.contains(newCard));
        }
    }

    @Test
    public void testCleanup_WithPlayedCards_DiscardsAll() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        
        // Play some cards
        List<Card> unplayed = new ArrayList<>(activePlayer.getUnplayedCards());
        if (unplayed.size() >= 2) {
            activePlayer.playCard(unplayed.get(0));
            activePlayer.playCard(unplayed.get(1));
        }
        
        MutableHand hand = getPrivateField(activePlayer, "hand");
        int totalCardsInHand = hand.size(); // Both played and unplayed
        
        List<Card> discard = getPrivateField(activePlayer, "discard");
        int initialDiscardSize = discard.size();
        
        // Perform cleanup
        activePlayer.cleanup();
        
        // Verify all cards (both played and unplayed) moved to discard
        List<Card> finalDiscard = getPrivateField(activePlayer, "discard");
        assertEquals("All cards should be discarded", 
            initialDiscardSize + totalCardsInHand, finalDiscard.size());
        
        // Verify new hand is exactly 5 cards
        assertEquals("Should draw exactly 5 new cards", 5, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testTurnReset_ResetsResourcesCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        
        // Modify resources
        activePlayer.adjustActions(5);
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(3);
        
        // Verify modified
        assertTrue("Actions should be modified", activePlayer.numActions() > 1);
        assertTrue("Money should be modified", activePlayer.numMoney() > 0);
        assertTrue("Buys should be modified", activePlayer.numBuys() > 1);
        
        // Reset turn
        activePlayer.turnReset();
        
        // Verify reset to starting values
        assertEquals("Actions should reset to 1", 1, activePlayer.numActions());
        assertEquals("Money should reset to 0", 0, activePlayer.numMoney());
        assertEquals("Buys should reset to 1", 1, activePlayer.numBuys());
    }

    @Test
    public void testCleanup_WithEmptyDeck_ReshufflesDiscard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = new OurEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        
        // Empty the deck completely
        List<Card> deck = getPrivateField(activePlayer, "deck");
        List<Card> discard = getPrivateField(activePlayer, "discard");
        discard.addAll(deck);
        deck.clear();
        
        // Verify deck is empty
        assertEquals("Deck should be empty", 0, deck.size());
        int discardSizeBefore = discard.size();
        
        // Perform cleanup (should reshuffle discard into deck to draw)
        activePlayer.cleanup();
        
        // Verify hand has 5 cards
        assertEquals("Should draw 5 cards", 5, activePlayer.getUnplayedCards().size());
        
        // Verify cards were taken from discard (via reshuffle)
        List<Card> finalDiscard = getPrivateField(activePlayer, "discard");
        assertTrue("Discard should have fewer cards after cleanup/reshuffle", 
            finalDiscard.size() < discardSizeBefore);
    }
}
