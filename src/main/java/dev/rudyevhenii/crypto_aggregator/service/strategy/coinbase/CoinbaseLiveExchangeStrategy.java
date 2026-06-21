package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.EventType;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto.CoinbaseSubscribeRequest;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.dto.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.mapper.CoinbaseTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.coinbase.properties.CoinbaseProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractLiveExchangeStrategy;
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
    private final CoinbaseTickerMapper mapper;
    private final ObjectMapper objectMapper;

    public CoinbaseLiveExchangeStrategy(ObjectMapper objectMapper, CoinbaseProperties properties,
                                        CoinbaseTickerMapper mapper) {
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
