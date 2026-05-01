package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** +1 Card, +1 Action, +1 Buy. */
public class SprintPlanningEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.SPRINT_PLANNING;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.adjustActions(1);
        ctx.adjustBuys(1);
        // ctx.log("  +1 Card, +1 Action, +1 Buy");
    }
}
