package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerStats;
import edu.brandeis.cosi103a.groupe.main.RatingHelper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RatingHelperTest {

    @Test
    public void testUpdateStatsFromResult_RecordsPlacementsAndScores() throws Exception {
        PlayerResult winner =
                new PlayerResult("Winner", 12, ImmutableList.of(new Card(Card.Type.BITCOIN, 1)));
        PlayerResult loser =
                new PlayerResult("Loser", 8, ImmutableList.of(new Card(Card.Type.METHOD, 2)));
        GameResult result = new GameResult(ImmutableList.of(winner, loser));

        Map<String, PlayerStats> stats = new HashMap<>();
        stats.put("Winner", new PlayerStats());
        stats.put("Loser", new PlayerStats());

        RatingHelper.updateStatsFromResult(result, stats);

        PlayerStats winnerStats = stats.get("Winner");
        PlayerStats loserStats = stats.get("Loser");

        assertEquals(1, winnerStats.getWins());
        assertEquals(0, winnerStats.getSeconds());
        assertEquals(0, winnerStats.getThirds());
        assertEquals(0, winnerStats.getFourths());
        assertEquals(12.0, winnerStats.getAverageScore(), 0.0001);
        assertEquals(4.0, winnerStats.getAverageWinMargin(), 0.0001);

        assertEquals(0, loserStats.getWins());
        assertEquals(1, loserStats.getSeconds());
        assertEquals(0, loserStats.getThirds());
        assertEquals(0, loserStats.getFourths());
        assertEquals(8.0, loserStats.getAverageScore(), 0.0001);
        assertEquals(-1.0, loserStats.getAverageWinMargin(), 0.0001);
    }

    @Test
    public void testUpdateStatsFromResult_TiedForFirst_CompetitionRanking() {
        PlayerResult firstA =
                new PlayerResult("A", 10, ImmutableList.of(new Card(Card.Type.BITCOIN, 1)));
        PlayerResult firstB =
                new PlayerResult("B", 10, ImmutableList.of(new Card(Card.Type.METHOD, 2)));
        PlayerResult third =
                new PlayerResult("C", 7, ImmutableList.of(new Card(Card.Type.DOGECOIN, 3)));
        GameResult result = new GameResult(ImmutableList.of(firstA, firstB, third));

        Map<String, PlayerStats> stats = new HashMap<>();
        stats.put("A", new PlayerStats());
        stats.put("B", new PlayerStats());
        stats.put("C", new PlayerStats());

        RatingHelper.updateStatsFromResult(result, stats);

        assertEquals(1, stats.get("A").getWins());
        assertEquals(1, stats.get("B").getWins());
        assertEquals(0, stats.get("A").getSeconds());
        assertEquals(0, stats.get("B").getSeconds());
        assertEquals(1, stats.get("C").getThirds());
        assertEquals(0.0, stats.get("A").getAverageWinMargin(), 0.0001);
        assertEquals(0.0, stats.get("B").getAverageWinMargin(), 0.0001);
    }

    @Test
    public void testUpdateStatsFromResult_TiedForSecond_CompetitionRanking() {
        PlayerResult first =
                new PlayerResult("A", 11, ImmutableList.of(new Card(Card.Type.BITCOIN, 4)));
        PlayerResult secondA =
                new PlayerResult("B", 8, ImmutableList.of(new Card(Card.Type.METHOD, 5)));
        PlayerResult secondB =
                new PlayerResult("C", 8, ImmutableList.of(new Card(Card.Type.ETHEREUM, 6)));
        PlayerResult fourth =
                new PlayerResult("D", 4, ImmutableList.of(new Card(Card.Type.DOGECOIN, 7)));
        GameResult result = new GameResult(ImmutableList.of(first, secondA, secondB, fourth));

        Map<String, PlayerStats> stats = new HashMap<>();
        stats.put("A", new PlayerStats());
        stats.put("B", new PlayerStats());
        stats.put("C", new PlayerStats());
        stats.put("D", new PlayerStats());

        RatingHelper.updateStatsFromResult(result, stats);

        assertEquals(1, stats.get("A").getWins());
        assertEquals(1, stats.get("B").getSeconds());
        assertEquals(1, stats.get("C").getSeconds());
        assertEquals(1, stats.get("D").getFourths());
        assertEquals(3.0, stats.get("A").getAverageWinMargin(), 0.0001);
    }
}
