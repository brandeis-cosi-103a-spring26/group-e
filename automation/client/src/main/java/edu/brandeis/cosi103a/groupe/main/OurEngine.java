package edu.brandeis.cosi103a.groupe.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.event.GameStartEvent;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.player.ConsolePlayer;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.main.actions.BacklogEffect;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.DailyScrumEffect;
import edu.brandeis.cosi103a.groupe.main.actions.DeploymentPipelineEffect;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.HackEffect;
import edu.brandeis.cosi103a.groupe.main.actions.IPOEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MergeConflictEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ParallelizationEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RansomwareEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import edu.brandeis.cosi103a.groupe.main.actions.SprintPlanningEffect;
import edu.brandeis.cosi103a.groupe.main.actions.TechDebtEffect;
import edu.brandeis.cosi103a.groupe.main.actions.UnitTestEffect;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OurEngine implements Engine {

    // These fields are kept so that OurEngineTest can inspect initial state via reflection.
    private final GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
    private final List<PlayerInfo> players;
    private PlayerInfo activePlayer;
    private final MutableCardStacks shop;
    private final AtomicInteger cardIdCounter = new AtomicInteger(1);

    private final GameRunner runner;
    private final MegaObserver observer = new MegaObserver();

    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    /**
     * Validates players and action cards, wires up {@link ShopManager}, {@link
     * ActionEffectRegistry}, and {@link GameRunner}.
     *
     * @param players A list of 2–4 players in turn order.
     * @param actions A list of exactly 10 unique action card types.
     * @throws IllegalArgumentException if players is null, too short, too long, or contains nulls;
     *     if actions is null, contains nulls, contains non-action types, contains duplicates, or
     *     does not contain exactly 10 entries.
     */
    public OurEngine(List<Player> players, List<Type> actions) throws IllegalArgumentException {
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Provide at most four players");
        }
        if (players.size() < MIN_PLAYERS) {
            throw new IllegalArgumentException("Provide at least two players");
        }
        if (players.contains(null)) {
            throw new IllegalArgumentException("Players list cannot contain null players");
        }
        if (actions == null) {
            throw new IllegalArgumentException("Actions list cannot be null");
        }
        for (Type action : actions) {
            if (action == null) {
                throw new IllegalArgumentException("Actions list cannot contain null types");
            }
            if (action.category() != Type.Category.ACTION) {
                throw new IllegalArgumentException(
                        "Actions list cannot contain non-action types, found: " + action);
            }
        }
        if (actions.size() != 10) {
            throw new IllegalArgumentException("Provide exactly 10 action types");
        }
        ImmutableSet<Type> actionCards = ImmutableSet.copyOf(actions);
        if (actionCards.size() != actions.size()) {
            throw new IllegalArgumentException("Actions list cannot contain duplicate types");
        }

        this.players = new ArrayList<>();
        for (Player p : players) {
            this.players.add(new PlayerInfo(p, cardIdCounter));
            p.getObserver().ifPresent(o -> observer.addObserver(o));
        }
        activePlayer = this.players.get(this.players.size() - 1);

        shop = new MutableCardStacks(this.players.size(), actionCards);

        ShopManager shopManager = new ShopManager(shop, cardIdCounter);

        for (Player p : players) {
            if (p instanceof ConsolePlayer consolePlayer) {
                consolePlayer.setDiscountSupplier(shopManager::getDiscount);
            }
        }

        // Instantiate ParallelizationEffect first (no registry yet) so all effects
        // are created exactly once, then inject the registry back in via setter.
        ParallelizationEffect parallelization = new ParallelizationEffect();
        ActionEffectRegistry registry =
                new ActionEffectRegistry(
                        List.of(
                                new CodeReviewEffect(),
                                new RefactorEffect(),
                                new EvergreenTestEffect(),
                                new IPOEffect(),
                                new MonitoringEffect(),
                                new HackEffect(),
                                new BacklogEffect(),
                                new DailyScrumEffect(),
                                new TechDebtEffect(),
                                new SprintPlanningEffect(),
                                new RansomwareEffect(),
                                new MergeConflictEffect(),
                                new UnitTestEffect(),
                                new DeploymentPipelineEffect(),
                                parallelization));
        parallelization.setRegistry(registry);
        runner = new GameRunner(this.players, shopManager, registry, observer);
    }

    /** Delegates the full game loop to {@link GameRunner}. */
    @Override
    public GameResult play() throws PlayerViolationException {
        List<String> playerNames = new ArrayList<>();
        for (PlayerInfo player : players) {
            playerNames.add(player.getDisplayName());
        }
        PlayerInfo startingPlayer = players.get(0);
        GameStartEvent startEvent =
                new GameStartEvent(ImmutableList.copyOf(playerNames), shop.toImmutableCardStacks());
        GameState state =
            new GameState(
                startingPlayer.getDisplayName(),
                startingPlayer.getImmutableHand(),
                phase,
                startingPlayer.numActions(),
                startingPlayer.numMoney(),
                startingPlayer.numBuys(),
                shop.toImmutableCardStacks());
        observer.notifyEvent(state, startEvent);
        return runner.play();
    }

    /** Returns the total number of players in this game. */
    public int getNumPlayers() {
        return players.size();
    }

    @VisibleForTesting
    public PlayerInfo getNextPlayer() throws IllegalStateException {
        if (players.stream().filter(PlayerInfo::isDisqualified).count() >= players.size() - 1) {
            throw new IllegalStateException(
                    "All but one player is disqualified, game should be over");
        }
        PlayerInfo next = players.get((players.indexOf(activePlayer) + 1) % players.size());
        while (next.isDisqualified()) {
            next = players.get((players.indexOf(next) + 1) % players.size());
        }
        return next;
    }

    @VisibleForTesting
    public boolean isGameOver() {
        long disqualified = players.stream().filter(PlayerInfo::isDisqualified).count();
        if (disqualified >= players.size() - 1) return true;
        return shop.getNumAvailable(Type.FRAMEWORK) == 0;
    }
}
