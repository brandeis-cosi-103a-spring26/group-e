package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the AutoPlayer class. Tests decision-making logic for all game
 * phases and various scenarios.
 */
public class AutoPlayerTest {

    private AutoPlayer player;

    // Treasure cards
    private Card bitcoinCard;
    private Card ethereumCard;
    private Card dogecoinCard;

    // Action cards
    private Card ipoCard;
    private Card techDebtCard;
    private Card codeReviewCard;
    private Card unitTestCard;
    private Card backlogCard;

    // Victory cards
    private Card frameworkCard;
    private Card bugCard;
    private Card methodCard;
    private Card moduleCard;

    @Before
    public void setUp() {
        player = new AutoPlayer();

        bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        ethereumCard = new Card(Card.Type.ETHEREUM, 2);
        dogecoinCard = new Card(Card.Type.DOGECOIN, 3);

        ipoCard = new Card(Card.Type.IPO, 4);
        techDebtCard = new Card(Card.Type.TECH_DEBT, 5);
        codeReviewCard = new Card(Card.Type.CODE_REVIEW, 6);
        unitTestCard = new Card(Card.Type.UNIT_TEST, 7);
        backlogCard = new Card(Card.Type.BACKLOG, 8);

        frameworkCard = new Card(Card.Type.FRAMEWORK, 9);
        bugCard = new Card(Card.Type.BUG, 10);
        methodCard = new Card(Card.Type.METHOD, 11);
        moduleCard = new Card(Card.Type.MODULE, 12);
    }

    // ========== Helper Methods ==========

