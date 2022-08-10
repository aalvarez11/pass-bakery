CREATE TABLE IF NOT EXISTS product (
    id          UUID          NOT NULL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    quantity    INTEGER,
    price       DECIMAL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);
