package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

/** Choose one: +2 Actions; +$2; +2 Cards. */
public class UnitTestEffect implements ActionCardEffect {

    @Override
    public Card.Type handles() {
        return Card.Type.UNIT_TEST;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        ImmutableList.Builder<Decision> effectDecisionsBuilder = ImmutableList.builder();
        effectDecisionsBuilder.add(
                new ChooseEffectDecision(ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_ACTIONS));
        effectDecisionsBuilder.add(
                new ChooseEffectDecision(ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_MONEY));
        effectDecisionsBuilder.add(
                new ChooseEffectDecision(ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_CARDS));
        ImmutableList<Decision> effectDecisions = effectDecisionsBuilder.build();
        Decision chosenDecision = ctx.makeDecision(effectDecisions);
        ChooseEffectDecision chosenEffectDecision = (ChooseEffectDecision) chosenDecision;
        switch (chosenEffectDecision.effect()) {
            case UNIT_TEST_PLUS_TWO_ACTIONS:
                // ctx.log("  +2 Actions");
                ctx.adjustActions(2);
                break;
            case UNIT_TEST_PLUS_TWO_MONEY:
                // ctx.log("  +2 Money");
                ctx.adjustMoney(2);
                break;
            case UNIT_TEST_PLUS_TWO_CARDS:
                // ctx.log("  +2 Cards");
                ctx.drawCard();
                ctx.drawCard();
                break;
        }
    }
}
