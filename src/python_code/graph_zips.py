import csv
import numpy as np
import matplotlib.pyplot as plt


class House:
    def __init__(self, address, price, area, lot, bedroom, bathroom):
        self.address = address
        self.price = price
        self.area = area
        self.lot = lot
        self.bedroom = bedroom
        self.bathroom = bathroom


house_list = []


def show_graph(list_temp_address, list_temp_price, list_temp_area, list_temp_lot, area_zip):
    description = str("Zip is " + str(area_zip))
    plt.title(description)
    plt.scatter(list_temp_area, list_temp_price, marker='o', color=['red'])

    # Use the below two lines to add address to the graph
    for i, txt in enumerate(list_temp_address):
        text = plt.annotate(txt, (list_temp_area[i], list_temp_price[i]))
        text.set_fontsize(14)

    # mng = plt.get_current_fig_manager()
    # mng.resize(*mng.window.maxsize())
    plt.show()
    return


def calculate_zips(unique_zip):
    plt.style.use('seaborn-whitegrid')
    house_temp = []
    for area_zip in unique_zip:
        house_temp.clear()
        for house in house_list:
            if str(area_zip) in house.address:
                print(house.address)
                house_temp.append(house)
        print("going to next zip")

        list_temp_address = []
        list_temp_price = []
        list_temp_area = []
        list_temp_lot = []
        for x in house_temp:
            list_temp_address.append(x.address)
            list_temp_price.append(x.price)
            list_temp_area.append(x.area)
            list_temp_lot.append(x.lot)
        show_graph(list_temp_address, list_temp_price, list_temp_area, list_temp_lot, area_zip)
    return


with open('data/remodel_sf.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if line_count != 0:
            obj = (House(row[1], float(row[2]), float(row[3]), float(row[4]), int(row[5]), int(row[6])))
            house_list.append(obj)
        line_count += 1

# print(len(house_list))
# for x in house_list:
#     print(x.address, sep="\n")
#     print(type(x.area))

# list_address = []
# list_price = []
# list_area = []
# list_lot = []
# list_bedroom = []
# list_bathroom = []
list_zip = []

for x in house_list:
    # list_address.append(x.address)
    # list_price.append(x.price)
    # list_area.append(x.area)
    # list_lot.append(x.lot)
    # list_bedroom.append(x.bedroom)
    # list_bathroom.append(x.bathroom)
    list_zip.append(int(x.address[-5:]))

unique_zip = []
for number in list_zip:
    if number not in unique_zip:
        unique_zip.append(number)
print(unique_zip)
print(len(unique_zip))

calculate_zips(unique_zip)
