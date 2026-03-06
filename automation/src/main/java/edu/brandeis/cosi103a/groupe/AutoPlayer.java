package edu.brandeis.cosi103a.groupe;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public class AutoPlayer extends AbstractPlayer {

    /**
     * Makes a decision based on the current game state and available options.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @param reason The event that triggered this decision, if any.
     * @return The chosen decision.
     */
    @Override
    public Decision makeDecision(
        GameState state,
        ImmutableList<Decision> options,
        Optional<Event> reason
    ) {
        // If the only option is EndPhaseDecision, return it
        if (options.size() == 1 && options.get(0) instanceof EndPhaseDecision) {
            return options.get(0);
        }

        // Delegate decision based on the current phase
        // Note that CLEANUP is not listed since this method is not called during cleanup phase
        switch (state.phase()) {
            case ACTION: return makeActionDecision(state, options);
            case BUY: return makeBuyDecision(state, options);
            case DISCARD: return makeDiscardDecision(state, options);
            case GAIN: return makeGainDecision(state, options);
            case MONEY: return makeMoneyDecision(state, options);
            case REACTION: return makeReactionDecision(state, options);
            default: return options.get(0); // Fallback if phase is unrecognized
        }
    }

    /**
     * Handle decision-making for the ACTION phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeActionDecision(GameState state, ImmutableList<Decision> options) {
        return options.get(0); // No actions in Big Money strategy, so just return first option
    }

    /**
     * Handle decision-making for the BUY phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeBuyDecision(GameState state, ImmutableList<Decision> options) {
        // Define preferred cards in order of preference (most expensive first)
        Card.Type[] preferredCards = {
            Card.Type.FRAMEWORK,
            Card.Type.DOGECOIN,
            Card.Type.ETHEREUM,
            Card.Type.BITCOIN
        };
        
        // Try to find each preferred card in order
        for (Card.Type preferredType : preferredCards) {
            for (Decision option : options) {
                if (option instanceof BuyDecision buyDecision) {
                    if (buyDecision.cardType() == preferredType) {
                        return buyDecision;
                    }
                }
            }
        }
        
        // If none of the preferred cards are available, end phase
        return options.stream()
            .filter(option -> option instanceof EndPhaseDecision)
            .findFirst()
            .orElse(options.get(0));
    }

    /**
     * Handle decision-making for the DISCARD phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeDiscardDecision(GameState state, ImmutableList<Decision> options) {
        return options.get(0); // No specific discard strategy yet, so just return first option
    }

    /**
     * Handle decision-making for the GAIN phase.
     * @param state The current game state.
     * @param options The list of available decisions.
     * @return The chosen decision.
     */
    protected Decision makeGainDecision(GameState state, ImmutableList<Decision> options) {
        return options.get(0); // No specific gain strategy yet, so just return first option
    }

    /**
     * Handle decision-making for the MONEY phase.
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
