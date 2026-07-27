CREATE TABLE IF NOT EXISTS "exchange_pairs"
(
    "id"           UUID NOT NULL,
    "trading_pair"  VARCHAR(255) NOT NULL,
    "exchange"     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_exchange_pairs PRIMARY KEY ("id")
);

ALTER TABLE "exchange_pairs"
    ADD CONSTRAINT uq_trading_pair_exchange UNIQUE ("trading_pair", "exchange");
