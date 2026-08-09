package dev.rudyevhenii.crypto_aggregator.workspace.dto;

import lombok.Builder;

@Builder
public record WorkspaceRequest(
        String name
) {
}
