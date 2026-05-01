package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

public interface ActionCardEffect {
    Card.Type handles();

    void execute(ActionContext ctx) throws PlayerViolationException;
}
