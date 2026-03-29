package edu.brandeis.cosi103a.groupe.server;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.api.DecisionRequest;
import edu.brandeis.cosi103a.groupe.api.DecisionResponse;
import edu.brandeis.cosi103a.groupe.api.LogEventRequest;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController {
    private final ConcurrentMap<String, Player> playerMap;

    public PlayerController(ConcurrentMap<String, Player> playerMap) {
        this.playerMap = playerMap;
    }

    @PostMapping("/decide")
    public DecisionResponse choose(@RequestBody DecisionRequest decisionRequest) {
        Player player = getOrCreatePlayer(decisionRequest.playerUUID());
        GameState state = decisionRequest.state();
        ImmutableList<Decision> options = decisionRequest.options();
        Optional<Event> reason = decisionRequest.reason();
        return new DecisionResponse(player.makeDecision(state, options, reason));
    }

    @PostMapping("/log-event")
    public void logEvent(@RequestBody LogEventRequest eventRequest) {
        Player player = getOrCreatePlayer(eventRequest.playerUUID());
        GameState state = eventRequest.state();
        Event event = eventRequest.event();
        player.getObserver().ifPresent(o -> o.notifyEvent(state, event));
    }


    private Player getOrCreatePlayer(String playerUUID) {
        return playerMap.computeIfAbsent(playerUUID, ignored -> new AutoPlayer());
    }
}
