package edu.brandeis.cosi103a.groupe.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.api.DecisionRequest;
import edu.brandeis.cosi103a.groupe.api.LogEventRequest;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class PlayerControllerMvcTest {

    private Player player;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        player = mock(Player.class);
        ConcurrentMap<String, Player> playerMap = new ConcurrentHashMap<>();
        playerMap.put("uuid-1", player);
        playerMap.put("uuid-2", player);
        playerMap.put("uuid-3", player);
        @SuppressWarnings("unchecked")
        ObjectProvider<Player> playerProvider = mock(ObjectProvider.class);
        PlayerController controller = new PlayerController(playerMap, playerProvider);

        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new AppConfig().guavaModule());

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(jsonConverter)
                        .build();
    }

    private GameState createState(GameState.TurnPhase phase) {
        Hand hand =
                new Hand(
                        ImmutableList.of(new Card(Card.Type.BITCOIN, 1)),
                        ImmutableList.of(new Card(Card.Type.METHOD, 2)));
        CardStacks shop = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 10));
        return new GameState("Player-1", hand, phase, 1, 0, 1, shop);
    }

    @Test
    public void choose_PostReturnsOkAndSerializedDecision() throws Exception {
        GameState state = createState(GameState.TurnPhase.ACTION);
        EndPhaseDecision expected = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(expected);
        DecisionRequest request = new DecisionRequest(state, options, Optional.empty(), "uuid-1");

        when(player.makeDecision(state, options, Optional.empty())).thenReturn(expected);

        MvcResult result =
                mockMvc.perform(
                                post("/decide")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("ACTION"));
        verify(player).makeDecision(state, options, Optional.empty());
    }

    @Test
    public void logEvent_PostNotifiesObserverAndReturnsOk() throws Exception {
        GameState state = createState(GameState.TurnPhase.BUY);
        Event event = new PlayCardEvent(new Card(Card.Type.BITCOIN, 3), "Player-1");
        GameObserver observer = mock(GameObserver.class);
        when(player.getObserver()).thenReturn(Optional.of(observer));

        LogEventRequest request = new LogEventRequest(state, event, "uuid-2");

        mockMvc.perform(
                        post("/log-event")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(observer).notifyEvent(state, event);
    }

    @Test
    public void logEvent_PostWithoutObserverStillReturnsOk() throws Exception {
        GameState state = createState(GameState.TurnPhase.BUY);
        Event event = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 4), "Player-2");
        when(player.getObserver()).thenReturn(Optional.empty());

        LogEventRequest request = new LogEventRequest(state, event, "uuid-3");

        mockMvc.perform(
                        post("/log-event")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(player).getObserver();
        verifyNoMoreInteractions(player);
    }

    @Test
    public void choose_InvalidJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/decide")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{bad-json"))
                .andExpect(status().isBadRequest());
    }
}
