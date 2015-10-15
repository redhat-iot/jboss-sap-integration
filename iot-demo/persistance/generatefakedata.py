from faker import Faker
import csv
import random

fake = Faker()

with open('customers.dat', 'wb') as csvfile:
  fakewriter = csv.writer(csvfile, delimiter='|')
  for i in range(5000):
    id = 10000 + i
    name = fake.name()
    lastVisit = random.randint(1356998400, 1444331721)
    averagePurchaseAmount = abs(round(random.gauss(100.0, 50), 2))

    fakewriter.writerow([id, name, lastVisit, averagePurchaseAmount])

