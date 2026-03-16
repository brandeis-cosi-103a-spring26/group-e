package edu.brandeis.cosi103a.groupe.cosi21;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import org.junit.Test;

/** Unit tests for {@link EventNotif}. */
public class EventNotifTest {

    @Test
    public void testConstructor_StoresProvidedStateAndEvent() {
        GameState state = createGameState();
        Event event = new PlayCardEvent(new Card(Card.Type.BITCOIN, 99), "Player-1");

        EventNotif notification = new EventNotif(state, event);

        assertSame("Should keep exact state reference", state, notification.getGameState());
        assertSame("Should keep exact event reference", event, notification.getEvent());
    }

    @Test
    public void testConstructor_NullState_IsPreserved() {
        Event event = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 100), "Player-1");

        EventNotif notification = new EventNotif(null, event);

        assertNull(
                "State should remain null when constructed with null", notification.getGameState());
        assertSame("Event should still be preserved", event, notification.getEvent());
    }

    @Test
    public void testConstructor_NullEvent_IsPreserved() {
        GameState state = createGameState();

        EventNotif notification = new EventNotif(state, null);

        assertSame("State should still be preserved", state, notification.getGameState());
        assertNull("Event should remain null when constructed with null", notification.getEvent());
    }

    private GameState createGameState() {
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(new Card(Card.Type.BITCOIN, 1)));
        CardStacks supply = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 60));
        return new GameState("Player-1", hand, GameState.TurnPhase.ACTION, 1, 0, 1, supply);
    }
}
