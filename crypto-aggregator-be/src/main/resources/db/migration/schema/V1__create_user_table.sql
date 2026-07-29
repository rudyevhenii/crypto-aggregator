CREATE TABLE IF NOT EXISTS "users"
(
    "id"         UUID    NOT NULL,
    "email"      VARCHAR(100) NOT NULL,
    "password"   VARCHAR(100) NOT NULL,
    "firstName"  VARCHAR(50) NOT NULL,
    "lastName"   VARCHAR(50) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY ("id"),
    CONSTRAINT uc_users_email UNIQUE ("email")
);
