package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.ArrayList;

/**
 * Comprehensive unit tests for the card game application
 */
public class AppTest {

    private Player player;

    @Before
    public void setUp() {
        player = new Player("Test Player");
    }

    // ========== Card Tests ==========

    @Test
    public void testCardConstructorWithName_Method() {
        Card card = new Card(Card.Name.METHOD);
        assertEquals(2, card.getCost());
        assertEquals(1, card.getValue());
        assertEquals(Card.Type.AUTOMATION, card.getType());
        assertEquals(Card.Name.METHOD, card.getName());
        assertEquals("Method", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithName_Module() {
        Card card = new Card(Card.Name.MODULE);
        assertEquals(5, card.getCost());
        assertEquals(3, card.getValue());
        assertEquals(Card.Type.AUTOMATION, card.getType());
        assertEquals("Module", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithName_Framework() {
        Card card = new Card(Card.Name.FRAMEWORK);
        assertEquals(8, card.getCost());
        assertEquals(6, card.getValue());
        assertEquals(Card.Type.AUTOMATION, card.getType());
        assertEquals("Framework", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithName_Bitcoin() {
        Card card = new Card(Card.Name.BITCOIN);
        assertEquals(0, card.getCost());
        assertEquals(1, card.getValue());
        assertEquals(Card.Type.CRYPTOCURRENCY, card.getType());
        assertEquals("Bitcoin", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithName_Ethereum() {
        Card card = new Card(Card.Name.ETHEREUM);
        assertEquals(3, card.getCost());
        assertEquals(2, card.getValue());
        assertEquals(Card.Type.CRYPTOCURRENCY, card.getType());
        assertEquals("Ethereum", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithName_Dogecoin() {
        Card card = new Card(Card.Name.DOGECOIN);
        assertEquals(6, card.getCost());
        assertEquals(3, card.getValue());
        assertEquals(Card.Type.CRYPTOCURRENCY, card.getType());
        assertEquals("Dogecoin", card.getColloquialName());
    }

    @Test
    public void testCardConstructorWithParameters() {
        Card card = new Card(10, 5, Card.Type.AUTOMATION, Card.Name.FRAMEWORK);
        assertEquals(10, card.getCost());
        assertEquals(5, card.getValue());
        assertEquals(Card.Type.AUTOMATION, card.getType());
        assertEquals(Card.Name.FRAMEWORK, card.getName());
    }

    // ========== ShopInfo Tests ==========

    @Test
    public void testShopInfoConstructor() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);
        assertEquals(14, shopInfo.methodCount);
        assertEquals(8, shopInfo.moduleCount);
        assertEquals(8, shopInfo.frameworkCount);
        assertEquals(60, shopInfo.bitcoinCount);
        assertEquals(40, shopInfo.ethereumCount);
        assertEquals(30, shopInfo.dogecoinCount);
    }

    @Test
    public void testShopInfoToString() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);
        String expected = "Methods: 14, Modules: 8, Frameworks: 8, Bitcoin: 60, Ethereum: 40, Dogecoin: 30";
        assertEquals(expected, shopInfo.toString());
    }

    @Test
    public void testShopInfoWithZeroCounts() {
        ShopInfo shopInfo = new ShopInfo(0, 0, 0, 0, 0, 0);
        assertEquals(0, shopInfo.methodCount);
        assertEquals(0, shopInfo.moduleCount);
        assertEquals(0, shopInfo.frameworkCount);
    }

    // ========== Player Tests ==========

    @Test
    public void testPlayerConstructor() {
        Player newPlayer = new Player("Alice");
        assertEquals("Alice", newPlayer.name);
        assertEquals(10, newPlayer.getDeck().size());
        assertEquals(0, newPlayer.getHand().size());
        assertEquals(0, newPlayer.getDiscard().size());

        // Check deck composition: 7 bitcoin, 3 method
        int bitcoinCount = 0;
        int methodCount = 0;
        for (Card card : newPlayer.getDeck()) {
            if (card.getName() == Card.Name.BITCOIN)
                bitcoinCount++;
            if (card.getName() == Card.Name.METHOD)
                methodCount++;
        }
        assertEquals(7, bitcoinCount);
        assertEquals(3, methodCount);
    }

    @Test
    public void testBuyCard_Success() {
        player.coins = 5;
        Card card = new Card(Card.Name.METHOD); // costs 2
        boolean result = player.buyCard(card);

        assertTrue(result);
        assertEquals(3, player.coins);
        assertTrue(player.getDiscard().contains(card));
    }

    @Test
    public void testBuyCard_NotEnoughCoins() {
        player.coins = 1;
        Card card = new Card(Card.Name.METHOD); // costs 2
        boolean result = player.buyCard(card);

        assertFalse(result);
        assertEquals(1, player.coins);
        assertFalse(player.getDiscard().contains(card));
    }

    @Test
    public void testBuyCard_ExactCoins() {
        player.coins = 8;
        Card card = new Card(Card.Name.FRAMEWORK); // costs 8
        boolean result = player.buyCard(card);

        assertTrue(result);
        assertEquals(0, player.coins);
        assertTrue(player.getDiscard().contains(card));
    }

    @Test
    public void testBuyCard_ZeroCostCard() {
        player.coins = 0;
        Card card = new Card(Card.Name.BITCOIN); // costs 0
        boolean result = player.buyCard(card);

        assertTrue(result);
        assertEquals(0, player.coins);
        assertTrue(player.getDiscard().contains(card));
    }

    @Test
    public void testGetDeckValue_OnlyAutomationCards() {
        Player newPlayer = new Player("Test");
        newPlayer.getDeck().clear();
        newPlayer.getDeck().add(new Card(Card.Name.METHOD)); // value 1
        newPlayer.getDeck().add(new Card(Card.Name.MODULE)); // value 3
        newPlayer.getDeck().add(new Card(Card.Name.FRAMEWORK)); // value 6

        int value = newPlayer.getDeckValue();
        assertEquals(10, value);
    }

    @Test
    public void testGetDeckValue_OnlyCryptocurrencyCards() {
        Player newPlayer = new Player("Test");
        newPlayer.getDeck().clear();
        newPlayer.getDeck().add(new Card(Card.Name.BITCOIN));
        newPlayer.getDeck().add(new Card(Card.Name.ETHEREUM));
        newPlayer.getDeck().add(new Card(Card.Name.DOGECOIN));

        int value = newPlayer.getDeckValue();
        assertEquals(0, value); // Cryptocurrency cards don't count for points
    }

    @Test
    public void testGetDeckValue_MixedCards() {
        Player newPlayer = new Player("Test");
        newPlayer.getDeck().clear();
        newPlayer.getDeck().add(new Card(Card.Name.METHOD)); // value 1
        newPlayer.getDeck().add(new Card(Card.Name.BITCOIN)); // value 0 (crypto)
        newPlayer.getDeck().add(new Card(Card.Name.MODULE)); // value 3

        int value = newPlayer.getDeckValue();
        assertEquals(4, value);
    }

    @Test
    public void testGetDeckValue_EmptyDeck() {
        Player newPlayer = new Player("Test");
        newPlayer.getDeck().clear();

        int value = newPlayer.getDeckValue();
        assertEquals(0, value);
    }

    @Test
    public void testPlayTurn_WithEnoughCoinsForFramework() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);

        // Set up a hand that gives 8+ coins
        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3 coins
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3 coins
        player.getDeck().add(new Card(Card.Name.ETHEREUM)); // 2 coins
        player.getDeck().add(new Card(Card.Name.BITCOIN)); // 1 coin
        player.getDeck().add(new Card(Card.Name.BITCOIN)); // 1 coin

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        // Should buy 1 Framework (8 coins) and 1 Method (2 coins)
        assertTrue(requests.size() >= 1);
        assertEquals(Card.Name.FRAMEWORK, requests.get(0).getName());
    }

    @Test
    public void testPlayTurn_NoCoins() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);

        // Set up a hand with only automation cards (no coins)
        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.MODULE));
        player.getDeck().add(new Card(Card.Name.FRAMEWORK));

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        // With 0 coins and no other purchasable cards, should buy 1 Bitcoin (costs 0)
        assertEquals(1, requests.size());
        assertEquals(Card.Name.BITCOIN, requests.get(0).getName());
    }

