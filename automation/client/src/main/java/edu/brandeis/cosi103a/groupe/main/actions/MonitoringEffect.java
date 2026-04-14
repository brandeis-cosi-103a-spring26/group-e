package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** (Reaction card) +2 Cards. */
public class MonitoringEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.MONITORING;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.drawCard();
        ctx.log("  +2 Cards");
    }
}
