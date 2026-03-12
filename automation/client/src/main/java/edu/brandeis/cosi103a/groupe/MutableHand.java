package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.Hand;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MutableHand {
    @Nonnull List<Card> unplayedCards = new ArrayList<>();
    @Nonnull List<Card> playedCards = new ArrayList<>();

    public List<Card> getUnplayedCards() {
        return List.copyOf(unplayedCards);
    }

    public List<Card> getPlayedCards() {
        return List.copyOf(playedCards);
    }

    public List<Card> getAllCards() {
        List<Card> allCards = new ArrayList<>(unplayedCards);
        allCards.addAll(playedCards);
        return List.copyOf(allCards);
    }

    public Hand toImmutableHand() {
        return new Hand(
                Objects.requireNonNull(ImmutableList.copyOf(playedCards)),
                Objects.requireNonNull(ImmutableList.copyOf(unplayedCards)));
    }

    /**
     * Moves a card from unplayed to played cards.
     *
     * @param card The card to play
     * @throws IllegalArgumentException if the card is not in unplayed cards
     */
    public void playCard(Card card) {
        if (unplayedCards.remove(card)) {
            playedCards.add(card);
        } else {
            throw new IllegalArgumentException("Card not in unplayed cards: " + card);
        }
    }

    /**
     * Adds a card to the unplayed cards (e.g., when drawing).
     *
     * @param card The card to add
     */
    public void addToUnplayed(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add null card");
        }
        unplayedCards.add(card);
    }

    /**
     * Removes a card from unplayed cards.
     *
     * @param card The card to remove
     * @return true if the card was found and removed, false otherwise
     */
    public boolean removeFromUnplayed(Card card) {
        return unplayedCards.remove(card);
    }

    /**
     * Removes a card from played cards.
     *
     * @param card The card to remove
     * @return true if the card was found and removed, false otherwise
     */
    public boolean removeFromPlayed(Card card) {
        return playedCards.remove(card);
    }

    /**
     * Moves all cards from both unplayed and played to the provided destination list. Clears both
     * internal lists after moving.
     *
     * @param destination The list to move all cards to (e.g., discard pile)
     */
    public void moveAllCardsTo(List<Card> destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        }
        destination.addAll(unplayedCards);
        destination.addAll(playedCards);
        unplayedCards.clear();
        playedCards.clear();
    }

    /** Moves all unplayed cards to the played pile. Useful for certain card effects. */
    public void playAllCards() {
        playedCards.addAll(unplayedCards);
        unplayedCards.clear();
    }

    /**
     * Clears both unplayed and played card lists. Use with caution - typically you want
     * moveAllCardsTo instead.
     */
    public void clear() {
        unplayedCards.clear();
        playedCards.clear();
    }

    /**
     * Returns the total number of cards in hand (both played and unplayed).
     *
     * @return The total card count
     */
    public int size() {
        return unplayedCards.size() + playedCards.size();
    }

    /**
     * Checks if the hand is completely empty.
     *
     * @return true if both unplayed and played lists are empty
     */
    public boolean isEmpty() {
        return unplayedCards.isEmpty() && playedCards.isEmpty();
    }

    /**
     * Checks if a specific card is in the hand (either played or unplayed).
     *
     * @param card The card to check for
     * @return true if the card is in either list
     */
    public boolean contains(Card card) {
        return unplayedCards.contains(card) || playedCards.contains(card);
    }
}
