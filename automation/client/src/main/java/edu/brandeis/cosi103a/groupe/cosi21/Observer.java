package edu.brandeis.cosi103a.groupe.cosi21;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer implementation that records every engine notification as an EventNotif entry.
 *
 * <p>Each call to notifyEvent appends a new item to the internal history in arrival order.
 */
public class Observer implements GameObserver {

    private final List<EventNotif> events;

    /** Creates an empty observer history. */
    public Observer() {
        this.events = new ArrayList<>();
    }

    /**
     * Returns an immutable snapshot of recorded events.
     *
     * @return an immutable copy of the current event history
     */
    public ImmutableList<EventNotif> getEvents() {
        return ImmutableList.copyOf(events);
    }

    /**
     * Records a game notification by storing its state and event together.
     *
     * @param state the game state associated with the notification
     * @param event the event emitted by the engine
     */
    @Override
    public void notifyEvent(GameState state, Event event) {
        events.add(new EventNotif(state, event));
    }
}
