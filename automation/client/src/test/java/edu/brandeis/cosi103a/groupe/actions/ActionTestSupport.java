package edu.brandeis.cosi103a.groupe.actions;

import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.main.TurnRunner;
import edu.brandeis.cosi103a.groupe.main.actions.ActionCardEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import edu.brandeis.cosi103a.groupe.observers.Observer;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

abstract class ActionTestSupport {

    protected static class Setup {
        final List<PlayerInfo> players;
        final PlayerInfo active;
        final ShopManager shop;
        final TurnRunner runner;
        final MegaObserver observer;
        final Observer eventSink;

        Setup(int numPlayers, ImmutableSet<Card.Type> actionCards, List<ActionCardEffect> effects)
                throws Exception {
            AtomicInteger counter = new AtomicInteger(1);
            ImmutableSet<Card.Type> nonNullActions = Objects.requireNonNull(actionCards);
            MutableCardStacks stacks = new MutableCardStacks(numPlayers, nonNullActions);
            shop = new ShopManager(stacks, counter);

            players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new PlayerInfo(new BigMoneyPlayer(), counter));
            }
            active = players.get(0);

            ActionEffectRegistry registry = new ActionEffectRegistry(effects);
            observer = new MegaObserver();
            eventSink = new Observer();
            observer.addObserver(eventSink);
            runner = new TurnRunner(players, shop, registry, observer);
            runner.setActivePlayer(active);
            runner.setPhase(GameState.TurnPhase.ACTION);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getField(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(obj);
    }
}
