import pandas as pd
import yfinance as yf

# 1. Definir los pares de divisas (Chile = CLP=X, Colombia = COP=X)
tickers = {"Chile": "CLP=X", "Colombia": "COP=X"}

df_list = []

for pais, ticker in tickers.items():
    # Descargar datos diarios de todo el año 2025
    data = yf.download(
        ticker, start="2023-01-01", end="2026-12-31", progress=False
    )

    if not data.empty:
        # Extraer fecha y precio de cierre
        df_temp = data["Close"].reset_index()
        df_temp.columns = ["Fecha", "Cambio_USD"]
        df_temp["País"] = pais
        df_list.append(df_temp)

# 2. Unir Chile y Colombia
df_final = pd.concat(df_list, ignore_index=True)

# 3. Agregar Ecuador (Tasa constante = 1.00 por estar dolarizado)
fechas_unicas = df_final["Fecha"].unique()
df_ecuador = pd.DataFrame(
    {"Fecha": fechas_unicas, "Cambio_USD": 1.00, "País": "Ecuador"}
)

# 4. Consolidar, ordenar y seleccionar las 3 columnas
df_completo = (
    pd.concat([df_final, df_ecuador], ignore_index=True)
    .sort_values(by=["Fecha", "País"])
    .reset_index(drop=True)
)

df_completo = df_completo[["País", "Fecha", "Cambio_USD"]]

# 5. Guardar CSV
df_completo.to_csv("tipo_cambio_diario_2025.csv", index=False)
print(df_completo.head(10))
