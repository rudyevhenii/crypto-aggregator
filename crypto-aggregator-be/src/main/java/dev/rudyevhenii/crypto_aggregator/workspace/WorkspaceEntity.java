package dev.rudyevhenii.crypto_aggregator.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = WorkspaceEntity.TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"id", "name"}))
public class WorkspaceEntity implements Persistable<UUID> {
    public static final String TABLE_NAME = "workspaces";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Column(name = Fields.name)
    private String name;

    @Column(name = Fields.userId)
    private UUID userId;

    @Column(name = Fields.createdAt)
    private Instant createdAt;

    @Column(name = Fields.updatedAt)
    private Instant updatedAt;

    @Transient
    private boolean newEntity;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
