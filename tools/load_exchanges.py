# load_exchanges.py
"""Carga idempotente del CSV de tipos de cambio en la tabla ``exchanges``.

Lee la salida de ``conversion.py`` (columnas ``País``, ``Fecha``, ``Cambio_USD``)
y hace upsert por la clave natural (ex_country, ex_date), que tiene un índice
único en ``db/01-model.sql``.

Idempotencia: una fila sólo se escribe si no existe o si su tasa cambió. Volver a
ejecutar con el mismo CSV no modifica la tabla y reporta todas las filas como
"sin cambios".

Conexión: por defecto usa los mismos valores que la API (app/src/main/resources/
application.properties): localhost:5432, base ``exchangesdb``, usuario y clave
``utem``. Las variables estándar de libpq (PGHOST, PGPORT, PGDATABASE, PGUSER,
PGPASSWORD) las sobrescriben si están definidas, y --dsn reemplaza todo.
"""

import argparse
import csv
import os
import sys
from datetime import date
from decimal import ROUND_HALF_UP, Decimal, InvalidOperation

import psycopg

# Mismo mapeo que Country.getId() en app/.../domain/enums/Country.java.
ID_PAIS = {"Chile": 0, "Colombia": 1, "Ecuador": 2}

# Valores por defecto de la API (jdbc:postgresql:exchangesdb, utem/utem).
DEFECTOS_BD = {
    "host": "localhost",
    "port": "5432",
    "dbname": "exchangesdb",
    "user": "utem",
    "password": "utem",
}
# Variable de entorno libpq que sobrescribe cada valor por defecto.
VARIABLES_BD = {
    "host": "PGHOST",
    "port": "PGPORT",
    "dbname": "PGDATABASE",
    "user": "PGUSER",
    "password": "PGPASSWORD",
}

COLUMNA_PAIS = "País"
COLUMNA_FECHA = "Fecha"
COLUMNA_TASA = "Cambio_USD"

# Inserta o actualiza. RETURNING xmax = 0 indica inserción; si el WHERE no se
# cumple (tasa igual), no hay fila devuelta y se cuenta como sin cambios.
SQL_UPSERT = """
    INSERT INTO exchanges (ex_country, ex_date, ex_rate)
    VALUES (%(country)s, %(date)s, %(rate)s)
    ON CONFLICT (ex_country, ex_date) DO UPDATE
        SET ex_rate = EXCLUDED.ex_rate,
            updated = NOW()
      WHERE exchanges.ex_rate IS DISTINCT FROM EXCLUDED.ex_rate
    RETURNING (xmax = 0) AS inserted
"""


def configurar_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Carga idempotente de tipos de cambio en la tabla exchanges."
    )
    parser.add_argument("--input", required=True, help="Ruta del CSV generado por conversion.py")
    parser.add_argument(
        "--dsn",
        default=None,
        help="Cadena de conexión PostgreSQL. Si no se indica, se usan las variables PG* "
        "y, por defecto, los valores de la API (exchangesdb, utem/utem en localhost).",
    )
    return parser


def conectar(dsn: str | None) -> psycopg.Connection:
    """Abre la conexión. Sin --dsn, combina variables PG* con los valores de la API."""
    if dsn:
        return psycopg.connect(dsn)
    parametros = {
        clave: os.environ.get(VARIABLES_BD[clave]) or valor
        for clave, valor in DEFECTOS_BD.items()
    }
    return psycopg.connect(**parametros)


def leer_filas(ruta: str) -> dict[tuple[int, date], Decimal]:
    """Lee el CSV y devuelve {(id_pais, fecha): tasa}.

    Lanza ValueError si hay países desconocidos, fechas o tasas inválidas, o si
    el mismo país y fecha aparecen con tasas distintas.
    """
    filas: dict[tuple[int, date], Decimal] = {}
    with open(ruta, newline="", encoding="utf-8") as archivo:
        lector = csv.DictReader(archivo)
        for numero, fila in enumerate(lector, start=2):  # línea 1 es el encabezado
            pais = (fila.get(COLUMNA_PAIS) or "").strip()
            if pais not in ID_PAIS:
                raise ValueError(f"línea {numero}: país desconocido '{pais}'")
            try:
                fecha = date.fromisoformat((fila.get(COLUMNA_FECHA) or "").strip())
            except ValueError as e:
                raise ValueError(f"línea {numero}: fecha inválida") from e
            try:
                tasa = Decimal(fila.get(COLUMNA_TASA) or "").quantize(
                    Decimal("0.01"), rounding=ROUND_HALF_UP
                )
            except InvalidOperation as e:
                raise ValueError(f"línea {numero}: tasa inválida") from e

            clave = (ID_PAIS[pais], fecha)
            if clave in filas and filas[clave] != tasa:
                raise ValueError(
                    f"línea {numero}: {pais} {fecha} aparece con tasas distintas "
                    f"({filas[clave]} y {tasa})"
                )
            filas[clave] = tasa
    return filas


def main() -> None:
    args = configurar_parser().parse_args()

    try:
        filas = leer_filas(args.input)
    except (OSError, ValueError) as e:
        print(f"Error leyendo el CSV: {e}", file=sys.stderr)
        sys.exit(1)

    insertadas = actualizadas = sin_cambios = 0
    try:
        # Una sola transacción: si algo falla, no queda una carga a medias.
        with conectar(args.dsn) as conexion:
            with conexion.cursor() as cursor:
                for (id_pais, fecha), tasa in sorted(filas.items()):
                    cursor.execute(
                        SQL_UPSERT,
                        {"country": id_pais, "date": fecha, "rate": tasa},
                    )
                    resultado = cursor.fetchone()
                    if resultado is None:
                        sin_cambios += 1
                    elif resultado[0]:
                        insertadas += 1
                    else:
                        actualizadas += 1
    except psycopg.Error as e:
        print(f"Error de base de datos: {e}", file=sys.stderr)
        sys.exit(1)

    print(
        f"Filas en CSV: {len(filas)} | insertadas: {insertadas} | "
        f"actualizadas: {actualizadas} | sin cambios: {sin_cambios}"
    )


if __name__ == "__main__":
    main()
