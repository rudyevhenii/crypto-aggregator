CREATE TABLE IF NOT EXISTS "users"
(
    "id"         UUID    NOT NULL,
    "email"      VARCHAR(100) NOT NULL,
    "password"   VARCHAR(100) NOT NULL,
    "first_name"  VARCHAR(50) NOT NULL,
    "last_name"   VARCHAR(50) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY ("id")
);

ALTER TABLE "users"
    ADD CONSTRAINT uc_users_email UNIQUE ("email");
