package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;

/** (Attack card) +2 Cards; Each other player gains a Bug. */
public class EvergreenTestEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.EVERGREEN_TEST;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.drawCard();
        ctx.log("  +2 Cards; each other player gains a Bug");
        for (PlayerInfo p : ctx.otherPlayers()) {
            if (ctx.isUnaffectedByAttack(p, handles())) {
                continue;
            }
            ctx.giveCardTo(p, Card.Type.BUG);
        }
    }
}
