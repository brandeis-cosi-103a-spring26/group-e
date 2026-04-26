package edu.brandeis.cosi103a.groupe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.event.DiscardCardEvent;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.GameEndEvent;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.event.GameStartEvent;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.event.TrashCardEvent;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.EventNotif;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.MutableHand;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.OurEngine;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.main.TurnRunner;
import edu.brandeis.cosi103a.groupe.main.actions.ActionContext;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ParallelizationEffect;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import edu.brandeis.cosi103a.groupe.observers.Observer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ObserverEventFlowTest {

    private static class ScriptedPlayer implements Player {
        private final String name;
        private final Observer observer = new Observer();
        private final List<Decision> scriptedDecisions;
        private int idx = 0;
        private final boolean alwaysInvalid;

        ScriptedPlayer(String name, List<Decision> scriptedDecisions, boolean alwaysInvalid) {
            this.name = name;
            this.scriptedDecisions = scriptedDecisions;
            this.alwaysInvalid = alwaysInvalid;
        }

        @Override
        public Decision makeDecision(
                GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
            if (alwaysInvalid) {
                return null;
            }
            if (idx < scriptedDecisions.size()) {
                return scriptedDecisions.get(idx++);
            }
            return options.get(0);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Optional<GameObserver> getObserver() {
            return Optional.of(observer);
        }

        Observer localObserver() {
            return observer;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    private Event latestEvent(Observer observer) {
        ImmutableList<EventNotif> events = observer.getEvents();
        return events.get(events.size() - 1).getEvent();
    }

    private List<PlayerInfo> createPlayerInfos(List<Player> players, AtomicInteger counter) {
        List<PlayerInfo> infos = new ArrayList<>();
        for (Player p : players) {
            infos.add(new PlayerInfo(p, counter));
        }
        return infos;
    }

    private ActionContext createActionContext(
            PlayerInfo active,
            List<PlayerInfo> players,
            ShopManager shop,
            MegaObserver megaObserver,
            GameState.TurnPhase phase) {
        return new ActionContext(active, players, shop, null, phase, megaObserver);
    }

    @Test
    public void testOurEnginePlay_EmitsGameStartAndGameEndEvents() throws Exception {
        ScriptedPlayer invalidFirst = new ScriptedPlayer("invalid", List.of(), true);
        ScriptedPlayer passiveSecond = new ScriptedPlayer("passive", List.of(), false);

        OurEngine engine = new OurEngine(new ArrayList<>(List.of(invalidFirst, passiveSecond)));
        GameResult result = engine.play();

        assertNotNull(result);

        ImmutableList<EventNotif> events = invalidFirst.localObserver().getEvents();
        assertTrue(events.stream().anyMatch(n -> n.getEvent() instanceof GameStartEvent));
        assertTrue(events.stream().anyMatch(n -> n.getEvent() instanceof GameEndEvent));
    }

    @Test
    public void testTurnRunner_ProcessPlayCardDecision_EmitsPlayCardEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        TurnRunner runner = new TurnRunner(infos, shop, new ActionEffectRegistry(List.of()), mega);
        runner.setActivePlayer(active);
        runner.setPhase(GameState.TurnPhase.MONEY);

        MutableHand hand = getPrivateField(active, "hand");
        hand.clear();
        Card bitcoin = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(bitcoin);

        runner.processPlayCardDecision(new PlayCardDecision(bitcoin));

        assertTrue(latestEvent(sink) instanceof PlayCardEvent);
    }

    @Test
    public void testTurnRunner_Parallelization_EmitsPlayEventsForBothCards() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks =
                new MutableCardStacks(
                        2, ImmutableSet.of(Card.Type.PARALLELIZATION, Card.Type.CODE_REVIEW));
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        ParallelizationEffect parallelization = new ParallelizationEffect();
        ActionEffectRegistry registry =
                new ActionEffectRegistry(List.of(new CodeReviewEffect(), parallelization));
        parallelization.setRegistry(registry);

        TurnRunner runner = new TurnRunner(infos, shop, registry, mega);
        runner.setActivePlayer(active);
        runner.setPhase(GameState.TurnPhase.ACTION);
        active.turnReset();

        MutableHand hand = getPrivateField(active, "hand");
        hand.clear();
        Card parallelizationCard = new Card(Card.Type.PARALLELIZATION, counter.getAndIncrement());
        Card codeReviewCard = new Card(Card.Type.CODE_REVIEW, counter.getAndIncrement());
        hand.addToUnplayed(parallelizationCard);
        hand.addToUnplayed(codeReviewCard);

        runner.processPlayCardDecision(new PlayCardDecision(parallelizationCard));

        List<PlayCardEvent> playEvents =
                sink.getEvents().stream()
                        .map(EventNotif::getEvent)
                        .filter(event -> event instanceof PlayCardEvent)
                        .map(event -> (PlayCardEvent) event)
                        .toList();

        assertEquals(2, playEvents.size());
        assertEquals(Card.Type.PARALLELIZATION, playEvents.get(0).card().type());
        assertEquals(Card.Type.CODE_REVIEW, playEvents.get(1).card().type());
    }

    @Test
    public void testTurnRunner_ProcessTrashCardDecision_EmitsTrashCardEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        TurnRunner runner = new TurnRunner(infos, shop, new ActionEffectRegistry(List.of()), mega);
        runner.setActivePlayer(active);
        runner.setPhase(GameState.TurnPhase.ACTION);

        MutableHand hand = getPrivateField(active, "hand");
        hand.clear();
        Card trashTarget = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(trashTarget);

        runner.processTrashCardDecision(new TrashCardDecision(trashTarget));

        assertTrue(latestEvent(sink) instanceof TrashCardEvent);
    }

    @Test
    public void testTurnRunner_ProcessBuyDecision_EmitsGainCardEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        TurnRunner runner = new TurnRunner(infos, shop, new ActionEffectRegistry(List.of()), mega);
        runner.setActivePlayer(active);
        runner.setPhase(GameState.TurnPhase.BUY);

        active.turnReset();
        active.adjustMoney(10);

        runner.processBuyDecision(new BuyDecision(Card.Type.BITCOIN));

        assertTrue(latestEvent(sink) instanceof GainCardEvent);
    }

    @Test
    public void testTurnRunner_RunTurn_EmitsEndTurnEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        ScriptedPlayer scripted =
                new ScriptedPlayer(
                        "scripted",
                        List.of(
                                new EndPhaseDecision(GameState.TurnPhase.ACTION),
                                new EndPhaseDecision(GameState.TurnPhase.MONEY),
                                new EndPhaseDecision(GameState.TurnPhase.BUY)),
                        false);
        List<Player> players = List.of(scripted, new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        TurnRunner runner = new TurnRunner(infos, shop, new ActionEffectRegistry(List.of()), mega);

        runner.runTurn(infos.get(0), new TurnLog(1, infos.get(0).getDisplayName()));

        assertTrue(latestEvent(sink) instanceof EndTurnEvent);
    }

    @Test
    public void testActionContext_GainCardAndGiveCardTo_EmitGainCardEvents() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);
        PlayerInfo other = infos.get(1);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        ActionContext ctx =
                createActionContext(active, infos, shop, mega, GameState.TurnPhase.ACTION);

        ctx.gainCard(Card.Type.BITCOIN);
        ctx.giveCardTo(other, Card.Type.BITCOIN);

        long gainEvents =
                sink.getEvents().stream()
                        .filter(n -> n.getEvent() instanceof GainCardEvent)
                        .count();
        assertEquals(2, gainEvents);
    }

    @Test
    public void testActionContext_TrashCard_EmitsTrashCardEvent() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        ActionContext ctx =
                createActionContext(active, infos, shop, mega, GameState.TurnPhase.ACTION);

        MutableHand hand = getPrivateField(active, "hand");
        hand.clear();
        Card target = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        hand.addToUnplayed(target);

        ctx.trashCard(target);

        assertTrue(latestEvent(sink) instanceof TrashCardEvent);
    }

    @Test
    public void testActionContext_DiscardMethods_EmitDiscardCardEvents() throws Exception {
        AtomicInteger counter = new AtomicInteger(1);
        MutableCardStacks stacks = new MutableCardStacks(2, ImmutableSet.of());
        ShopManager shop = new ShopManager(stacks, counter);

        List<Player> players =
                List.of(
                        new ScriptedPlayer("p1", List.of(), false),
                        new ScriptedPlayer("p2", List.of(), false));
        List<PlayerInfo> infos = createPlayerInfos(players, counter);
        PlayerInfo active = infos.get(0);
        PlayerInfo other = infos.get(1);

        MegaObserver mega = new MegaObserver();
        Observer sink = new Observer();
        mega.addObserver(sink);

        ActionContext ctx =
                createActionContext(active, infos, shop, mega, GameState.TurnPhase.ACTION);

        MutableHand activeHand = getPrivateField(active, "hand");
        activeHand.clear();
        Card activeCard = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        activeHand.addToUnplayed(activeCard);

        MutableHand otherHand = getPrivateField(other, "hand");
        otherHand.clear();
        Card otherCard = new Card(Card.Type.BITCOIN, counter.getAndIncrement());
        otherHand.addToUnplayed(otherCard);

        ctx.discardCard(activeCard);
        ctx.discardCardForPlayer(other, otherCard);

        long discardEvents =
                sink.getEvents().stream()
                        .filter(n -> n.getEvent() instanceof DiscardCardEvent)
                        .count();
        assertEquals(2, discardEvents);
    }
}
