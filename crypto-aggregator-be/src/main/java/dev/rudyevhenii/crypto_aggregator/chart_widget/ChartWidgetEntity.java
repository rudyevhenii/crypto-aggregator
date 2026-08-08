package dev.rudyevhenii.crypto_aggregator.chart_widget;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = ChartWidgetEntity.TABLE_NAME)
public class ChartWidgetEntity implements Persistable<UUID> {
    public static final String TABLE_NAME = "chartWidgets";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.chartInterval)
    private ChartInterval chartInterval;

    @Column(name = Fields.exchangePairId)
    private UUID exchangePairId;

    @Column(name = Fields.workspaceId)
    private UUID workspaceId;

    @Column(name = Fields.position)
    private int position;

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
