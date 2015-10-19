import random, time, datetime, uuid, json, ConfigParser, os
import paho.mqtt.client as mqtt


# This script generates the following MQTT messages
#
# 1. customer/enter
#
#    { 
#      id: --GUID representing customer--,
#      ts: --timestamp of the entrance, in seconds since epoch--
#    }
#
# 2. customer/move
#
#    { 
#      id: --GUID representing customer--,
#      ts: --timestamp of the move, in seconds since epoch--,
#       x: --x coordinate of location sensor that fired--,
#       y: --y coordinate of location sensor that fired--
#    }
#
# 2. customer/exit
#
#    { 
#      id: --GUID representing customer--,
#      ts: --timestamp of the exit, in seconds since epoch--
#    }
#

# Represents a "square" in the store at a particular location, and contain all valid moves from that location
class Location:
   def __init__(self,x,y,width, height):
      self.x = x
      self.y = y
      # a list of all valid moves. Each move is a tuple of the form ("adjacent x location", "adjacent y loaction", "is this closer to the exit?")
      self.validMoves = [(a,b, True if a <= self.x and b <= self.y else False) for a in range(max(self.x - 1, 0), min(self.x + 2,width)) for b in range(max(self.y -1, 0), min(self.y + 2, height)) if not(a == self.x and b == self.y)]

class Store:
   def __init__(self, width, height):
      self.height = height
      self.width = width
      self.locations = [[Location(x,y, width, height) for y in range(0,height)] for x in range(0, width)]

class Customer:
   def __init__(self, store):
      self.store = store
      self.currentLocation = store.locations[0][0]  # customers enter and exit from the bottom left corner of the store

      self.meanDwellTime = random.uniform(1, 20)    # the *average* amount of time this customer will spend on a square
      self.consistancy = random.uniform(1,5)        # how consistantly the customer spends that time. Higher means more inconsistant
      self.nextMoveTime = self.getNextMoveTime()   
      self.isExiting = False
      self.exitTime = datetime.datetime.now() + datetime.timedelta(0, random.uniform(1, 600))  # the time this customer will start to exit
      self.id = uuid.uuid1()

   def getNextMoveTime(self):
      # amount of time spent at a location is a random value picked from a guassian distribution, with a mean equal to the customer's 
      # average dwell time and a standard devivation equal to the customer's consistancy
      return (datetime.datetime.now() + datetime.timedelta(0, random.gauss(self.meanDwellTime, self.consistancy)))

   def move(self):
      # if the customer is exiting, only move to an adjacent location that is towards the exit. If they are already at the door, don't move
      if self.isExiting:
         if self.currentLocation.x == 0 and self.currentLocation.y == 0:
            (newX, newY) = (0,0)
         else:
            (newX, newY, isTowardsExit) = random.choice([(x,y,e) for (x,y,e) in self.currentLocation.validMoves if e is True])
      else:
      # if the customer is not exiting, pick any adjacent location
         (newX, newY, isTowardsExit) = random.choice(self.currentLocation.validMoves)
      
      self.currentLocation = self.store.locations[newX][newY]

   def tick(self):
      if self.isExiting == False and self.exitTime < datetime.datetime.now():
         self.isExiting = True
      
      if self.nextMoveTime < datetime.datetime.now():
         self.nextMoveTime = self.getNextMoveTime()
         self.move()
         return True

      return False

def main():

   # configuration information
   config = ConfigParser.ConfigParser()
   config.read('config.cfg')
   mqttHost = config.get('MQTT', 'host')
   mqttPort = config.get('MQTT', 'port')
   mqttClientName = config.get('MQTT', 'name')
   width = config.getint('Store', 'width')
   height = config.getint('Store', 'height')
   averageCustomersInStore = config.getint('Customers', 'averageCustomersInStore')

   mqttc = mqtt.Client('StoreSimulator')
   mqttc.connect('localhost', 1883)

   myStore = Store(width, height)
   customerQueue = []   # List of customers in the store
   nextCustomerEntranceTime = datetime.datetime.now()

   def manageCustomerMovements(c):
      if c.tick():
         if c.isExiting and c.currentLocation.x == 0 and c.currentLocation.y == 0:
            # remove the customer and signal the exit
            msg = {'id': str(c.id), 'ts': int(datetime.datetime.now().strftime("%s"))*1000}
            mqttc.publish("customerexit", json.dumps(msg))
            print('Customer is Exiting: %s') % (c.id)
            customerQueue.remove(c)
         else:
            # signal move
 	    msg = {'id': str(c.id), 'ts': int(datetime.datetime.now().strftime("%s"))*1000, 'x': c.currentLocation.x, 'y': c.currentLocation.y}
            print('Customer is Moving: %s') % (msg)
            mqttc.publish("customermove", json.dumps(msg))

   # Check if any customers are due to entier, move, or exit. Sleep for one second, then repeat
   while True:
      if nextCustomerEntranceTime < datetime.datetime.now():
         # add the new customer, and signal the entrance
         newCustomer = Customer(myStore)
         customerQueue.append(newCustomer)

         msg = {'id': str(newCustomer.id), 'ts': int(datetime.datetime.now().strftime("%s"))*1000}
         mqttc.publish("customerenter", json.dumps(msg))

         nextCustomerEntranceTime = datetime.datetime.now() + datetime.timedelta(0, random.uniform(1, 600 / averageCustomersInStore))
         print('%s is Entering. MDT: %0.1f, C: %0.1f, E: %s') % (newCustomer.id, newCustomer.meanDwellTime, newCustomer.consistancy, newCustomer.exitTime)
         print('New Customer is Entering: %s') % (newCustomer.id)
         
         print('Next Customer Entering at %s.') % (nextCustomerEntranceTime)

      [manageCustomerMovements(c) for c in customerQueue]  
      time.sleep(1)

if __name__=='__main__':
   main()
