package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.state.GameState;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit tests for the DecisionBuilder class.
 * Tests the static factory methods for building valid decision lists
 * for each game phase: ACTION, MONEY, BUY, and trash card selection.
 */
public class DecisionBuilderTest {

    private AtomicInteger cardId;

    @Before
    public void setUp() {
        cardId = new AtomicInteger(1);
    }

    // ========== Helper Methods ==========

    /**
     * Creates a fresh PlayerInfo backed by a BigMoneyPlayer.
     * Starts with a default TurnInfo (1 action, 0 money, 1 buy) and a
     * randomly drawn 5-card hand of BITCOIN and METHOD cards.
     */
    private PlayerInfo createPlayer() {
        return new PlayerInfo(new BigMoneyPlayer(), cardId);
    }

    /**
     * Replaces the player's hand with a fresh MutableHand containing only the provided cards.
     *
     * @param playerInfo the player whose hand to replace
     * @param cards      the cards to place in the new hand (varargs, may be empty)
     */
    private void setHand(PlayerInfo playerInfo, Card... cards) throws Exception {
        Field handField = PlayerInfo.class.getDeclaredField("hand");
        handField.setAccessible(true);
        MutableHand hand = new MutableHand();
        for (Card card : cards) {
            hand.addToUnplayed(card);
        }
        handField.set(playerInfo, hand);
    }

    /**
     * Returns the MutableHand backing the given PlayerInfo, accessed via reflection.
     *
     * @param playerInfo the player whose hand to retrieve
     * @return the player's MutableHand
     */
    private MutableHand getHand(PlayerInfo playerInfo) throws Exception {
        Field handField = PlayerInfo.class.getDeclaredField("hand");
        handField.setAccessible(true);
        return (MutableHand) handField.get(playerInfo);
    }

    /**
     * Returns the set of all Card.Type values whose cost is at most {@code cost}.
     *
     * @param cost the maximum cost (inclusive)
     * @return an ImmutableSet of affordable card types
     */
    private ImmutableSet<Card.Type> buyableCardsForCost(int cost) {
        return Arrays.stream(Card.Type.values())
                .filter(t -> t.cost() <= cost)
                .collect(ImmutableSet.toImmutableSet());
    }

    // ========== buildActionDecisions Tests ==========

    @Test
    public void testBuildActionDecisions_NoActions_OnlyEndPhaseDecision() {
        PlayerInfo playerInfo = createPlayer();
        // TurnInfo starts at 1 action; reduce to 0
        playerInfo.adjustActions(-1);
        assertEquals("Should have 0 actions", 0, playerInfo.numActions());

        ImmutableList<Decision> decisions = DecisionBuilder.buildActionDecisions(playerInfo);

        assertEquals("Should have only one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        assertEquals("Should be ACTION phase",
                GameState.TurnPhase.ACTION, ((EndPhaseDecision) decisions.get(0)).phase());
    }

