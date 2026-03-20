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
import edu.brandeis.cosi103a.groupe.Observers.Observer;

import org.junit.Test;

/** Unit tests for Observer. */
public class ObserverTest {

    @Test
    public void testConstructor_StartsWithEmptyHistory() {
        Observer observer = new Observer();

        assertTrue("Observer should start with no events", observer.getEvents().isEmpty());
    }

    @Test
    public void testNotifyEvent_RecordsStateAndEvent() {
        Observer observer = new Observer();
        GameState state = createGameState("Player-1", 1);
        Event event = new PlayCardEvent(new Card(Card.Type.BITCOIN, 99), "Player-1");

        observer.notifyEvent(state, event);

        ImmutableList<EventNotif> events = observer.getEvents();
        assertEquals("Should record one event", 1, events.size());
        assertSame("Should keep exact GameState reference", state, events.get(0).getGameState());
        assertSame("Should keep exact Event reference", event, events.get(0).getEvent());
    }

    @Test
    public void testNotifyEvent_MultipleEvents_PreservesOrder() {
        Observer observer = new Observer();
        GameState firstState = createGameState("Player-1", 1);
        Event firstEvent = new PlayCardEvent(new Card(Card.Type.BITCOIN, 100), "Player-1");
        GameState secondState = createGameState("Player-2", 2);
        Event secondEvent = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 101), "Player-2");

        observer.notifyEvent(firstState, firstEvent);
        observer.notifyEvent(secondState, secondEvent);

        ImmutableList<EventNotif> events = observer.getEvents();
        assertEquals("Should record two events", 2, events.size());
        assertSame("First event should stay first", firstState, events.get(0).getGameState());
        assertSame(
                "First notification should keep its event", firstEvent, events.get(0).getEvent());
        assertSame("Second event should stay second", secondState, events.get(1).getGameState());
        assertSame(
                "Second notification should keep its event", secondEvent, events.get(1).getEvent());
    }

    @Test
    public void testGetEvents_ReturnsImmutableSnapshot() {
        Observer observer = new Observer();
        observer.notifyEvent(
                createGameState("Player-1", 1),
                new PlayCardEvent(new Card(Card.Type.BITCOIN, 201), "Player-1"));

        ImmutableList<EventNotif> snapshot = observer.getEvents();

        try {
            snapshot.add(new EventNotif(createGameState("Player-3", 3), null));
            fail("Snapshot should be immutable");
        } catch (UnsupportedOperationException expected) {
            // Expected: ImmutableList cannot be modified.
        }

        observer.notifyEvent(
                createGameState("Player-2", 2),
                new PlayCardEvent(new Card(Card.Type.DOGECOIN, 202), "Player-2"));

        assertEquals("Snapshot should remain unchanged", 1, snapshot.size());
        assertEquals("Observer history should continue to grow", 2, observer.getEvents().size());
    }

    @Test
    public void testNotifyEvent_NullStateAndEvent_AreRecorded() {
        Observer observer = new Observer();

        observer.notifyEvent(null, null);

        ImmutableList<EventNotif> events = observer.getEvents();
        assertEquals("Should record one notification", 1, events.size());
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
