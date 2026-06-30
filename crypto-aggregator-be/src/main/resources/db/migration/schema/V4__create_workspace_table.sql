CREATE TABLE workspaces
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    user_id    UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_workspaces PRIMARY KEY (id)
);

ALTER TABLE workspaces
    ADD CONSTRAINT uc_workspaces_name UNIQUE (name);