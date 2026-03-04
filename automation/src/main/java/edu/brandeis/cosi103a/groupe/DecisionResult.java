package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.decisions.Decision;

/**
 * Immutable carrier pairing a {@link Decision} with its associated UI intent.
 *
 * <p>Returned by {@link Harness#getDecisionFromConsole} so that auto-play intent
 * is part of the return value rather than communicated through a separate static
 * flag, eliminating hidden side-channel coupling between {@code Harness} and its
 * callers.
 */
public final class DecisionResult {

    private final Decision decision;
    private final boolean autoPlayRequested;

    /**
     * Constructs a {@code DecisionResult}.
     *
     * @param decision         the decision chosen by the user; must not be null
     * @param autoPlayRequested {@code true} when the user typed {@code "A"} /
     *                         {@code "auto"} to request automatic treasure play
     */
    public DecisionResult(Decision decision, boolean autoPlayRequested) {
        if (decision == null) {
            throw new IllegalArgumentException("decision must not be null");
        }
        this.decision = decision;
        this.autoPlayRequested = autoPlayRequested;
    }

    /**
     * Returns the chosen {@link Decision}.
     *
     * @return the decision
     */
    public Decision getDecision() {
        return decision;
    }

    /**
     * Returns {@code true} when the user requested auto-play of all treasures
     * (i.e. typed {@code "A"} or {@code "auto"} during the Money phase).
     *
     * @return whether auto-play was requested
     */
    public boolean isAutoPlayRequested() {
        return autoPlayRequested;
    }
}
