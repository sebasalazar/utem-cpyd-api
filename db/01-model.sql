BEGIN;


DROP TABLE IF EXISTS credentials CASCADE;
CREATE TABLE credentials (
    pk      bigint       GENERATED ALWAYS AS IDENTITY, -- Identificador único autoincremental
    token   uuid         NOT NULL,
    email   varchar(255) NOT NULL,
    api_key varchar(255) NOT NULL,
    created timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de creación del registro 
    updated timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de actualización del registro
    PRIMARY KEY (pk)
);
CREATE UNIQUE INDEX ON credentials(token);
CREATE UNIQUE INDEX ON credentials(UPPER(email));


DROP TABLE IF EXISTS exchanges CASCADE;
CREATE TABLE exchanges (
    pk         bigint        GENERATED ALWAYS AS IDENTITY, -- Identificador único autoincremental
    ex_country smallint      NOT NULL, -- País (Chile, Colombia o Ecuador)
    ex_date    date          NOT NULL, -- fecha de la operación
    ex_rate    numeric(17,2) NOT NULL, -- VALOR en Moneda 
    created    timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de creación del registro 
    updated    timestamptz  NOT NULL DEFAULT NOW(),       -- Fecha de actualización del registro
    PRIMARY KEY (pk)
);
CREATE INDEX ON exchanges(ex_country);
CREATE INDEX ON exchanges(ex_date);
CREATE UNIQUE INDEX ON exchanges(ex_country, ex_date);



COMMIT;
