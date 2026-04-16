package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** +1 Card, +2 Actions. */
public class CodeReviewEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.CODE_REVIEW;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.adjustActions(2);
        ctx.log("  +1 Card, +2 Actions");
    }
}
