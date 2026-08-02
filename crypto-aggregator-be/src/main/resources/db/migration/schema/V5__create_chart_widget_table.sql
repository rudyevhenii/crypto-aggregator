CREATE TABLE IF NOT EXISTS "chartWidgets"
(
    "id"               UUID                        NOT NULL,
    "chartInterval"    VARCHAR(255)                NOT NULL,
    "exchangePairId"   UUID                        NOT NULL,
    "workspaceId"      UUID                        NOT NULL,
    "position"         INTEGER                     NOT NULL,
    "createdAt"        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updatedAt"        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_chart_widgets PRIMARY KEY ("id"),
    CONSTRAINT fk_chart_widgets_exchange_pair FOREIGN KEY ("exchangePairId") REFERENCES "exchangePairs" ("id"),
    CONSTRAINT fk_chart_widgets_workspace FOREIGN KEY ("workspaceId") REFERENCES "workspaces" ("id") ON DELETE CASCADE
);
