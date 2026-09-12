package dev.rudyevhenii.crypto_aggregator.price_alert.service;

import dev.rudyevhenii.crypto_aggregator.auth.context.UserContext;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
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
    private final UserContext userContext;
    private final GeneratorUtils generator;

    @Override
    @Transactional
    public PriceAlert create(PriceAlertRequest request) {
        PriceAlert priceAlert = toDomain(request);

        log.info("User [{}] created a new Price Alert", userContext.getUserId());
        return repository.create(priceAlert);
    }

    @Override
    @Transactional
    public PriceAlert update(UUID id, PriceAlertUpdateRequest request) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);

        mapper.toUpdateDomain(request, priceAlert, generator);
        log.info("User [{}] updated Price Alert [{}]", userContext.getUserId(), id);

        return repository.update(priceAlert);
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
        repository.update(priceAlert);
        log.info("User [{}] activated Price Alert [{}]", userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        PriceAlert priceAlert = getById(userContext.getUserId(), id);
        priceAlert.setActive(false);
        repository.update(priceAlert);
        log.info("User [{}] deactivated Price Alert [{}]", userContext.getUserId(), id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
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
                .targetPrice(request.targetPrice())
                .conditionType(request.conditionType())
                .recurring(request.recurring())
                .cooldownMinutes(request.cooldownMinutes())
                .deliveryMethods(request.deliveryMethods())
                .expiresAt(request.expiresAt())
                .createdAt(generator.now())
                .updatedAt(generator.now())
                .build();
    }
}
