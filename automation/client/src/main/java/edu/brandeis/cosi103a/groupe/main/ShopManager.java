package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulates all shop operations: gaining cards, checking availability, and building immutable
 * snapshots for GameState. Wraps a shared MutableCardStacks and AtomicInteger card ID counter.
 */
public class ShopManager {

    private final MutableCardStacks stacks;
    private final AtomicInteger cardIdCounter;

    public ShopManager(MutableCardStacks stacks, AtomicInteger cardIdCounter) {
        this.stacks = stacks;
        this.cardIdCounter = cardIdCounter;
    }

    /**
     * Attempts to gain a card of the given type for a player. Decrements the shop count, creates a
     * new Card instance, and adds it to the player's discard pile.
     *
     * @return true if the card was gained; false if none are available in the shop
     */
    public boolean gain(Card.Type type, PlayerInfo player) {
        if (stacks.getNumAvailable(type) <= 0) {
            return false;
        }
        stacks.decrementCount(type);
        Card gained = new Card(type, cardIdCounter.getAndIncrement());
        player.gainCard(gained);
        return true;
    }

    public int getNumAvailable(Card.Type type) {
        return stacks.getNumAvailable(type);
    }

    public ImmutableSet<Card.Type> availableForCost(int maxCost) {
        return stacks.availableCardsForCost(maxCost);
    }

    public CardStacks toImmutableCardStacks() {
        return stacks.toImmutableCardStacks();
    }

    public void incrementDiscount() {
        stacks.incrementDiscount();
    }

    public void resetDiscount() {
        stacks.resetDiscount();
    }

    public int getDiscount() {
        return stacks.getDiscount();
    }
}
