BEGIN;


DROP TABLE IF EXISTS credentials CASCADE;
CREATE TABLE credentials (
    pk      bigint       GENERATED ALWAYS AS IDENTITY, -- Identificador único autoincremental
    token   uuid         NOT NULL DEFAULT gen_random_uuid(),
    email   varchar(255) NOT NULL,
    api_key varchar(255) NOT NULL,
    active  boolean      NOT NULL DEFAULT true,
    created timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de creación del registro 
    updated timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de actualización del registro
    PRIMARY KEY (pk)
);
CREATE UNIQUE INDEX ON credentials(token);
CREATE UNIQUE INDEX ON credentials(UPPER(email));


DROP TABLE IF EXISTS exchanges CASCADE;
CREATE TABLE exchanges (
    pk         bigint        GENERATED ALWAYS AS IDENTITY, -- Identificador único autoincremental
    ex_country smallint      NOT NULL, -- País: 0 = Chile, 1 = Colombia, 2 = Ecuador (ver Country.getId)
    ex_date    date          NOT NULL, -- fecha de la operación
    ex_rate    numeric(17,2) NOT NULL, -- VALOR en Moneda 
    created    timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de creación del registro 
    updated    timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de actualización del registro
    PRIMARY KEY (pk)
);
CREATE INDEX ON exchanges(ex_country);
CREATE INDEX ON exchanges(ex_date);
CREATE UNIQUE INDEX ON exchanges(ex_country, ex_date);


DROP TABLE IF EXISTS sales CASCADE;
CREATE TABLE sales (
    pk       uuid           NOT NULL DEFAULT uuidv7() ,
    country  smallint       NOT NULL,
    customer uuid           NOT NULL,
    receipt  uuid           NOT NULL,
    trx_date timestamp      NOT NULL,
    amount   numeric(21, 4) NOT NULL,
    created  timestamptz    NOT NULL DEFAULT now(),
    updated  timestamptz    NOT NULL DEFAULT now(),
    PRIMARY KEY (pk)
);
CREATE UNIQUE INDEX ON sales(receipt);


COMMIT;
