package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.LivePriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.dto.coinbase.CoinbaseTickerWsResponse;
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
import java.math.RoundingMode;
import java.net.URI;

@Slf4j
@Component
public class CoinbaseLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final URI WS_COINBASE_URI = URI.create("wss://ws-feed.exchange.coinbase.com");
    private static final int PERCENT_TO_MULTIPLIER_SHIFT = 2;

    private final ObjectMapper objectMapper;

    public CoinbaseLiveExchangeStrategy(CryptoProperties properties, ObjectMapper objectMapper) {
        super(EXCHANGE_NAME, properties);
        this.objectMapper = objectMapper;
    }

    @Override
    protected URI getWebSocketUri() {
        return WS_COINBASE_URI;
    }

    @Override
    protected Mono<WebSocketMessage> createSubscribeMessage(WebSocketSession session) {
        CoinbaseSubscribeRequest request = CoinbaseSubscribeRequest.create(getExchangeProperties());
        String jsonPayload = objectMapper.writeValueAsString(request);
        return Mono.just(session.textMessage(jsonPayload));
    }

    @Override
    protected LivePriceDto parseMessage(String jsonPayload) {
        try {
            CoinbaseTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, CoinbaseTickerWsResponse.class);
            TradingPair tradingPair = resolveTradingPair(response.tradingPair());
            return mapLivePrice(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Coinbase: {}", jsonPayload);
            return null;
        }
    }

    private LivePriceDto mapLivePrice(CoinbaseTickerWsResponse res, TradingPair tradingPair) {
        BigDecimal lastPrice = new BigDecimal(res.lastPrice());
        BigDecimal open24h = new BigDecimal(res.open24h());
        BigDecimal percentChange = calculatePercentChange(lastPrice, open24h);

        return LivePriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(lastPrice)
                .priceChangePercent24h(percentChange)
                .high24h(new BigDecimal(res.high24h()))
                .low24h(new BigDecimal(res.low24h()))
                .volume24h(new BigDecimal(res.volume24h()))
                .timestamp(res.timestamp())
                .build();
    }

    private BigDecimal calculatePercentChange(BigDecimal lastPrice, BigDecimal open24h) {
        if (open24h == null || open24h.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return lastPrice.subtract(open24h)
                .divide(open24h, 6, RoundingMode.HALF_EVEN)
                .movePointRight(PERCENT_TO_MULTIPLIER_SHIFT)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
