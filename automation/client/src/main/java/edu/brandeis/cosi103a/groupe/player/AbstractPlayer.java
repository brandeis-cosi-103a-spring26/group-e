package edu.brandeis.cosi103a.groupe.player;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractPlayer implements Player {

    private final String id;
    private final String name;

    /** Constructs a new AbstractPlayer with a unique UUID and a name derived from that UUID. */
    public AbstractPlayer() {
        this.id = UUID.randomUUID().toString();
        this.name = "Player-" + id.substring(0, 4);
    }

    /**
     * Gets the unique identifier for this player.
     *
     * @return the UUID-based player ID
     */
    @Override
    public String getName() {
        return id;
    }

    /**
     * Gets the human-readable name for this player.
     *
     * @return the player name in a format based on a substring of the UUID
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Gets an optional GameObserver for this player. By default, returns an empty Optional, but can
     * be overridden by subclasses to provide a specific observer if needed.
     *
     * @return an Optional containing a GameObserver if one is provided, or an empty Optional if not
     */
    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.empty();
    }

    /**
     * Makes a decision based on the current game state and available options.
     *
     * @param state the current game state
     * @param options the list of available decisions
     * @param reason the reason for the decision (optional)
     * @return the chosen decision
     */
    @Override
    public abstract Decision makeDecision(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason);
}
