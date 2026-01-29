package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;

public class Player {
    ArrayList<Card> deck;
    ArrayList<Card> hand;
    ArrayList<Card> discard;
    int coins;
    String name;

    /**
     * Constructs a new Player with a starting deck and the specified name.
     * The deck is initialized with 7 BITCOIN cards and 3 METHOD cards.
     */
    public Player(String name) {
        this.name = name;
        deck = new ArrayList<>();
        hand = new ArrayList<>();
        discard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            deck.add(new Card(Card.Name.BITCOIN));
        }
        for (int i = 0; i < 3; i++) {
            deck.add(new Card(Card.Name.METHOD));
        }
        java.util.Collections.shuffle(deck);
    }

    /**
     * Draws 5 cards from the deck into the hand and calculates the total coins from
     * CRYPTOCURRENCY cards.
     * If the deck runs out of cards during drawing, it reshuffles the discard pile
     * back into the deck.
     * If verbose is true, prints the drawn hand, played cryptocurrency cards, and
     * total coins.
     *
     * @param verbose whether to print detailed hand information
     */
    private void drawHand(boolean verbose) {
        String handString = "";
        String playedCards = "";
        coins = 0;
        for (int i = 0; i < 5; i++) {
            if (deck.size() == 0) {
                reshuffle(verbose);
            }
            hand.add(deck.remove(0));
        }
        for (Card card : hand) {
            if (verbose) {
                handString += card.getColloquialName() + ", ";
            }
            if (card.getType() == Card.Type.CRYPTOCURRENCY) {
                if (verbose) {
                    playedCards += card.getColloquialName() + ", ";
                }
                coins += card.getValue();
            }
        }
        if (verbose) {
            System.out.println(name + " drew hand: " + handString);
            System.out.println(name + " played: " + playedCards);
            System.out.println(name + " has " + coins + " coins this turn.");
        }
    }

    /**
     * Discards all cards from the hand into the discard pile
     */
    private void discardHand() {
        discard.addAll(hand);
        hand.clear();
    }

    /**
     * Reshuffles the discard pile back into the deck.
     * All cards from the discard pile are added to the deck, then the deck is
     * shuffled.
     * If verbose is true, prints a message indicating the reshuffle action.
     *
     * @param verbose whether to print reshuffle information
     */
    private void reshuffle(boolean verbose) {
        deck.addAll(discard);
        discard.clear();
        java.util.Collections.shuffle(deck);
        if (verbose) {
            System.out.println(name + " reshuffles their discard pile into their deck.");
        }
    }

    /**
     * Calculates the total value of AUTOMATION cards in the player's entire
     * collection.
     * Discards the hand and reshuffles the discard pile into the deck before
     * calculating
     *
     * @return the total value of all automation cards in the deck
     */
    public int getDeckValue() {
        discardHand();
        reshuffle(false);
        int value = 0;
        for (Card card : deck) {
            if (card.getType() == Card.Type.AUTOMATION) {
                value += card.getValue();
            }
        }
        return value;
    }

    /**
     * Attempts to buy a card if the player has enough coins.
     * If successful, deducts the card's cost from the player's coins and adds the
     * card to the discard pile.
     *
     * @param card the card to purchase
     * @return true if the purchase was successful, false if the player didn't have
     *         enough coins
     */
    public boolean buyCard(Card card) {
        if (card.getCost() <= coins) {
            coins -= card.getCost();
            discard.add(card);
            return true;
        }
        return false;
    }

    /**
     * Plays a turn for the player by drawing a hand, evaluating available cards in
     * the shop,
     * and deciding which cards to buy based on the player's current coins.
     * Uses a greedy strategy to buy the most expensive cards possible, prioritizing
     * FRAMEWORK, DOGECOIN, MODULE, ETHEREUM, METHOD in that order, and only buying
     * a singular BITCOIN if no other cards are available.
     * Discards the hand at the end of the turn.
     *
     * @param shopInfo information about the available cards in the shop
     * @param verbose  whether to print detailed turn information (passed to
     *                 drawHand)
     * @return the list of cards the player wants to buy this turn
     */
    public ArrayList<Card> playTurn(ShopInfo shopInfo, boolean verbose) {
        drawHand(verbose);
        ArrayList<Card> requests = new ArrayList<>();
        int theoreticalCoins = coins;
        // Buys the most expensive cards possible with available coins and
        // never buys bitcoin unless no other action can be taken, and only ever buys
        // one bitcoin at a time.
        turnLoop: while (theoreticalCoins > 0) {
            if (theoreticalCoins >= 8 && shopInfo.frameworkCount > 0) {
                requests.add(new Card(Card.Name.FRAMEWORK));
                theoreticalCoins -= 8;
                shopInfo.frameworkCount--;
            } else if (theoreticalCoins >= 6 && shopInfo.dogecoinCount > 0) {
                requests.add(new Card(Card.Name.DOGECOIN));
                theoreticalCoins -= 6;
                shopInfo.dogecoinCount--;
            } else if (theoreticalCoins >= 5 && shopInfo.moduleCount > 0) {
                requests.add(new Card(Card.Name.MODULE));
                theoreticalCoins -= 5;
                shopInfo.moduleCount--;
            } else if (theoreticalCoins >= 3 && shopInfo.ethereumCount > 0) {
                requests.add(new Card(Card.Name.ETHEREUM));
                theoreticalCoins -= 3;
                shopInfo.ethereumCount--;
            } else if (theoreticalCoins >= 2 && shopInfo.methodCount > 0) {
                requests.add(new Card(Card.Name.METHOD));
                theoreticalCoins -= 2;
                shopInfo.methodCount--;
            } else {
                break turnLoop;
            }
        }
        if (requests.isEmpty() && shopInfo.bitcoinCount > 0) {
            requests.add(new Card(Card.Name.BITCOIN));
            shopInfo.bitcoinCount--;
        }
        discardHand();
        return requests;
    }

    /**
     * Gets the player's current hand.
     *
     * @return the list of cards in the player's hand
     */
    public ArrayList<Card> getHand() {
        // Return a defensive copy to avoid exposing the internal representation.
        return hand;
    }

    /**
     * Gets the player's deck of cards.
     *
     * @return the list of cards in the player's deck
     */
    public ArrayList<Card> getDeck() {
        // Return a defensive copy to avoid exposing the internal representation.
        return deck;
    }

    /**
     * Gets the player's discard pile.
     *
     * @return a copy of the list of cards in the player's discard pile
     */
    public ArrayList<Card> getDiscard() {
        return discard;
    }
}
