JBoss A-MQ, BRMS, SQLAnywhere, HANA IoT Demo 
=====================================================================

Demo based on JBoss A-MQ product.

Setup and Configuration
-----------------------

- Download JBoss A-MQ product from RED HAT (version 6.2) into install folder

- run 'init.sh' 

- start JBoss A-MQ

   go to ./target/jboss-a-mq-{$version}/bin
   execute JBoss A-MQ by executing ./amq

- Run the custsim.py sript

  The custsim.py script is a Python script that will generate simulated customer actions 
  related to a retail store (entering, moving and exiting).
  To execute this script you will need Python installed and can exexute the script using 
  the command "python custsim.py" from the custsim folder. 

  Installing Python:

  OSx: https://www.python.org/downloads/mac-osx/
  Linux: http://docs.python-guide.org/en/latest/starting/install/linux/
  Windows: https://www.python.org/downloads/windows/


Supporting Articles
-------------------

Coming soon...


Released versions
-----------------

See the tagged releases for the following versions of the product:

- v0.1 - JBoss A-MQ 6.2.



