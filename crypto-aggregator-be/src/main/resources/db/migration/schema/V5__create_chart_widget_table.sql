CREATE TABLE chart_widgets
(
    id               UUID                        NOT NULL,
    chart_interval   VARCHAR(255)                NOT NULL,
    exchange_pair_id UUID                        NOT NULL,
    workspace_id     UUID                        NOT NULL,
    position         INTEGER                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_chart_widgets PRIMARY KEY (id)
);

ALTER TABLE chart_widgets
    ADD CONSTRAINT FK_CHART_WIDGETS_ON_EXCHANGE_PAIR FOREIGN KEY (exchange_pair_id) REFERENCES exchange_pairs (id);

ALTER TABLE chart_widgets
    ADD CONSTRAINT FK_CHART_WIDGETS_ON_WORKSPACE FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE;