-- Products schema

-- !Ups

CREATE TABLE IF NOT EXISTS Product (
    id          UUID        NOT NULL PRIMARY KEY,
    name        VARCHAR     NOT NULL,
    quantity    INTEGER,
    price       DECIMAL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- !Downs

DROP TABLE Product;
