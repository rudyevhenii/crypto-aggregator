package dev.rudyevhenii.crypto_aggregator.service.strategy;

import dev.rudyevhenii.crypto_aggregator.dto.ExchangeHealthDto;
import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.ConnectionStatus;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
public abstract class AbstractLiveExchangeStrategy implements LiveExchangeStrategy {

    private final Exchange exchange;

    private final WebSocketClient webSocketClient;

    private final Sinks.Many<LivePriceDto> priceSink;
    private final Sinks.Many<ExchangeHealthDto> healthSink;

    protected AbstractLiveExchangeStrategy(Exchange exchange) {
        this.exchange = exchange;
        this.webSocketClient = new ReactorNettyWebSocketClient();
        this.priceSink = Sinks.many().multicast().directBestEffort();
        this.healthSink = Sinks.many().replay().latest();
    }

    protected abstract URI getWebSocketUri();

    protected abstract Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession webSocketSession);

    protected abstract LivePriceDto parseMessage(String payload);

    @Override
    public Flux<LivePriceDto> streamPrice(TradingPair tradingPair) {
        return priceSink.asFlux()
                .filter(cryptoPriceDto -> cryptoPriceDto.tradingPair().equals(tradingPair));
    }

    @Override
    public Flux<ExchangeHealthDto> streamHealth() {
        return healthSink.asFlux();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        emitHealth(ConnectionStatus.RECONNECTING);

        executeWebSocketSession()
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofMinutes(1))
                        .jitter(0.5)
                        .doBeforeRetry(retrySignal -> {
                            log.warn("[{}] Reconnecting... Attempt #{}. Cause: {}",
                                    exchange, retrySignal.totalRetries() + 1, retrySignal.failure().getMessage());
                            emitHealth(ConnectionStatus.RECONNECTING);
                        }))
                .repeatWhenEmpty(repeatSignal -> {
                    log.info("[{}] Connection closed normally. Repeating...", exchange);
                    return repeatSignal.delayElements(Duration.ofSeconds(1));
                })
                .subscribe();
    }

    private Mono<Void> executeWebSocketSession() {
        return webSocketClient.execute(getWebSocketUri(), session -> {
                    emitHealth(ConnectionStatus.CONNECTED);

                    return session.send(createSubscribeMessage(session))
                            .thenMany(session.receive())
                            .map(WebSocketMessage::getPayloadAsText)
                            .mapNotNull(this::parseMessage)
                            .doOnNext(priceSink::tryEmitNext)
                            .then();
                })
                .doOnError(err -> {
                    log.error("Kraken Connection Error", err);
                    emitHealth(ConnectionStatus.ERROR);
                })
                .doFinally(signalType -> {
                    log.warn("Kraken WebSocket Closed. Signal: {}", signalType);
                    emitHealth(ConnectionStatus.DISCONNECTED);
                });
    }

    private void emitHealth(ConnectionStatus connectionStatus) {
        ExchangeHealthDto exchangeHealthDto = ExchangeHealthDto.builder()
                .exchange(exchange)
                .connectionStatus(connectionStatus)
                .timestamp(Instant.now())
                .build();

        healthSink.tryEmitNext(exchangeHealthDto);
        log.info("[{}] Health Status: {}", exchange, connectionStatus);
    }

    protected TradingPair resolveTradingPair(Map<TradingPair, String> tradingPairMap, String tradingPair) {
        return tradingPairMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tradingPair))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
