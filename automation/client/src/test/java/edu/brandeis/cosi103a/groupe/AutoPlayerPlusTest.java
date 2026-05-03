package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.player.AutoPlayerPlus;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for the AutoPlayerPlus class. Tests decision-making logic for all game
 * phases and various scenarios.
 */
public class AutoPlayerPlusTest {

    private AutoPlayerPlus player;

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
    private Card methodCard;
    private Card moduleCard;

    @Before
    public void setUp() {
        player = new AutoPlayerPlus();

        bitcoinCard = new Card(Card.Type.BITCOIN, 1);
        ethereumCard = new Card(Card.Type.ETHEREUM, 2);
        dogecoinCard = new Card(Card.Type.DOGECOIN, 3);

        ipoCard = new Card(Card.Type.IPO, 4);
        techDebtCard = new Card(Card.Type.TECH_DEBT, 5);
        codeReviewCard = new Card(Card.Type.CODE_REVIEW, 6);
        unitTestCard = new Card(Card.Type.UNIT_TEST, 7);
        backlogCard = new Card(Card.Type.BACKLOG, 8);

        frameworkCard = new Card(Card.Type.FRAMEWORK, 9);
        methodCard = new Card(Card.Type.METHOD, 11);
        moduleCard = new Card(Card.Type.MODULE, 12);
    }

    // ========== Helper Methods ==========

