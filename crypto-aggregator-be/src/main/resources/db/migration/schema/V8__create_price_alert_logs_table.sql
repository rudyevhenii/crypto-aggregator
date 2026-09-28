CREATE TABLE IF NOT EXISTS "priceAlertLogs"
(
    "id"              UUID                        NOT NULL,
    "priceAlertId"    UUID,
    "userId"          UUID                        NOT NULL,
    "exchange"        VARCHAR(255)                NOT NULL,
    "tradingPair"     VARCHAR(255)                NOT NULL,
    "conditionType"   VARCHAR(50)                 NOT NULL,
    "triggeredPrice"  DECIMAL(18, 8)              NOT NULL,
    "message"         TEXT                        NOT NULL,
    "deliveryMethods" TEXT[]                      NOT NULL,
    "createdAt"       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_price_alert_logs PRIMARY KEY ("id"),
    CONSTRAINT fk_logs_users FOREIGN KEY ("userId") REFERENCES "users" ("id"),
    CONSTRAINT fk_logs_alerts FOREIGN KEY ("priceAlertId") REFERENCES "priceAlerts" ("id") ON DELETE SET NULL
);
