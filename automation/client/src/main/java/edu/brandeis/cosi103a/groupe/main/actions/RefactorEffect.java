package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;
import edu.brandeis.cosi103a.groupe.main.Renderer;

/** Trash a card from your hand; Gain a card costing up to 2 more than the trashed card. */
public class RefactorEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.REFACTOR;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ImmutableList<Decision> trashOptions =
                DecisionBuilder.buildTrashCardDecisions(ctx.activePlayer());
        if (trashOptions.isEmpty()) {
            return;
        }
        Decision playerChoice = ctx.makeDecision(trashOptions);

        TrashCardDecision trashCardDecision = (TrashCardDecision) playerChoice;
        ctx.log("  Trashed " + Renderer.displayName(trashCardDecision.card().type()));
        ctx.trashCard(trashCardDecision.card());

        int maxGainCost = trashCardDecision.card().cost() + 2;
        ImmutableSet<Card.Type> availableCardTypes = ctx.availableCardsForCost(maxGainCost);

        ImmutableList.Builder<Decision> gainDecisionsBuilder = ImmutableList.builder();
        if (availableCardTypes.isEmpty()) {
            gainDecisionsBuilder.add(new EndPhaseDecision(TurnPhase.GAIN));
        }
        for (Card.Type cardType : availableCardTypes) {
            gainDecisionsBuilder.add(new GainCardDecision(cardType));
        }
        ImmutableList<Decision> gainDecisions = gainDecisionsBuilder.build();

        ctx.setPhase(TurnPhase.GAIN);
        Decision gainDecisionOrEndPhase = ctx.makeDecision(gainDecisions);
        if (gainDecisionOrEndPhase instanceof EndPhaseDecision) {
            ctx.setPhase(TurnPhase.ACTION);
            return;
        }

        GainCardDecision gainCardDecision = (GainCardDecision) gainDecisionOrEndPhase;
        ctx.log("  Gained " + Renderer.displayName(gainCardDecision.cardType()));
        ctx.gainCard(gainCardDecision.cardType());
        ctx.setPhase(TurnPhase.ACTION);
    }
}
