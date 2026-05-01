package edu.brandeis.cosi103a.groupe.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupe.api.DecisionRequest;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.Test;

public class ClientPlayerTest {

    @Test
    public void makeDecision_SendsRequestAndReturnsParsedDecision() throws Exception {
        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{\"request\":\"payload\"}";

        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision expected = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(fallback, expected);

        FakeHttpClient httpClient = new FakeHttpClient();
        ObjectMapper payloadMapper = new ObjectMapper();
        httpClient.responseBody =
                "{\"decision\":" + payloadMapper.writeValueAsString(expected) + "}";

        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, mapper);

        Decision actual = player.makeDecision(null, options, Optional.empty());

        assertSame(expected, actual);
        assertEquals("http://localhost:9000/api/decide", httpClient.lastRequest.uri().toString());
        assertTrue(mapper.lastWrittenValue instanceof DecisionRequest);
    }

    @Test
    public void makeDecision_ReturnsFirstOptionWhenIOExceptionOccurs() {
        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{}";

        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.throwIOException = true;
        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, mapper);

        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision alternative = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(fallback, alternative);

        Decision actual = player.makeDecision(null, options, Optional.empty());
        assertSame(fallback, actual);
    }

    @Test
    public void makeDecision_ReturnsFirstOptionAndPreservesInterrupt() {
        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{}";

        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.throwInterrupted = true;
        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, mapper);

        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision alternative = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(fallback, alternative);

        try {
            Decision actual = player.makeDecision(null, options, Optional.empty());
            assertSame(fallback, actual);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void makeDecision_RequestBodyContainsStateAndOptionsFields() throws Exception {
        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{\"state\":null,\"options\":[]}";

        FakeHttpClient httpClient = new FakeHttpClient();
        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ObjectMapper payloadMapper = new ObjectMapper();
        httpClient.responseBody =
                "{\"decision\":" + payloadMapper.writeValueAsString(fallback) + "}";

        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, mapper);

        player.makeDecision(null, ImmutableList.of(fallback), Optional.empty());

        String requestBody = readRequestBody(httpClient.lastRequest);
        assertTrue(requestBody.contains("\"state\""));
        assertTrue(requestBody.contains("\"options\""));
    }

    @Test
    public void makeDecision_WithRealObjectMapper_MapsDecisionJsonToMatchingOption()
            throws Exception {
        ObjectMapper realMapper = new ObjectMapper().findAndRegisterModules();
        FakeHttpClient httpClient = new FakeHttpClient();

        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision expected = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(fallback, expected);

        httpClient.responseBody = "{\"decision\":" + realMapper.writeValueAsString(expected) + "}";
        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, realMapper);

        Decision actual = player.makeDecision(null, options, Optional.<Event>empty());

        assertSame(expected, actual);
    }

    @Test
    public void makeDecision_ReturnsFirstOptionWhenResponseDecisionDoesNotMatchAnyOption()
            throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        FakeHttpClient httpClient = new FakeHttpClient();

        Decision fallback = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision alternative = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(fallback, alternative);

        Decision unmatched = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        httpClient.responseBody = "{\"decision\":" + realMapper.writeValueAsString(unmatched) + "}";

        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, realMapper);

        Decision actual = player.makeDecision(null, options, Optional.empty());

        assertSame(fallback, actual);
    }

    @Test
    public void makeDecision_WithReason_SerializesExpectedRequestJsonShape() throws Exception {
        ObjectMapper realMapper = new ObjectMapper().findAndRegisterModules();
        FakeHttpClient httpClient = new FakeHttpClient();

        Decision expected = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(expected);
        Event reason = new PlayCardEvent(new Card(Card.Type.BITCOIN, 2), "Player-2");
        httpClient.responseBody = "{\"decision\":" + realMapper.writeValueAsString(expected) + "}";

        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, realMapper);

        player.makeDecision(createState(GameState.TurnPhase.BUY), options, Optional.of(reason));

        String requestBody = readRequestBody(httpClient.lastRequest);
        com.fasterxml.jackson.databind.JsonNode body = realMapper.readTree(requestBody);

        assertEquals("http://localhost:9000/api/decide", httpClient.lastRequest.uri().toString());
        assertEquals(player.getName(), body.get("player_uuid").asText());
        assertTrue(body.has("state"));
        assertTrue(body.has("options"));
        assertEquals(1, body.get("options").size());
        assertTrue(body.has("reason"));
        assertEquals("play_card", body.get("reason").get("type").asText());
    }

    @Test
    public void observerNotifyEvent_SerializesExpectedRequestJsonShape() throws Exception {
        ObjectMapper realMapper = new ObjectMapper().findAndRegisterModules();
        FakeHttpClient httpClient = new FakeHttpClient();
        ClientPlayer player = new ClientPlayer("http://localhost:9000/api", httpClient, realMapper);

        GameState state = createState(GameState.TurnPhase.MONEY);
        Event event = new PlayCardEvent(new Card(Card.Type.ETHEREUM, 4), "Player-2");
        GameObserver observer = player.getObserver().orElseThrow();

        observer.notifyEvent(state, event);

        String requestBody = readRequestBody(httpClient.lastRequest);
        com.fasterxml.jackson.databind.JsonNode body = realMapper.readTree(requestBody);

        assertEquals(
                "http://localhost:9000/api/log-event", httpClient.lastRequest.uri().toString());
        assertEquals(player.getName(), body.get("player_uuid").asText());
        assertTrue(body.has("state"));
        assertTrue(body.get("state").isObject());
        assertTrue(body.get("state").size() > 0);
        assertTrue(body.has("event"));
        assertEquals("play_card", body.get("event").get("type").asText());
    }

    private static GameState createState(GameState.TurnPhase phase) {
        Hand hand =
                new Hand(
                        ImmutableList.of(new Card(Card.Type.BITCOIN, 1)),
                        ImmutableList.of(new Card(Card.Type.METHOD, 2)));
        CardStacks shop = new CardStacks(ImmutableMap.of(Card.Type.BITCOIN, 10));
        return new GameState("Player-1", hand, phase, 1, 0, 1, shop);
    }

    private static String readRequestBody(HttpRequest request) throws Exception {
        Optional<HttpRequest.BodyPublisher> publisherOptional = request.bodyPublisher();
        if (publisherOptional.isEmpty()) {
            throw new IllegalStateException("Request did not contain a body publisher");
        }

        StringBuilder bodyBuilder = new StringBuilder();
        CountDownLatch done = new CountDownLatch(1);

        publisherOptional
                .get()
                .subscribe(
                        new Flow.Subscriber<ByteBuffer>() {
                            @Override
                            public void onSubscribe(Flow.Subscription subscription) {
                                subscription.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(ByteBuffer item) {
                                byte[] bytes = new byte[item.remaining()];
                                item.get(bytes);
                                bodyBuilder.append(new String(bytes, StandardCharsets.UTF_8));
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                done.countDown();
                            }

                            @Override
                            public void onComplete() {
                                done.countDown();
                            }
                        });

        if (!done.await(2, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out while reading request body");
        }
        return bodyBuilder.toString();
    }

    private static class FakeObjectMapper extends ObjectMapper {
        String serializedBody;
        Object lastWrittenValue;

        @Override
        public String writeValueAsString(Object value) {
            lastWrittenValue = value;
            return serializedBody;
        }
    }

    private static class FakeHttpClient extends HttpClient {
        HttpRequest lastRequest;
        String responseBody = "{}";
        boolean throwIOException;
        boolean throwInterrupted;
        int statusCode = 200;

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            if (throwIOException) {
                throw new IOException("io failed");
            }
            if (throwInterrupted) {
                throw new InterruptedException("interrupted");
            }
            lastRequest = request;
            @SuppressWarnings("unchecked")
            HttpResponse<T> response =
                    (HttpResponse<T>) new FakeHttpResponse(responseBody, request, statusCode);
            return response;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request, BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                BodyHandler<T> responseBodyHandler,
                PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }
    }

    private static class FakeHttpResponse implements HttpResponse<String> {
        private final String body;
        private final HttpRequest request;
        private final int statusCode;

        FakeHttpResponse(String body, HttpRequest request, int statusCode) {
            this.body = body;
            this.request = request;
            this.statusCode = statusCode;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (s1, s2) -> true);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
