package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;
import edu.brandeis.cosi103a.groupe.main.Renderer;

/** +1 Action; Discard any number of cards, then draw that many. */
public class BacklogEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.BACKLOG;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        // +1 Action
        ctx.adjustActions(1);
        ctx.log("  +1 Action");

        TurnPhase previousPhase = ctx.getPhase();
        ctx.setPhase(TurnPhase.DISCARD);
        int discardedCount = 0;

        // Loop until hand is empty or EndPhaseDecision is chosen
        try {
            while (true) {
                ImmutableList<Decision> discardOptions =
                        DecisionBuilder.buildDiscardDecisions(ctx.activePlayer());
                Decision playerChoice = ctx.makeDecision(discardOptions);

                if (playerChoice instanceof EndPhaseDecision) {
                    break;
                }

                DiscardCardDecision discardDecision = (DiscardCardDecision) playerChoice;
                ctx.log("  Discarded " + Renderer.displayName(discardDecision.card().type()));
                ctx.discardCard(discardDecision.card());
                discardedCount++;
            }
        } finally {
            ctx.setPhase(previousPhase);
        }

        // Draw cards equal to the number discarded
        if (discardedCount > 0) {
            for (int i = 0; i < discardedCount; i++) {
                ctx.drawCard();
            }
            ctx.log("  Drew " + discardedCount + " cards");
        }
    }
}
