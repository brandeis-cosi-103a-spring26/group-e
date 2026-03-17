package edu.brandeis.cosi103a.groupe.server;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import java.util.Optional;
import org.junit.Test;

public class PlayerControllerTest {

    private GameState createState(GameState.TurnPhase phase) {
        Hand hand =
                new Hand(
                        ImmutableList.of(new Card(Card.Type.BITCOIN, 1)),
                        ImmutableList.of(new Card(Card.Type.METHOD, 2)));
        CardStacks shop = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 10));
        return new GameState("Player-1", hand, phase, 1, 0, 1, shop);
    }

    @Test
    public void choose_DelegatesToPlayerAndReturnsDecision() {
        Player player = mock(Player.class);
        PlayerController controller = new PlayerController(player);

        GameState state = createState(GameState.TurnPhase.ACTION);
        EndPhaseDecision expected = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(expected);
        Optional<Event> reason = Optional.empty();

        when(player.makeDecision(state, options, reason)).thenReturn(expected);

        DecisionRequest request = new DecisionRequest(state, options, reason, "uuid-1");
        Decision actual = controller.choose(request);

        assertSame(expected, actual);
        verify(player).makeDecision(state, options, reason);
    }

    @Test
    public void logEvent_NotifiesObserverWhenPresent() {
        Player player = mock(Player.class);
        GameObserver observer = mock(GameObserver.class);
        PlayerController controller = new PlayerController(player);

        GameState state = createState(GameState.TurnPhase.BUY);
        Event event = new PlayCardEvent(new Card(Card.Type.BITCOIN, 3), "Player-1");

        when(player.getObserver()).thenReturn(Optional.of(observer));

        LogEventRequest request = new LogEventRequest(state, event, "uuid-2");
        controller.logEvent(request);

        verify(observer).notifyEvent(state, event);
    }

    @Test
    public void logEvent_DoesNothingWhenObserverMissing() {
        Player player = mock(Player.class);
        GameObserver observer = mock(GameObserver.class);
        PlayerController controller = new PlayerController(player);

        GameState state = createState(GameState.TurnPhase.BUY);
        Event event = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 4), "Player-2");

        when(player.getObserver()).thenReturn(Optional.empty());

        LogEventRequest request = new LogEventRequest(state, event, "uuid-3");
        controller.logEvent(request);

        verify(observer, never()).notifyEvent(state, event);
    }
}