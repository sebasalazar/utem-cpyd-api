# exchange_rate_generator.py
import argparse
import sys
import pandas as pd
import yfinance as yf

def configurar_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Generador de tipos de cambio diarios.")
    parser.add_argument("--start", required=True, help="Fecha inicio (YYYY-MM-DD)")
    parser.add_argument("--end", required=True, help="Fecha término (YYYY-MM-DD)")
    parser.add_argument("--output", required=True, help="Ruta del archivo CSV de salida")
    return parser

def main() -> None:
    parser = configurar_parser()
    args = parser.parse_args()

    tickers = {"Chile": "CLP=X", "Colombia": "COP=X"}
    df_list = []

    try:
        for pais, ticker in tickers.items():
            data = yf.download(
                ticker, start=args.start, end=args.end, progress=False
            )
            if not data.empty:
                df_temp = data["Close"].reset_index()
                df_temp.columns = ["Fecha", "Cambio_USD"]
                df_temp["País"] = pais
                df_list.append(df_temp)

        df_final = pd.concat(df_list, ignore_index=True)

        fechas_unicas = df_final["Fecha"].unique()
        df_ecuador = pd.DataFrame(
            {"Fecha": fechas_unicas, "Cambio_USD": 1.00, "País": "Ecuador"}
        )

        df_completo = (
            pd.concat([df_final, df_ecuador], ignore_index=True)
            .sort_values(by=["Fecha", "País"])
            .reset_index(drop=True)
        )
        df_completo = df_completo[["País", "Fecha", "Cambio_USD"]]

        df_completo.to_csv(args.output, index=False)
        print(f"Archivo generado exitosamente en: {args.output}")
        sys.exit(0) # Código de éxito

    except Exception as e:
        print(f"Error crítico durante la ejecución: {str(e)}", file=sys.stderr)
        sys.exit(1) # Código de error

if __name__ == "__main__":
    main()
