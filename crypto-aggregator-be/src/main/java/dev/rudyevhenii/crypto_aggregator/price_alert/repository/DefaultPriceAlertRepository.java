package dev.rudyevhenii.crypto_aggregator.price_alert.repository;

import dev.rudyevhenii.crypto_aggregator.price_alert.PriceAlertEntity;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DefaultPriceAlertRepository implements PriceAlertRepository {

    private final SpringDataPriceAlertRepository repository;
    private final PriceAlertEntityMapper mapper;

    @Override
    public PriceAlert create(PriceAlert priceAlert) {
        PriceAlertEntity entity = mapper.toCreateEntity(priceAlert);
        PriceAlertEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public PriceAlert update(PriceAlert priceAlert) {
        PriceAlertEntity entity = mapper.toUpdateEntity(priceAlert);
        PriceAlertEntity updatedEntity = repository.save(entity);
        return mapper.toDomain(updatedEntity);
    }

    @Override
    public Optional<PriceAlert> findById(UUID userId, UUID id) {
        return repository.findByUserIdAndId(userId, id)
                .map(mapper::toDomain);
    }

    @Override
    public List<PriceAlert> findAll(UUID userId) {
        return repository.findAllByUserIdOrderByActiveDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PriceAlert> findAllActive() {
        return repository.findAllByActiveIsTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void activate(UUID id) {
        repository.activatePriceAlert(id);
    }

    @Override
    public void deactivate(UUID id) {
        repository.deactivatePriceAlert(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
