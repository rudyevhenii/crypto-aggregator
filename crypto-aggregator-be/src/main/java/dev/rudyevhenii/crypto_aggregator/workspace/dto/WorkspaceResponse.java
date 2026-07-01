package dev.rudyevhenii.crypto_aggregator.workspace.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        Instant createAt,
        Instant updatedAt
) {
}
