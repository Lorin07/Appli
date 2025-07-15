import csv

def convert_stations(input_path, output_path):
    with open(input_path, newline='', encoding='utf-8') as infile, \
         open(output_path, mode='w', newline='', encoding='utf-8') as outfile:

        reader = csv.reader(infile)
        writer = csv.writer(outfile)

        for row in reader:
            if len(row) < 4:
                continue
            stn_id, wban_id, lat, lon = row[0], row[1], row[2].replace(',', '.'), row[3].replace(',', '.')
            writer.writerow([stn_id, wban_id, lat, lon])

def convert_temperatures(input_path, output_path):
    with open(input_path, newline='', encoding='utf-8') as infile, \
         open(output_path, mode='w', newline='', encoding='utf-8') as outfile:

        reader = csv.reader(infile)
        writer = csv.writer(outfile)

        for row in reader:
            if len(row) < 6:
                continue
            stn_id, wban_id, year, month, day, temp = row
            temp = temp.replace(',', '.')
            writer.writerow([stn_id, wban_id, year, month, day, temp])

if __name__ == '__main__':
    convert_stations('src/main/resources/stations.csv', 'src/main/resources/stations_clean.csv')
    convert_temperatures('src/main/resources/2015.csv', 'src/main/resources/2015_clean.csv')
