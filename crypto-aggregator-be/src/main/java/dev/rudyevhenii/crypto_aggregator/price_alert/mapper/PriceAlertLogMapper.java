package dev.rudyevhenii.crypto_aggregator.price_alert.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertLogRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertLogScrollRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.dto.PriceAlertLogScrollRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceAlertLogMapper {

    PriceAlertLogScrollRequest map(PriceAlertLogScrollRequestRqDto scrollRequestRqDto);

    PriceAlertLogScrollRequestRqDto map(PriceAlertLogScrollRequest scrollRequest);

    PriceAlertLog map(PriceAlertLogRqDto alertLogRqDto);

    PriceAlertLogRqDto map(PriceAlertLog priceAlertLog);

    default Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
