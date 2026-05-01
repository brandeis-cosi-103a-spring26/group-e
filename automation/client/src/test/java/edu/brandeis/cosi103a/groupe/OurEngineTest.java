package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.main.actions.*;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import edu.brandeis.cosi103a.groupe.player.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the OurEngine class. Tests constructor validation, game mechanics,
 * decision processing, action card execution, and game orchestration logic.
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

    /** Creates a list of BigMoneyPlayer instances. */
    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new BigMoneyPlayer());
        }
        return players;
    }

    private List<Card.Type> defaultActions() {
        return List.of(
                Card.Type.CODE_REVIEW,
                Card.Type.REFACTOR,
                Card.Type.EVERGREEN_TEST,
                Card.Type.IPO,
                Card.Type.MONITORING,
                Card.Type.HACK,
                Card.Type.PARALLELIZATION,
                Card.Type.TECH_DEBT,
                Card.Type.BACKLOG,
                Card.Type.DAILY_SCRUM);
    }

    private OurEngine createEngine(List<Player> players) {
        return new OurEngine(players, defaultActions());
    }

    /** Uses reflection to access a private field from an object. */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    /** Uses reflection to set a private field in an object. */
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * Creates a TurnRunner backed by the same players/shop as the given engine, so tests can call
     * TurnRunner methods directly without going through a full turn.
     */
    private TurnRunner createTurnRunner(OurEngine engine) throws Exception {
        List<PlayerInfo> players = getPrivateField(engine, "players");
        MutableCardStacks shop = getPrivateField(engine, "shop");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        ShopManager shopManager = new ShopManager(shop, cardIdCounter);
        ActionEffectRegistry registry =
                new ActionEffectRegistry(
                        java.util.List.of(
                                new CodeReviewEffect(),
                                new RefactorEffect(),
                                new EvergreenTestEffect(),
                                new IPOEffect(),
                                new MonitoringEffect(),
                                new DailyScrumEffect()));
        return new TurnRunner(players, shopManager, registry, new MegaObserver());
    }

    /** Creates a ShopManager backed by the same MutableCardStacks as the given engine. */
    private ShopManager createShopManager(OurEngine engine) throws Exception {
        MutableCardStacks shop = getPrivateField(engine, "shop");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        return new ShopManager(shop, cardIdCounter);
    }

    /** Creates an ActionContext for direct effect testing. */
    private ActionContext createActionContext(
            PlayerInfo activePlayer,
            List<PlayerInfo> players,
            ShopManager shopManager,
            GameState.TurnPhase initialPhase) {
        return new ActionContext(
                activePlayer, players, shopManager, null, initialPhase, new MegaObserver());
    }

    // ========== Constructor Validation Tests ==========

    @Test
    public void testConstructor_TwoPlayers_Success() {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        assertNotNull("Engine should be created successfully", engine);
    }

    @Test
    public void testConstructor_ThreePlayers_Success() {
        OurEngine engine = createEngine(threePlayers);
        assertNotNull("Engine should be created successfully", engine);
    }

    @Test
    public void testConstructor_FourPlayers_Success() {
        OurEngine engine = createEngine(fourPlayers);
        assertNotNull("Engine should be created successfully", engine);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_OnePlayer_ThrowsException() {
        createEngine(createPlayers(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroPlayers_ThrowsException() {
        createEngine(createPlayers(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_FivePlayers_ThrowsException() {
        createEngine(createPlayers(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullPlayersList_ThrowsException() {
        createEngine(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ListContainsNullPlayer_ThrowsException() {
        List<Player> playersWithNull = new ArrayList<>();
        playersWithNull.add(new BigMoneyPlayer());
        playersWithNull.add(null);
        createEngine(playersWithNull);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullActions_ThrowsException() {
        new OurEngine(createPlayers(2), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ActionsContainNull_ThrowsException() {
        List<Card.Type> actions = new ArrayList<>(defaultActions());
        actions.set(0, null);
        new OurEngine(createPlayers(2), actions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ActionsNotTen_ThrowsException() {
        List<Card.Type> actions = new ArrayList<>(defaultActions().subList(0, 9));
        new OurEngine(createPlayers(2), actions);
    }

    @Test
    public void testConstructor_InitializesPhaseToAction() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        GameState.TurnPhase phase = getPrivateField(engine, "phase");
        assertEquals("Initial phase should be ACTION", GameState.TurnPhase.ACTION, phase);
    }

    @Test
    public void testConstructor_CreatesPlayerInfoForAllPlayers() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> playerInfos = getPrivateField(engine, "players");

        assertEquals("Should have PlayerInfo instances for all players", 3, playerInfos.size());

        for (int i = 0; i < threePlayers.size(); i++) {
            PlayerInfo info = playerInfos.get(i);
            assertNotNull("PlayerInfo should not be null", info);
            assertEquals(
                    "PlayerInfo should wrap the correct player",
                    threePlayers.get(i).getName(),
                    info.getName());
        }
    }

    @Test
    public void testConstructor_InitializesActivePlayerToLastPlayer() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        PlayerInfo activePlayer = getPrivateField(engine, "activePlayer");
        List<PlayerInfo> players = getPrivateField(engine, "players");

        // activePlayer should be last so getNextPlayer() returns first
        assertEquals(
                "Active player should be last player initially",
                players.get(players.size() - 1),
                activePlayer);
    }

    @Test
    public void testConstructor_EachPlayerStartsWithFiveCardHand() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        for (PlayerInfo playerInfo : players) {
            assertEquals(
                    "Each player should start with 5 cards in hand",
                    5,
                    playerInfo.getUnplayedCards().size());
        }
    }

    @Test
    public void testConstructor_EachPlayerStartsWithFiveCardsInDeck() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        for (PlayerInfo playerInfo : players) {
            // Each player starts with 10 cards total: 5 in hand, 5 in deck
            // Use reflection to get deck size
            List<Card> deck = getPrivateField(playerInfo, "deck");
            assertEquals("Each player should have 5 cards remaining in deck", 5, deck.size());
        }
    }

    @Test
    public void testConstructor_VerifyStartingCardsComposition() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals("BITCOIN should have 60", 60, shop.getNumAvailable(Card.Type.BITCOIN));
        assertEquals("ETHEREUM should have 40", 40, shop.getNumAvailable(Card.Type.ETHEREUM));
        assertEquals("DOGECOIN should have 30", 30, shop.getNumAvailable(Card.Type.DOGECOIN));
        assertEquals(
                "METHOD should have 14 (2 players * 7 starting)",
                14,
                shop.getNumAvailable(Card.Type.METHOD));
        assertEquals("MODULE should have 8", 8, shop.getNumAvailable(Card.Type.MODULE));
        assertEquals("FRAMEWORK should have 8", 8, shop.getNumAvailable(Card.Type.FRAMEWORK));
        assertEquals(
                "BUG should have 20 (2 players * 10)", 20, shop.getNumAvailable(Card.Type.BUG));
        assertEquals("CODE_REVIEW should have 10", 10, shop.getNumAvailable(Card.Type.CODE_REVIEW));
        assertEquals("REFACTOR should have 10", 10, shop.getNumAvailable(Card.Type.REFACTOR));
        assertEquals(
                "EVERGREEN_TEST should have 10",
                10,
                shop.getNumAvailable(Card.Type.EVERGREEN_TEST));
        assertEquals("MONITORING should have 10", 10, shop.getNumAvailable(Card.Type.MONITORING));
        assertEquals("HACK should have 10", 10, shop.getNumAvailable(Card.Type.HACK));
        assertEquals("TECH_DEBT should have 10", 10, shop.getNumAvailable(Card.Type.TECH_DEBT));
        assertEquals("BACKLOG should have 10", 10, shop.getNumAvailable(Card.Type.BACKLOG));
        assertEquals("DAILY_SCRUM should have 10", 10, shop.getNumAvailable(Card.Type.DAILY_SCRUM));
        assertEquals(
                "SPRINT_PLANNING should have 0",
                0,
                shop.getNumAvailable(Card.Type.SPRINT_PLANNING));
        assertEquals("RANSOMWARE should have 0", 0, shop.getNumAvailable(Card.Type.RANSOMWARE));
        assertEquals(
                "MERGE_CONFLICT should have 0", 0, shop.getNumAvailable(Card.Type.MERGE_CONFLICT));
        assertEquals("UNIT_TEST should have 0", 0, shop.getNumAvailable(Card.Type.UNIT_TEST));
        assertEquals(
                "DEPLOYMENT_PIPELINE should have 0",
                0,
                shop.getNumAvailable(Card.Type.DEPLOYMENT_PIPELINE));
    }

    @Test
    public void testConstructor_InitializesShopCorrectly_FourPlayers() throws Exception {
        OurEngine engine = createEngine(fourPlayers);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        assertEquals(
                "BUG should have 40 (4 players * 10)", 40, shop.getNumAvailable(Card.Type.BUG));
        assertEquals(
                "METHOD should have 14 (constant, not player-dependent)",
                14,
                shop.getNumAvailable(Card.Type.METHOD));
        assertEquals(
                "FRAMEWORK should have 12 for 4 players",
                12,
                shop.getNumAvailable(Card.Type.FRAMEWORK));
    }

    @Test
    public void testConstructor_InitializesCardIdCounter() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        assertNotNull("Card ID counter should be initialized", cardIdCounter);
        // After creating 20 starting cards (2 players * 10 cards each), ID should be at least 21
        assertTrue(
                "Card ID counter should be > 20 after creating starting cards",
                cardIdCounter.get() > 20);
    }

    @Test
    public void testConstructor_InitializesTurnNumber() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        int turnNumber = getPrivateField(engine, "turnNumber");

        assertEquals("Turn number should start at 0", 0, turnNumber);
    }

    @Test
    public void testConstructor_AllCardsHaveUniqueIDs() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
                assertTrue(
                        "Card ID " + card.id() + " should be unique (found duplicate in deck)",
                        wasNew);
            }
            for (Card card : discard) {
                boolean wasNew = allCardIds.add(card.id());
                assertTrue(
                        "Card ID " + card.id() + " should be unique (found duplicate in discard)",
                        wasNew);
            }
            for (Card card : hand.getAllCards()) {
                boolean wasNew = allCardIds.add(card.id());
                assertTrue(
                        "Card ID " + card.id() + " should be unique (found duplicate in hand)",
                        wasNew);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(threePlayers);
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
        OurEngine engine = createEngine(threePlayers);
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
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        // Disqualify one player (leaving only one)
        players.get(0).forfeit();

        // This should throw IllegalStateException
        engine.getNextPlayer();
    }

    @Test
    public void testGetNextPlayer_MultipleCallsCycleThroughAllPlayers() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(3);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        // Track which players we visit
        List<PlayerInfo> visitedPlayers = new ArrayList<>();

        // Simulate multiple turns (more than the number of players)
        for (int i = 0; i < 10; i++) {
            PlayerInfo nextPlayer = engine.getNextPlayer();
            visitedPlayers.add(nextPlayer);
            setPrivateField(engine, "activePlayer", nextPlayer);
        }

        // Verify all players were visited
        assertTrue("Player 0 should be visited", visitedPlayers.contains(players.get(0)));
        assertTrue("Player 1 should be visited", visitedPlayers.contains(players.get(1)));
        assertTrue("Player 2 should be visited", visitedPlayers.contains(players.get(2)));

        // Verify cycling pattern (should go 0,1,2,0,1,2,0,1,2,0)
        assertEquals("Turn 0: player 0", players.get(0), visitedPlayers.get(0));
        assertEquals("Turn 1: player 1", players.get(1), visitedPlayers.get(1));
        assertEquals("Turn 2: player 2", players.get(2), visitedPlayers.get(2));
        assertEquals("Turn 3: player 0 (cycle)", players.get(0), visitedPlayers.get(3));
        assertEquals("Turn 4: player 1 (cycle)", players.get(1), visitedPlayers.get(4));
        assertEquals("Turn 5: player 2 (cycle)", players.get(2), visitedPlayers.get(5));
    }

    @Test
    public void testGetNextPlayer_PlayerDisqualifiedMidGame_SkipsCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(4);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        // Start normal rotation: 0 -> 1 -> 2
        PlayerInfo player0 = engine.getNextPlayer();
        assertEquals("Should start with player 0", players.get(0), player0);
        setPrivateField(engine, "activePlayer", player0);

        PlayerInfo player1 = engine.getNextPlayer();
        assertEquals("Should move to player 1", players.get(1), player1);
        setPrivateField(engine, "activePlayer", player1);

        PlayerInfo player2 = engine.getNextPlayer();
        assertEquals("Should move to player 2", players.get(2), player2);
        setPrivateField(engine, "activePlayer", player2);

        // Disqualify player 3 mid-game (hasn't had a turn yet)
        players.get(3).forfeit();

        // Next call should skip player 3 and wrap to player 0
        PlayerInfo nextPlayer = engine.getNextPlayer();
        assertEquals(
                "Should skip disqualified player 3 and return to player 0",
                players.get(0),
                nextPlayer);
        setPrivateField(engine, "activePlayer", nextPlayer);

        // Continue rotation: should go to player 1 (player 3 still skipped)
        PlayerInfo afterSkip = engine.getNextPlayer();
        assertEquals(
                "Should continue to player 1, still skipping player 3", players.get(1), afterSkip);
        setPrivateField(engine, "activePlayer", afterSkip);

        // Verify pattern continues correctly
        PlayerInfo player2Again = engine.getNextPlayer();
        assertEquals("Should move to player 2", players.get(2), player2Again);
        setPrivateField(engine, "activePlayer", player2Again);

        PlayerInfo player0Again = engine.getNextPlayer();
        assertEquals(
                "Should wrap to player 0 again, still skipping player 3",
                players.get(0),
                player0Again);
    }

    // ========== isGameOver Tests ==========

    @Test
    public void testIsGameOver_InitialState_ReturnsFalse() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        assertFalse("Game should not be over initially", engine.isGameOver());
    }

    @Test
    public void testIsGameOver_FrameworksSoldOut_ReturnsTrue() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        // Disqualify all but one player
        players.get(0).forfeit();
        players.get(1).forfeit();

        assertTrue(
                "Game should be over when all but one player is disqualified", engine.isGameOver());
    }

    // ========== buildGameState Tests ==========

    @Test
    public void testBuildGameState_CorrectActivePlayerName() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo firstPlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(firstPlayer);
        runner.setPhase(GameState.TurnPhase.ACTION);

        GameState gameState = runner.buildGameState();

        assertEquals(
                "GameState should have correct active player name",
                firstPlayer.getDisplayName(),
                gameState.currentPlayerName());
    }

    @Test
    public void testBuildGameState_CorrectPhase() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(players.get(0));
        runner.setPhase(GameState.TurnPhase.BUY);

        GameState gameState = runner.buildGameState();

        assertEquals(
                "GameState should reflect current phase",
                GameState.TurnPhase.BUY,
                gameState.phase());
    }

    @Test
    public void testBuildGameState_CorrectTurnResources() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setPhase(GameState.TurnPhase.ACTION);

        activePlayer.turnReset(); // gives 1 action, 0 money, 1 buy
        activePlayer.adjustActions(2); // now 3 actions
        activePlayer.adjustMoney(5); // now 5 money

        GameState gameState = runner.buildGameState();

        assertEquals("GameState should have correct actions", 3, gameState.availableActions());
        assertEquals("GameState should have correct money", 5, gameState.spendableMoney());
        assertEquals("GameState should have correct buys", 1, gameState.availableBuys());
    }

    @Test
    public void testBuildGameState_ContainsShopState() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(players.get(0));
        runner.setPhase(GameState.TurnPhase.ACTION);

        GameState gameState = runner.buildGameState();

        assertNotNull("GameState should have shop", gameState.buyableCards());
        assertTrue(
                "Shop should contain BITCOIN",
                gameState.buyableCards().getNumAvailable(Card.Type.BITCOIN) > 0);
    }

    // ========== validateDecision Tests ==========

    @Test
    public void testValidateDecision_ValidDecision_ReturnsTrue() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);

        EndPhaseDecision decision = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> validDecisions = ImmutableList.of(decision);

        assertTrue(
                "Valid decision should return true",
                runner.validateDecision(decision, validDecisions));
    }

    @Test
    public void testValidateDecision_InvalidDecision_ReturnsFalse() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);

        EndPhaseDecision decision1 = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        EndPhaseDecision decision2 = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> validDecisions = ImmutableList.of(decision1);

        assertFalse(
                "Invalid decision should return false",
                runner.validateDecision(decision2, validDecisions));
    }

    @Test
    public void testValidateDecision_NullDecision_ReturnsFalse() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);

        ImmutableList<Decision> validDecisions =
                ImmutableList.of(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        assertFalse(
                "Null decision should return false", runner.validateDecision(null, validDecisions));
    }

    // ========== processEndPhaseDecision Tests ==========

    @Test
    public void testProcessEndPhaseDecision_ActionToMoney_ChangesPhase() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);
        runner.setPhase(GameState.TurnPhase.ACTION);

        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        assertEquals(
                "Phase should change from ACTION to MONEY",
                GameState.TurnPhase.MONEY,
                runner.getPhase());
    }

    @Test
    public void testProcessEndPhaseDecision_MoneyToBuy_ChangesPhase() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);
        runner.setPhase(GameState.TurnPhase.MONEY);

        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        assertEquals(
                "Phase should change from MONEY to BUY",
                GameState.TurnPhase.BUY,
                runner.getPhase());
    }

    @Test
    public void testProcessEndPhaseDecision_BuyToCleanup_ChangesPhase() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);
        runner.setPhase(GameState.TurnPhase.BUY);

        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.BUY));

        assertEquals(
                "Phase should change from BUY to CLEANUP",
                GameState.TurnPhase.CLEANUP,
                runner.getPhase());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessEndPhaseDecision_MismatchedPhase_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);
        runner.setPhase(GameState.TurnPhase.ACTION);

        // Try to end BUY phase when in ACTION phase
        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.BUY));
    }

    // ========== processBuyDecision Tests ==========

    @Test
    public void testProcessBuyDecision_ValidBuy_DecrementsBuysAndMoney() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setPhase(GameState.TurnPhase.BUY);

        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);

        int initialMoney = activePlayer.numMoney();
        int initialBuys = activePlayer.numBuys();

        runner.processBuyDecision(new BuyDecision(Card.Type.BITCOIN));

        assertEquals(
                "Money should decrease by card cost",
                initialMoney - Card.Type.BITCOIN.cost(),
                activePlayer.numMoney());
        assertEquals("Buys should decrease by 1", initialBuys - 1, activePlayer.numBuys());
    }

    @Test
    public void testProcessBuyDecision_ValidBuy_AddsCardToDiscard() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setPhase(GameState.TurnPhase.BUY);

        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);

        List<Card> discard = getPrivateField(activePlayer, "discard");
        int initialDiscardSize = discard.size();

        runner.processBuyDecision(new BuyDecision(Card.Type.ETHEREUM));

        assertEquals("Discard should gain one card", initialDiscardSize + 1, discard.size());
    }

    @Test
    public void testProcessBuyDecision_ValidBuy_DecrementsShop() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setPhase(GameState.TurnPhase.BUY);
        activePlayer.adjustMoney(5);
        activePlayer.adjustBuys(1);

        int initialShopCount = shop.getNumAvailable(Card.Type.ETHEREUM);

        runner.processBuyDecision(new BuyDecision(Card.Type.ETHEREUM));

        assertEquals(
                "Shop should decrease by 1",
                initialShopCount - 1,
                shop.getNumAvailable(Card.Type.ETHEREUM));
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_InsufficientMoney_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        activePlayer.adjustMoney(1);
        activePlayer.adjustBuys(1);

        runner.processBuyDecision(new BuyDecision(Card.Type.FRAMEWORK));
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_NoBuysRemaining_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        activePlayer.turnReset();
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(-1);
        assertEquals("Should have 0 buys", 0, activePlayer.numBuys());

        runner.processBuyDecision(new BuyDecision(Card.Type.BITCOIN));
    }

    @Test(expected = IllegalStateException.class)
    public void testProcessBuyDecision_CardOutOfStock_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(1);

        while (shop.getNumAvailable(Card.Type.FRAMEWORK) > 0) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        runner.processBuyDecision(new BuyDecision(Card.Type.FRAMEWORK));
    }

    // ========== processGainingCard Tests ==========

    @Test
    public void testProcessGainingCard_ValidGain_AddsCardToDiscard() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> discard = getPrivateField(activePlayer, "discard");
        int initialDiscardSize = discard.size();

        boolean result = shopManager.gain(Card.Type.BITCOIN, activePlayer);

        assertTrue("Gaining should succeed", result);
        assertEquals("Discard should gain one card", initialDiscardSize + 1, discard.size());
    }

    @Test
    public void testProcessGainingCard_ValidGain_DecrementsShop() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        ShopManager shopManager = createShopManager(engine);

        int initialShopCount = shop.getNumAvailable(Card.Type.BITCOIN);

        shopManager.gain(Card.Type.BITCOIN, activePlayer);

        assertEquals(
                "Shop should decrease by 1",
                initialShopCount - 1,
                shop.getNumAvailable(Card.Type.BITCOIN));
    }

    @Test
    public void testProcessGainingCard_CardNotAvailable_ReturnsFalse() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        ShopManager shopManager = createShopManager(engine);

        while (shop.getNumAvailable(Card.Type.FRAMEWORK) > 0) {
            shop.decrementCount(Card.Type.FRAMEWORK);
        }

        boolean result = shopManager.gain(Card.Type.FRAMEWORK, activePlayer);

        assertFalse("Gaining should fail when card unavailable", result);
    }

    // ========== Action Card Execution Tests ==========

    @Test
    public void testExecuteActionCodeReview_DrawsCard() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> deck = getPrivateField(activePlayer, "deck");
        if (deck.isEmpty()) {
            AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialHandSize = activePlayer.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new CodeReviewEffect().execute(ctx);

        assertEquals(
                "Should draw 1 card", initialHandSize + 1, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionCodeReview_AddsTwoActions() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        int initialActions = activePlayer.numActions();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new CodeReviewEffect().execute(ctx);

        assertEquals("Should add 2 actions", initialActions + 2, activePlayer.numActions());
    }

    @Test
    public void testExecuteActionEvergreenTest_DrawsTwoCards() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> deck = getPrivateField(activePlayer, "deck");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialHandSize = activePlayer.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new EvergreenTestEffect().execute(ctx);

        assertEquals(
                "Should draw 2 cards", initialHandSize + 2, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionEvergreenTest_GivesBugToOtherPlayers() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        List<Card> player3Discard = getPrivateField(players.get(2), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();
        int initialPlayer3DiscardSize = player3Discard.size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new EvergreenTestEffect().execute(ctx);

        assertEquals(
                "Player 2 should gain a BUG", initialPlayer2DiscardSize + 1, player2Discard.size());
        assertEquals(
                "Player 3 should gain a BUG", initialPlayer3DiscardSize + 1, player3Discard.size());
    }

    @Test
    public void testExecuteActionEvergreenTest_SkipsDisqualifiedPlayers() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        players.get(1).forfeit();

        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        List<Card> player3Discard = getPrivateField(players.get(2), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();
        int initialPlayer3DiscardSize = player3Discard.size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new EvergreenTestEffect().execute(ctx);

        assertEquals(
                "Disqualified player should not gain BUG",
                initialPlayer2DiscardSize,
                player2Discard.size());
        assertEquals(
                "Active player 3 should gain a BUG",
                initialPlayer3DiscardSize + 1,
                player3Discard.size());
    }

    @Test
    public void testExecuteActionEvergreenTest_InsufficientBugsInSupply() throws Exception {
        OurEngine engine = createEngine(fourPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        ShopManager shopManager = createShopManager(engine);

        int initialBugCount = shop.getNumAvailable(Card.Type.BUG);
        for (int i = 0; i < initialBugCount - 1; i++) {
            shop.decrementCount(Card.Type.BUG);
        }
        assertEquals("Should have exactly 1 BUG in supply", 1, shop.getNumAvailable(Card.Type.BUG));

        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        List<Card> player3Discard = getPrivateField(players.get(2), "discard");
        List<Card> player4Discard = getPrivateField(players.get(3), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();
        int initialPlayer3DiscardSize = player3Discard.size();
        int initialPlayer4DiscardSize = player4Discard.size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new EvergreenTestEffect().execute(ctx);

        int totalBugsGained =
                (player2Discard.size() - initialPlayer2DiscardSize)
                        + (player3Discard.size() - initialPlayer3DiscardSize)
                        + (player4Discard.size() - initialPlayer4DiscardSize);
        assertEquals("Exactly 1 BUG should be distributed", 1, totalBugsGained);
        assertEquals("BUG supply should be exhausted", 0, shop.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testExecuteActionEvergreenTest_NoBugsInSupply() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        MutableCardStacks shop = getPrivateField(engine, "shop");
        ShopManager shopManager = createShopManager(engine);

        int initialBugCount = shop.getNumAvailable(Card.Type.BUG);
        for (int i = 0; i < initialBugCount; i++) {
            shop.decrementCount(Card.Type.BUG);
        }
        assertEquals("Should have 0 BUGs in supply", 0, shop.getNumAvailable(Card.Type.BUG));

        List<Card> player2Discard = getPrivateField(players.get(1), "discard");
        int initialPlayer2DiscardSize = player2Discard.size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new EvergreenTestEffect().execute(ctx);

        assertEquals(
                "Opponent should not gain BUG when supply is empty",
                initialPlayer2DiscardSize,
                player2Discard.size());
        assertEquals("BUG supply should still be 0", 0, shop.getNumAvailable(Card.Type.BUG));
    }

    @Test
    public void testExecuteActionDailyScrum_DrawsFourCardsAndAddsBuy() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> deck = getPrivateField(activePlayer, "deck");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        while (deck.size() < 4) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialHandSize = activePlayer.getUnplayedCards().size();
        int initialBuys = activePlayer.numBuys();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new DailyScrumEffect().execute(ctx);

        assertEquals(
                "Should draw 4 cards", initialHandSize + 4, activePlayer.getUnplayedCards().size());
        assertEquals("Should add 1 buy", initialBuys + 1, activePlayer.numBuys());
    }

    @Test
    public void testExecuteActionDailyScrum_OtherPlayersDrawOneCardEach() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        List<Card> activeDeck = getPrivateField(activePlayer, "deck");
        while (activeDeck.size() < 4) {
            activeDeck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        PlayerInfo player2 = players.get(1);
        PlayerInfo player3 = players.get(2);
        List<Card> player2Deck = getPrivateField(player2, "deck");
        List<Card> player3Deck = getPrivateField(player3, "deck");
        if (player2Deck.isEmpty()) {
            player2Deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }
        if (player3Deck.isEmpty()) {
            player3Deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialPlayer2HandSize = player2.getUnplayedCards().size();
        int initialPlayer3HandSize = player3.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new DailyScrumEffect().execute(ctx);

        assertEquals(
                "Player 2 should draw one card",
                initialPlayer2HandSize + 1,
                player2.getUnplayedCards().size());
        assertEquals(
                "Player 3 should draw one card",
                initialPlayer3HandSize + 1,
                player3.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionDailyScrum_SkipsDisqualifiedPlayers() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        List<Card> activeDeck = getPrivateField(activePlayer, "deck");
        while (activeDeck.size() < 4) {
            activeDeck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        PlayerInfo player2 = players.get(1);
        PlayerInfo player3 = players.get(2);
        player2.forfeit();

        List<Card> player3Deck = getPrivateField(player3, "deck");
        if (player3Deck.isEmpty()) {
            player3Deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialPlayer2HandSize = player2.getUnplayedCards().size();
        int initialPlayer3HandSize = player3.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new DailyScrumEffect().execute(ctx);

        assertEquals(
                "Disqualified player should not draw",
                initialPlayer2HandSize,
                player2.getUnplayedCards().size());
        assertEquals(
                "Non-disqualified player should draw",
                initialPlayer3HandSize + 1,
                player3.getUnplayedCards().size());
    }

    // ========== Player Disqualification Tests ==========

    @Test
    public void testHandleCheating_DisqualifiesPlayer() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo cheater = players.get(0);

        assertFalse("Player should not be disqualified initially", cheater.isDisqualified());

        // forfeit() is what the engine calls when a PlayerViolationException is caught
        cheater.forfeit();

        assertTrue("Player should be disqualified after cheating", cheater.isDisqualified());
    }

    // ========== endGame Tests ==========

    @Test
    public void testEndGame_ReturnsGameResult() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        GameRunner gameRunner = getPrivateField(engine, "runner");

        GameResult result = gameRunner.endGame();

        assertNotNull("Should return a GameResult", result);
    }

    @Test
    public void testEndGame_IncludesAllPlayers() throws Exception {
        OurEngine engine = createEngine(threePlayers);
        GameRunner gameRunner = getPrivateField(engine, "runner");

        GameResult result = gameRunner.endGame();

        assertEquals("Should include all players in result", 3, result.playerResults().size());
    }

    @Test
    public void testEndGame_SortsPlayersByScore() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        GameRunner gameRunner = getPrivateField(engine, "runner");

        for (int i = 0; i < 3; i++) {
            players.get(1).gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        }

        GameResult result = gameRunner.endGame();

        PlayerResult first = result.playerResults().get(0);
        PlayerResult second = result.playerResults().get(1);

        assertTrue("Results should be sorted by score descending", first.score() >= second.score());
    }

    // ========== Integration-Style Tests ==========

    @Test
    public void testPlay_GameCompletesSuccessfully() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        GameResult result = engine.play();

        assertNotNull("Should return a game result", result);
        assertEquals("Should have results for both players", 2, result.playerResults().size());
    }

    @Test
    public void testPlay_AllPlayersHaveFinalDecks() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        GameResult result = engine.play();

        for (PlayerResult pr : result.playerResults()) {
            assertNotNull("Each player should have a final deck", pr.endingDeck());
            assertFalse("Final deck should not be empty", pr.endingDeck().isEmpty());
        }
    }

    @Test
    public void testGetNumPlayers_ReturnsCorrectCount() {
        OurEngine engine = createEngine(threePlayers);
        assertEquals("Should return correct player count", 3, engine.getNumPlayers());
    }

    @Test
    public void testGetTurnLogs_ReturnsNonNullList() {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<TurnLog> logs = engine.getTurnLogs();

        assertNotNull("Turn logs should not be null", logs);
    }

    // ========== processPlayActionCard Tests ==========

    @Test(expected = IllegalStateException.class)
    public void testProcessPlayActionCard_NoActionsRemaining_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setCurrentTurnLog(null);

        activePlayer.turnReset();
        activePlayer.adjustActions(-1);
        assertEquals("Should have 0 actions", 0, activePlayer.numActions());

        runner.processPlayActionCard(Card.Type.CODE_REVIEW);
    }

    @Test
    public void testProcessPlayActionCard_ValidAction_DecrementsActions() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setCurrentTurnLog(null);
        runner.setPhase(GameState.TurnPhase.ACTION);

        activePlayer.adjustActions(3);
        int initialActions = activePlayer.numActions();

        runner.processPlayActionCard(Card.Type.CODE_REVIEW);

        // CODE_REVIEW gives +2 actions, playing costs -1: net +1
        int expectedActions = initialActions + 2 - 1;
        assertEquals(
                "Actions should be correctly adjusted", expectedActions, activePlayer.numActions());
    }

    // ========== processPlayMoneyCard Tests ==========

    @Test
    public void testProcessPlayMoneyCard_Bitcoin_AddsOneMoney() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        int initialMoney = activePlayer.numMoney();

        runner.processPlayMoneyCard(Card.Type.BITCOIN);

        assertEquals("BITCOIN should add 1 money", initialMoney + 1, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayMoneyCard_Ethereum_AddsTwoMoney() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        int initialMoney = activePlayer.numMoney();

        runner.processPlayMoneyCard(Card.Type.ETHEREUM);

        assertEquals("ETHEREUM should add 2 money", initialMoney + 2, activePlayer.numMoney());
    }

    @Test
    public void testProcessPlayMoneyCard_Dogecoin_AddsThreeMoney() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        int initialMoney = activePlayer.numMoney();

        runner.processPlayMoneyCard(Card.Type.DOGECOIN);

        assertEquals("DOGECOIN should add 3 money", initialMoney + 3, activePlayer.numMoney());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProcessPlayMoneyCard_NonMoneyCard_ThrowsException() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(players.get(0));

        runner.processPlayMoneyCard(Card.Type.CODE_REVIEW);
    }

    // ========== executeActionRefactor Tests ==========

    @Test
    public void testExecuteActionRefactor_TrashAndGain_Success() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        ShopManager shopManager = createShopManager(engine);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        Card bitcoinToTrash = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoinToTrash);

        int initialHandSize = activePlayer.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new RefactorEffect().execute(ctx);

        assertEquals(
                "Hand should have one less card after trashing",
                initialHandSize - 1,
                activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionRefactor_NoCardsInHand_ReturnsEarly() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new RefactorEffect().execute(ctx);

        assertEquals("Hand should still be empty", 0, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecuteActionRefactor_CostCalculation_AllowsCardsUpToCostPlusTwo()
            throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        ShopManager shopManager = createShopManager(engine);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card dogecoin = new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(dogecoin);

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new RefactorEffect().execute(ctx);

        List<Card> discard = getPrivateField(activePlayer, "discard");
        assertTrue("Should have gained a card to discard", discard.size() > 0);
    }

    @Test
    public void testExecuteActionRefactor_TrashExpensiveCard_CanGainExpensiveReplacement()
            throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        ShopManager shopManager = createShopManager(engine);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card framework = new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(framework);

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new RefactorEffect().execute(ctx);

        List<Card> discard = getPrivateField(activePlayer, "discard");
        boolean trashedInDiscard = discard.stream().anyMatch(c -> c.id() == framework.id());
        assertFalse("Trashed FRAMEWORK should not be in discard", trashedInDiscard);
        assertTrue("Should have gained a card", discard.size() > 0);
    }

    @Test
    public void testExecuteActionRefactor_PhasesTransition_GainPhase() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        ShopManager shopManager = createShopManager(engine);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        Card bitcoin = new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement());
        hand.addToUnplayed(bitcoin);

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new RefactorEffect().execute(ctx);

        assertEquals(
                "Phase should return to ACTION after REFACTOR",
                GameState.TurnPhase.ACTION,
                ctx.getPhase());
    }

    // ========== Card Drawing Edge Case Tests ==========

    @Test
    public void testDrawCard_EmptyDeck_ReshufflesDiscard() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        assertEquals(
                "Hand size should not change when no cards available",
                handSizeBefore,
                handSizeAfter);
    }

    @Test
    public void testDrawMultipleCards_InsufficientCards_DrawsWhatAvailable() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        MutableHand hand = getPrivateField(activePlayer, "hand");
        hand.clear();
        activePlayer.turnReset();

        hand.addToUnplayed(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        hand.addToUnplayed(new Card(Card.Type.ETHEREUM, cardIdCounter.getAndIncrement()));
        hand.addToUnplayed(new Card(Card.Type.DOGECOIN, cardIdCounter.getAndIncrement()));

        runner.processPlayMoneyCard(Card.Type.BITCOIN);
        runner.processPlayMoneyCard(Card.Type.ETHEREUM);
        runner.processPlayMoneyCard(Card.Type.DOGECOIN);

        assertEquals("Money should accumulate correctly", 6, activePlayer.numMoney());
    }

    @Test
    public void testMoneyPhase_MultipleSameTypeCards_EachAddsValue() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);

        activePlayer.turnReset();

        runner.processPlayMoneyCard(Card.Type.DOGECOIN);
        runner.processPlayMoneyCard(Card.Type.DOGECOIN);
        runner.processPlayMoneyCard(Card.Type.DOGECOIN);

        assertEquals("Multiple cards of same type should accumulate", 9, activePlayer.numMoney());
    }

    // ========== Score Calculation Tests ==========

    @Test
    public void testEndGame_ScoreCalculation_MethodCardsCountedCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo player = players.get(0);
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");

        // Player starts with 3 METHOD cards (3 points)
        int initialScore = player.getScore();
        assertEquals("Should start with 3 points", 3, initialScore);

        // Gain mixed victory cards: 1 METHOD (1 VP), 2 FRAMEWORK (12 VP), 1 MODULE (3 VP) = 16 VP
        // total
        player.gainCard(new Card(Card.Type.METHOD, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.FRAMEWORK, cardIdCounter.getAndIncrement()));
        player.gainCard(new Card(Card.Type.MODULE, cardIdCounter.getAndIncrement()));

        // Score should now be 19 (3 starting + 16 gained)
        assertEquals("Mixed cards with FRAMEWORKs should sum correctly", 19, player.getScore());
    }

    // ========== Phase Transition Tests ==========

    @Test
    public void testProcessEndPhaseDecision_AllPhases_CompleteSequence() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        TurnRunner runner = createTurnRunner(engine);

        runner.setPhase(GameState.TurnPhase.ACTION);
        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        assertEquals("After ACTION phase", GameState.TurnPhase.MONEY, runner.getPhase());

        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        assertEquals("After MONEY phase", GameState.TurnPhase.BUY, runner.getPhase());

        runner.processEndPhaseDecision(new EndPhaseDecision(GameState.TurnPhase.BUY));
        assertEquals("After BUY phase", GameState.TurnPhase.CLEANUP, runner.getPhase());
    }

    // ========== Multiple Sequential Buys Tests ==========

    @Test
    public void testProcessBuyDecision_MultipleBuys_DecrementsCorrectly() throws Exception {
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        TurnRunner runner = createTurnRunner(engine);
        runner.setActivePlayer(activePlayer);
        runner.setPhase(GameState.TurnPhase.BUY);

        activePlayer.turnReset();
        activePlayer.adjustMoney(10);
        activePlayer.adjustBuys(1); // Total 2 buys (1 from reset + 1)

        assertEquals("Should start with 2 buys", 2, activePlayer.numBuys());
        assertEquals("Should start with 10 money", 10, activePlayer.numMoney());

        runner.processBuyDecision(new BuyDecision(Card.Type.ETHEREUM));

        assertEquals("Should have 1 buy remaining", 1, activePlayer.numBuys());
        assertEquals("Should have 7 money remaining", 7, activePlayer.numMoney());

        runner.processBuyDecision(new BuyDecision(Card.Type.BITCOIN));

        assertEquals("Should have 0 buys remaining", 0, activePlayer.numBuys());
        assertEquals("Should have 7 money remaining", 7, activePlayer.numMoney());
    }

    // ========== Cleanup Phase Tests ==========

    @Test
    public void testCleanup_DiscardsAllCards_DrawsFiveNew() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);

        // Get initial state
        List<Card> initialHandCards = new ArrayList<>(activePlayer.getUnplayedCards());

        // Perform cleanup
        activePlayer.cleanup();

        // Verify hand has exactly 5 cards
        assertEquals(
                "Hand should have exactly 5 cards after cleanup",
                5,
                activePlayer.getUnplayedCards().size());

        // Verify old cards are in discard
        List<Card> currentDiscard = getPrivateField(activePlayer, "discard");
        for (Card oldCard : initialHandCards) {
            assertTrue("Old hand cards should be in discard", currentDiscard.contains(oldCard));
        }

        // Verify new hand has different cards
        List<Card> newHandCards = activePlayer.getUnplayedCards();
        for (Card newCard : newHandCards) {
            assertFalse(
                    "New hand should not contain old cards", initialHandCards.contains(newCard));
        }
    }

    @Test
    public void testCleanup_WithPlayedCards_DiscardsAll() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        assertEquals(
                "All cards should be discarded",
                initialDiscardSize + totalCardsInHand,
                finalDiscard.size());

        // Verify new hand is exactly 5 cards
        assertEquals("Should draw exactly 5 new cards", 5, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testTurnReset_ResetsResourcesCorrectly() throws Exception {
        // Create fresh players to avoid any state sharing
        List<Player> freshPlayers = createPlayers(2);
        OurEngine engine = createEngine(freshPlayers);
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
        OurEngine engine = createEngine(freshPlayers);
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
        assertTrue(
                "Discard should have fewer cards after cleanup/reshuffle",
                finalDiscard.size() < discardSizeBefore);
    }
}
