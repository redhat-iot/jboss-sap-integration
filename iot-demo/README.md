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

- Startup the instance by

	goto project/amqp-demo-web-retail
	mvn jetty:run to startup the server 
	
	goto project/mqtt-drools
	mvn camel:run -Psimple

- verify  DashBoard

    http://localhost:8282/demo/retailStoreDashboard.html

- click on start button.


Supporting Articles
-------------------

Coming soon...


Released versions
-----------------

See the tagged releases for the following versions of the product:

- v0.1 - JBoss A-MQ 6.2.



