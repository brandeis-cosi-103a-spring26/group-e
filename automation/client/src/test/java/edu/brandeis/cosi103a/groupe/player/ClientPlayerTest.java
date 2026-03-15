package edu.brandeis.cosi103a.groupe.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupe.cosi21.MakeDecisionBody;
import java.io.IOException;
import java.lang.reflect.Field;
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
        ClientPlayer player = new ClientPlayer();

        Decision expected = new EndPhaseDecision(GameState.TurnPhase.BUY);
        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{\"request\":\"payload\"}";
        mapper.parsedDecision = expected;

        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.responseBody = "{\"decision\":\"placeholder\"}";

        setField(player, "BASE_URL", "http://localhost:9000/api");
        setField(player, "om", mapper);
        setField(player, "httpClient", httpClient);

        Decision actual = player.makeDecision(null, ImmutableList.of());

        assertSame(expected, actual);
        assertEquals(
                "http://localhost:9000/api/makeDecision", httpClient.lastRequest.uri().toString());
        assertTrue(mapper.lastWrittenValue instanceof MakeDecisionBody);
        assertEquals(httpClient.responseBody, mapper.lastReadBody);
    }

    @Test
    public void makeDecision_WrapsIOExceptionAsRuntimeException() throws Exception {
        ClientPlayer player = new ClientPlayer();

        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{}";
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.throwIOException = true;

        setField(player, "BASE_URL", "http://localhost:9000/api");
        setField(player, "om", mapper);
        setField(player, "httpClient", httpClient);

        try {
            player.makeDecision(null, ImmutableList.of());
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void makeDecision_WrapsInterruptedExceptionAsRuntimeException() throws Exception {
        ClientPlayer player = new ClientPlayer();

        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{}";
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.throwInterrupted = true;

        setField(player, "BASE_URL", "http://localhost:9000/api");
        setField(player, "om", mapper);
        setField(player, "httpClient", httpClient);

        try {
            player.makeDecision(null, ImmutableList.of());
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void makeDecision_RequestBodyContainsStateAndOptionsFields() throws Exception {
        ClientPlayer player = new ClientPlayer();

        FakeObjectMapper mapper = new FakeObjectMapper();
        mapper.serializedBody = "{\"state\":null,\"options\":[]}";
        mapper.parsedDecision = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.responseBody = "{\"decision\":\"ignored\"}";

        setField(player, "BASE_URL", "http://localhost:9000/api");
        setField(player, "om", mapper);
        setField(player, "httpClient", httpClient);

        player.makeDecision(null, ImmutableList.of());

        String requestBody = readRequestBody(httpClient.lastRequest);
        assertTrue(requestBody.contains("\"state\""));
        assertTrue(requestBody.contains("\"options\""));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static String readRequestBody(HttpRequest request) throws Exception {
        Optional<HttpRequest.BodyPublisher> publisherOptional = request.bodyPublisher();
        assertTrue("Request should contain a body", publisherOptional.isPresent());

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

        assertTrue("Timed out waiting for request body", done.await(2, TimeUnit.SECONDS));
        return bodyBuilder.toString();
    }

    private static class FakeObjectMapper extends ObjectMapper {
        String serializedBody;
        Object lastWrittenValue;

        String lastReadBody;
        Decision parsedDecision;

        @Override
        public String writeValueAsString(Object value) {
            lastWrittenValue = value;
            return serializedBody;
        }

        @Override
        public <T> T readValue(String content, Class<T> valueType) {
            lastReadBody = content;
            return valueType.cast(parsedDecision);
        }
    }

    private static class FakeHttpClient extends HttpClient {
        HttpRequest lastRequest;
        String responseBody = "{}";
        boolean throwIOException;
        boolean throwInterrupted;

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
                    (HttpResponse<T>) new FakeHttpResponse(responseBody, request);
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

        FakeHttpResponse(String body, HttpRequest request) {
            this.body = body;
            this.request = request;
        }

        @Override
        public int statusCode() {
            return 200;
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
