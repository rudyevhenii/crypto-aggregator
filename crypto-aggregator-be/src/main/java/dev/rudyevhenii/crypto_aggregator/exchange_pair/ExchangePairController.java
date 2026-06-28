package dev.rudyevhenii.crypto_aggregator.exchange_pair;

import dev.rudyevhenii.crypto_aggregator.api.dto.ExchangePairRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.ExchangePairApi;
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
        List<ExchangePair> response = exchangePairService.findAllTradingPairs();
        return ResponseEntity.ok(response.stream()
                .map(mapper::toDto)
                .toList());
    }

    @Override
    public ResponseEntity<List<ExchangePairRqDto>> searchExchangePairs(String pattern) {
        List<ExchangePair> response = exchangePairService.searchByPattern(pattern);
        return ResponseEntity.ok(response.stream()
                .map(mapper::toDto)
                .toList());
    }
}
