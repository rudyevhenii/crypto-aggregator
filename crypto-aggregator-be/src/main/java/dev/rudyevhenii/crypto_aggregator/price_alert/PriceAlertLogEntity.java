package dev.rudyevhenii.crypto_aggregator.price_alert;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
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

import java.math.BigDecimal;
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
@Table(name = PriceAlertLogEntity.TABLE_NAME)
public class PriceAlertLogEntity implements Persistable<UUID> {
    public static final String TABLE_NAME = "priceAlertLogs";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Column(name = Fields.priceAlertId)
    private UUID priceAlertId;

    @Column(name = Fields.userId)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.exchange)
    private Exchange exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.tradingPair)
    private TradingPair tradingPair;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.conditionType)
    private ConditionType conditionType;

    @Column(name = Fields.triggeredPrice)
    private BigDecimal triggeredPrice;

    @Column(name = Fields.message)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.deliveryMethods)
    private Set<DeliveryMethod> deliveryMethods = new HashSet<>();;

    @Column(name = Fields.createdAt)
    private Instant createdAt;

    @Transient
    private boolean newEntity;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
