package dev.rudyevhenii.crypto_aggregator.service.strategy.binance;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.EventType;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.binance.dto.BinanceSubscribeRequest;
import dev.rudyevhenii.crypto_aggregator.integration.binance.dto.BinanceTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.integration.binance.mapper.BinanceTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.binance.properties.BinanceProperties;
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
public class BinanceLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.BINANCE;
    private static final URI WS_BINANCE_URI = URI.create("wss://stream.binance.com:9443/ws");

    private final BinanceProperties properties;
    private final BinanceTickerMapper mapper;
    private final ObjectMapper objectMapper;

    public BinanceLiveExchangeStrategy(ObjectMapper objectMapper, BinanceProperties properties,
                                       BinanceTickerMapper mapper) {
        super(EXCHANGE_TYPE);
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_BINANCE_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        return Mono.fromCallable(() -> {
            BinanceSubscribeRequest request = BinanceSubscribeRequest.create(properties.tradingPair());
            String jsonPayload = objectMapper.writeValueAsString(request);
            return session.textMessage(jsonPayload);
        }).onErrorMap(e -> new RuntimeException("Failed to serialize subscribe message", e));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            BinanceTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, BinanceTickerWsResponse.class);

            if (!EventType.BINANCE.getEventType().equals(response.type())) {
                return null;
            }
            TradingPair tradingPair = resolveTradingPair(properties.tradingPair(), response.tradingPair());

            return mapper.toLivePriceDto(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Binance: {}", jsonPayload);
            return null;
        }
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
