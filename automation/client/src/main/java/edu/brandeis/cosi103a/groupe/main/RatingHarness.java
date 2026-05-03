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
import java.util.Comparator;
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
            for (int i = 0; i < 150; i++) {
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
            for (int rotation = 0; rotation < 50; rotation++) {
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
        printRatings(stats);
    }

    private static void printRatings(Map<String, PlayerStats> stats) {
        List<Map.Entry<String, PlayerStats>> entries = new ArrayList<>(stats.entrySet());
        entries.sort(
                Comparator.comparingDouble(
                                (Map.Entry<String, PlayerStats> e) -> computeRating(e.getValue()))
                        .reversed());

        System.out.println("\n=== Player Ratings ===");
        System.out.println();
        int rank = 1;
        for (Map.Entry<String, PlayerStats> entry : entries) {
            String name = entry.getKey();
            PlayerStats s = entry.getValue();
            double rating = computeRating(s);
            if (rank == 1) {
                System.out.print("1st: ");
            } else if (rank == 2) {
                System.out.print("2nd: ");
            } else if (rank == 3) {
                System.out.print("3rd: ");
            } else if (rank == 4) {
                System.out.print("4th: ");
            } else {
                System.out.print("5th: ");
            }
            System.out.println(name + " (rating points: " + String.format("%.2f", rating) + ")");
            System.out.print("  Wins: " + s.getWins());
            if (s.getWins() > 0) {
                double percentWin = (s.getWins() / 150.0) * 100;
                System.out.println(" (" + String.format("%.1f", percentWin) + "% of games)");
            } else {
                System.out.println();
            }
            double percentSecond = (s.getSeconds() / 150.0) * 100;
            System.out.println(
                    "    2nd place: " + String.format("%.1f", percentSecond) + "% of games");
            if (entries.size() > 2) {
                double percentThird = (s.getThirds() / 150.0) * 100;
                System.out.println(
                        "    3rd place: " + String.format("%.1f", percentThird) + "% of games");
            }
            if (entries.size() > 3) {
                double percentFourth = (s.getFourths() / 150.0) * 100;
                System.out.println(
                        "    Last place: " + String.format("%.1f", percentFourth) + "% of games");
            }
            System.out.println(
                    "  Average score: " + String.format("%.2f", s.getAverageScore()) + " points");
            System.out.println(
                    "    Standard deviation: "
                            + String.format("%.2f", s.getScoreStandardDeviation())
                            + " points");
            if (s.getWins() > 0) {
                System.out.println(
                        "  Average win margin: "
                                + String.format("%.2f", s.getAverageWinMargin())
                                + " points");
            }
            System.out.println();
            rank++;
        }
    }

    private static double computeRating(PlayerStats stats) {
        int placementPoints =
                (3 * stats.getWins())
                        + (2 * stats.getSeconds())
                        + stats.getThirds()
                        + (0 * stats.getFourths());
        double avgScore = stats.getAverageScore();
        double avgMargin = Math.max(0, stats.getAverageWinMargin());
        double scoreStdDev = stats.getScoreStandardDeviation();
        return placementPoints + (0.04 * avgScore) + (0.08 * avgMargin) - (0.03 * scoreStdDev);
    }

    private static void updateStatsFromResult(GameResult result, Map<String, PlayerStats> stats) {
        List<PlayerResult> results = result.playerResults();
        if (results.isEmpty()) {
            return;
        }

        List<Integer> distinctScores = new ArrayList<>();
        for (PlayerResult pResult : results) {
            int score = pResult.score();
            if (!distinctScores.contains(score)) {
                distinctScores.add(score);
            }
        }

        Map<Integer, Integer> scoreToPlacement = new HashMap<>();
        for (int i = 0; i < distinctScores.size(); i++) {
            scoreToPlacement.put(distinctScores.get(i), i + 1);
        }

        int topScore = distinctScores.get(0);
        int winningMargin = distinctScores.size() > 1 ? topScore - distinctScores.get(1) : 0;

        for (PlayerResult pResult : results) {
            String pName = pResult.playerName();
            PlayerStats pStats = stats.get(pName);
            if (pStats == null) {
                continue;
            }

            int placement = scoreToPlacement.get(pResult.score());
            pStats.trackScore(pResult.score());
            if (placement == 1) {
                pStats.incrementWins();
                pStats.trackWinMargin(winningMargin);
            } else if (placement == 2) {
                pStats.incrementSecond();
            } else if (placement == 3) {
                pStats.incrementThird();
            } else {
                pStats.incrementFourth();
            }
        }
    }
}
