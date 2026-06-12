package dev.rudyevhenii.crypto_aggregator.service.strategy.coinbase;

import dev.rudyevhenii.crypto_aggregator.dto.CryptoPriceDto;
import dev.rudyevhenii.crypto_aggregator.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.integration.dto.coinbase.CoinbaseTickerWsResponse;
import dev.rudyevhenii.crypto_aggregator.properties.CryptoProperties;
import dev.rudyevhenii.crypto_aggregator.service.strategy.AbstractLiveExchangeStrategy;
import dev.rudyevhenii.crypto_aggregator.service.strategy.kraken.KrakenSubscribeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;

@Slf4j
@Component
public class CoinbaseLiveExchangeStrategy extends AbstractLiveExchangeStrategy {

    private static final Exchange EXCHANGE_NAME = Exchange.COINBASE;
    private static final URI WS_COINBASE_URI = URI.create("wss://ws-feed.exchange.coinbase.com");

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
        KrakenSubscribeRequest request = KrakenSubscribeRequest.create(getExchangeProperties());
        String jsonPayload = objectMapper.writeValueAsString(request);
        return Mono.just(session.textMessage(jsonPayload));
    }

    @Override
    protected CryptoPriceDto parseMessage(String jsonPayload) {
        try {
            CoinbaseTickerWsResponse response = objectMapper
                    .readValue(jsonPayload, CoinbaseTickerWsResponse.class);
            TradingPair tradingPair = resolveTradingPair(response.tradingPair());
            return mapCryptoPrice(response, tradingPair);
        } catch (JacksonException e) {
            log.debug("Ignored non-ticker message from Coinbase: {}", jsonPayload);
            return null;
        }
    }

    private CryptoPriceDto mapCryptoPrice(CoinbaseTickerWsResponse res, TradingPair tradingPair) {
        return CryptoPriceDto.builder()
                .exchange(getExchangeType())
                .tradingPair(tradingPair)
                .price(new BigDecimal(res.price()))
                .timestamp(res.timestamp())
                .build();
    }

    @Override
    public Exchange getExchangeType() {
        return EXCHANGE_NAME;
    }
}
