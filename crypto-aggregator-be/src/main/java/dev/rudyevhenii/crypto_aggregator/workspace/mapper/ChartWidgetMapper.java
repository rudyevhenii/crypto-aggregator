package dev.rudyevhenii.crypto_aggregator.workspace.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRqDto;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChartWidgetMapper {

    ChartWidgetRequest toDomain(ChartWidgetRequestRqDto requestRqDto);

    ChartWidgetRqDto toResponse(ChartWidget chartWidget);

    default OffsetDateTime toOffsetDateTime(Instant endTimeCursor) {
        return endTimeCursor.atOffset(ZoneOffset.UTC);
    }
}
