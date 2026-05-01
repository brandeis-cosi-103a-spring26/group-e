package edu.brandeis.cosi103a.groupe.observers;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.EventNotif;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer implementation that records every engine notification as an EventNotif entry.
 *
 * <p>Each call to notifyEvent appends a new item to the internal history in arrival order, keeping
 * only the most recent {@link #MAX_EVENT_HISTORY} entries.
 */
public class Observer implements GameObserver {

    private static final int MAX_EVENT_HISTORY = 200;
    private final List<EventNotif> events;
    private int currentTurn;

    /** Creates an empty observer history. */
    public Observer() {
        this.events = new ArrayList<>();
        this.currentTurn = 1;
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
     * Returns an immutable snapshot of the recorded events.
     *
     * <p>This method preserves the current behavior of returning a copied, unmodifiable list,
     * equivalent to {@link #getEvents()}.
     *
     * @return an immutable copy of the current event history
     */
    public List<EventNotif> getEventsView() {
        return getEvents();
    }

    /**
     * Records a game notification by storing its state and event together.
     *
     * @param state the game state associated with the notification
     * @param event the event emitted by the engine
     */
    @Override
    public void notifyEvent(GameState state, Event event) {
        events.add(new EventNotif(state, event, currentTurn));
        if (event instanceof EndTurnEvent) {
            currentTurn++;
        }
        while (events.size() > MAX_EVENT_HISTORY) {
            events.remove(0);
        }
    }
}
