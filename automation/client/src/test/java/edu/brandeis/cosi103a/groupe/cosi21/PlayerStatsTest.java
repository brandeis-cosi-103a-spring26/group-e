package edu.brandeis.cosi103a.groupe.cosi21;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlayerStatsTest {

    @Test
    public void testTrackScore_AverageAndStandardDeviation() {
        PlayerStats stats = new PlayerStats();
        stats.trackScore(10);
        stats.trackScore(20);
        stats.trackScore(30);

        assertEquals(20.0, stats.getAverageScore(), 0.0001);
        double expectedStdDev = Math.sqrt(200.0 / 3.0);
        assertEquals(expectedStdDev, stats.getScoreStandardDeviation(), 0.0001);
    }

    @Test
    public void testGetScoreStandardDeviation_NoScores() {
        PlayerStats stats = new PlayerStats();
        assertEquals(0.0, stats.getScoreStandardDeviation(), 0.0001);
    }

    @Test
    public void testGetNumScores_TracksCount() {
        PlayerStats stats = new PlayerStats();
        assertEquals(0.0, stats.getNumScores(), 0.0001);

        stats.trackScore(7);
        stats.trackScore(12);

        assertEquals(2.0, stats.getNumScores(), 0.0001);
    }

    @Test
    public void testWinMargins_DefaultAndAverage() {
        PlayerStats stats = new PlayerStats();
        assertEquals(-1.0, stats.getAverageWinMargin(), 0.0001);

        stats.trackWinMargin(5);
        stats.trackWinMargin(1);
        assertEquals(3.0, stats.getAverageWinMargin(), 0.0001);
    }

    @Test
    public void testPlacementCounters() {
        PlayerStats stats = new PlayerStats();
        stats.incrementWins();
        stats.incrementSecond();
        stats.incrementThird();
        stats.incrementFourth();

        assertEquals(1, stats.getWins());
        assertEquals(1, stats.getSeconds());
        assertEquals(1, stats.getThirds());
        assertEquals(1, stats.getFourths());
    }
}
