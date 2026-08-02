package dev.rudyevhenii.crypto_aggregator.exchange_pair;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangePairRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangeRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.ExchangePairApi;
import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.domain.ExchangePair;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.mapper.ExchangePairMapper;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.service.ExchangePairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangePairController implements ExchangePairApi {

    private final ExchangePairService exchangePairService;
    private final ExchangePairMapper mapper;

    @Override
    public ResponseEntity<List<ExchangePairRqDto>> findAllExchangePairs() {
        List<ExchangePair> response = exchangePairService.findAllExchangePairs();
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<List<ExchangePairRqDto>> searchExchangePairs(ExchangeRqDto exchangeRqDto, String tradingPairId) {
        Exchange exchange = mapper.map(exchangeRqDto);
        List<ExchangePair> response = exchangePairService.searchByPattern(exchange, tradingPairId);
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }
}
