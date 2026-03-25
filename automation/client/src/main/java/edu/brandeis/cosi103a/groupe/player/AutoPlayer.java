package edu.brandeis.cosi103a.groupe.player;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.Optional;

public class AutoPlayer extends AbstractPlayer {

    private static final Card.Type[] PLUS_ACTION_CARDS = {
        Card.Type.IPO,
        Card.Type.TECH_DEBT,
        Card.Type.CODE_REVIEW,
        Card.Type.SPRINT_PLANNING,
        Card.Type.UNIT_TEST,
        Card.Type.BACKLOG
    };

    private static final Card.Type[] PREFERRED_CARDS = {
        Card.Type.FRAMEWORK, Card.Type.IPO, Card.Type.DOGECOIN
    };

    /**
     * Gets the human-readable name for this player.
     * 
     * @return the player name in a format based on a substring of the UUID
     */
    @Override
    public String getDisplayName() {
        return "AutoPlayer-" + id.substring(0, 4);
    }

    /**
     * Makes a decision based on the current game state and available options.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @param reason The event that triggered this decision, if any.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(
            GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        // If the only option is EndPhaseDecision, return it
        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            return options.get(0);
        }

        // Delegate decision based on the current phase
        // Note that CLEANUP is not listed since this method is not called during cleanup phase
        switch (state.phase()) {
            case ACTION:
                return makeActionDecision(state, options);
            case BUY:
                return makeBuyDecision(state, options);
            case DISCARD:
                return makeDiscardDecision(state, options);
            case GAIN:
                return makeGainDecision(state, options);
            case MONEY:
                return makeMoneyDecision(state, options);
            case REACTION:
                return makeReactionDecision(state, options);
            default:
                return options.get(0); // Fallback if phase is unrecognized
        }
    }

    /**
     * Handle decision-making for the ACTION phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeActionDecision(GameState state, ImmutableList<Decision> options) {
        // Try to play a card that gives +action(s) first
        for (Card.Type plusActionType : PLUS_ACTION_CARDS) {
            for (Decision option : options) {
                if (option instanceof PlayCardDecision playCardDecision) {
                    if (playCardDecision.card().type() == plusActionType) {
                        return playCardDecision;
                    }
                }
            }
        }

        // If no +action cards, try to play other action cards in decreasing cost order
        for (int cost = 6; cost >= 2; cost--) {
            for (Decision option : options) {
                if (option instanceof PlayCardDecision playCardDecision) {
                    if (playCardDecision.card().cost() == cost) {
                        return playCardDecision;
                    }
                }
            }
        }

        return options.get(0); // Fallback if no suitable action is found
    }

    /**
     * Handle decision-making for the BUY phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeBuyDecision(GameState state, ImmutableList<Decision> options) {
        // Try to find each preferred card in order
        for (Card.Type preferredType : PREFERRED_CARDS) {
            for (Decision option : options) {
                if (option instanceof BuyDecision buyDecision) {
                    if (buyDecision.cardType() == preferredType) {
                        return buyDecision;
                    }
                }
            }
        }

        // If none of the preferred cards are available, try to gain cards in decreasing cost order
        for (int cost = 6; cost >= 2; cost--) {
            for (Decision option : options) {
                if (option instanceof BuyDecision buyDecision) {
                    // Avoid buying non-Framework victory cards
                    if (buyDecision.cardType().category() != Card.Type.Category.VICTORY
                            && buyDecision.cardType().cost() == cost) {
                        return buyDecision;
                    }
                }
            }
        }

        // Attempt to end phase before buying anything else if possible
        return options.stream()
                .filter(option -> option instanceof EndPhaseDecision)
                .findFirst()
                .orElse(options.get(0)); // Fallback if no suitable buy is found
    }

    /**
     * Handle decision-making for the DISCARD phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeDiscardDecision(GameState state, ImmutableList<Decision> options) {
        // Discard victory cards first
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision discardDecision) {
                if (discardDecision.card().category() == Card.Type.Category.VICTORY) {
                    return discardDecision;
                }
            }
        }

        // If no victory cards, discard BITCOIN next since it's the least valuable money card
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision discardDecision) {
                if (discardDecision.card().type() == Card.Type.BITCOIN) {
                    return discardDecision;
                }
            }
        }

        // Attempt to end phase before discarding anything else if possible
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision endPhaseDecision) {
                return endPhaseDecision;
            }
        }

        // If no victory cards or BITCOIN, discard other cards starting with the least expensive
        for (int cost = 2; cost <= 8; cost++) {
            for (Decision option : options) {
                if (option instanceof DiscardCardDecision discardDecision) {
                    if (discardDecision.card().cost() == cost) {
                        return discardDecision;
                    }
                }
            }
        }

        return options.get(0); // Fallback if no suitable discard is found
    }

    /**
     * Handle decision-making for the GAIN phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeGainDecision(GameState state, ImmutableList<Decision> options) {
        // Try to find each preferred card in order
        for (Card.Type preferredType : PREFERRED_CARDS) {
            for (Decision option : options) {
                if (option instanceof GainCardDecision gainCardDecision) {
                    if (gainCardDecision.cardType() == preferredType) {
                        return gainCardDecision;
                    }
                }
            }
        }

        // If none of the preferred cards are available, try to gain cards in decreasing cost order
        for (int cost = 6; cost >= 2; cost--) {
            for (Decision option : options) {
                if (option instanceof GainCardDecision gainCardDecision) {
                    // Avoid gaining non-Framework victory cards
                    if (gainCardDecision.cardType().category() != Card.Type.Category.VICTORY
                            && gainCardDecision.cardType().cost() == cost) {
                        return gainCardDecision;
                    }
                }
            }
        }

        // Attempt to end phase before gaining anything else if possible
        return options.stream()
                .filter(option -> option instanceof EndPhaseDecision)
                .findFirst()
                .orElse(options.get(0)); // Fallback if no suitable gain is found
    }

    /**
     * Handle decision-making for the MONEY phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeMoneyDecision(GameState state, ImmutableList<Decision> options) {
        // Find and return first PlayCardDecision
        return options.stream()
                .filter(option -> option instanceof PlayCardDecision)
                .findFirst()
                .orElse(options.get(0));
    }

    /**
     * Handle decision-making for the REACTION phase.
     *
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeReactionDecision(GameState state, ImmutableList<Decision> options) {
        // Find and return first PlayCardDecision (always want to react if possible)
        return options.stream()
                .filter(option -> option instanceof PlayCardDecision)
                .findFirst()
                .orElse(options.get(0));
    }
}
