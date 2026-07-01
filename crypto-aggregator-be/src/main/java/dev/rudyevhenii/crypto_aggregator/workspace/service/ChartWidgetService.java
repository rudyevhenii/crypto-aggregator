package dev.rudyevhenii.crypto_aggregator.workspace.service;

import dev.rudyevhenii.crypto_aggregator.workspace.domain.ChartWidget;
import dev.rudyevhenii.crypto_aggregator.workspace.dto.ChartWidgetRequest;

import java.util.UUID;

public interface ChartWidgetService {

    ChartWidget create(UUID workspaceId, ChartWidgetRequest request);


}
