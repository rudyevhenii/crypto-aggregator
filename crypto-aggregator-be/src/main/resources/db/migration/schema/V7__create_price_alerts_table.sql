CREATE TABLE IF NOT EXISTS "priceAlerts"
(
    "id"              UUID                        NOT NULL,
    "userId"          UUID                        NOT NULL,
    "exchange"        VARCHAR(255)                NOT NULL,
    "tradingPair"     VARCHAR(255)                NOT NULL,
    "targetPrice"     DECIMAL                     NOT NULL,
    "conditionType"   VARCHAR(255)                NOT NULL,
    "recurring"       BOOLEAN                     NOT NULL,
    "cooldownMinutes" INTEGER                     NOT NULL,
    "deliveryMethods" TEXT[]                      NOT NULL,
    "active"          BOOLEAN                     NOT NULL,
    "expiresAt"       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "createdAt"       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updatedAt"       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_price_alerts PRIMARY KEY ("id"),
    CONSTRAINT fk_price_alerts_users FOREIGN KEY ("userId") REFERENCES "users" ("id")
);