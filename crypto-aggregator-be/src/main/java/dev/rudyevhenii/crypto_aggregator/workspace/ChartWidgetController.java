package dev.rudyevhenii.crypto_aggregator.workspace;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.UpdateChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.ChartWidgetApi;
import dev.rudyevhenii.crypto_aggregator.core.util.SecurityUtils;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.ChartWidgetMapper;
import dev.rudyevhenii.crypto_aggregator.workspace.service.ChartWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChartWidgetController implements ChartWidgetApi {

    private final ChartWidgetService chartWidgetService;
    private final ChartWidgetMapper mapper;

    @Override
    public ResponseEntity<ChartWidgetRqDto> createChartWidget(UUID workspaceId, ChartWidgetRequestRqDto request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        ChartWidget response = chartWidgetService.create(userId, workspaceId, mapper.toDto(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(response));
    }

    @Override
    public ResponseEntity<ChartWidgetRqDto> updateChartWidget(UUID workspaceId, UUID chartWidgetId,
                                                              UpdateChartWidgetRequestRqDto request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        ChartWidget response = chartWidgetService.update(userId, workspaceId, chartWidgetId, mapper.toDto(request));
        return ResponseEntity.ok(mapper.toResponse(response));
    }
}
