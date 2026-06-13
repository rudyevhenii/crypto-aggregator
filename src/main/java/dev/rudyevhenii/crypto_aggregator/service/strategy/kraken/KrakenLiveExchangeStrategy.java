package dev.rudyevhenii.crypto_aggregator.service.strategy.kraken;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.dto.kraken.KrakenTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
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

    private static final Exchange EXCHANGE_NAME = Exchange.KRAKEN;
    private static final URI WS_KRAKEN_URI = URI.create("wss://ws.kraken.com/v2");

    private final ObjectMapper objectMapper;

    public KrakenLiveExchangeStrategy(CryptoProperties properties, ObjectMapper objectMapper) {
        super(EXCHANGE_NAME, properties);
        this.objectMapper = objectMapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_KRAKEN_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        KrakenSubscribeRequest request = KrakenSubscribeRequest.create(getExchangeProperties());
        String jsonPayload = objectMapper.writeValueAsString(request);
        return Mono.just(session.textMessage(jsonPayload));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            KrakenTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, KrakenTickerWsResponse.class);
            TradingPair tradingPair = resolveTradingPair(response.data().getFirst().tradingPair());
            return mapLivePrice(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Kraken: {}", jsonPayload);
            return null;
        }
    }

    private LivePriceDto mapLivePrice(KrakenTickerWsResponse res, TradingPair tradingPair) {
        KrakenTickerWsResponse.KrakenTickerData tickerData = res.data().getFirst();

        return LivePriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(tickerData.lastPrice())
                .priceChangePercent24h(tickerData.priceChangePercent24h())
                .high24h(tickerData.high24h())
                .low24h(tickerData.low24h())
                .volume24h(tickerData.volume24h())
                .timestamp(tickerData.timestamp())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
