CREATE TABLE IF NOT EXISTS "exchangePairs"
(
    "id"           UUID NOT NULL,
    "tradingPair"  VARCHAR(255) NOT NULL,
    "exchange"     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_exchange_pairs PRIMARY KEY ("id"),
    CONSTRAINT uq_trading_pair_exchange UNIQUE ("tradingPair", "exchange")
);
