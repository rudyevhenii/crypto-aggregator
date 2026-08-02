package dev.rudyevhenii.crypto_aggregator.exchange.live.strategy;

import dev.rudyevhenii.crypto_aggregator.core.enums.EventType;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.KrakenSubscribeRequest;
import dev.rudyevhenii.crypto_aggregator.exchange.live.integration.dto.KrakenTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.exchange.live.mapper.LiveKrakenMapper;
import dev.rudyevhenii.crypto_aggregator.exchange.live.model.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.exchange.properties.KrakenProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.exchanges.kraken.enable", havingValue = "true")
public class KrakenLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_TYPE = Exchange.KRAKEN;
    private static final URI WS_KRAKEN_URI = URI.create("wss://ws.kraken.com/v2");

    private final KrakenProperties properties;
    private final LiveKrakenMapper mapper;
    private final ObjectMapper objectMapper;

    public KrakenLiveExchangeStrategy(ObjectMapper objectMapper, KrakenProperties properties,
                                      LiveKrakenMapper mapper) {
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

            String rawTradingPair = response.data().getFirst().tradingPair();
            TradingPair tradingPair = TradingPair.valueOf(rawTradingPair.replace("/", "_"));

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
