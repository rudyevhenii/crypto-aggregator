package dev.rudyevhenii.crypto_aggregator.price_alert.spec;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertLogEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class PriceAlertLogSpec {

    public static Specification<PriceAlertLogEntity> equalsToUserId(UUID userId) {
        return (root, q, cb) ->
                cb.equal(root.get(PriceAlertLogEntity.Fields.userId), userId);
    }

    public static Specification<PriceAlertLogEntity> lessThanCreatedAt(Instant lastCreatedAt) {
        return (root, q, cb) -> {
            if (lastCreatedAt == null) {
                return null;
            }
            return cb.lessThan(root.get(PriceAlertLogEntity.Fields.createdAt), lastCreatedAt);
        };
    }

    public static Specification<PriceAlertLogEntity> equalsToCreatedAtAndLessThanId(Instant lastCreatedAt, UUID lastId) {
        return (root, q, cb) -> {
            if (lastCreatedAt == null && lastId == null) {
                return null;
            }
            return equalsToCreated(lastCreatedAt)
                    .and(lessThanId(lastId))
                    .toPredicate(root, q, cb);
        };
    }

    private static Specification<PriceAlertLogEntity> equalsToCreated(Instant lastCreatedAt) {
        return (root, q, cb) ->
                cb.equal(root.get(PriceAlertLogEntity.Fields.createdAt), lastCreatedAt);
    }

    public static Specification<PriceAlertLogEntity> lessThanId(UUID lastId) {
        return (root, q, cb) ->
                cb.lessThan(root.get(PriceAlertLogEntity.Fields.id), lastId);
    }
}
