package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertDomainMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(generator.now())")
    void toUpdateDomain(PriceAlertUpdateRequest request, @MappingTarget PriceAlert priceAlert, @Context GeneratorUtils generator);

    @AfterMapping
    default void mapExpiresAt(@MappingTarget PriceAlert priceAlert, PriceAlertUpdateRequest request) {
        priceAlert.setExpiresAt(request.expiresAt());
    }
}
