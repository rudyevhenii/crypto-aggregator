package dev.rudyevhenii.crypto_aggregator.price_alert;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = PriceAlertEntity.TABLE_NAME)
public class PriceAlertEntity implements Persistable<UUID> {
    public static final String TABLE_NAME = "priceAlerts";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Column(name = Fields.userId)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.exchange)
    private Exchange exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.tradingPair)
    private TradingPair tradingPair;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = Fields.triggerPolicy)
    private TriggerPolicy triggerPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.deliveryMethods)
    private Set<DeliveryMethod> deliveryMethods = new HashSet<>();

    @Column(name = Fields.active)
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = Fields.conditionPayload)
    private ConditionPayload conditionPayload;

    @Column(name = Fields.expiresAt)
    private Instant expiresAt;

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
