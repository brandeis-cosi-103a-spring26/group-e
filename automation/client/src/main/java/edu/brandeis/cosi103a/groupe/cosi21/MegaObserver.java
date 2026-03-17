package edu.brandeis.cosi103a.groupe.cosi21;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Observer implementation that forwards each engine notification to multiple observers.
 *
 * <p>Each call to notifyEvent broadcasts the same notification to all registered observers in
 * insertion order.
 */
public class MegaObserver implements GameObserver {

    private List<Observer> observers;

    /** Creates an empty collection of observers. */
    public MegaObserver() {
        this.observers = new ArrayList<>();
    }

    /**
     * Returns an immutable snapshot of the registered observers.
     *
     * @return an immutable copy of the current observer list
     */
    public ImmutableList<Observer> getObservers() {
        return ImmutableList.copyOf(observers);
    }

    /**
     * Registers a new observer to receive future notifications.
     *
     * @param observer the observer to add; null observers are ignored
     */
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Broadcasts a game notification to every registered observer.
     *
     * @param state the game state associated with the notification
     * @param event the event emitted by the engine
     */
    @Override
    public void notifyEvent(GameState state, Event event) {
        for (Observer observer : observers) {
            observer.notifyEvent(state, event);
        }
    }
}
