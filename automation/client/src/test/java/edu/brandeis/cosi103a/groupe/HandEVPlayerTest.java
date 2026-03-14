package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.cosi21.*;
import edu.brandeis.cosi103a.groupe.main.*;
import edu.brandeis.cosi103a.groupe.player.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the HandEVPlayer class. Tests decision-making logic for all game
 * phases and various scenarios.
 *
 * <p>HandEVPlayer behaves like BigMoneyPlayer except for makeBuyDecision(): it only buys Framework
 * when expected hand value >= Framework cost (8). Expected hand value = HAND_SIZE (5) *
 * totalMoneyInDeck / deckSize
 */
public class HandEVPlayerTest {

    private HandEVPlayer player;
    private Card bitcoinCard;
    private Card ethereumCard;
    private Card dogecoinCard;
    private Card methodCard;
    private Card moduleCard;

    @Before
    public void setUp() {
        player = new HandEVPlayer();

        // Create sample cards for testing
        bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        ethereumCard = new Card(Card.Type.ETHEREUM, 2);
        dogecoinCard = new Card(Card.Type.DOGECOIN, 3);
        methodCard = new Card(Card.Type.METHOD, 4);
        moduleCard = new Card(Card.Type.MODULE, 5);
    }

    // ========== Helper Methods ==========

