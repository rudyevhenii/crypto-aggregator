package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.LiveExchangeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@Slf4j
@Component
public class CoinbaseLiveExchangeStrategy implements LiveExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final URI WS_COINBASE_URI = URI.create("wss://ws-feed.exchange.coinbase.com");

    private final Sinks.Many<CryptoPriceDto> priceSink;
    private final ObjectMapper objectMapper;
    private final WebSocketClient webSocketClient;
    private final CryptoProperties properties;

    public CoinbaseLiveExchangeStrategy(CryptoProperties properties, ObjectMapper objectMapper) {
        this.webSocketClient = new ReactorNettyWebSocketClient();
        this.priceSink = Sinks.many().multicast().directBestEffort();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connectToExchange() {
        webSocketClient.execute(WS_COINBASE_URI, session -> {
                    CoinbaseSubscribeRequest request = CoinbaseSubscribeRequest.create(getExchangeProperties());
                    String jsonPayload = objectMapper.writeValueAsString(request);

                    return session.send(Mono.just(session.textMessage(jsonPayload)))
                            .thenMany(session.receive())
                            .map(WebSocketMessage::getPayloadAsText)
                            .mapNotNull(this::parseMessage)
                            .doOnNext(priceSink::tryEmitNext)
                            .doOnError(err -> log.error("Coinbase WS Error: {}", err.getMessage()))
                            .then();
                })
                .subscribe(
                        null,
                        error -> log.error("Coinbase Connection Failed!", error),
                        () -> log.warn("Coinbase WS Connection Closed. Need reconnect logic!")
                );
    }

    @Override
    public Flux<CryptoPriceDto> streamPrice(TradingPair tradingPair) {
        return priceSink.asFlux()
                .filter(cryptoPriceDto -> cryptoPriceDto.tradingPair().equals(tradingPair));
    }

    private CryptoPriceDto parseMessage(String jsonPayload) {
        try {
            CoinbaseTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, CoinbaseTickerWsResponse.class);
            TradingPair tradingPair = resolveTradingPair(response.tradingPair());
            return toCryptoPriceBuilder(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Coinbase: {}", jsonPayload);
            return null;
        }
    }

    private CryptoPriceDto toCryptoPriceBuilder(CoinbaseTickerWsResponse res, TradingPair tradingPair) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(new BigDecimal(res.price()))
                .timestamp(res.timestamp())
                .build();
    }

    private TradingPair resolveTradingPair(String tradingPair) {
        Map<TradingPair, String> tradingPairMap = getExchangeProperties().tradingPair();

        return tradingPairMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tradingPair))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private CryptoProperties.ExchangeProperties getExchangeProperties() {
        return getCryptoProperties().exchanges().get(getExchangeType());
    }

    @Override
    public CryptoProperties getCryptoProperties() {
        return properties;
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
