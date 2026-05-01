package edu.brandeis.cosi103a.groupe.main.actions;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.GameState;
import java.util.List;

/**
 * You may play an Action card from your hand twice.
 *
 * <p>When played, the player is prompted with {@link PlayCardDecision}s for every other unplayed
 * action card in their hand, plus an {@link EndPhaseDecision} to skip parallelizing. If an action
 * card is chosen, it is executed twice via the {@link ActionEffectRegistry}. Executing the chosen
 * card does not consume additional actions beyond the one action used to play Parallelization
 * itself.
 *
 * <p>The {@link ActionEffectRegistry} must be injected after construction via {@link
 * #setRegistry(ActionEffectRegistry)} before this effect is ever executed.
 */
public class ParallelizationEffect implements ActionCardEffect {

    private ActionEffectRegistry registry;

    /**
     * Injects the {@link ActionEffectRegistry} after construction. Called by {@code OurEngine}
     * immediately after the registry is built to break the circular dependency.
     *
     * @param registry the fully-constructed registry to use for re-dispatching
     */
    public void setRegistry(ActionEffectRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Card.Type handles() {
        return Card.Type.PARALLELIZATION;
    }

    @Override
    public void execute(ActionContext ctx) throws PlayerViolationException {
        if (registry == null) {
            throw new IllegalStateException(
                    "ActionEffectRegistry not injected into ParallelizationEffect."
                            + " Call setRegistry() before executing.");
        }

        List<Card> otherActions = ctx.activePlayer().getActionsInHand();
        if (otherActions.isEmpty()) {
            // ctx.log("  No action cards to parallelize");
            return;
        }

        ImmutableList.Builder<Decision> opts = ImmutableList.builder();
        for (Card c : otherActions) {
            opts.add(new PlayCardDecision(c));
        }
        opts.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        Decision choice = ctx.makeDecision(opts.build());
        if (choice instanceof EndPhaseDecision) {
            // ctx.log("  Parallelization skipped");
            return;
        }

        Card chosen = ((PlayCardDecision) choice).card();

        // Move chosen card to the played zone so nested effects that inspect the hand
        // (e.g. another Parallelization) do not see it as unplayed.
        ctx.activePlayer().playCard(chosen);
        PlayCardEvent playEvent = new PlayCardEvent(chosen, ctx.activePlayer().getDisplayName());
        // ctx.log("  Parallelizing " + Renderer.displayName(chosen.type()));
        ctx.notifyObserver(playEvent);
        registry.execute(chosen.type(), ctx); // first execution
        ctx.notifyObserver(playEvent);
        registry.execute(chosen.type(), ctx); // second execution
    }
}