    @Test
    public void testPlayTurn_GreedyStrategy() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);

        // Give player exactly 10 coins
        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3 coins
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3 coins
        player.getDeck().add(new Card(Card.Name.ETHEREUM)); // 2 coins
        player.getDeck().add(new Card(Card.Name.ETHEREUM)); // 2 coins
        player.getDeck().add(new Card(Card.Name.METHOD)); // 0 coins (automation)

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        // With 10 coins, should buy Framework (8) + Method (2)
        assertEquals(2, requests.size());
        assertEquals(Card.Name.FRAMEWORK, requests.get(0).getName());
        assertEquals(Card.Name.METHOD, requests.get(1).getName());
    }

    @Test
    public void testPlayTurn_EmptyShop() {
        ShopInfo shopInfo = new ShopInfo(0, 0, 0, 60, 0, 0);

        // Give player 10 coins
        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.DOGECOIN));
        player.getDeck().add(new Card(Card.Name.DOGECOIN));
        player.getDeck().add(new Card(Card.Name.DOGECOIN));
        player.getDeck().add(new Card(Card.Name.BITCOIN));
        player.getDeck().add(new Card(Card.Name.BITCOIN));

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        // Should only buy 1 Bitcoin when nothing else available
        assertEquals(1, requests.size());
        assertEquals(Card.Name.BITCOIN, requests.get(0).getName());
    }

    @Test
    public void testPlayTurn_BuysPriorityOrder() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);

        // Give player 15 coins to test priority order
        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3
        player.getDeck().add(new Card(Card.Name.DOGECOIN)); // 3
        // Total: 15 coins

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        // Priority: Framework(8), Dogecoin(6), Module(5), Ethereum(3), Method(2)
        // With 15 coins: Framework(8) leaves 7, Dogecoin(6) leaves 1, nothing else
        assertTrue(requests.size() >= 2);
        assertEquals(Card.Name.FRAMEWORK, requests.get(0).getName());
        assertEquals(Card.Name.DOGECOIN, requests.get(1).getName());
    }

    @Test
    public void testPlayTurn_HandIsDiscardedAfter() {
        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);

        player.playTurn(shopInfo, false);

        assertEquals(0, player.getHand().size());
        assertTrue(player.getDiscard().size() > 0);
    }

    // ========== Game Tests ==========

    @Test
    public void testGameConstructor() {
        Game newGame = new Game(false);
        assertNotNull(newGame);
    }

    @Test
    public void testGameConstructor_VerboseMode() {
        Game verboseGame = new Game(true);
        assertNotNull(verboseGame);
    }

    // Edge case tests

    @Test
    public void testPlayerReshuffleWhenDeckEmpty() {
        // Clear deck and add 5+ cards to discard so we can draw a full hand
        player.getDeck().clear();
        player.getDiscard().add(new Card(Card.Name.METHOD));
        player.getDiscard().add(new Card(Card.Name.BITCOIN));
        player.getDiscard().add(new Card(Card.Name.ETHEREUM));
        player.getDiscard().add(new Card(Card.Name.DOGECOIN));
        player.getDiscard().add(new Card(Card.Name.MODULE));
        player.getDiscard().add(new Card(Card.Name.FRAMEWORK));

        int initialDiscardSize = player.getDiscard().size();

        ShopInfo shopInfo = new ShopInfo(14, 8, 8, 60, 40, 30);
        player.playTurn(shopInfo, false);

        // After playing turn, deck should have been reshuffled from discard
        // Total cards should be preserved
        int totalCards = player.getDeck().size() + player.getDiscard().size() + player.getHand().size();
        assertEquals(initialDiscardSize, totalCards);
    }

    @Test
    public void testMultipleBuyAttempts() {
        player.coins = 20;

        // Buy multiple cards in succession
        assertTrue(player.buyCard(new Card(Card.Name.METHOD))); // 18 left
        assertTrue(player.buyCard(new Card(Card.Name.MODULE))); // 13 left
        assertTrue(player.buyCard(new Card(Card.Name.FRAMEWORK))); // 5 left
        assertTrue(player.buyCard(new Card(Card.Name.ETHEREUM))); // 2 left
        assertTrue(player.buyCard(new Card(Card.Name.METHOD))); // 0 left

        assertEquals(0, player.coins);
        assertEquals(5, player.getDiscard().size());
    }

    @Test
    public void testBuyCard_NegativeCoinsAttempt() {
        player.coins = 1;
        Card expensiveCard = new Card(Card.Name.FRAMEWORK); // costs 8

        assertFalse(player.buyCard(expensiveCard));
        assertEquals(1, player.coins); // Coins unchanged
    }

    @Test
    public void testGetDeckValue_WithCardsInHandAndDiscard() {
        Player newPlayer = new Player("Test");
        newPlayer.getDeck().clear();
        newPlayer.getHand().clear();
        newPlayer.getDiscard().clear();

        // Add cards to different piles
        newPlayer.getDeck().add(new Card(Card.Name.METHOD)); // 1 point
        newPlayer.getHand().add(new Card(Card.Name.MODULE)); // 3 points
        newPlayer.getDiscard().add(new Card(Card.Name.FRAMEWORK)); // 6 points

        int value = newPlayer.getDeckValue();

        // getDeckValue should consolidate everything and count all automation cards
        assertEquals(10, value);
        assertEquals(0, newPlayer.getHand().size()); // Hand should be discarded
    }

    @Test
    public void testShopInfoWithLargeCounts() {
        ShopInfo shopInfo = new ShopInfo(1000, 999, 888, 777, 666, 555);
        assertEquals(1000, shopInfo.methodCount);
        assertEquals(999, shopInfo.moduleCount);
        assertEquals(888, shopInfo.frameworkCount);
        assertEquals(777, shopInfo.bitcoinCount);
        assertEquals(666, shopInfo.ethereumCount);
        assertEquals(555, shopInfo.dogecoinCount);
    }

    @Test
    public void testPlayerName() {
        Player namedPlayer = new Player("Alice");
        assertEquals("Alice", namedPlayer.name);

        Player emptyNamePlayer = new Player("");
        assertEquals("", emptyNamePlayer.name);
    }

    @Test
    public void testPlayTurn_WithOnlyOneCoinShopItem() {
        ShopInfo shopInfo = new ShopInfo(1, 0, 0, 60, 0, 0);

        player.getDeck().clear();
        player.getDeck().add(new Card(Card.Name.ETHEREUM)); // 2 coins
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.METHOD));
        player.getDeck().add(new Card(Card.Name.METHOD));

        ArrayList<Card> requests = player.playTurn(shopInfo, false);

        assertEquals(1, requests.size());
        assertEquals(Card.Name.METHOD, requests.get(0).getName());
    }
}
