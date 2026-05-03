package edu.brandeis.cosi.atg.state;

import com.google.common.collect.ImmutableCollection;
import edu.brandeis.cosi.atg.cards.Card;

/**
 * Represents a player's result at the end of a game, including their name,
 * score, and ending deck.
 *
 * @param playerName the name of the player
 * @param score      the score of the player
 * @param endingDeck the player's deck at the end of the game
 */
public record PlayerResult(String playerName, int score, ImmutableCollection<Card> endingDeck) {
    /**
     * Compact constructor that validates required fields are not null.
     */
    public PlayerResult {
        java.util.Objects.requireNonNull(playerName, "playerName must not be null");
        java.util.Objects.requireNonNull(endingDeck, "endingDeck must not be null");
    }
}
