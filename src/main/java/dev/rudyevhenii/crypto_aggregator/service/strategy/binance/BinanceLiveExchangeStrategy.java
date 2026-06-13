package dev.rudyevhenii.crypto_aggregator.service.strategy.binance;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.dto.binance.BinanceTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractLiveExchangeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

@Slf4j
@Component
public class BinanceLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.BINANCE;
    private static final URI WS_BINANCE_URI = URI.create("wss://stream.binance.com:9443/ws");

    private final ObjectMapper objectMapper;

    public BinanceLiveExchangeStrategy(ObjectMapper objectMapper, CryptoProperties properties) {
        super(EXCHANGE_NAME, properties);
        this.objectMapper = objectMapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_BINANCE_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        BinanceSubscribeRequest request = BinanceSubscribeRequest.create(getExchangeProperties());
        String jsonPayload = objectMapper.writeValueAsString(request);
        return Mono.just(session.textMessage(jsonPayload));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            BinanceTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, BinanceTickerWsResponse.class);
            TradingPair tradingPair = resolveTradingPair(response.tradingPair());
            return mapLivePrice(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Binance: {}", jsonPayload);
            return null;
        }
    }

    private LivePriceDto mapLivePrice(BinanceTickerWsResponse res, TradingPair tradingPair) {
        return LivePriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(new BigDecimal(res.lastPrice()))
                .priceChangePercent24h(new BigDecimal(res.priceChangePercent24h()))
                .high24h(new BigDecimal(res.high24h()))
                .low24h(new BigDecimal(res.low24h()))
                .volume24h(new BigDecimal(res.volume24h()))
                .timestamp(Instant.ofEpochMilli(res.eventTime()))
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
