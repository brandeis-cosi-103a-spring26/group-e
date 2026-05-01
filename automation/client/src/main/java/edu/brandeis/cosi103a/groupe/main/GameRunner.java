package edu.brandeis.cosi103a.groupe.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.event.GameEndEvent;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
import edu.brandeis.cosi103a.groupe.observers.MegaObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates the main game loop: iterates turns until the game is over, delegates each turn to a
 * {@link TurnRunner}, and compiles the final {@link GameResult}.
 */
public class GameRunner {

    private final List<PlayerInfo> players;
    private final ShopManager shop;
    private final ActionEffectRegistry registry;
    private int turnNumber = 0;
    private final List<TurnLog> allTurnLogs = new ArrayList<>();
    private PlayerInfo activePlayer;
    private final MegaObserver observer;

    public GameRunner(
            List<PlayerInfo> players,
            ShopManager shop,
            ActionEffectRegistry registry,
            MegaObserver observer) {
        this.players = players;
        this.shop = shop;
        this.registry = registry;
        this.activePlayer = players.get(players.size() - 1);
        this.observer = observer;
    }

    /**
     * Runs the game to completion and returns the final result. Also prints the result to the
     * console via {@link Renderer#printGameResult}.
     */
    public GameResult play() throws PlayerViolationException {
        while (!isGameOver()) {
            activePlayer = getNextPlayer();
            turnNumber++;
            TurnLog log = new TurnLog(turnNumber, activePlayer.getDisplayName());
            allTurnLogs.add(log);
            if (allTurnLogs.size() > 10) {
                allTurnLogs.remove(0);
            }
            TurnRunner runner = new TurnRunner(players, shop, registry, observer);
            runner.runTurn(activePlayer, log);
        }
        GameResult finalResult = endGame();
        GameEndEvent endEvent = new GameEndEvent(shop.toImmutableCardStacks(), finalResult);
        GameState state =
                new GameState(
                        activePlayer.getDisplayName(),
                        activePlayer.getImmutableHand(),
                        TurnPhase.CLEANUP,
                        activePlayer.numActions(),
                        activePlayer.numMoney(),
                        activePlayer.numBuys(),
                        shop.toImmutableCardStacks());
        observer.notifyEvent(state, endEvent);
        Renderer.printGameResult(finalResult);
        return finalResult;
    }

    @VisibleForTesting
    public boolean isGameOver() {
        long disqualifiedCount = players.stream().filter(PlayerInfo::isDisqualified).count();
        if (disqualifiedCount >= players.size() - 1) return true;
        return shop.getNumAvailable(Card.Type.FRAMEWORK) == 0;
    }

    @VisibleForTesting
    public PlayerInfo getNextPlayer() {
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
    public GameResult endGame() {
        ImmutableList.Builder<PlayerResult> builder = ImmutableList.builder();
        players.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        for (PlayerInfo p : players) {
            builder.add(new PlayerResult(p.getDisplayName(), p.getScore(), p.getEndingDeck()));
        }
        return new GameResult(builder.build());
    }

    public List<TurnLog> getTurnLogs() {
        return Collections.unmodifiableList(allTurnLogs);
    }
}
