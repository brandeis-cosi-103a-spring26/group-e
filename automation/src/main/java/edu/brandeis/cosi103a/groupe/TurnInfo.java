package edu.brandeis.cosi103a.groupe;

/**
 * Manages turn-specific resources for a player during their turn in the game. Tracks the number of
 * actions, money, and buys available to the player.
 *
 * <p>Each turn starts with default values (1 action, 0 money, 1 buy) which can be modified by
 * playing action cards or money cards.
 */
public class TurnInfo {

    private int numActions;
    private int numMoney;
    private int numBuys;

    /**
     * Constructs a new TurnInfo with default starting values. Initializes with 1 action, 0 money,
     * and 1 buy.
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
     * Adjusts the number of actions by the specified amount. Positive values increase actions
     * (e.g., +2 from CODE_REVIEW card). Negative values decrease actions (e.g., -1 when playing an
     * action card). If decreasing by more than available, actions are set to 0.
     *
     * @param amount The amount to adjust actions by (can be positive or negative)
     */
    public void adjustActions(int amount) {
        if (amount < 0 && numActions < -amount) {
            numActions = 0;
        } else {
            numActions += amount;
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
     * Adjusts the amount of money by the specified amount. Positive values increase money (e.g., +1
     * from BITCOIN, +2 from ETHEREUM). Negative values decrease money (e.g., -3 when buying a
     * card). Only decreases if sufficient money is available.
     *
     * @param amount The amount to adjust money by (can be positive or negative)
     */
    public void adjustMoney(int amount) {
        if (amount < 0 && numMoney >= -amount) {
            numMoney += amount;
        } else if (amount > 0) {
            numMoney += amount;
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
     * Adjusts the number of buys by the specified amount. Positive values increase buys (e.g., +1
     * from an action card). Negative values decrease buys (e.g., -1 when purchasing a card). If
     * decreasing by more than available, buys are set to 0.
     *
     * @param amount The amount to adjust buys by (can be positive or negative)
     */
    public void adjustBuys(int amount) {
        if (amount < 0 && numBuys < -amount) {
            numBuys = 0;
        } else {
            numBuys += amount;
        }
    }

    /**
     * Resets all turn resources to their default starting values. Called at the beginning of each
     * new turn (1 action, 0 money, 1 buy).
     */
    public void reset() {
        numActions = 1;
        numMoney = 0;
        numBuys = 1;
    }
}
