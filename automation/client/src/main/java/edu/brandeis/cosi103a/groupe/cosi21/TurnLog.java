package edu.brandeis.cosi103a.groupe.cosi21;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records the events of a single player's turn for display in the turn log panel.
 *
 * <p>A TurnLog is created at the start of each turn and entries are appended live as events occur
 * (cards played, shuffles, draws, buys, etc.). The engine owns and mutates TurnLog instances; all
 * players share a read-only view of the history.
 */
public class TurnLog {

    private final int turnNumber;
    private final String playerName;
    private final List<String> entries = new ArrayList<>();

    /**
     * Creates a new TurnLog for the given turn number and player.
     *
     * @param turnNumber the sequential turn number (1-indexed across all players)
     * @param playerName the name of the player whose turn this is
     */
    public TurnLog(int turnNumber, String playerName) {
        this.turnNumber = turnNumber;
        this.playerName = playerName;
    }

    /** Returns the header line for this turn, e.g. {@code "Turn 3 - Player-2"}. */
    public String getHeader() {
        return "Turn " + turnNumber + " - " + playerName;
    }

    /**
     * Appends an event line to this turn's log.
     *
     * @param entry a short description of the event (not null)
     */
    public void addEntry(String entry) {
        if (entry == null) {
            return;
        }
        entries.add(entry);
    }

    /** Returns an unmodifiable view of this turn's log entries in the order they were added. */
    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Returns the sequential turn number. */
    public int getTurnNumber() {
        return turnNumber;
    }

    /** Returns the player name for this turn. */
    public String getPlayerName() {
        return playerName;
    }
}
