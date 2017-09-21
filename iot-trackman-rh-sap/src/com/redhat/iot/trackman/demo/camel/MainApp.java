package com.redhat.iot.trackman.demo.camel;


import org.apache.camel.main.Main;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * A Camel Application
 */
public class MainApp {

	/**
	 * A main() so we can easily run these routing rules in our IDE
	 */
	public static void main(String... args) throws Exception {
		Main main = new Main();
		DriverManagerDataSource ds = new DriverManagerDataSource();

		ds.setDriverClassName("sybase.jdbc.sqlanywhere.IDriver");
		ds.setUrl("jdbc:datasource:Host=localhost:2638;uid=DBA;pwd=sql;eng=golf");
		ds.setUsername("DBA");
		ds.setPassword("sql");	 

		main.bind("sqlanywhere", ds);

		main.addRouteBuilder(new TestRoute());
		main.run(args);
	}

}
