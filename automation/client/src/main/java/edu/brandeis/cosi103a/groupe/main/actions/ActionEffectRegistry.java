package edu.brandeis.cosi103a.groupe.main.actions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry mapping Card.Type → ActionCardEffect. Adding a new action card requires only
 * implementing ActionCardEffect and registering it here.
 */
public class ActionEffectRegistry {

    private final Map<Card.Type, ActionCardEffect> registry = new HashMap<>();

    public ActionEffectRegistry(List<ActionCardEffect> effects) {
        for (ActionCardEffect e : effects) {
            registry.put(e.handles(), e);
        }
    }

    /**
     * Dispatches execution to the registered effect for the given card type.
     *
     * @throws UnsupportedOperationException if no effect is registered for the type
     */
    public void execute(Card.Type type, ActionContext ctx) throws PlayerViolationException {
        ActionCardEffect effect = registry.get(type);
        if (effect == null) {
            throw new UnsupportedOperationException("Unsupported action card type: " + type);
        }
        effect.execute(ctx);
    }
}
