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

- Generate fake customer data by either running generatefakedata.py *or* use the included customers.dat.example file by renaming it to customer.dat. Copy the customer.dat file into the custsim directory.

- Run the custsim.py sript

  The custsim.py script is a Python script that will generate simulated customer actions 
  related to a retail store (entering, moving and exiting).
  To execute this script you will need Python installed and can exexute the script using 
  the command "python custsim.py" from the custsim folder. 

- Download [SQL Anywhere 17 Developer Edition](https://www.sap.com/cmp/syb/crm-xm15-dwn-dt015/index.html) and install. You may need to [source the binaries](http://dcx.sap.com/index.html#sqla170/en/html/8135b1bb6ce2101499f0f55a54bc1ab2.html) before using.

- Rename persistance/remote.cfg.example to persistance/remote.cfg and set the correct host and port for fuse

- Create and populate the remote databaes. These commands will create the database, start it as a daemon listening on port 8080, and populate it with the fake customer data.
  - dbinit -dba dba,sqlsql remote.db
  - dbsrv17 -ud -xs http{port=8080} remote.db
  - dbisql -c "uid=dba;pwd=sqlsql" remote.sql
 
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



