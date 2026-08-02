package dev.rudyevhenii.crypto_aggregator.exchange_pair;

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

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = ExchangePairEntity.TABLE_NAME)
public class ExchangePairEntity implements Persistable<UUID> {
    public static final String TABLE_NAME = "exchangePairs";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.tradingPair)
    private TradingPair tradingPair;

    @Enumerated(EnumType.STRING)
    @Column(name = Fields.exchange)
    private Exchange exchange;

    @Transient
    private boolean newEntity;

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
