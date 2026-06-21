package dev.rudyevhenii.crypto_aggregator.service.strategy.kraken;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.EventType;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenSubscribeRequest;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.dto.KrakenTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.mapper.KrakenTickerMapper;
import dev.rudyevhenii.crypto_aggregator.integration.kraken.properties.KrakenProperties;
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
public class KrakenLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;
    private static final URI WS_KRAKEN_URI = URI.create("wss://ws.kraken.com/v2");

    private final KrakenProperties properties;
    private final KrakenTickerMapper mapper;
    private final ObjectMapper objectMapper;

    public KrakenLiveExchangeStrategy(ObjectMapper objectMapper, KrakenProperties properties,
                                      KrakenTickerMapper mapper) {
        super(EXCHANGE_TYPE);
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_KRAKEN_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        return Mono.fromCallable(() -> {
            KrakenSubscribeRequest request = KrakenSubscribeRequest.create(properties.tradingPair());
            String jsonPayload = objectMapper.writeValueAsString(request);
            return session.textMessage(jsonPayload);
        }).onErrorMap(e -> new RuntimeException("Failed to serialize subscribe message", e));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            KrakenTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, KrakenTickerWsResponse.class);

            if (!EventType.KRAKEN.getEventType().equals(response.type())) {
                return null;
            }
            TradingPair tradingPair = resolveTradingPair(properties.tradingPair(), response.data()
                    .getFirst()
                    .tradingPair());

            return mapper.toLivePriceDto(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Kraken: {}", jsonPayload);
            return null;
        }
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
