package dev.rudyevhenii.crypto_aggregator.exchange_pair.spec;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity.Fields;
import org.springframework.data.jpa.domain.Specification;

public class ExchangePairSpec {

    public static Specification<ExchangePairEntity> equalToExchange(Exchange exchange) {
        return (root, query, cb) -> {
            if (exchange == null) {
                return null;
            }
            return cb.equal(root.get(Fields.exchange), exchange);
        };
    }

    public static Specification<ExchangePairEntity> hasTradingPairPattern(String tradingPair) {
        return (root, query, cb) -> {
            if (tradingPair == null || tradingPair.isBlank()) {
                return null;
            }
            return cb.like(
                    root.get(Fields.tradingPair), "%" + tradingPair.toUpperCase() + "%"
            );
        };
    }
}
