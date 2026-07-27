CREATE TABLE IF NOT EXISTS "workspaces"
(
    "id"         UUID                        NOT NULL,
    "name"       VARCHAR(255)                NOT NULL,
    "user_id"     UUID                        NOT NULL,
    "created_at"  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    "updated_at"  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_workspaces PRIMARY KEY ("id")
);

ALTER TABLE "workspaces"
    ADD CONSTRAINT uc_workspaces_user_id_name UNIQUE ("user_id", "name");

ALTER TABLE "workspaces"
    ADD CONSTRAINT fk_workspaces_user FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE;
