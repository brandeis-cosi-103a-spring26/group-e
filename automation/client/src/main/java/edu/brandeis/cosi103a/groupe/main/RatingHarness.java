package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerStats;
import edu.brandeis.cosi103a.groupe.player.AbstractPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RatingHarness {

    public static void main(String[] args) {
        System.out.println("This is the player rating harness, it's still a work in progress!");
        Scanner scanner = new Scanner(System.in);
        List<Player> players = RatingInput.getPlayersFromUserInput(scanner);
        Map<String, PlayerStats> stats = new HashMap<>();
        for (Player p : players) {
            AbstractPlayer ap = (AbstractPlayer) p;
            stats.put(ap.getDisplayName(), new PlayerStats());
        }
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
                    updateStatsFromResult(result, stats);
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
                        updateStatsFromResult(result, stats);
                    } catch (Exception e) {
                        System.out.println("An error occurred during the game: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        // after loop: print ratings
    }

    private static void updateStatsFromResult(GameResult result, Map<String, PlayerStats> stats) {
        List<PlayerResult> results = result.playerResults();
        for (int i = 0; i < results.size(); i++) {
            PlayerResult pResult = results.get(i);
            String pName = pResult.playerName();
            PlayerStats pStats = stats.get(pName);
            if (pStats == null) {
                continue;
            }
            pStats.trackScore(pResult.score());
            if (i == 0) {
                pStats.incrementWins();
                int secondPlaceScore = results.get(1).score();
                pStats.trackWinMargin(pResult.score() - secondPlaceScore);
            } else if (i == 1) {
                pStats.incrementSecond();
            } else if (i == 2) {
                pStats.incrementThird();
            } else {
                pStats.incrementFourth();
            }
        }
    }
}
