package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** +1 Buy, +$1. This turn, cards cost $1 less (but not less than $0). */
public class DeploymentPipelineEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.DEPLOYMENT_PIPELINE;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.adjustBuys(1);
        ctx.adjustMoney(1);
        ctx.discountShop();
        ctx.log("  +1 Buy, +1 Money; cards cost $1 less this turn");
    }
}
