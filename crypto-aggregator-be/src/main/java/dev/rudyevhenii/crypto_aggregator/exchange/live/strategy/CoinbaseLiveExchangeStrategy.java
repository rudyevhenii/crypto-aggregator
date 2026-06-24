package dev.rudyevhenii.crypto_aggregator.exchange.live.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.EventType;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.CoinbaseSubscribeRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.live.mapper.LiveCoinbaseMapper;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.CoinbaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Slf4j
@Component
public class CoinbaseLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.COINBASE;
    private static final URI WS_COINBASE_URI = URI.create("wss://ws-feed.exchange.coinbase.com");

    private final CoinbaseProperties properties;
    private final LiveCoinbaseMapper mapper;
    private final ObjectMapper objectMapper;

    public CoinbaseLiveExchangeStrategy(ObjectMapper objectMapper, CoinbaseProperties properties,
                                        LiveCoinbaseMapper mapper) {
        super(EXCHANGE_TYPE);
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_COINBASE_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        return Mono.fromCallable(() -> {
            CoinbaseSubscribeRequest request = CoinbaseSubscribeRequest.create(properties.tradingPair());
            String jsonPayload = objectMapper.writeValueAsString(request);
            return session.textMessage(jsonPayload);
        }).onErrorMap(e -> new RuntimeException("Failed to serialize subscribe message", e));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            CoinbaseTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, CoinbaseTickerWsResponse.class);

            if (!EventType.COINBASE.getEventType().equals(response.type())) {
                return null;
            }
            TradingPair tradingPair = resolveTradingPair(properties.tradingPair(), response.tradingPair());

            return mapper.toLivePriceDto(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Coinbase: {}", jsonPayload);
            return null;
        }
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
