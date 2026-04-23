package edu.brandeis.cosi103a.groupe.cosi21;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.state.CardStacks;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/** Mutable version of CardStacks. Convert to CardStacks with {@link #toImmutableCardStacks()}. */
public class MutableCardStacks {

    private Map<Type, Integer> shop;
    private boolean isDiscounted = false;

    // Starting shop quantities
    private static final int BITCOIN_COUNT = 60;
    private static final int ETHEREUM_COUNT = 40;
    private static final int DOGECOIN_COUNT = 30;

    private static final int METHOD_COUNT = 14;
    private static final int MODULE_COUNT = 8;
    private static final int FRAMEWORK_COUNT_TWO_PLAYERS = 8;
    private static final int FRAMEWORK_COUNT_THREE_FOUR_PLAYERS = 12;
    private static final int BUG_COUNT_PER_PLAYER = 10; // Multiplied by number of players

    private static final int ACTION_COUNT = 10;

    /**
     * Initializes the mutable card stacks with the starting quantities for each card type based on
     * the number of players and the action cards included in the game.
     *
     * @param numPlayers The number of players in the game, which affects the quantity of certain
     *     cards like BUG and FRAMEWORK
     * @param actionCardsForGame The set of action card types that will be included in the game
     * @throws IllegalArgumentException if numPlayers is invalid or actionCardsForGame is null or
     *     contains null/invalid types
     */
    public MutableCardStacks(int numPlayers, @Nonnull ImmutableSet<Type> actionCardsForGame) {
        if (numPlayers < 2 || numPlayers > 4) {
            throw new IllegalArgumentException(
                    "numPlayers must be between 2 and 4, got: " + numPlayers);
        }

        shop = new HashMap<>();

        // Starting treasures
        shop.put(Type.BITCOIN, BITCOIN_COUNT);
        shop.put(Type.ETHEREUM, ETHEREUM_COUNT);
        shop.put(Type.DOGECOIN, DOGECOIN_COUNT);

        // Starting victories
        shop.put(Type.METHOD, METHOD_COUNT);
        shop.put(Type.MODULE, MODULE_COUNT);
        shop.put(Type.BUG, numPlayers * BUG_COUNT_PER_PLAYER);
        // Set FRAMEWORK count based on the number of players.
        shop.put(
                Type.FRAMEWORK,
                (numPlayers == 2)
                        ? FRAMEWORK_COUNT_TWO_PLAYERS
                        : FRAMEWORK_COUNT_THREE_FOUR_PLAYERS);

        // Starting actions
        for (Type actionType : actionCardsForGame) {
            // Guard against non-action cards
            if (actionType.category() != Type.Category.ACTION) {
                throw new IllegalArgumentException(
                        "actionCardsForGame must only contain ACTION cards, got: "
                                + actionType
                                + " (category: "
                                + actionType.category()
                                + ")");
            }
            shop.put(actionType, ACTION_COUNT);
        }
    }

    /**
     * Converts this mutable card stacks to an immutable CardStacks instance for use in the game
     * state.
     *
     * @return Immutable CardStacks instance representing the current shop state
     */
    public CardStacks toImmutableCardStacks() {
        return new CardStacks(ImmutableMap.copyOf(shop));
    }

    /**
     * Gets the count of a specific card type in the shop.
     *
     * @param type The card type to check
     * @return The count of the specified card type, or 0 if it's not in the shop
     */
    public int getNumAvailable(Type type) {
        return shop.getOrDefault(type, 0);
    }

    /**
     * Decrements the count of a specific card type in the shop by 1, if available.
     *
     * @param type The card type to decrement
     * @return true if the count was successfully decremented, false if the card type is not
     *     available or count is already 0
     */
    public boolean decrementCount(Type type) {
        int currentCount = shop.getOrDefault(type, 0);
        if (currentCount <= 0) {
            return false; // Cannot decrement if count is already 0
        }
        shop.put(type, currentCount - 1);
        return true;
    }

    /**
     * Returns a set of card types that are available for gain based on the current shop state and
     * the given cost.
     *
     * @param cost The cost threshold for available cards
     * @return An immutable set of card types that are available for gain at or below the given cost
     */
    public ImmutableSet<Type> availableCardsForCost(int cost) {
        int discount = 0;
        if (isDiscounted) {
            discount = 1;
        }
        ImmutableSet.Builder<Type> availableCards = ImmutableSet.builder();
        for (Map.Entry<Type, Integer> entry : shop.entrySet()) {
            Type type = entry.getKey();
            int count = entry.getValue();
            if (count > 0 && type.cost() - discount <= cost) {
                availableCards.add(type);
            }
        }
        return availableCards.build();
    }

    /**
     * Enables or disables a 1-cost discount used when checking which cards are affordable.
     *
     * @param d true to enable discount mode, false to disable it
     */
    public void setDiscount(boolean d) {
        isDiscounted = d;
    }

    /**
     * Returns whether discount mode is currently enabled.
     *
     * @return true if discount mode is enabled, false otherwise
     */
    public boolean discounted() {
        return isDiscounted;
    }
}
