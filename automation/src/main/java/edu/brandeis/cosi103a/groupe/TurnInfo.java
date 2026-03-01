package edu.brandeis.cosi103a.groupe;

/**
 * Manages turn-specific resources for a player during their turn in the game.
 * Tracks the number of actions, money, and buys available to the player.
 * 
 * Each turn starts with default values (1 action, 0 money, 1 buy) which can be
 * modified by playing action cards or money cards.
 */
public class TurnInfo {

    private int numActions;
    private int numMoney;
    private int numBuys;

    /**
     * Constructs a new TurnInfo with default starting values.
     * Initializes with 1 action, 0 money, and 1 buy.
     */
    public TurnInfo() {
        numActions = 1;
        numMoney = 0;
        numBuys = 1;
    }

    /**
     * Gets the number of actions remaining for the current turn.
     * 
     * @return The number of actions available
     */
    public int getNumActions() {
        return numActions;
    }

    /**
     * Increases the number of actions by the specified amount.
     * Used when action cards grant additional actions (e.g., CODE_REVIEW grants +2 actions).
     * 
     * @param amount The number of actions to add
     */
    public void incrementActions(int amount) {
        numActions += amount;
    }

    /**
     * Decreases the number of actions by 1.
     * Used when an action card is played.
     * Only decrements if at least one action is available.
     */
    public void decrementActions() {
        if (numActions > 0) {
            numActions -= 1;
        }
    }

    /**
     * Gets the amount of money available for purchases during the current turn.
     * 
     * @return The amount of money (coins) available
     */
    public int getNumMoney() {
        return numMoney;
    }

    /**
     * Increases the amount of money by the specified amount.
     * Used when money cards are played (e.g., BITCOIN adds 1, ETHEREUM adds 2).
     * 
     * @param amount The amount of money to add
     */
    public void incrementMoney(int amount) {
        numMoney += amount;
    }

    /**
     * Decreases the amount of money by the specified amount.
     * Used when purchasing cards during the buy phase.
     * Only decrements if sufficient money is available.
     * 
     * @param amount The amount of money to subtract
     */
    public void decrementMoney(int amount) {
        if (numMoney >= amount) {
            numMoney -= amount;
        }
    }

    /**
     * Gets the number of buy opportunities remaining for the current turn.
     * 
     * @return The number of buys available
     */
    public int getNumBuys() {
        return numBuys;
    }

    /**
     * Increases the number of buys by 1.
     * Used when action cards grant additional buys.
     */
    public void incrementBuys() {
        numBuys += 1;
    }

    /**
     * Decreases the number of buys by 1.
     * Used when a card is purchased during the buy phase.
     * Only decrements if at least one buy is available.
     */
    public void decrementBuys() {
        if (numBuys > 0) {
            numBuys -= 1;
        }
    }

    /**
     * Resets all turn resources to their default starting values.
     * Called at the beginning of each new turn (1 action, 0 money, 1 buy).
     */
    public void reset() {
        numActions = 1;
        numMoney = 0;
        numBuys = 1;
    }

}
