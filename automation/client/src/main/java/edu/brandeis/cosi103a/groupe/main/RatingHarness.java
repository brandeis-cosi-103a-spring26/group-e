package edu.brandeis.cosi103a.groupe.main;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
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
        System.out.println(
                "Welcome to the player rating harness! It runs over a hundred games between 2 or more automated players and uses a formula that weighs player placements and scores to rate them based on the results.");
        Scanner scanner = new Scanner(System.in);
        List<Player> players = RatingHelper.getPlayersFromUserInput(scanner);
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
                    RatingHelper.updateStatsFromResult(result, stats);
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
                        RatingHelper.updateStatsFromResult(result, stats);
                    } catch (Exception e) {
                        System.out.println("An error occurred during the game: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        printRatings(stats);
        scanner.close();
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
            double numScores = s.getNumScores();
            System.out.print("  Wins: " + s.getWins());
            if (numScores > 0 && s.getWins() > 0) {
                double percentWin = (s.getWins() / numScores) * 100;
                System.out.println(" (" + String.format("%.1f", percentWin) + "% of games)");
            } else {
                System.out.println();
            }
            if (numScores == 0) {
                System.out.println("    Placement stats: no games recorded");
            } else {
                double percentSecond = (s.getSeconds() / numScores) * 100;
                System.out.println(
                        "    2nd place: " + String.format("%.1f", percentSecond) + "% of games");
                if (entries.size() > 2) {
                    double percentThird = (s.getThirds() / numScores) * 100;
                    System.out.println(
                            "    3rd place: " + String.format("%.1f", percentThird) + "% of games");
                }
                if (entries.size() > 3) {
                    double percentFourth = (s.getFourths() / numScores) * 100;
                    System.out.println(
                            "    Last place: " + String.format("%.1f", percentFourth) + "% of games");
                }
            }
            System.out.println(
                    "  Average score: " + String.format("%.2f", s.getAverageScore()) + " points");
            System.out.println(
                    "    Standard deviation: "
                            + String.format("%.2f", s.getScoreStandardDeviation())
                            + " points");
            if (s.getAverageWinMargin() >= 0) {
                System.out.println(
                        "  Average win margin: "
                                + String.format("%.2f", s.getAverageWinMargin())
                                + " points");
            }
            System.out.println();
            rank++;
        }
    }
}
