package edu.brandeis.cosi103a.groupe.cosi21;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

/**
 * Immutable container pairing a game snapshot with the event that occurred at that moment.
 *
 * <p>Observer records notifications as EventNotif instances so callers can inspect both the emitted
 * event and the corresponding game state.
 */
public class EventNotif {

    private final GameState gameState;
    private final Event event;

    /**
     * Creates a notification containing the supplied game state and event.
     *
     * @param state the game state associated with the event
     * @param event the event that was emitted
     */
    public EventNotif(GameState state, Event event) {
        this.gameState = state;
        this.event = event;
    }

    /**
     * Returns the game snapshot captured when this event occurred.
     *
     * @return the associated game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Returns the event associated with this notification.
     *
     * @return the emitted event
     */
    public Event getEvent() {
        return event;
    }
}