    private GameState createMockGameState(GameState.TurnPhase phase) {
        Hand mockHand = new Hand(ImmutableList.of(), ImmutableList.of());
        CardStacks mockShop =
                new CardStacks(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 50,
                                Card.Type.FRAMEWORK, 8,
                                Card.Type.IPO, 8,
                                Card.Type.DOGECOIN, 8));
        return new GameState("Player-1", mockHand, phase, 1, 0, 1, mockShop);
    }

    private GameState createMockBuyState(ImmutableMap<Card.Type, Integer> shopCounts) {
        Hand mockHand = new Hand(ImmutableList.of(), ImmutableList.of());
        CardStacks mockShop = new CardStacks(shopCounts);
        return new GameState("Player-1", mockHand, GameState.TurnPhase.BUY, 1, 0, 1, mockShop);
    }

    // ========== Constructor and Name Tests ==========

    @Test
    public void testConstructor_CreatesPlayerSuccessfully() {
        AutoPlayerPlus newPlayer = new AutoPlayerPlus();
        assertNotNull("AutoPlayerPlus should be created", newPlayer);
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
                "AutoPlayerPlus-" + uuidName.substring(0, 4),
                displayName);
        assertTrue(
                "Display name should match expected prefix and 4 hex chars",
                displayName.matches("AutoPlayerPlus-[0-9a-f]{4}"));
    }

    @Test
    public void testConstructor_GeneratesUniqueNames() {
        AutoPlayerPlus p1 = new AutoPlayerPlus();
        AutoPlayerPlus p2 = new AutoPlayerPlus();
        AutoPlayerPlus p3 = new AutoPlayerPlus();
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
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        PlayCardDecision playIpo = new PlayCardDecision(ipoCard); // cost 1, +action
        PlayCardDecision playModule = new PlayCardDecision(moduleCard); // cost 5, no +action
        ImmutableList<Decision> options = ImmutableList.of(playModule, playIpo);

        assertSame(
                "Plus-action card should beat higher cost non-plus-action card",
                playIpo,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeActionDecision_PrefersParallelizationOverOtherActions() {
        GameState state = createMockGameState(GameState.TurnPhase.ACTION);
        Card parallelizationCard = new Card(Card.Type.PARALLELIZATION, 13);
        PlayCardDecision playParallelization = new PlayCardDecision(parallelizationCard);
        PlayCardDecision playIpo = new PlayCardDecision(ipoCard);
        PlayCardDecision playModule = new PlayCardDecision(moduleCard);
        ImmutableList<Decision> options =
                ImmutableList.of(playModule, playIpo, playParallelization);

        assertSame(
                "Should prefer PARALLELIZATION over all other action cards",
                playParallelization,
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

    @Test
    public void testMakeBuyDecision_BuysVictoryCards_WhenTwoStacksEmpty() {
        GameState state =
                createMockBuyState(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.FRAMEWORK, 8,
                                Card.Type.IPO, 0,
                                Card.Type.DOGECOIN, 0));

        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyModule = new BuyDecision(Card.Type.MODULE);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options =
                ImmutableList.of(buyDogecoin, buyModule, endPhase, buyFramework);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame("Two empty stacks should trigger victory-card buying", buyFramework, result);
    }

    @Test
    public void testMakeBuyDecision_BuysVictoryCards_WhenFrameworksAreLow() {
        GameState state =
                createMockBuyState(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.FRAMEWORK, 2,
                                Card.Type.IPO, 4,
                                Card.Type.DOGECOIN, 4));

        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyModule = new BuyDecision(Card.Type.MODULE);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyDogecoin, buyModule, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "Fewer than 3 Frameworks left should prioritize victory cards", buyModule, result);
    }

    @Test
    public void testMakeBuyDecision_DoesNotBuyVictoryCards_WhenGameIsNotCloseToEnding() {
        GameState state =
                createMockBuyState(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.FRAMEWORK, 5,
                                Card.Type.IPO, 4,
                                Card.Type.DOGECOIN, 4));

        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        BuyDecision buyModule = new BuyDecision(Card.Type.MODULE);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(buyDogecoin, buyModule, endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());

        assertSame(
                "When the game is not close to ending, the player should keep the normal buy order",
                buyDogecoin,
                result);
    }

    @Test
    public void testDeckStats_TracksTerminalBuysAndGains_FrameworkStillWinsAtThreshold() {
        GameState buyState =
                createMockBuyState(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.FRAMEWORK, 5,
                                Card.Type.IPO, 5,
                                Card.Type.DOGECOIN, 5));

        BuyDecision buyTerminal = new BuyDecision(Card.Type.PARALLELIZATION);
        ImmutableList<Decision> terminalBuyOptions = ImmutableList.of(buyTerminal);

        assertSame(
                buyTerminal,
                player.makeDecision(buyState, terminalBuyOptions, java.util.Optional.empty()));
        assertSame(
                buyTerminal,
                player.makeDecision(buyState, terminalBuyOptions, java.util.Optional.empty()));

        GameState gainState = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainTerminal = new GainCardDecision(Card.Type.PARALLELIZATION);
        ImmutableList<Decision> terminalGainOptions = ImmutableList.of(gainTerminal);

        assertSame(
                gainTerminal,
                player.makeDecision(gainState, terminalGainOptions, java.util.Optional.empty()));

        BuyDecision buyTechDebt = new BuyDecision(Card.Type.TECH_DEBT);
        BuyDecision buyFramework = new BuyDecision(Card.Type.FRAMEWORK);
        ImmutableList<Decision> options = ImmutableList.of(buyFramework, buyTechDebt);

        Decision result = player.makeDecision(buyState, options, java.util.Optional.empty());

        assertSame(
                "Framework should still win even over the terminal threshold",
                buyFramework,
                result);
    }

    @Test
    public void testDeckStats_TrashLowersTerminalRatio_StopsForcingPlusActions() {
        GameState buyState =
                createMockBuyState(
                        ImmutableMap.of(
                                Card.Type.BITCOIN, 10,
                                Card.Type.FRAMEWORK, 5,
                                Card.Type.IPO, 5,
                                Card.Type.DOGECOIN, 5));

        BuyDecision buyTerminal = new BuyDecision(Card.Type.PARALLELIZATION);
        ImmutableList<Decision> terminalBuyOptions = ImmutableList.of(buyTerminal);

        player.makeDecision(buyState, terminalBuyOptions, java.util.Optional.empty());
        player.makeDecision(buyState, terminalBuyOptions, java.util.Optional.empty());
        player.makeDecision(buyState, terminalBuyOptions, java.util.Optional.empty());

        BuyDecision buyTechDebt = new BuyDecision(Card.Type.TECH_DEBT);
        BuyDecision buyDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> beforeTrashOptions = ImmutableList.of(buyDogecoin, buyTechDebt);

        Decision beforeTrash =
                player.makeDecision(buyState, beforeTrashOptions, java.util.Optional.empty());

        assertSame(
                "Over the threshold, prefer plus-action buys when framework is unavailable",
                buyTechDebt,
                beforeTrash);

        GameState trashState = createMockGameState(GameState.TurnPhase.ACTION);
        Card terminalCard = new Card(Card.Type.PARALLELIZATION, 13);
        TrashCardDecision trashTerminal = new TrashCardDecision(terminalCard);
        ImmutableList<Decision> trashOptions = ImmutableList.of(trashTerminal);

        assertSame(
                trashTerminal,
                player.makeDecision(trashState, trashOptions, java.util.Optional.empty()));

        Decision result =
                player.makeDecision(buyState, beforeTrashOptions, java.util.Optional.empty());

        assertSame(
                "After trashing a terminal, should fall back to normal buy priority",
                buyDogecoin,
                result);
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
                "Should prefer DOGECOIN when FRAMEWORK and IPO unavailable",
                gainDogecoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeGainDecision_FallsBackToFirstOption_WhenNothingUseful() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainBug = new GainCardDecision(Card.Type.BUG);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.GAIN);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, gainBug);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("Should fall back to first option", endPhase, result);
    }

    @Test
    public void testMakeGainDecision_AllPreferredAvailable_PicksFrameworkFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        GainCardDecision gainFramework = new GainCardDecision(Card.Type.FRAMEWORK);
        GainCardDecision gainIpo = new GainCardDecision(Card.Type.IPO);
        GainCardDecision gainDogecoin = new GainCardDecision(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(gainDogecoin, gainIpo, gainFramework);

        assertSame(
                "FRAMEWORK is first in preferred list",
                gainFramework,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeGainDecision_OnlyEndPhase_ReturnsEndPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.GAIN);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.GAIN);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeMoneyDecision ==========

    @Test
    public void testMakeMoneyDecision_ReturnsFirstPlayCardDecision() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(playEthereum, playBitcoin);

        assertSame(
                "Should return the first PlayCardDecision in MONEY phase",
                playEthereum,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_ReturnsFirstPlayCardDecision_RegardlessOfValue() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playDogecoin = new PlayCardDecision(dogecoinCard);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, playDogecoin);

        assertSame(
                "Should not skip the first PlayCardDecision in MONEY phase",
                playBitcoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_FallsBackToFirstOption_WhenNothingUseful() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        Decision result = player.makeDecision(state, options, java.util.Optional.empty());
        assertSame("Should fall back to first option", endPhase, result);
    }

    @Test
    public void testMakeMoneyDecision_ReturnsFirstPlayCardDecision_WhenMultipleArePresent() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        PlayCardDecision playEthereum = new PlayCardDecision(ethereumCard);
        PlayCardDecision playDogecoin = new PlayCardDecision(dogecoinCard);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin, playEthereum, playDogecoin);

        assertSame(
                "Should pick the first PlayCardDecision rather than value order",
                playBitcoin,
                player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeMoneyDecision_OnlyEndPhase_ReturnsEndPhase() {
        GameState state = createMockGameState(GameState.TurnPhase.MONEY);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    // ========== makeReactionDecision ==========

    @Test
    public void testMakeReactionDecision_UsesFirstOption() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.REACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);

        assertSame(endPhase, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeReactionDecision_UsesFirstOptionWhenMultiple() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        EndPhaseDecision endPhase = new EndPhaseDecision(GameState.TurnPhase.REACTION);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(endPhase, playBitcoin);

        assertSame(playBitcoin, player.makeDecision(state, options, java.util.Optional.empty()));
    }

    @Test
    public void testMakeReactionDecision_WithNoSpecialOptions_FallsBackToFirst() {
        GameState state = createMockGameState(GameState.TurnPhase.REACTION);
        PlayCardDecision playBitcoin = new PlayCardDecision(bitcoinCard);
        ImmutableList<Decision> options = ImmutableList.of(playBitcoin);

        assertSame(playBitcoin, player.makeDecision(state, options, java.util.Optional.empty()));
    }
}
