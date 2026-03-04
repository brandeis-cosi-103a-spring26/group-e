package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the BigMoneyPlayer class.
 * Tests decision-making logic for all game phases and various scenarios.
 */
public class BigMoneyPlayerTest {

    private BigMoneyPlayer player;
    private Card bitcoinCard;
    private Card ethereumCard;
    private Card dogecoinCard;
    private Card methodCard;
    private Card moduleCard;

    @Before
    public void setUp() {
        player = new BigMoneyPlayer();
        
        // Create sample cards for testing
        bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        ethereumCard = new Card(Card.Type.ETHEREUM, 2);
        dogecoinCard = new Card(Card.Type.DOGECOIN, 3);
        methodCard = new Card(Card.Type.METHOD, 4);
        moduleCard = new Card(Card.Type.MODULE, 5);
    }

    // ========== Helper Methods ==========

    /**
     * Creates a mock GameState for testing.
     */
    private GameState createMockGameState(GameState.TurnPhase phase) {
        Hand mockHand = new Hand(ImmutableList.of(bitcoinCard, methodCard), ImmutableList.of());
        CardStacks mockShop = new CardStacks(ImmutableMap.of(
            Card.Type.BITCOIN, 50,
            Card.Type.FRAMEWORK, 8
        ));
        
        return new GameState(
            "Player-1",
            mockHand,
            phase,
            1,  // numActions
            0,  // numMoney
            1,  // numBuys
            mockShop
        );
    }

    // ========== Constructor and Name Tests ==========

    @Test
    public void testConstructor_CreatesPlayerSuccessfully() {
        BigMoneyPlayer newPlayer = new BigMoneyPlayer();
        assertNotNull("BigMoneyPlayer should be created", newPlayer);
    }

    @Test
    public void testGetName_ReturnsPlayerName() {
        String name = player.getName();
        assertNotNull("Player name should not be null", name);
        assertTrue("Player name should start with 'Player-'", name.startsWith("Player-"));
    }

    @Test
    public void testConstructor_GeneratesUniqueNames() {
        BigMoneyPlayer player1 = new BigMoneyPlayer();
        BigMoneyPlayer player2 = new BigMoneyPlayer();
        BigMoneyPlayer player3 = new BigMoneyPlayer();
        
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
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }

