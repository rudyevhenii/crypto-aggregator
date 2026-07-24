package dev.rudyevhenii.crypto_aggregator.workspace;

import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.ChartWidgetRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.UpdateChartWidgetPositionsRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.UpdateChartWidgetRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.ChartWidgetApi;
import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.UpdateChartWidgetPositionsRequest;
import dev.rudyevhenii.crypto_aggregator.workspace.mapper.ChartWidgetMapper;
import dev.rudyevhenii.crypto_aggregator.workspace.service.ChartWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChartWidgetController implements ChartWidgetApi {

    private final ChartWidgetService chartWidgetService;
    private final ChartWidgetMapper mapper;

    @Override
    public ResponseEntity<ChartWidgetRqDto> createChartWidget(UUID workspaceId, ChartWidgetRequestRqDto request) {
        ChartWidget response = chartWidgetService.create(workspaceId, mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.map(response));
    }

    @Override
    public ResponseEntity<ChartWidgetRqDto> updateChartWidget(UUID workspaceId, UUID chartWidgetId,
                                                              UpdateChartWidgetRequestRqDto request) {
        ChartWidget response = chartWidgetService.update(workspaceId, chartWidgetId, mapper.map(request));
        return ResponseEntity.ok(mapper.map(response));
    }

    @Override
    public ResponseEntity<Void> updateChartWidgetPositions(UUID workspaceId,
                                                           List<UpdateChartWidgetPositionsRequestRqDto> request) {
        List<UpdateChartWidgetPositionsRequest> requestList = request.stream()
                .map(mapper::map)
                .toList();
        chartWidgetService.updatePositions(workspaceId, requestList);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Override
    public ResponseEntity<List<ChartWidgetRqDto>> getAllByWorkspaceId(UUID workspaceId) {
        List<ChartWidget> response = chartWidgetService.getAllByWorkspaceId(workspaceId);
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<Void> deleteChartWidget(UUID workspaceId, UUID chartWidgetId) {
        chartWidgetService.delete(workspaceId, chartWidgetId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
