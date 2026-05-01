package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.DecisionBuilder;

/** (Attack card) +3 Cards; Each other player chooses to discard 2 cards or gain a Bug. */
public class RansomwareEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.RANSOMWARE;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ctx.drawCard();
        ctx.drawCard();
        ctx.drawCard();
        // ctx.log("  +3 Cards; each other player discards 2 cards or gains a Bug");

        for (PlayerInfo player : ctx.otherPlayers()) {
            if (ctx.isUnaffectedByAttack(player, handles())) {
                continue;
            }

            ImmutableList<Decision> options =
                    ImmutableList.of(
                            new ChooseEffectDecision(
                                    ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO),
                            new ChooseEffectDecision(
                                    ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG));
            Decision choice = ctx.makeDecisionForPlayer(player, options);

            if (!(choice instanceof ChooseEffectDecision effectChoice)) {
                throw new PlayerViolationException(
                        "Expected ChooseEffectDecision for RANSOMWARE effect, got: " + choice);
            }

            if (effectChoice.effect() == ChooseEffectDecision.Effect.RANSOMWARE_DISCARD_TWO) {
                TurnPhase previousPhase = ctx.getPhase();
                ctx.setPhase(TurnPhase.DISCARD);
                try {
                    for (int i = 0; i < 2; i++) {
                        ImmutableList<Decision> discardOptions =
                                DecisionBuilder.buildForcedDiscardDecisions(player);
                        if (discardOptions.isEmpty()) {
                            break;
                        }

                        Decision discardChoice = ctx.makeDecisionForPlayer(player, discardOptions);
                        if (!(discardChoice instanceof DiscardCardDecision discardDecision)) {
                            throw new PlayerViolationException(
                                    "Expected DiscardCardDecision for RANSOMWARE discard branch, got: "
                                            + discardChoice);
                        }

                        ctx.discardCardForPlayer(player, discardDecision.card());
                        // ctx.log(
                        //         "  "
                        //                 + player.getDisplayName()
                        //                 + " discarded "
                        //                 + Renderer.displayName(discardDecision.card().type()));
                    }
                } finally {
                    ctx.setPhase(previousPhase);
                }
                continue;
            }

            if (effectChoice.effect() == ChooseEffectDecision.Effect.RANSOMWARE_GAIN_BUG) {
                ctx.giveCardTo(player, Card.Type.BUG);
                // ctx.log("  " + player.getDisplayName() + " gained a Bug");
                continue;
            }

            throw new PlayerViolationException(
                    "Unexpected ChooseEffectDecision effect for RANSOMWARE: "
                            + effectChoice.effect());
        }
    }
}
