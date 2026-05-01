package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;

/** +4 Cards, +1 Buy; each other player draws a card. */
public class DailyScrumEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.DAILY_SCRUM;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        for (int i = 0; i < 4; i++) {
            ctx.drawCard();
        }
        ctx.adjustBuys(1);
        // ctx.log("  +4 Cards, +1 Buy; each other player draws a card");
        for (PlayerInfo p : ctx.otherPlayers()) {
            ctx.drawCard(p);
        }
    }
}