    @Test
    public void testBuildActionDecisions_HasActionsButNoActionCards_OnlyEndPhaseDecision() {
        PlayerInfo playerInfo = createPlayer();
        // Default TurnInfo gives 1 action, but the starting hand has only BITCOIN/METHOD (no ACTION cards)
        assertTrue("Should have actions", playerInfo.numActions() > 0);
        assertTrue("Starting hand should have no ACTION cards",
                playerInfo.getActionsInHand().isEmpty());

        ImmutableList<Decision> decisions = DecisionBuilder.buildActionDecisions(playerInfo);

        assertEquals("Should have only EndPhaseDecision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildActionDecisions_HasActionsAndActionCards_IncludesPlayCardDecisions()
            throws Exception {
        PlayerInfo playerInfo = createPlayer();
        // Add an action card (CODE_REVIEW) to the hand
        Card codeReview = new Card(Card.Type.CODE_REVIEW, cardId.getAndIncrement());
        getHand(playerInfo).addToUnplayed(codeReview);

        assertTrue("Should have actions", playerInfo.numActions() > 0);
        assertFalse("Should have action cards in hand",
                playerInfo.getActionsInHand().isEmpty());

        ImmutableList<Decision> decisions = DecisionBuilder.buildActionDecisions(playerInfo);

        assertTrue("Should have at least 2 decisions", decisions.size() >= 2);
        assertTrue("Should have PlayCardDecision",
                decisions.stream().anyMatch(d -> d instanceof PlayCardDecision));
        assertTrue("Should have EndPhaseDecision",
                decisions.stream().anyMatch(d -> d instanceof EndPhaseDecision));
    }

    // ========== buildMoneyDecisions Tests ==========

    @Test
    public void testBuildMoneyDecisions_NoMoneyCards_OnlyEndPhaseDecision() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        // Replace hand with an empty hand (no money cards)
        setHand(playerInfo);

        ImmutableList<Decision> decisions = DecisionBuilder.buildMoneyDecisions(playerInfo);

        assertEquals("Should have only one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        assertEquals("Should be MONEY phase",
                GameState.TurnPhase.MONEY, ((EndPhaseDecision) decisions.get(0)).phase());
    }

    @Test
    public void testBuildMoneyDecisions_HasMoneyCards_IncludesPlayCardDecisions() {
        PlayerInfo playerInfo = createPlayer();
        // Starting hand has BITCOIN cards (money category)
        assertFalse("Starting hand should have money cards",
                playerInfo.getMoneysInHand().isEmpty());

        ImmutableList<Decision> decisions = DecisionBuilder.buildMoneyDecisions(playerInfo);

        assertTrue("Should have multiple decisions", decisions.size() > 1);
        assertTrue("Should have PlayCardDecision",
                decisions.stream().anyMatch(d -> d instanceof PlayCardDecision));
        assertTrue("Should have EndPhaseDecision",
                decisions.stream().anyMatch(d -> d instanceof EndPhaseDecision));
    }

    // ========== buildBuyDecisions Tests ==========

    @Test
    public void testBuildBuyDecisions_NoBuys_OnlyEndPhaseDecision() {
        PlayerInfo playerInfo = createPlayer();
        // TurnInfo starts with 1 buy; reduce to 0
        playerInfo.adjustBuys(-1);
        assertEquals("Should have 0 buys", 0, playerInfo.numBuys());

        ImmutableSet<Card.Type> buyableCards = buyableCardsForCost(5);
        ImmutableList<Decision> decisions = DecisionBuilder.buildBuyDecisions(playerInfo, buyableCards);

        assertEquals("Should have only one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        assertEquals("Should be BUY phase",
                GameState.TurnPhase.BUY, ((EndPhaseDecision) decisions.get(0)).phase());
    }

    @Test
    public void testBuildBuyDecisions_HasBuysAndBuyableCards_IncludesBuyDecisions() {
        PlayerInfo playerInfo = createPlayer();
        // Default 1 buy; add 2 more = 3 total
        playerInfo.adjustBuys(2);

        ImmutableSet<Card.Type> buyableCards = buyableCardsForCost(5);
        ImmutableList<Decision> decisions = DecisionBuilder.buildBuyDecisions(playerInfo, buyableCards);

        assertTrue("Should have multiple decisions", decisions.size() > 1);
        assertTrue("Should have BuyDecision",
                decisions.stream().anyMatch(d -> d instanceof BuyDecision));
        assertTrue("Should have EndPhaseDecision",
                decisions.stream().anyMatch(d -> d instanceof EndPhaseDecision));
    }

    @Test
    public void testBuildBuyDecisions_OnlyIncludesCardsFromBuyableSet() {
        PlayerInfo playerInfo = createPlayer();
        // Pass only cards costing <= 3
        ImmutableSet<Card.Type> buyableCards = buyableCardsForCost(3);
        ImmutableList<Decision> decisions = DecisionBuilder.buildBuyDecisions(playerInfo, buyableCards);

        for (Decision decision : decisions) {
            if (decision instanceof BuyDecision) {
                BuyDecision buyDecision = (BuyDecision) decision;
                assertTrue("Should only include cards from the buyable set",
                        buyableCards.contains(buyDecision.cardType()));
            }
        }
    }

    // ========== buildTrashCardDecisions Tests ==========

    @Test
    public void testBuildTrashCardDecisions_EmptyHand_ReturnsEndPhaseDecision() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        setHand(playerInfo); // replace with empty hand

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have one decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
        assertEquals("Should be ACTION phase",
                GameState.TurnPhase.ACTION, ((EndPhaseDecision) decisions.get(0)).phase());
    }

