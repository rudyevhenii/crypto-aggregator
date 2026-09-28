package dev.rudyevhenii.crypto_aggregator.chart_widget.mapper;

import dev.rudyevhenii.crypto_aggregator.api.dto.chartWidget.ChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.chartWidget.ChartWidgetRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.chartWidget.UpdateChartWidgetPositionsRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.chartWidget.UpdateChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.chart_widget.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.ChartWidgetRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.chart_widget.dto.UpdateChartWidgetRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChartWidgetMapper {

    ChartWidgetRequest map(ChartWidgetRequestRqDto requestRqDto);

    ChartWidgetRqDto map(ChartWidget chartWidget);

    UpdateChartWidgetRequest map(UpdateChartWidgetRequestRqDto requestRqDto);

    UpdateChartWidgetPositionsRequest map(UpdateChartWidgetPositionsRequestRqDto requestRqDto);

    default OffsetDateTime toOffsetDateTime(Instant endTimeCursor) {
        return endTimeCursor.atOffset(ZoneOffset.UTC);
    }
}
