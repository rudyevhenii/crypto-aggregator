package dev.rudyevhenii.crypto_aggregator.workspace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {
    private UUID id;
    private String name;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
}
