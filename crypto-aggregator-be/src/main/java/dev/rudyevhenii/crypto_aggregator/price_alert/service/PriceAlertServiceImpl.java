package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertEngineService;
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
    private final PriceAlertEngineService alertEngineService;
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public PriceAlert create(PriceAlertRequest request) {
        PriceAlert priceAlert = toDomain(request);

        PriceAlert createdPriceAlert = repository.create(priceAlert);
        alertEngineService.addAlertToCache(createdPriceAlert);
        log.info("User [{}] created a new Price Alert", userContext.getUserId());

        return createdPriceAlert;
    }

    @Override
    @Transactional
    public PriceAlert update(UUID id, PriceAlertUpdateRequest request) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        mapper.toUpdateDomain(request, priceAlert, generator);

        PriceAlert updatedPriceAlert = repository.update(priceAlert);
        alertEngineService.updateAlertFromCache(updatedPriceAlert);
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
    public void activate(UUID id) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        priceAlert.setActive(true);
        PriceAlert activatedPriceAlert = repository.update(priceAlert);
        alertEngineService.updateAlertFromCache(activatedPriceAlert);
        log.info("User [{}] activated Price Alert [{}]", userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        priceAlert.setActive(false);
        PriceAlert deactivatedPriceAlert = repository.update(priceAlert);
        alertEngineService.updateAlertFromCache(deactivatedPriceAlert);
        log.info("User [{}] deactivated Price Alert [{}]", userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        alertEngineService.removeAlertFromCache(getById(userContext.getUserId(), id));
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
                .cooldownMinutes(request.cooldownMinutes())
                .deliveryMethods(request.deliveryMethods())
                .expiresAt(request.expiresAt())
                .conditionPayload(request.conditionPayload())
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
