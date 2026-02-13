package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

import java.util.Scanner;

import com.google.common.collect.ImmutableList;

public abstract class AbstractPlayer implements Player {

    // Static counter to generate unique player names
    private static int counter = 1;

    // Static scanner instance for all players to use for console input
    protected static final Scanner scanner = new Scanner(System.in);

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
