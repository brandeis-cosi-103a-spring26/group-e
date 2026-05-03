package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** +2 Cards, +1 Action, +2 Money. */
public class IPOEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.IPO;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.drawCard();
        ctx.adjustActions(1);
        ctx.adjustMoney(2);
        // ctx.log("  +2 Cards, +1 Action, +2 Money");
    }
}
