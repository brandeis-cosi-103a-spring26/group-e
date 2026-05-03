package edu.brandeis.cosi103a.groupe.cosi21;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats {

    private int timesWon = 0;
    private int timesSecond = 0;
    private int timesThird = 0;
    private int timesFourth = 0;
    private List<Integer> scores = new ArrayList<>();
    private List<Integer> wonBy = new ArrayList<>();

    public void incrementWins() {
        timesWon++;
    }

    public int getWins() {
        return timesWon;
    }

    public void incrementSecond() {
        timesSecond++;
    }

    public int getSeconds() {
        return timesSecond;
    }

    public void incrementThird() {
        timesThird++;
    }

    public int getThirds() {
        return timesThird;
    }

    public void incrementFourth() {
        timesFourth++;
    }

    public int getFourths() {
        return timesFourth;
    }

    public void trackScore(int score) {
        scores.add(score);
    }

    public double getAverageScore() {
        return scores.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public double getScoreStandardDeviation() {
        double average = getAverageScore();
        double variance =
                scores.stream()
                        .mapToDouble(score -> Math.pow(score - average, 2))
                        .average()
                        .orElse(0);
        return Math.sqrt(variance);
    }

    public void trackWinMargin(int points) {
        wonBy.add(points);
    }

    public double getAverageWinMargin() {
        return wonBy.stream().mapToInt(Integer::intValue).average().orElse(-1);
    }
}
