package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;
import edu.brandeis.cosi103a.groupe.main.Renderer;

/** +1 Card, +1 Action, +1 Money; Discard one card per empty Supply pile. */
public class TechDebtEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.TECH_DEBT;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.adjustActions(1);
        ctx.adjustMoney(1);
        ctx.log("  +1 Card, +1 Action, +1 Money");

        GameState state = ctx.buildGameState();
        int emptyPiles = 0;
        for (Card.Type type : state.buyableCards().getCardTypes()) {
            if (state.buyableCards().getNumAvailable(type) == 0) {
                emptyPiles++;
            }
        }

        TurnPhase previousPhase = ctx.getPhase();
        ctx.setPhase(TurnPhase.DISCARD);
        try {
            for (int i = 0; i < emptyPiles; i++) {
                ImmutableList<Decision> options =
                        DecisionBuilder.buildForcedDiscardDecisions(ctx.activePlayer());
                if (options.isEmpty()) {
                    break;
                }

                Decision choice = ctx.makeDecision(options);
                if (!(choice instanceof DiscardCardDecision discardDecision)) {
                    throw new PlayerViolationException(
                            "Expected DiscardCardDecision for TECH_DEBT effect, got: " + choice);
                }

                ctx.discardCard(discardDecision.card());
                ctx.log("  Discarded " + Renderer.displayName(discardDecision.card().type()));
            }
        } finally {
            ctx.setPhase(previousPhase);
        }
    }
}
