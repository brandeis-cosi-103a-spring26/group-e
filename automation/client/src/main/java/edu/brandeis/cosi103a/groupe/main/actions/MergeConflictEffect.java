package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;
import edu.brandeis.cosi103a.groupe.main.Renderer;

/** Trash a card from your hand. +1 Card per $1 it costs. */
public class MergeConflictEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.MERGE_CONFLICT;
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
        // ctx.log("  Trashed " + Renderer.displayName(trashCardDecision.card().type()));
        ctx.trashCard(trashCardDecision.card());

        for (int i = 0; i < trashCardDecision.card().cost(); i++) {
            ctx.drawCard();
        }
    }
}
