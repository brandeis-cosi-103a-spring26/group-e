package edu.brandeis.cosi.atg.state;

import com.google.common.collect.ImmutableList;

/**
 * Represents the results of a completed game, including all player results
 * sorted in order of decreasing score.
 *
 * @param playerResults an ordered list of player results, sorted from highest
 *                      score to lowest
 */
public record GameResult(ImmutableList<PlayerResult> playerResults) {
    /**
     * Compact constructor that validates the playerResults list is not null.
     */
    public GameResult {
        java.util.Objects.requireNonNull(playerResults, "playerResults must not be null");
    }
}
