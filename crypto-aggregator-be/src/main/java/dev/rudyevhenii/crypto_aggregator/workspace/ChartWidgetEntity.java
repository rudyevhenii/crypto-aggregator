package dev.rudyevhenii.crypto_aggregator.workspace;

import dev.rudyevhenii.crypto_aggregator.core.enums.ChartInterval;
import dev.rudyevhenii.crypto_aggregator.exchange_pair.ExchangePairEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    public static final String TABLE_NAME = "chart_widgets";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.chartInterval)
    private ChartInterval chartInterval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_pair_id")
    private ExchangePairEntity exchangePair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private WorkspaceEntity workspace;

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
