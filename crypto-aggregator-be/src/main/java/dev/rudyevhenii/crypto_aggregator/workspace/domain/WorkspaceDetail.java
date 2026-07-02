package dev.rudyevhenii.crypto_aggregator.workspace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceDetail {
    private UUID id;
    private String name;
    private List<ChartWidget> chartWidgets;
    private Instant createdAt;
    private Instant updatedAt;
}
