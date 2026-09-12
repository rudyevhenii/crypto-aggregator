package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertUpdateRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertRequest;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertMapper {

    PriceAlertRequest map(PriceAlertRequestRqDto requestRqDto);

    PriceAlertUpdateRequest map(PriceAlertUpdateRequestRqDto requestRqDto);

    PriceAlertRqDto map(PriceAlert priceAlert);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    default Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }
}
