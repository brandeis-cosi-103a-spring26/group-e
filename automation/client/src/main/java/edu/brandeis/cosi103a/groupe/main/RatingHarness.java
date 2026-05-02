package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RatingHarness {

    public static void main(String[] args) {
        System.out.println("This is the player rating harness, it's still a work in progress!");
        Scanner scanner = new Scanner(System.in);
        List<Player> players = RatingInput.getPlayersFromUserInput(scanner);
        ImmutableSet<Type> actionCards =
                ImmutableSet.of(
                        Type.CODE_REVIEW,
                        Type.REFACTOR,
                        Type.EVERGREEN_TEST,
                        Type.IPO,
                        Type.MONITORING,
                        Type.HACK,
                        Type.PARALLELIZATION,
                        Type.TECH_DEBT,
                        Type.BACKLOG,
                        Type.DAILY_SCRUM,
                        Type.SPRINT_PLANNING,
                        Type.RANSOMWARE,
                        Type.MERGE_CONFLICT,
                        Type.UNIT_TEST,
                        Type.DEPLOYMENT_PIPELINE);
        if (players.size() <= 4) {
            for (int i = 0; i < 15; i++) {
                List<Type> randomActions = new ArrayList<>(actionCards);
                Collections.shuffle(randomActions);
                randomActions = randomActions.subList(0, 10);
                OurEngine engine = new OurEngine(players, randomActions);
                try {
                    GameResult result = engine.play();
                    // update stats based on result
                } catch (Exception e) {
                    System.out.println("An error occurred during the game: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else { // 5 players
            for (int rotation = 0; rotation < 5; rotation++) {
                for (int i = 0; i < 5; i++) { // keep this at 5
                    Player sittingOut = players.get(i);
                    List<Player> playersThisGame = new ArrayList<>();
                    for (Player p : players) {
                        if (p != sittingOut) {
                            playersThisGame.add(p);
                        }
                    }
                    List<Type> randomActions = new ArrayList<>(actionCards);
                    Collections.shuffle(randomActions);
                    randomActions = randomActions.subList(0, 10);
                    OurEngine engine = new OurEngine(playersThisGame, randomActions);
                    try {
                        GameResult result = engine.play();
                        // update stats based on result
                    } catch (Exception e) {
                        System.out.println("An error occurred during the game: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        // in loop: get 10 random action cards, create engine with players and cards, run game,
        // update ratings based on result
        // after loop: print ratings
    }
}
