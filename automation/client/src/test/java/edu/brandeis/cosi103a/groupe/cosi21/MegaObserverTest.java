package edu.brandeis.cosi103a.groupe.cosi21;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import edu.brandeis.cosi103a.groupe.observers.Observer;
import org.junit.Test;

/** Unit tests for MegaObserver. */
public class MegaObserverTest {

    @Test
    public void testConstructor_StartsWithNoObservers() {
        MegaObserver megaObserver = new MegaObserver();

        assertTrue(
                "MegaObserver should start with no observers",
                megaObserver.getObservers().isEmpty());
    }

    @Test
    public void testAddObserver_RegistersObserver() {
        MegaObserver megaObserver = new MegaObserver();
        Observer observer = new Observer();

        megaObserver.addObserver(observer);

        ImmutableList<Observer> observers = megaObserver.getObservers();
        assertEquals("Should have one registered observer", 1, observers.size());
        assertSame("Should store the same observer instance", observer, observers.get(0));
    }

    @Test
    public void testAddObserver_DuplicateObserver_IsNotAddedTwice() {
        MegaObserver megaObserver = new MegaObserver();
        Observer observer = new Observer();

        megaObserver.addObserver(observer);
        megaObserver.addObserver(observer);

        ImmutableList<Observer> observers = megaObserver.getObservers();
        assertEquals("Duplicate observer should not be added twice", 1, observers.size());

        GameState state = createGameState("Player-1", 3);
        Event event = new PlayCardEvent(new Card(Card.Type.BITCOIN, 101), "Player-1");
        megaObserver.notifyEvent(state, event);

        assertEquals(
                "Observer should only receive one forwarded event", 1, observer.getEvents().size());
    }

    @Test
    public void testAddObserver_NullObserver_IsIgnored() {
        MegaObserver megaObserver = new MegaObserver();

        megaObserver.addObserver(null);

        assertTrue("Null observer should not be added", megaObserver.getObservers().isEmpty());
    }

    @Test
    public void testGetObservers_ReturnsImmutableSnapshot() {
        MegaObserver megaObserver = new MegaObserver();
        Observer firstObserver = new Observer();
        Observer secondObserver = new Observer();
        megaObserver.addObserver(firstObserver);

        ImmutableList<Observer> snapshot = megaObserver.getObservers();

        try {
            snapshot.add(secondObserver);
            fail("Snapshot should be immutable");
        } catch (UnsupportedOperationException expected) {
            // Expected: ImmutableList cannot be modified.
        }

        megaObserver.addObserver(secondObserver);

        assertEquals("Snapshot should remain unchanged", 1, snapshot.size());
        assertEquals(
                "Observer list should continue to grow", 2, megaObserver.getObservers().size());
    }

    @Test
    public void testNotifyEvent_NoObservers_DoesNotThrow() {
        MegaObserver megaObserver = new MegaObserver();

        megaObserver.notifyEvent(
                createGameState("Player-1", 1),
                new PlayCardEvent(new Card(Card.Type.BITCOIN, 99), "Player-1"));

        assertTrue("Observer list should still be empty", megaObserver.getObservers().isEmpty());
    }

    @Test
    public void testNotifyEvent_BroadcastsToAllObservers() {
        MegaObserver megaObserver = new MegaObserver();
        Observer firstObserver = new Observer();
        Observer secondObserver = new Observer();
        megaObserver.addObserver(firstObserver);
        megaObserver.addObserver(secondObserver);

        GameState state = createGameState("Player-1", 2);
        Event event = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 100), "Player-1");

        megaObserver.notifyEvent(state, event);

        ImmutableList<EventNotif> firstEvents = firstObserver.getEvents();
        ImmutableList<EventNotif> secondEvents = secondObserver.getEvents();

        assertEquals("First observer should receive one event", 1, firstEvents.size());
        assertEquals("Second observer should receive one event", 1, secondEvents.size());

        assertSame(
                "First observer should receive same state instance",
                state,
                firstEvents.get(0).getGameState());
        assertSame(
                "First observer should receive same event instance",
                event,
                firstEvents.get(0).getEvent());

        assertSame(
                "Second observer should receive same state instance",
                state,
                secondEvents.get(0).getGameState());
        assertSame(
                "Second observer should receive same event instance",
                event,
                secondEvents.get(0).getEvent());
    }

    @Test
    public void testNotifyEvent_MultipleEvents_PreserveOrderPerObserver() {
        MegaObserver megaObserver = new MegaObserver();
        Observer firstObserver = new Observer();
        Observer secondObserver = new Observer();
        megaObserver.addObserver(firstObserver);
        megaObserver.addObserver(secondObserver);

        GameState firstState = createGameState("Player-1", 4);
        Event firstEvent = new PlayCardEvent(new Card(Card.Type.BITCOIN, 102), "Player-1");
        GameState secondState = createGameState("Player-2", 5);
        Event secondEvent = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 103), "Player-2");

        megaObserver.notifyEvent(firstState, firstEvent);
        megaObserver.notifyEvent(secondState, secondEvent);

        ImmutableList<EventNotif> firstObserverEvents = firstObserver.getEvents();
        ImmutableList<EventNotif> secondObserverEvents = secondObserver.getEvents();

        assertEquals("First observer should receive two events", 2, firstObserverEvents.size());
        assertEquals("Second observer should receive two events", 2, secondObserverEvents.size());

        assertSame(
                "First observer should keep first state in order",
                firstState,
                firstObserverEvents.get(0).getGameState());
        assertSame(
                "First observer should keep first event in order",
                firstEvent,
                firstObserverEvents.get(0).getEvent());
        assertSame(
                "First observer should keep second state in order",
                secondState,
                firstObserverEvents.get(1).getGameState());
        assertSame(
                "First observer should keep second event in order",
                secondEvent,
                firstObserverEvents.get(1).getEvent());

        assertSame(
                "Second observer should keep first state in order",
                firstState,
                secondObserverEvents.get(0).getGameState());
        assertSame(
                "Second observer should keep first event in order",
                firstEvent,
                secondObserverEvents.get(0).getEvent());
        assertSame(
                "Second observer should keep second state in order",
                secondState,
                secondObserverEvents.get(1).getGameState());
        assertSame(
                "Second observer should keep second event in order",
                secondEvent,
                secondObserverEvents.get(1).getEvent());
    }

    @Test
    public void testNotifyEvent_NullStateAndEvent_AreForwarded() {
        MegaObserver megaObserver = new MegaObserver();
        Observer observer = new Observer();
        megaObserver.addObserver(observer);

        megaObserver.notifyEvent(null, null);

        ImmutableList<EventNotif> events = observer.getEvents();
        assertEquals("Observer should receive one notification", 1, events.size());
        assertNull("Stored state should be null", events.get(0).getGameState());
        assertNull("Stored event should be null", events.get(0).getEvent());
    }

    private GameState createGameState(String playerName, int cardId) {
        Hand hand =
                new Hand(ImmutableList.of(), ImmutableList.of(new Card(Card.Type.BITCOIN, cardId)));
        CardStacks supply = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 60));
        return new GameState(playerName, hand, GameState.TurnPhase.ACTION, 1, 0, 1, supply);
    }
}
