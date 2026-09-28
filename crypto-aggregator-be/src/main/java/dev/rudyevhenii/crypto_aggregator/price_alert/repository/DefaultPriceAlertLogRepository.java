package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertLogEntity;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertLogEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.rudyevhenii.crypto_aggregator.price_alert.spec.PriceAlertLogSpec.*;

@Repository
@RequiredArgsConstructor
public class DefaultPriceAlertLogRepository implements PriceAlertLogRepository {

    private final SpringDataPriceAlertLogRepository repository;
    private final PriceAlertLogEntityMapper mapper;

    @Override
    public void create(PriceAlertLog priceAlertLog) {
        PriceAlertLogEntity createEntity = mapper.toCreateEntity(priceAlertLog);
        repository.save(createEntity);
    }

    @Override
    public List<PriceAlertLog> findAllAlertLogs(UUID userId, PriceAlertLogScrollRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, PriceAlertLogEntity.Fields.createdAt, PriceAlertLogEntity.Fields.id);

        Specification<PriceAlertLogEntity> spec = Specification.where(equalsToUserId(userId))
                .and(lessThanCreatedAt(request.lastCreatedAt())
                        .or(equalsToCreatedAtAndLessThanId(request.lastCreatedAt(), request.lastId())));

        return repository.findBy(spec, q -> q.sortBy(sort)
                        .limit(request.limit())
                        .all()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