    @Test
    public void testMakeDecision_SingleEndPhaseDecision_InMoneyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return the EndPhaseDecision regardless of phase", endPhase, result);
    }

    @Test
    public void testMakeDecision_SingleEndPhaseDecision_InBuyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }

    // ========== makeDecision - ACTION Phase Tests ==========

    @Test
    public void testMakeDecision_ActionPhase_ReturnsFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playCard = new PlayCardDecision(methodCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playCard, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option in ACTION phase", playCard, result);
    }

    @Test
    public void testMakeDecision_ActionPhase_MultiplePlayCardOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playCard1 = new PlayCardDecision(methodCard);
        PlayCardDecision playCard2 = new PlayCardDecision(moduleCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playCard1, playCard2, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option", playCard1, result);
    }

    @Test
    public void testMakeDecision_ActionPhase_OnlyEndPhaseAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return EndPhaseDecision when it's the only option", endPhase, result);
    }

    // ========== makeDecision - BUY Phase Tests ==========

    @Test
    public void testMakeDecision_BuyPhase_PrefersFramework() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyFramework, buyEthereum, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer FRAMEWORK over other cards", buyFramework, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PrefersDogecoinOverEthereum() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer DOGECOIN over ETHEREUM", buyDogecoin, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PrefersEthereumOverBitcoin() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer ETHEREUM over BITCOIN", buyEthereum, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_OnlyBitcoinAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should buy BITCOIN when it's the only treasure available", buyBitcoin, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_NoPreferredCardsAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyMethod = new BuyDecision(Card.Type.METHOD);
        BuyDecision buyModule = new BuyDecision(Card.Type.MODULE);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyMethod, buyModule, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should end phase when no preferred cards available", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_OnlyEndPhaseAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return EndPhaseDecision when it's the only option", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_FrameworkFirstInList() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyFramework, buyDogecoin);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer FRAMEWORK even when it's first", buyFramework, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_FrameworkLastInList() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum, buyFramework);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer FRAMEWORK even when it's last", buyFramework, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_AllPreferredCardsAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin, buyFramework);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should prefer FRAMEWORK when all are available", buyFramework, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_MixedDecisionTypes() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        TrashCardDecision trashCard = new TrashCardDecision(methodCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(playCard, buyEthereum, trashCard, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should find and return the BuyDecision for preferred card", buyEthereum, result);
    }

    // ========== makeDecision - GAIN Phase Tests ==========

    @Test
    public void testMakeDecision_GainPhase_ReturnsFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainMethod = new GainCardDecision(Card.Type.METHOD);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, gainMethod);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option in GAIN phase", gainBitcoin, result);
    }

    @Test
    public void testMakeDecision_GainPhase_SingleGainOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainEthereum = new GainCardDecision(Card.Type.ETHEREUM);
        ImmutableList<Decision> options = ImmutableList.of(gainEthereum);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return the only gain option", gainEthereum, result);
    }

    @Test
    public void testMakeDecision_GainPhase_MultipleGainOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainEthereum = new GainCardDecision(Card.Type.ETHEREUM);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, gainEthereum, gainDogecoin);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option with multiple gain options", gainBitcoin, result);
    }

    @Test
    public void testMakeDecision_GainPhase_WithEndPhaseOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.GAIN);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option even with EndPhaseDecision present", gainBitcoin, result);
    }

    // ========== makeDecision - MONEY Phase Tests ==========

    @Test
    public void testMakeDecision_MoneyPhase_ReturnsPlayCardDecision() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return PlayCardDecision in MONEY phase", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_MultiplePlayCardOptions() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, playEthereum, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first PlayCardDecision", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_PlayCardNotFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should find and return PlayCardDecision even if not first", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_NoPlayCardAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option when no PlayCardDecision available", endPhase, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_MixedDecisionTypes() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        BuyDecision buyCard = new BuyDecision(Card.Type.BITCOIN);
        TrashCardDecision trashCard = new TrashCardDecision(methodCard);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(buyCard, trashCard, playCard, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should find PlayCardDecision among mixed types", playCard, result);
    }

    @Test
    public void testMakeDecision_MoneyPhase_OnlyPlayCardAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playCard = new PlayCardDecision(ethereumCard);
        ImmutableList<Decision> options = ImmutableList.of(playCard);
        
        Decision result = player.makeDecision(state, options);
        
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
        
        Decision result = player.makeDecision(state, options);
        
        assertSame("Should return first option for unrecognized phase", endPhase, result);
    }

    @Test
    public void testMakeDecision_BuyPhase_PreferenceOrder() {
        // Test that the preference order is: FRAMEWORK > DOGECOIN > ETHEREUM > BITCOIN
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        
        // Test Framework is preferred over all
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyEthereum = new BuyDecision(Card.Type.ETHEREUM);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        
        ImmutableList<Decision> options1 = ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin, buyFramework);
        assertEquals("FRAMEWORK should be preferred", buyFramework, player.makeDecision(state, options1));
        
        // Test Dogecoin is preferred over Ethereum and Bitcoin (no Framework)
        ImmutableList<Decision> options2 = ImmutableList.of(buyBitcoin, buyEthereum, buyDogecoin);
        assertEquals("DOGECOIN should be preferred", buyDogecoin, player.makeDecision(state, options2));
        
        // Test Ethereum is preferred over Bitcoin (no Framework or Dogecoin)
        ImmutableList<Decision> options3 = ImmutableList.of(buyBitcoin, buyEthereum);
        assertEquals("ETHEREUM should be preferred", buyEthereum, player.makeDecision(state, options3));
    }

    @Test
    public void testMakeDecision_ActionPhase_FollowsBigMoneyStrategy() {
        // Big Money strategy: actions are not important, just return first option
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playModule, endPhase);
        
        Decision result = player.makeDecision(state, options);
        
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
        
        Decision result1 = player.makeDecision(state, options);
        Decision result2 = player.makeDecision(state, options);
        Decision result3 = player.makeDecision(state, options);
        
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
        
        ImmutableList<Decision> options1 = ImmutableList.of(playBitcoin, playEthereum, playDogecoin);
        Decision result1 = player.makeDecision(state, options1);
        assertTrue("Should return a PlayCardDecision", result1 instanceof PlayCardDecision);
        
        ImmutableList<Decision> options2 = ImmutableList.of(playDogecoin, playBitcoin, playEthereum);
        Decision result2 = player.makeDecision(state, options2);
        assertTrue("Should return a PlayCardDecision", result2 instanceof PlayCardDecision);
    }

    @Test
    public void testMakeDecision_NullSafety_ValidInputs() {
        // Ensure the method handles valid inputs correctly
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        
        Decision result = player.makeDecision(state, options);
        
        assertNotNull("Should return a non-null decision", result);
        assertSame("Should return the EndPhaseDecision", endPhase, result);
    }
}
