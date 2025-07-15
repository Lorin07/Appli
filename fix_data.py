import csv
import pandas as pd

def fix_stations(input_path, output_path):
    with open(input_path, newline='', encoding='utf-8') as infile, open(output_path, 'w', newline='', encoding='utf-8') as outfile:
        reader = csv.reader(infile)
        writer = csv.writer(outfile)
        for row in reader:
            if len(row) >= 4:
                lat = row[2].replace(',', '.')
                lon = row[3].replace(',', '.')
                writer.writerow([row[0], row[1], lat, lon])

def fix_temperatures(input_path, output_path):
    with open(input_path, newline='', encoding='utf-8') as infile, open(output_path, 'w', newline='', encoding='utf-8') as outfile:
        reader = csv.reader(infile)
        writer = csv.writer(outfile)
        for row in reader:
            if len(row) >= 6:
                temp = row[5].replace(',', '.')
                writer.writerow([row[0], row[1], row[2], row[3], row[4], temp])

def complete_missing_stations(temp_path, station_path):
    df_temp = pd.read_csv(temp_path, header=None, names=["stn", "wban", "year", "month", "day", "temp"])
    df_stations = pd.read_csv(station_path, header=None, names=["stn", "wban", "lat", "lon"])
    
    temp_keys = set(zip(df_temp['stn'], df_temp['wban']))
    station_keys = set(zip(df_stations['stn'], df_stations['wban']))
    missing = temp_keys - station_keys

    rows_to_add = [(stn, wban, 0.0, 0.0) for (stn, wban) in missing]
    df_missing = pd.DataFrame(rows_to_add, columns=["stn", "wban", "lat", "lon"])

    df_final = pd.concat([df_stations, df_missing], ignore_index=True)
    df_final.to_csv(station_path, index=False, header=False)

if __name__ == "__main__":
    fix_stations("src/main/resources/stations_original.csv", "src/main/resources/stations.csv")
    fix_temperatures("src/main/resources/2015_original.csv", "src/main/resources/2015.csv")
    complete_missing_stations("src/main/resources/2015.csv", "src/main/resources/stations.csv")
