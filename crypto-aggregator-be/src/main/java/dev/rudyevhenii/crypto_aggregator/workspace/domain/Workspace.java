package dev.rudyevhenii.crypto_aggregator.workspace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {
    private UUID id;
    private String name;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
}
