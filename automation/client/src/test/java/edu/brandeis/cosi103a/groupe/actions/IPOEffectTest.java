package edu.brandeis.cosi103a.groupe.actions;

import static org.junit.Assert.assertEquals;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MutableCardStacks;
import edu.brandeis.cosi103a.groupe.cosi21.PlayerInfo;
import edu.brandeis.cosi103a.groupe.main.OurEngine;
import edu.brandeis.cosi103a.groupe.main.ShopManager;
import edu.brandeis.cosi103a.groupe.main.actions.ActionContext;
import edu.brandeis.cosi103a.groupe.main.actions.IPOEffect;
import edu.brandeis.cosi103a.groupe.player.BigMoneyPlayer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class IPOEffectTest {

    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new BigMoneyPlayer());
        }
        return players;
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    private ShopManager createShopManager(OurEngine engine) throws Exception {
        MutableCardStacks shop = getPrivateField(engine, "shop");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        return new ShopManager(shop, cardIdCounter);
    }

    private ActionContext createActionContext(
            PlayerInfo activePlayer,
            List<PlayerInfo> players,
            ShopManager shopManager,
            GameState.TurnPhase initialPhase) {
        return new ActionContext(activePlayer, players, shopManager, null, initialPhase);
    }

    @Test
    public void testExecute_DrawsTwoCards() throws Exception {
        OurEngine engine = new OurEngine(createPlayers(2));
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> deck = getPrivateField(activePlayer, "deck");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialHandSize = activePlayer.getUnplayedCards().size();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new IPOEffect().execute(ctx);

        assertEquals(initialHandSize + 2, activePlayer.getUnplayedCards().size());
    }

    @Test
    public void testExecute_AddsOneAction() throws Exception {
        OurEngine engine = new OurEngine(createPlayers(2));
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        int initialActions = activePlayer.numActions();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new IPOEffect().execute(ctx);

        assertEquals(initialActions + 1, activePlayer.numActions());
    }

    @Test
    public void testExecute_AddsTwoMoney() throws Exception {
        OurEngine engine = new OurEngine(createPlayers(2));
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        int initialMoney = activePlayer.numMoney();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new IPOEffect().execute(ctx);

        assertEquals(initialMoney + 2, activePlayer.numMoney());
    }

    @Test
    public void testExecute_AppliesAllBenefits() throws Exception {
        OurEngine engine = new OurEngine(createPlayers(2));
        List<PlayerInfo> players = getPrivateField(engine, "players");
        PlayerInfo activePlayer = players.get(0);
        ShopManager shopManager = createShopManager(engine);

        List<Card> deck = getPrivateField(activePlayer, "deck");
        AtomicInteger cardIdCounter = getPrivateField(engine, "cardIdCounter");
        while (deck.size() < 2) {
            deck.add(new Card(Card.Type.BITCOIN, cardIdCounter.getAndIncrement()));
        }

        int initialHandSize = activePlayer.getUnplayedCards().size();
        int initialActions = activePlayer.numActions();
        int initialMoney = activePlayer.numMoney();

        ActionContext ctx =
                createActionContext(activePlayer, players, shopManager, GameState.TurnPhase.ACTION);
        new IPOEffect().execute(ctx);

        assertEquals(initialHandSize + 2, activePlayer.getUnplayedCards().size());
        assertEquals(initialActions + 1, activePlayer.numActions());
        assertEquals(initialMoney + 2, activePlayer.numMoney());
    }
}
