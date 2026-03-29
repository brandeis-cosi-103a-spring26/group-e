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
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController {
    private final Map<String, Player> playerMap;

    public PlayerController(Map<String, Player> playerMap) {
        this.playerMap = playerMap;
    }

    @PostMapping("/decide")
    public DecisionResponse choose(@RequestBody DecisionRequest decisionRequest) {
        if (!playerMap.containsKey(decisionRequest.playerUUID())) {
            playerMap.put(decisionRequest.playerUUID(), new AutoPlayer()); // This player will have a different UUID but that's fine since it maps to the correct UUID
        }
        GameState state = decisionRequest.state();
        ImmutableList<Decision> options = decisionRequest.options();
        Optional<Event> reason = decisionRequest.reason();
        Player player = playerMap.get(decisionRequest.playerUUID());
        return new DecisionResponse(player.makeDecision(state, options, reason));
    }

    @PostMapping("/log-event")
    public void logEvent(@RequestBody LogEventRequest eventRequest) {
        if (!playerMap.containsKey(eventRequest.playerUUID())) {
            playerMap.put(eventRequest.playerUUID(), new AutoPlayer()); // This player will have a different UUID but that's fine since it maps to the correct UUID
        }
        GameState state = eventRequest.state();
        Event event = eventRequest.event();
        Player player = playerMap.get(eventRequest.playerUUID());
        player.getObserver().ifPresent(o -> o.notifyEvent(state, event));
    }
}