    @Test
    public void testBuildTrashCardDecisions_NonEmptyHand_ReturnsTrashDecisions() {
        PlayerInfo playerInfo = createPlayer();
        int handSize = playerInfo.getUnplayedCards().size();
        assertTrue("Should have cards to trash", handSize > 0);

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have TrashCardDecision for each card in hand",
                handSize, decisions.size());
        for (Decision decision : decisions) {
            assertTrue("All decisions should be TrashCardDecision",
                    decision instanceof TrashCardDecision);
        }
    }

    @Test
    public void testBuildTrashCardDecisions_OnlyPlayedCards_ReturnsEndPhaseDecision() {
        PlayerInfo playerInfo = createPlayer();
        // Play all cards so unplayed is empty
        List<Card> cards = new ArrayList<>(playerInfo.getUnplayedCards());
        for (Card card : cards) {
            playerInfo.playCard(card);
        }
        assertTrue("Unplayed should be empty", playerInfo.getUnplayedCards().isEmpty());

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have 1 decision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }

    @Test
    public void testBuildTrashCardDecisions_SingleCard() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        Card card = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        setHand(playerInfo, card);

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have 1 decision", 1, decisions.size());
        assertTrue("Should be TrashCardDecision", decisions.get(0) instanceof TrashCardDecision);
        assertEquals("Should be for the correct card",
                card, ((TrashCardDecision) decisions.get(0)).card());
    }

    @Test
    public void testBuildTrashCardDecisions_MultipleCards() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        Card card1 = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        Card card2 = new Card(Card.Type.ETHEREUM, cardId.getAndIncrement());
        Card card3 = new Card(Card.Type.METHOD, cardId.getAndIncrement());
        Card card4 = new Card(Card.Type.CODE_REVIEW, cardId.getAndIncrement());
        Card card5 = new Card(Card.Type.MODULE, cardId.getAndIncrement());
        setHand(playerInfo, card1, card2, card3, card4, card5);

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have 5 decisions", 5, decisions.size());
        for (Decision d : decisions) {
            assertTrue("All should be TrashCardDecisions", d instanceof TrashCardDecision);
        }
    }

    @Test
    public void testBuildTrashCardDecisions_VerifyCardInstances() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        Card card1 = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        Card card2 = new Card(Card.Type.ETHEREUM, cardId.getAndIncrement());
        setHand(playerInfo, card1, card2);

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        List<Card> decisionCards = decisions.stream()
                .map(d -> ((TrashCardDecision) d).card())
                .collect(Collectors.toList());
        assertTrue("Should contain card1", decisionCards.contains(card1));
        assertTrue("Should contain card2", decisionCards.contains(card2));
    }

    @Test
    public void testBuildTrashCardDecisions_SameTypesDifferentIds() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        Card bitcoin1 = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        Card bitcoin2 = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        Card bitcoin3 = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        setHand(playerInfo, bitcoin1, bitcoin2, bitcoin3);

        ImmutableList<Decision> decisions = DecisionBuilder.buildTrashCardDecisions(playerInfo);

        assertEquals("Should have 3 distinct decisions", 3, decisions.size());
        long uniqueIds = decisions.stream()
                .map(d -> ((TrashCardDecision) d).card().id())
                .distinct()
                .count();
        assertEquals("All IDs should be unique", 3, uniqueIds);
    }

    @Test
    public void testBuildMoneyDecisions_AllCardsAreMoney_IncludesAllPlusEndPhase() throws Exception {
        PlayerInfo playerInfo = createPlayer();
        // Replace hand with exactly 3 money cards
        Card bitcoin = new Card(Card.Type.BITCOIN, cardId.getAndIncrement());
        Card ethereum = new Card(Card.Type.ETHEREUM, cardId.getAndIncrement());
        Card dogecoin = new Card(Card.Type.DOGECOIN, cardId.getAndIncrement());
        setHand(playerInfo, bitcoin, ethereum, dogecoin);

        ImmutableList<Decision> decisions = DecisionBuilder.buildMoneyDecisions(playerInfo);

        // Should have 3 PlayCardDecisions + 1 EndPhaseDecision = 4 total
        assertEquals("Should have decision for each money card plus EndPhase", 4, decisions.size());
        long playCardCount = decisions.stream()
                .filter(d -> d instanceof PlayCardDecision)
                .count();
        assertEquals("Should have 3 PlayCardDecisions", 3, playCardCount);
    }

    @Test
    public void testBuildBuyDecisions_AfterFirstBuy_RebuildsProperly() {
        PlayerInfo playerInfo = createPlayer();
        // Setup: 2 buys total (default 1 + 1 more), 5 money
        playerInfo.adjustBuys(1);
        playerInfo.adjustMoney(5);

        ImmutableList<Decision> decisionsBeforeBuy =
                DecisionBuilder.buildBuyDecisions(playerInfo, buyableCardsForCost(5));

        long buyOptionsBeforeBuy = decisionsBeforeBuy.stream()
                .filter(d -> d instanceof BuyDecision).count();
        assertTrue("Should have buy options with 5 money", buyOptionsBeforeBuy > 0);

        // Simulate buying ETHEREUM (cost 2): deduct 1 buy and 2 money
        playerInfo.adjustBuys(-1);
        playerInfo.adjustMoney(-2);

        ImmutableList<Decision> decisionsAfterBuy =
                DecisionBuilder.buildBuyDecisions(playerInfo, buyableCardsForCost(playerInfo.numMoney()));

        assertTrue("Should still have buy options after first buy", decisionsAfterBuy.size() > 1);
        assertTrue("Should always include EndPhaseDecision",
                decisionsAfterBuy.stream().anyMatch(d -> d instanceof EndPhaseDecision));
    }

    @Test
    public void testBuildBuyDecisions_AfterExhaustingAllBuys_OnlyEndPhaseDecision() {
        PlayerInfo playerInfo = createPlayer();
        // Start with 1 buy (default) and spend it
        playerInfo.adjustBuys(-1);
        assertEquals("Should have 0 buys after exhausting", 0, playerInfo.numBuys());

        ImmutableList<Decision> decisions =
                DecisionBuilder.buildBuyDecisions(playerInfo, buyableCardsForCost(10));

        assertEquals("Should only have EndPhaseDecision", 1, decisions.size());
        assertTrue("Should be EndPhaseDecision", decisions.get(0) instanceof EndPhaseDecision);
    }
}
