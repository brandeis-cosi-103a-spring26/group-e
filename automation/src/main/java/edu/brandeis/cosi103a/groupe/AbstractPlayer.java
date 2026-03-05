package edu.brandeis.cosi103a.groupe;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;

public abstract class AbstractPlayer implements Player {

    // Static counter to generate unique player names
    private static int counter = 1;

    private final String name;

    // Constructor to initialize player name
    public AbstractPlayer() {
        this.name = "Player-" + counter;
        counter++;
    }

    @Override
    public String getName() {
        return name;
    }

    // Abstract method to be implemented by subclasses for making a decision
    @Override
    public abstract Decision makeDecision(GameState state, ImmutableList<Decision> options);
}
