package edu.brandeis.cosi103a.groupe.server;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController {
    private final Player player;

    public PlayerController(Player player) {
        this.player = player;
    }

    @PostMapping("/decide")
    public DecisionResponse choose(@RequestBody DecisionRequest decisionRequest) {
        GameState state = decisionRequest.state();
        ImmutableList<Decision> options = decisionRequest.options();
        Optional<Event> reason = decisionRequest.reason();
        return new DecisionResponse(player.makeDecision(state, options, reason));
    }

    @PostMapping("/log-event")
    public void logEvent(@RequestBody LogEventRequest eventRequest) {
        GameState state = eventRequest.state();
        Event event = eventRequest.event();
        player.getObserver().ifPresent(o -> o.notifyEvent(state, event));
    }
}
