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

INSERT INTO product(id, name, quantity, price) VALUES ('0a776c48-1ef0-4d2e-8fab-81d074b0c8d4', 'conchas', 30, 1.00);
INSERT INTO product(id, name, quantity, price) VALUES ('52ca91a5-f3ef-409c-a39f-cc8ff234260f', 'strawberry-frosted donuts', 24, 1.50);
INSERT INTO product(id, name, quantity, price) VALUES ('e69cf7fa-7b16-4636-a859-10d4675cfcc6', 'pastel tres leches', 10, 4.50);

-- !Downs

DROP TABLE Product;
