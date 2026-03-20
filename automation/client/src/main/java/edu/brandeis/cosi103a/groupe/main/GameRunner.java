package edu.brandeis.cosi103a.groupe.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.cosi21.TurnLog;
import edu.brandeis.cosi103a.groupe.main.actions.ActionEffectRegistry;
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

    public GameRunner(List<PlayerInfo> players, ShopManager shop, ActionEffectRegistry registry) {
        this.players = players;
        this.shop = shop;
        this.registry = registry;
        this.activePlayer = players.get(players.size() - 1);
    }

    /**
     * Runs the game to completion and returns the final result. Also prints the result to the
     * console via {@link Renderer#printGameResult}.
     */
    public GameResult play() throws PlayerViolationException {
        while (!isGameOver()) {
            activePlayer = getNextPlayer();
            turnNumber++;
            TurnLog log = new TurnLog(turnNumber, activePlayer.getName());
            allTurnLogs.add(log);
            if (allTurnLogs.size() > 10) {
                allTurnLogs.remove(0);
            }
            TurnRunner runner = new TurnRunner(players, shop, registry);
            runner.runTurn(activePlayer, log);
            activePlayer.cleanup();
        }
        GameResult finalResult = endGame();
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
            builder.add(new PlayerResult(p.getName(), p.getScore(), p.getEndingDeck()));
        }
        return new GameResult(builder.build());
    }

    public List<TurnLog> getTurnLogs() {
        return Collections.unmodifiableList(allTurnLogs);
    }
}
