package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractPlayer implements Player {

    // A thread-safe counter to ensure unique player names
    private static final AtomicInteger counter = new AtomicInteger(0);
    private String name;

    // Constructor to initialize player name
    public AbstractPlayer() {
        this.name = "Player-" + counter.incrementAndGet();
    }

    @Override
    public String getName() {
        return name;
    }

    // Abstract method to be implemented by subclasses for making a decision
    @Override
    public abstract Decision makeDecision(GameState state, ImmutableList<Decision> options);
}
