CREATE TABLE IF NOT EXISTS "workspaces"
(
    "id"         UUID                        NOT NULL,
    "name"       VARCHAR(255)                NOT NULL,
    "userId"     UUID                        NOT NULL,
    "createdAt"  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updatedAt"  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_workspaces PRIMARY KEY ("id")
);

ALTER TABLE "workspaces"
    ADD CONSTRAINT uc_workspaces_user_id_name UNIQUE ("userId", "name");

ALTER TABLE "workspaces"
    ADD CONSTRAINT FK_WORKSPACES_ON_USER FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE;