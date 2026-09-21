package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertInMemoryCacheManager;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertDomainMapper;
import dev.rudyevhenii.crypto_aggregator.price_alert.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertRepository repository;
    private final PriceAlertDomainMapper mapper;
    private final PriceAlertInMemoryCacheManager inMemoryCacheManager;
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public PriceAlert create(PriceAlertRequest request) {
        PriceAlert priceAlert = toDomain(request);

        PriceAlert createdPriceAlert = repository.create(priceAlert);
        inMemoryCacheManager.addAlertToCache(createdPriceAlert);
        log.info("User [{}] created a new Price Alert", userContext.getUserId());

        return createdPriceAlert;
    }

    @Override
    @Transactional
    public PriceAlert update(UUID id, PriceAlertUpdateRequest request) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        mapper.toUpdateDomain(request, priceAlert, generator);

        PriceAlert updatedPriceAlert = repository.update(priceAlert);
        inMemoryCacheManager.updateAlertFromCache(updatedPriceAlert);
        log.info("User [{}] updated Price Alert [{}]", userContext.getUserId(), id);

        return updatedPriceAlert;
    }

    @Override
    @Transactional(readOnly = true)
    public PriceAlert getPriceAlertById(UUID id) {
        return getById(userContext.getUserId(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceAlert> getAllPriceAlerts() {
        return repository.findAll(userContext.getUserId());
    }

    @Override
    @Transactional
    public List<PriceAlert> getAllActive() {
        return repository.findAllActive();
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        priceAlert.setActive(true);
        repository.activate(id);
        inMemoryCacheManager.updateAlertFromCache(priceAlert);
        log.info("User [{}] activated Price Alert [{}]", userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        deactivateForUser(userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deactivateForUser(UUID userId, UUID id) {
        PriceAlert priceAlert = getById(userId, id);
        repository.deactivate(id);
        inMemoryCacheManager.removeAlertFromCache(priceAlert);
        log.info("User [{}] deactivated Price Alert [{}]", userId, id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        inMemoryCacheManager.removeAlertFromCache(getById(userContext.getUserId(), id));
        repository.deleteById(id);
        log.info("User [{}] deleted Price Alert [{}]", userContext.getUserId(), id);
    }

    private PriceAlert getById(UUID userId, UUID id) {
        return repository.findById(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Price Alert not found with id: '%s'"
                        .formatted(id)));
    }

    private PriceAlert toDomain(PriceAlertRequest request) {
        return PriceAlert.builder()
                .id(generator.uuid())
                .userId(userContext.getUserId())
                .exchange(request.exchange())
                .tradingPair(request.tradingPair())
                .triggerPolicy(request.triggerPolicy())
                .deliveryMethods(request.deliveryMethods())
                .expiresAt(request.expiresAt())
                .conditionPayload(request.conditionPayload())
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
