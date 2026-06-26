CREATE TABLE users
(
    id         UUID    NOT NULL,
    email      VARCHAR(100),
    password   VARCHAR(100),
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);