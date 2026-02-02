package edu.brandeis.cosi103a.groupe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;

public class OurEngine implements Engine {
    GameState.TurnPhase phase;
    List<Player> players;
    Map<Player, List<Card>> hands; // This could also be Map<Player, Hand> but Hands are immutable so this will
                                   // probably be easier to work with
    Map<Player, List<Card>> decks;
    Map<Player, List<Card>> discards;
    Player activePlayer;
    int numActions;
    int numMoney;
    int numBuys;
    Map<Card, Integer> shop; // Mutable version of CardStacks. You can convert to CardStacks with `new CardStacks(ImmutableMap.copyOf(shop))`
    int cardIdCounter;

    public OurEngine(List<Player> players) {
        cardIdCounter = 0;
        // Throw exceptions for invalid number of players
        if (players.size() > 4) {
            throw new IllegalArgumentException("Provide at most four players");
        }
        if (players.size() <= 1) {
            throw new IllegalArgumentException("Provide at least two players");
        }

        // Initialize fields
        phase = null;
        this.players = players;
        // Create empty hands, decks, and discards for each player
        decks = new HashMap<>();
        discards = new HashMap<>();
        hands = new HashMap<>();
        for (Player p : players) {
            hands.put(p, new ArrayList<>());
            decks.put(p, new ArrayList<>());
            discards.put(p, new ArrayList<>());
        }

        // Initialize decks with starting cards
        for (List<Card> deck : decks.values()) {
            for (int i = 0; i < 7; i++) {
                deck.add(new Card(Card.Type.BITCOIN, cardIdCounter++));
            }
            for (int i = 0; i < 3; i++) {
                deck.add(new Card(Card.Type.METHOD, cardIdCounter++));
            }
        }

        // Initialize shop
        shop = new HashMap<>();
        shop.put(new Card(Card.Type.BITCOIN, cardIdCounter++), 60);
        shop.put(new Card(Card.Type.ETHEREUM, cardIdCounter++), 40);
        shop.put(new Card(Card.Type.DOGECOIN, cardIdCounter++), 30);
        shop.put(new Card(Card.Type.METHOD, cardIdCounter++), 14);
        shop.put(new Card(Card.Type.MODULE, cardIdCounter++), 8);
        shop.put(new Card(Card.Type.FRAMEWORK, cardIdCounter++), 8);
        shop.put(new Card(Card.Type.BUG, cardIdCounter++), players.size() * 10);
        shop.put(new Card(Card.Type.REFACTOR, cardIdCounter++), 10);
        shop.put(new Card(Card.Type.CODE_REVIEW, cardIdCounter++), 10);
        shop.put(new Card(Card.Type.EVERGREEN_TEST, cardIdCounter++), 10);
    }

    @Override
    public GameResult play() throws PlayerViolationException {
        throw new UnsupportedOperationException();
    }
}