    private GameState createMockGameState(GameState.TurnPhase phase) {
        Hand mockHand = new Hand(ImmutableList.of(bitcoinCard, methodCard), ImmutableList.of());
        CardStacks mockShop =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 50,
                                Card.Type.FRAMEWORK, 8));
        return new GameState("Player-1", mockHand, phase, 1, 0, 1, mockShop);
    }

    // ========== Constructor and Name Tests ==========

    @Test
    public void testConstructor_CreatesPlayerSuccessfully() {
        AutoPlayer newPlayer = new AutoPlayer();
        assertNotNull("AutoPlayer should be created", newPlayer);
    }

    @Test
    public void testGetName_ReturnsNonNullName() {
        assertNotNull("Player name should not be null", player.getName());
    }

    @Test
    public void testGetDisplayName_UsesExpectedPattern_AndIsSeparateFromUuid() {
        String uuidName = player.getName();
        String displayName = player.getDisplayName();

        assertNotEquals("Display name should not be the raw UUID", uuidName, displayName);
        assertEquals(
                "Display name should be based on UUID prefix",
                "AutoPlayer-" + uuidName.substring(0, 4),
                displayName);
        assertTrue(
                "Display name should match expected prefix and 4 hex chars",
                displayName.matches("AutoPlayer-[0-9a-f]{4}"));
    }

    @Test
    public void testConstructor_GeneratesUniqueNames() {
        AutoPlayer p1 = new AutoPlayer();
        AutoPlayer p2 = new AutoPlayer();
        AutoPlayer p3 = new AutoPlayer();
        assertNotEquals("Names should be unique", p1.getName(), p2.getName());
        assertNotEquals("Names should be unique", p2.getName(), p3.getName());
        assertNotEquals("Names should be unique", p1.getName(), p3.getName());
    }

    // ========== makeDecision - EndPhaseDecision shortcut ==========

    @Test
    public void testMakeDecision_SingleEndPhase_ReturnsEndPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeDecision_SingleEndPhase_InBuyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeDecision_SingleEndPhase_InMoneyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeDecision - Phase routing ==========

    @Test
    public void testMakeDecision_RoutesToActionPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playIpo = new PlayCardDecision(ipoCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playIpo);

        // IPO is a +action card; action phase logic should pick it over EndPhase
        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("ACTION phase should prefer IPO (+action card)", playIpo, result);
    }

    @Test
    public void testMakeDecision_RoutesToMoneyPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("MONEY phase should find PlayCardDecision", playBitcoin, result);
    }

    @Test
    public void testMakeDecision_UnrecognizedPhase_ReturnsFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.CLEANUP);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.CLEANUP);
        PlayCardDecision playCard = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playCard);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("Unrecognized phase should return first option", endPhase, result);
    }

    // ========== makeActionDecision ==========

    @Test
    public void testMakeActionDecision_PrefersIPO_PlusActionCard() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playIpo = new PlayCardDecision(ipoCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playModule, playIpo, endPhase);

        assertSame(
                "Should prefer IPO (+action) over MODULE",
                playIpo,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PrefersTechDebt_PlusActionCard() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playTechDebt = new PlayCardDecision(techDebtCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        ImmutableList<Decision> options = ImmutableList.of(playModule, playTechDebt);

        assertSame(
                "Should prefer TECH_DEBT (+action)",
                playTechDebt,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PrefersCodeReview_PlusActionCard() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playCodeReview = new PlayCardDecision(codeReviewCard);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playCodeReview);

        assertSame(
                "Should prefer CODE_REVIEW (+action)",
                playCodeReview,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PrefersUnitTest_PlusActionCard() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playUnitTest = new PlayCardDecision(unitTestCard);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playUnitTest);

        assertSame(
                "Should prefer UNIT_TEST (+action)",
                playUnitTest,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PrefersBacklog_PlusActionCard() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playBacklog = new PlayCardDecision(backlogCard);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playBacklog);

        assertSame(
                "Should prefer BACKLOG (+action)",
                playBacklog,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_NoPlusActionCards_PlayHighestCostFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard); // cost 4
        PlayCardDecision playModule = new PlayCardDecision(moduleCard); // cost 5
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, endPhase, playModule);

        assertSame(
                "Should play highest cost action card when no +action cards",
                playModule,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_NoPlayableCards_FallsBackToFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(
                "Should fall back to first option when no playable cards",
                endPhase,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PlusActionPriorityOverHighCost() {
        // IPO (cost 1, +action) should beat MODULE (cost 5, no +action)
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playIpo = new PlayCardDecision(ipoCard); // cost 1, +action
        PlayCardDecision playModule = new PlayCardDecision(moduleCard); // cost 5, no +action
        ImmutableList<Decision> options = ImmutableList.of(playModule, playIpo);

        assertSame(
                "Plus-action card should beat higher cost non-plus-action card",
                playIpo,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeBuyDecision ==========

    @Test
    public void testMakeBuyDecision_PrefersFramework() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyDogecoin, buyFramework);

        assertSame(
                "Should prefer FRAMEWORK",
                buyFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeBuyDecision_PrefersIPO_OverDogecoin() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyIpo = new BuyDecision(Card.Type.IPO);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyDogecoin, buyIpo);

        assertSame(
                "Should prefer IPO over DOGECOIN (IPO is second in PREFERRED_CARDS)",
                buyIpo,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeBuyDecision_PrefersDogecoin_WhenNoFrameworkOrIPO() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyDogecoin);

        assertSame(
                "Should prefer DOGECOIN when FRAMEWORK and IPO unavailable",
                buyDogecoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeBuyDecision_FallsBackToFirstOption_WhenNothingUseful() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        // Only victory cards that aren't FRAMEWORK, and cost 1 (below loop range)
        BuyDecision buyBug = new BuyDecision(Card.Type.BUG);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, buyBug);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("Should fall back to first option", endPhase, result);
    }

    @Test
    public void testMakeBuyDecision_AllPreferredAvailable_PicksFrameworkFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        BuyDecision buyIpo = new BuyDecision(Card.Type.IPO);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyDogecoin, buyIpo, buyFramework);

        assertSame(
                "FRAMEWORK is first in preferred list",
                buyFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeBuyDecision_OnlyEndPhase_ReturnsEndPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeDiscardDecision ==========

    @Test
    public void testMakeDiscardDecision_DiscardsVictoryCardFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.DISCARD);
        DiscardCardDecision discardBitcoin = new DiscardCardDecision(bitcoinCard);
        DiscardCardDecision discardFramework = new DiscardCardDecision(frameworkCard);
        ImmutableList<Decision> options = ImmutableList.of(discardBitcoin, discardFramework);

        assertSame(
                "Should discard victory card (FRAMEWORK) first",
                discardFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeDiscardDecision_VictoryCardBeforeBitcoin() {
        GameState state = createMockGameState(GameState.TurnPhase.DISCARD);
        DiscardCardDecision discardBitcoin = new DiscardCardDecision(bitcoinCard);
        DiscardCardDecision discardFramework = new DiscardCardDecision(frameworkCard);
        ImmutableList<Decision> options = ImmutableList.of(discardBitcoin, discardFramework);

        // Victory card should take priority over BITCOIN
        assertSame(
                "Victory card should be discarded before BITCOIN",
                discardFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeDiscardDecision_FallsBackToFirst_WhenOnlyFallback() {
        GameState state = createMockGameState(GameState.TurnPhase.DISCARD);
        Card weirdCard = new Card(Card.Type.ETHEREUM, 0);
        DiscardCardDecision discardWeird = new DiscardCardDecision(weirdCard);
        ImmutableList<Decision> options = ImmutableList.of(discardWeird);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("Should fall back to first option", discardWeird, result);
    }

    // ========== makeGainDecision ==========

    @Test
    public void testMakeGainDecision_PrefersFramework() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainFramework = new GainCardDecision(Card.Type.FRAMEWORK);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options =
                ImmutableList.of(gainBitcoin, gainDogecoin, gainFramework);

        assertSame(
                "Should prefer FRAMEWORK",
                gainFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeGainDecision_PrefersIPO_OverDogecoin() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainIpo = new GainCardDecision(Card.Type.IPO);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(gainDogecoin, gainIpo);

        assertSame(
                "Should prefer IPO over DOGECOIN",
                gainIpo,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeGainDecision_PrefersDogecoin_WhenNoFrameworkOrIPO() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBitcoin = new GainCardDecision(Card.Type.BITCOIN);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(gainBitcoin, gainDogecoin);

        assertSame(
                "Should prefer DOGECOIN when top two unavailable",
                gainDogecoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeGainDecision_FallsBackToFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBug = new GainCardDecision(Card.Type.BUG);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.GAIN);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, gainBug);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame(
                "Should fall back to first option when no non-victory cards in range",
                endPhase,
                result);
    }

    // ========== makeMoneyDecision ==========

    @Test
    public void testMakeMoneyDecision_ReturnsPlayCardDecision() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);

        assertSame(
                "Should return PlayCardDecision",
                playBitcoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_ReturnsFirstPlayCard_WhenMultipleAvailable() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, playEthereum);

        assertSame(
                "Should return first PlayCardDecision",
                playBitcoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_FallsBackToFirst_WhenNoPlayCard() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(
                "Should return first option when no PlayCardDecision",
                endPhase,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_FindsPlayCardNotAtFront() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);

        assertSame(
                "Should find PlayCardDecision even when not first",
                playBitcoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeReactionDecision ==========

    @Test
    public void testMakeReactionDecision_ReturnsPlayCardDecision() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        PlayCardDecision playCard = new PlayCardDecision(methodCard);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.REACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playCard);

        assertSame(
                "Should react by playing card if available",
                playCard,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeReactionDecision_FallsBackWhenNoPlayCard() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.REACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(
                "Should fall back to first option when no reaction card",
                endPhase,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeReactionDecision_ReturnsFirstPlayCard_WhenMultiple() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        PlayCardDecision playMethod = new PlayCardDecision(methodCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        ImmutableList<Decision> options = ImmutableList.of(playMethod, playModule);

        assertSame(
                "Should return first PlayCardDecision during reaction",
                playMethod,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== Consistency / determinism ==========

    @Test
    public void testMakeDecision_ConsistentResults_SameInputs() {
        GameState state = createMockGameState(GameState.TurnPhase.BUY);
        BuyDecision buyBitcoin = new BuyDecision(Card.Type.BITCOIN);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(buyBitcoin, buyDogecoin);

        Decision r1 = player.makeDecision(state, options, java.util.Optional.empty());
        Decision r2 = player.makeDecision(state, options, java.util.Optional.empty());
        Decision r3 = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(r1, r2);
        assertSame(r2, r3);
        assertSame("Should consistently prefer DOGECOIN", buyDogecoin, r1);
    }
}
