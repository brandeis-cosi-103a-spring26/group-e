package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;

/** (Attack card) +2 Money; Each other player discards down to 3 cards in hand. */
public class HackEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.HACK;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.adjustMoney(2);
        // ctx.log("  +2 Money; each other player discards down to 3 cards in hand");

        TurnPhase previousPhase = ctx.getPhase();
        ctx.setPhase(TurnPhase.DISCARD);
        try {
            for (PlayerInfo player : ctx.otherPlayers()) {
                if (ctx.isUnaffectedByAttack(player, handles())) {
                    continue;
                }

                while (player.getUnplayedCards().size() > 3) {
                    ImmutableList<Decision> options =
                            DecisionBuilder.buildForcedDiscardDecisions(player);
                    Decision choice = ctx.makeDecisionForPlayer(player, options);

                    if (!(choice instanceof DiscardCardDecision discardDecision)) {
                        throw new PlayerViolationException(
                                "Expected DiscardCardDecision for HACK effect, got: " + choice);
                    }

                    ctx.discardCardForPlayer(player, discardDecision.card());
                    // ctx.log(
                    //         "  "
                    //                 + player.getDisplayName()
                    //                 + " discarded "
                    //                 + Renderer.displayName(discardDecision.card().type()));
                }
            }
        } finally {
            ctx.setPhase(previousPhase);
        }
    }
}
