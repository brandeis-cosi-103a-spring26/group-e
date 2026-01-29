package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;

public class Game {
    private Player player1;
    private Player player2;
    private ArrayList<Card> methods;
    private ArrayList<Card> modules;
    private ArrayList<Card> frameworks;
    private ArrayList<Card> bitcoins;
    private ArrayList<Card> ethereums;
    private ArrayList<Card> dogecoins;
    private boolean globalVerbose;

    /**
     * Constructs a new Game with two players and initializes the shop's card
     * inventory.
     * The shop starts with:
     * - 14 METHOD cards
     * - 8 MODULE cards
     * - 8 FRAMEWORK cards
     * - 60 BITCOIN cards
     * - 40 ETHEREUM cards
     * - 30 DOGECOIN cards
     */
    public Game(boolean globalVerbose) {
        this.globalVerbose = globalVerbose;
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");
        methods = new ArrayList<>();
        modules = new ArrayList<>();
        frameworks = new ArrayList<>();
        bitcoins = new ArrayList<>();
        ethereums = new ArrayList<>();
        dogecoins = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            methods.add(new Card(Card.Name.METHOD));
        }
        for (int i = 0; i < 8; i++) {
            modules.add(new Card(Card.Name.MODULE));
        }
        for (int i = 0; i < 8; i++) {
            frameworks.add(new Card(Card.Name.FRAMEWORK));
        }
        for (int i = 0; i < 60; i++) {
            bitcoins.add(new Card(Card.Name.BITCOIN));
        }
        for (int i = 0; i < 40; i++) {
            ethereums.add(new Card(Card.Name.ETHEREUM));
        }
        for (int i = 0; i < 30; i++) {
            dogecoins.add(new Card(Card.Name.DOGECOIN));
        }
    }

    /**
     * Creates a ShopInfo object containing the current count of each card type
     * available in the shop.
     *
     * @return a ShopInfo object with the current inventory counts
     */
    private ShopInfo makeShopInfo() {
        return new ShopInfo(
                methods.size(),
                modules.size(),
                frameworks.size(),
                bitcoins.size(),
                ethereums.size(),
                dogecoins.size());
    }

    /**
     * Attempts to purchase a card from the shop for the specified player.
     * The purchase will fail if:
     * - The requested card type is not available in the shop
     * - The player does not have enough coins to buy the card
     *
     * @param player the player attempting to buy the card
     * @param name   the type of card to purchase
     * @return true if the purchase was successful, false otherwise
     */
    private boolean buyCard(Player player, Card.Name name) {
        ArrayList<Card> deck = null;

        switch (name) {
            case METHOD:
                deck = methods;
                break;
            case MODULE:
                deck = modules;
                break;
            case FRAMEWORK:
                deck = frameworks;
                break;
            case BITCOIN:
                deck = bitcoins;
                break;
            case ETHEREUM:
                deck = ethereums;
                break;
            case DOGECOIN:
                deck = dogecoins;
                break;
        }

        if (deck == null || deck.isEmpty()) {
            return false;
        }
        Card card = deck.get(0);

        if (player.coins < card.getCost()) {
            return false;
        }

        deck.remove(0);

        if (!player.buyCard(card)) {
            deck.add(0, card);
            return false;
        }

        return true;
    }

    /**
     * Executes a turn for the specified player.
     * The player draws a hand, evaluates available cards, and attempts to purchase
     * cards.
     * If a card purchase fails, an error message is printed to the console.
     * If verbose is true, prints information about card purchases and hand
     * discards.
     *
     * @param player  the player whose turn is being played
     * @param verbose whether to print detailed turn information
     */
    private void playTurn(Player player, boolean verbose) {
        ArrayList<Card> requests = player.playTurn(makeShopInfo(), verbose);
        for (Card card : requests) {
            boolean success = buyCard(player, card.getName());
            if (!success) {
                System.out.println("Failed to buy card: " + card.getColloquialName());
            } else if (verbose) {
                System.out.println(player.name + " buys a " + card.getColloquialName() + " card.");
            }
        }
        if (verbose) {
            System.out.println(player.name + " discards their hand.");
        }
    }

    /**
     * Runs the main game loop, alternating turns between players until all
     * FRAMEWORK cards are sold.
     * After the game ends, performs cleanup to determine and announce the winner.
     */
    public void gameLoop() {
        int turnCount = 1;
        Player currentPlayer = player1;
        while (true) {
            if (globalVerbose) {
                System.out.println("=== Turn " + turnCount + ": " + currentPlayer.name + "'s turn ===");
            }
            playTurn(currentPlayer, globalVerbose);
            if (frameworks.size() == 0) {
                cleanup();
                return;
            }
            System.out.println();
            currentPlayer = currentPlayer == player1 ? player2 : player1;
            turnCount++;
        }
    }

    /**
     * Calculates final scores for both players and prints the results.
     * Determines and announces the winner based on deck values.
     */
    private void cleanup() {
        int p1DeckValue = player1.getDeckValue();
        int p2DeckValue = player2.getDeckValue();
        Player winner = p1DeckValue > p2DeckValue ? player1 : player2;
        System.out.println(player1.name + " points: " + p1DeckValue);
        System.out.println(player2.name + " points: " + p2DeckValue);
        System.out.println("Winner: " + winner.name);
    }
}
