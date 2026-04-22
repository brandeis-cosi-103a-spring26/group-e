package edu.brandeis.cosi103a.groupe.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.main.actions.BacklogEffect;
import edu.brandeis.cosi103a.groupe.main.actions.CodeReviewEffect;
import edu.brandeis.cosi103a.groupe.main.actions.DailyScrumEffect;
import edu.brandeis.cosi103a.groupe.main.actions.EvergreenTestEffect;
import edu.brandeis.cosi103a.groupe.main.actions.HackEffect;
import edu.brandeis.cosi103a.groupe.main.actions.IPOEffect;
import edu.brandeis.cosi103a.groupe.main.actions.MonitoringEffect;
import edu.brandeis.cosi103a.groupe.main.actions.ParallelizationEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RansomwareEffect;
import edu.brandeis.cosi103a.groupe.main.actions.RefactorEffect;
import edu.brandeis.cosi103a.groupe.main.actions.SprintPlanningEffect;
import edu.brandeis.cosi103a.groupe.main.actions.TechDebtEffect;
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
    private final int turnNumber = 0;

    private final GameRunner runner;

    private static final int MAX_PLAYERS = 4;
    private static final int MIN_PLAYERS = 2;

    /**
     * Validates players, wires up {@link ShopManager}, {@link ActionEffectRegistry}, and {@link
     * GameRunner}.
     *
     * @param players A list of 2–4 players in turn order.
     * @throws IllegalArgumentException if the list is null, too short, too long, or contains nulls.
     */
    public OurEngine(List<Player> players) throws IllegalArgumentException {
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

        this.players = new ArrayList<>();
        for (Player p : players) {
            this.players.add(new PlayerInfo(p, cardIdCounter));
        }
        activePlayer = this.players.get(this.players.size() - 1);

        ImmutableSet<Type> actionCards =
                ImmutableSet.of(
                        Type.CODE_REVIEW,
                        Type.REFACTOR,
                        Type.EVERGREEN_TEST,
                        Type.IPO,
                        Type.MONITORING,
                        Type.HACK,
                        Type.PARALLELIZATION,
                        Type.TECH_DEBT,
                        Type.BACKLOG,
                        Type.DAILY_SCRUM,
                        Type.SPRINT_PLANNING,
                        Type.RANSOMWARE);
        shop = new MutableCardStacks(this.players.size(), actionCards);

        ShopManager shopManager = new ShopManager(shop, cardIdCounter);

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
                                parallelization));
        parallelization.setRegistry(registry);

        runner = new GameRunner(this.players, shopManager, registry);
    }

    /** Delegates the full game loop to {@link GameRunner}. */
    @Override
    public GameResult play() throws PlayerViolationException {
        return runner.play();
    }

    /**
     * Returns an unmodifiable view of the last 10 turn logs from the running game. The list is live
     * — entries are appended as events occur.
     */
    public List<TurnLog> getTurnLogs() {
        return runner.getTurnLogs();
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
