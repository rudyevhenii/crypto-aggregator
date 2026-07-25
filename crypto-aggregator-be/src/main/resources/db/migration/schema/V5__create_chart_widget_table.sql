CREATE TABLE IF NOT EXISTS "chartWidgets"
(
    "id"               UUID                        NOT NULL,
    "chartInterval"    VARCHAR(255)                NOT NULL,
    "exchangePairId"   UUID                        NOT NULL,
    "workspaceId"      UUID                        NOT NULL,
    "position"         INTEGER                     NOT NULL,
    "createdAt"        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updatedAt"        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_chart_widgets PRIMARY KEY ("id")
);

ALTER TABLE "chartWidgets"
    ADD CONSTRAINT FK_CHART_WIDGETS_ON_EXCHANGE_PAIR FOREIGN KEY ("exchangePairId") REFERENCES "exchangePairs" ("id");

ALTER TABLE "chartWidgets"
    ADD CONSTRAINT FK_CHART_WIDGETS_ON_WORKSPACE FOREIGN KEY ("workspaceId") REFERENCES "workspaces" ("id") ON DELETE CASCADE;