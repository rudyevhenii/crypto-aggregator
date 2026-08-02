CREATE TABLE IF NOT EXISTS "workspaces"
(
    "id"         UUID                        NOT NULL,
    "name"       VARCHAR(255)                NOT NULL,
    "userId"     UUID                        NOT NULL,
    "createdAt"  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updatedAt"  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_workspaces PRIMARY KEY ("id"),
    CONSTRAINT uc_workspaces_user_id_name UNIQUE ("userId", "name"),
    CONSTRAINT fk_workspaces_user FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