    /** Creates a mock GameState for testing. */
    private GameState createMockGameState(GameState.TurnPhase phase) {
        Hand mockHand = new Hand(ImmutableList.of(bitcoinCard, methodCard), ImmutableList.of());
        CardStacks mockShop =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 50,
                                Card.Type.FRAMEWORK, 8));

        return new GameState(
                "Player-1",
                mockHand,
                phase,
                1, // numActions
                0, // numMoney
                1, // numBuys
                mockShop);
    }

    // ========== Constructor and Name Tests ==========

    @Test
    public void testConstructor_CreatesPlayerSuccessfully() {
        HandEVPlayer newPlayer = new HandEVPlayer();
        assertNotNull("HandEVPlayer should be created", newPlayer);
    }

    @Test
    public void testGetName_ReturnsPlayerName() {
        String name = player.getName();
        assertNotNull("Player name should not be null", name);
        assertTrue("Player name should start with 'Player-'", name.startsWith("Player-"));
    }

    @Test
    public void testConstructor_GeneratesUniqueNames() {
        HandEVPlayer player1 = new HandEVPlayer();
        HandEVPlayer player2 = new HandEVPlayer();
        HandEVPlayer player3 = new HandEVPlayer();

        String name1 = player1.getName();
        String name2 = player2.getName();
        String name3 = player3.getName();

        assertNotEquals("Player names should be unique", name1, name2);
        assertNotEquals("Player names should be unique", name2, name3);
        assertNotEquals("Player names should be unique", name1, name3);
    }

    // ========== makeDecision - EndPhaseDecision Tests ==========

    @Test
    public void testMakeDecision_SingleEndPhaseDecision_ReturnsEndPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }

    @Test
    public void testMakeDecision_SingleEndPhaseDecision_InMoneyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return the EndPhaseDecision regardless of phase", endPhase, result);
    }

    @Test
    public void testMakeDecision_SingleEndPhaseDecision_InBuyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }

    // ========== makeDecision - ACTION Phase Tests ==========

    @Test
    public void testMakeDecision_ActionPhase_ReturnsFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playCard = new PlayCardDecision(methodCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playCard, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first option in ACTION phase", playCard, result);
    }

    @Test
    public void testMakeDecision_ActionPhase_MultiplePlayCardOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playCard1 = new PlayCardDecision(methodCard);
        PlayCardDecision playCard2 = new PlayCardDecision(moduleCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playCard1, playCard2, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first option", playCard1, result);
    }

    @Test
    public void testMakeDecision_ActionPhase_OnlyEndPhaseAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return EndPhaseDecision when it's the only option", endPhase, result);
    }

    // ========== makeDecision - BUY Phase Tests (HandEVPlayer-specific) ==========

    @Test
    public void testMakeDecision_BuyPhase_SkipsFramework_WhenEVTooLow() {
        // Create a fresh player with low expected value
        // Starting deck: 7 Bitcoins (value 1 each) + 3 Methods (value 0)
        // Total money = 7, deck size = 10
        // EV per card = 7/10 = 0.7
        // Expected hand value = 5 * 0.7 = 3.5 < 8 (Framework cost)
        // Should NOT buy Framework
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options =
                ImmutableList.of(buyBitcoin, buyFramework, buyEthereum, endPhase);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        // With starting deck (EV too low), should skip Framework and buy next best (Ethereum)
        assertNotSame("Should NOT buy FRAMEWORK when EV is too low", buyFramework, result);
        // Should buy Ethereum (next in preference order)
        assertSame("Should buy ETHEREUM instead", buyEthereum, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_BuysFramework_AfterBuyingEnoughMoney() {
        // Simulate buying enough money cards to reach the threshold
        // After buying several high-value treasures, EV should be high enough
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);

        // Simulate buying 10 Dogecoins (value 3 each = 30 total)
        // deckSize = 10, totalMoney = 7 (starting)
        // After 10 Dogecoins: deckSize = 20, totalMoney = 7 + 30 = 37
        // EV per card = 37.0/20 = 1.85
        // Expected hand value = 5 * 1.85 = 9.25 >= 8
        for (int i = 0; i < 10; i++) {
            testPlayer.makeDecision(
                    state,
                    ImmutableList.of(
                            new BuyDecision(Card.Type.DOGECOIN),
                            new EndPhaseDecision(GameState.TurnPhase.BUY)), java.util.Optional.empty());
        }

        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyFramework, buyDogecoin, endPhase);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Should buy FRAMEWORK after accumulating enough money cards", buyFramework, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PrefersDogecoinOverEthereum_WhenFrameworkSkipped() {
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options =
                ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin, buyFramework, endPhase);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        // With low EV, should skip Framework and prefer DOGECOIN
        assertSame("Should prefer DOGECOIN when Framework is skipped", buyDogecoin, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PrefersEthereumOverBitcoin() {
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum, endPhase);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should prefer ETHEREUM over BITCOIN", buyEthereum, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_OnlyBitcoinAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should buy BITCOIN when it's the only treasure available", buyBitcoin, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_NoPreferredCardsAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyMethod = new BuyDecision(Card.Type.METHOD);
        BuyDecision buyModule = new BuyDecision(Card.Type.MODULE);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyMethod, buyModule, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should end phase when no preferred cards available", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_OnlyEndPhaseAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return EndPhaseDecision when it's the only option", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_DeckStatisticsTracking() {
        // Verify that deck statistics are correctly tracked across multiple buys
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);

        // First buy: Bitcoin (value 1)
        testPlayer.makeDecision(
                state,
                ImmutableList.of(
                        new BuyDecision(Card.Type.BITCOIN),
                        new EndPhaseDecision(GameState.TurnPhase.BUY)), java.util.Optional.empty());

        // Second buy: Ethereum (value 2)
        testPlayer.makeDecision(
                state,
                ImmutableList.of(
                        new BuyDecision(Card.Type.ETHEREUM),
                        new EndPhaseDecision(GameState.TurnPhase.BUY)), java.util.Optional.empty());

        // Third buy: Dogecoin (value 3)
        Decision result =
                testPlayer.makeDecision(
                        state,
                        ImmutableList.of(
                                new BuyDecision(Card.Type.DOGECOIN),
                                new BuyDecision(Card.Type.BITCOIN),
                                new EndPhaseDecision(GameState.TurnPhase.BUY)), java.util.Optional.empty());

        // Should still prefer Dogecoin
        assertTrue("Should buy a money card", result instanceof BuyDecision);
        assertEquals(
                "Should prefer DOGECOIN", Card.Type.DOGECOIN, ((BuyDecision) result).cardType());
    }

    @Test
    public void testMakeDecision_BuyPhase_FrameworkFirstInList_ButEVTooLow() {
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyFramework, buyDogecoin);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should skip FRAMEWORK even when first due to low EV", buyDogecoin, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_MixedDecisionTypes() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        TrashCardDecision trashCard = new TrashCardDecision(methodCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options =
                ImmutableList.of(playCard, buyEthereum, trashCard, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Should find and return the BuyDecision for preferred card", buyEthereum, result);
    }

    // ========== makeDecision - GAIN Phase Tests ==========

    @Test
    public void testMakeDecision_GainPhase_ReturnsFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainMethod = new GainCardDecision(Card.Type.METHOD);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, gainMethod);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first option in GAIN phase", gainBitcoin, result);
    }

    @Test
    public void testMakeDecision_GainPhase_SingleGainOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainEthereum = new GainCardDecision(Card.Type.ETHEREUM);
        ImmutableList<Decision> options = ImmutableList.of(gainEthereum);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return the only gain option", gainEthereum, result);
    }

    @Test
    public void testMakeDecision_GainPhase_MultipleGainOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainEthereum = new GainCardDecision(Card.Type.ETHEREUM);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, gainEthereum, gainDogecoin);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first option with multiple gain options", gainBitcoin, result);
    }

    @Test
    public void testMakeDecision_GainPhase_WithEndPhaseOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.GAIN);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Should return first option even with EndPhaseDecision present",
                gainBitcoin,
                result);
    }

    // ========== makeDecision - MONEY Phase Tests ==========

    @Test
    public void testMakeDecision_MoneyPhase_ReturnsPlayCardDecision() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return PlayCardDecision in MONEY phase", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_MultiplePlayCardOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, playEthereum, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first PlayCardDecision", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_PlayCardNotFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Should find and return PlayCardDecision even if not first", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_NoPlayCardAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Should return first option when no PlayCardDecision available", endPhase, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_MixedDecisionTypes() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        BuyDecision buyCard = new BuyDecision(Card.Type.BITCOIN);
        TrashCardDecision trashCard = new TrashCardDecision(methodCard);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(buyCard, trashCard, playCard, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should find PlayCardDecision among mixed types", playCard, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_OnlyPlayCardAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playCard = new PlayCardDecision(ethereumCard);
        ImmutableList<Decision> options = ImmutableList.of(playCard);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return the only PlayCardDecision", playCard, result);
    }

    // ========== Edge Cases and Default Behavior Tests ==========

    @Test
    public void testMakeDecision_UnrecognizedPhase_ReturnsFirstOption() {
        // Create a GameState with CLEANUP phase (unhandled in switch)
        GameState state = createMockGameState(GameState.TurnPhase.CLEANUP);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.CLEANUP);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playCard);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return first option for unrecognized phase", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PreferenceOrder_WithLowEV() {
        // Test that the preference order is: DOGECOIN > ETHEREUM > BITCOIN when Framework is
        // skipped
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);

        // Test Dogecoin is preferred over Ethereum and Bitcoin (Framework skipped due to low EV)
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);

        ImmutableList<Decision> options1 =
                ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin, buyFramework);
        assertEquals(
                "DOGECOIN should be preferred when Framework skipped",
                buyDogecoin,
                testPlayer.makeDecision(state, options1, java.util.Optional.empty()));

        // Test Ethereum is preferred over Bitcoin (no Framework or Dogecoin)
        ImmutableList<Decision> options2 = ImmutableList.of(buyBitcoin, buyEthereum);
        assertEquals(
                "ETHEREUM should be preferred",
                buyEthereum,
                testPlayer.makeDecision(state, options2, java.util.Optional.empty()));
    }

    @Test
    public void testMakeDecision_ActionPhase_FollowsBigMoneyStrategy() {
        // Big Money strategy: actions are not important, just return first option
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playModule, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        // Big Money should return first option (which might be any of them)
        assertNotNull("Should return a decision", result);
        assertSame("Should return first option", playMethod, result);
    }

    @Test
    public void testMakeDecision_ConsistentBehavior_SameInputs() {
        // Test that the same inputs always produce the same output
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum);

        Decision result1 = player.makeDecision(state, options, java.util.Optional.empty());
        Decision result2 = player.makeDecision(state, options, java.util.Optional.empty());
        Decision result3 = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should return same decision for same inputs", result1, result2);
        assertSame("Should return same decision for same inputs", result2, result3);
        assertEquals("Should prefer ETHEREUM", buyEthereum, result1);
    }

    @Test
    public void testMakeDecision_MoneyPhase_PlaysAnyMoneyCard() {
        // In Big Money strategy, money card order doesn't matter
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        PlayCardDecision playDogecoin = new PlayCardDecision(dogecoinCard);

        ImmutableList<Decision> options1 =
                ImmutableList.of(playBitcoin, playEthereum, playDogecoin);
        Decision result1 = player.makeDecision(state, options1, java.util.Optional.empty());
        assertTrue("Should return a PlayCardDecision", result1 instanceof PlayCardDecision);

        ImmutableList<Decision> options2 =
                ImmutableList.of(playDogecoin, playBitcoin, playEthereum);
        Decision result2 = player.makeDecision(state, options2, java.util.Optional.empty());
        assertTrue("Should return a PlayCardDecision", result2 instanceof PlayCardDecision);
    }

    @Test
    public void testMakeDecision_NullSafety_ValidInputs() {
        // Ensure the method handles valid inputs correctly
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertNotNull("Should return a non-null decision", result);
        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_NonMoneyCardDoesNotAffectEV() {
        // Verify that buying non-money cards (like Method) doesn't increase totalMoney
        HandEVPlayer testPlayer = new HandEVPlayer();
        GameState state = createMockGameState(GameState.TurnPhase.BUY);

        // Buy a Method card (victory card, not money)
        testPlayer.makeDecision(
                state,
                ImmutableList.of(
                        new BuyDecision(Card.Type.METHOD),
                        new EndPhaseDecision(GameState.TurnPhase.BUY)), java.util.Optional.empty());

        // Framework should still be skipped (EV hasn't improved)
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyFramework, buyDogecoin);

        Decision result = testPlayer.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Should still skip FRAMEWORK after buying victory card", buyDogecoin, result);
    }
    
}
