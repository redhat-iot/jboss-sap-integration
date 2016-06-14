package com.redhat.iot.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.redhat.iot.persistence.RandomGenerator.City;
import com.redhat.iot.persistence.RandomGenerator.State;

public final class IotPostgresDdlGenerator {

    private static final String OUTPUT_FILE = "../persistance/postgres.sql";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private static final String DROP_TABLE_PATTERN = "DROP TABLE IF EXISTS %s CASCADE;";

    private static final int FIRST_DEPT = 1000;
    private static final int LAST_DEPT = 1006;

    private static final Timestamp FIRST_ORDER_DATE = Timestamp.valueOf( LocalDateTime.of( 2010, 1, 1, 1, 1 ) );
    private static final Timestamp LAST_ORDER_DATE = new Timestamp( Instant.now().toEpochMilli() );

    private static final float MIN_DISCOUNT = 10F;
    private static final float MAX_DISCOUNT = 50F;

    private static final float MIN_PRICE = 1F;
    private static final float MAX_PRICE = 100.0F;

    private static final int MAX_ORDER_DETAILS = 7;
    private static final int MAX_QUANTITY = 5;

    private static final int FIRST_CUSTOMER_ID = 10000;
    private static final int NUM_CUSTOMERS = 5000;
    private static final int LAST_CUSTOMER_ID = ( FIRST_CUSTOMER_ID + NUM_CUSTOMERS - 1 );
    private static final String CUSTOMER_TABLE = "\"Customer\"";
    private static final String CUSTOMER_COLUMNS = "\"id\", " + "\"name\", " + "\"phone\", " + "\"addressLine1\", "
                                                   + "\"addressLine2\", " + "\"city\", " + "\"state\", "
                                                   + "\"postalCode\", " + "\"country\", " + "\"creditLimit\"";
    private static final String CREATE_CUSTOMER_TABLE = "CREATE TABLE " + CUSTOMER_TABLE + " ( \n"
                                                        + "\t\"id\" int NOT NULL,\n"
                                                        + "\t\"name\" varchar(50) NOT NULL,\n"
                                                        + "\t\"phone\" varchar(50) NOT NULL,\n"
                                                        + "\t\"addressLine1\" varchar(50) NOT NULL,\n"
                                                        + "\t\"addressLine2\" varchar(50) DEFAULT NULL,\n"
                                                        + "\t\"city\" varchar(50) NOT NULL,\n"
                                                        + "\t\"state\" varchar(50) DEFAULT NULL,\n"
                                                        + "\t\"postalCode\" varchar(15) DEFAULT NULL,\n"
                                                        + "\t\"country\" varchar(50) NOT NULL,\n"
                                                        + "\t\"creditLimit\" integer DEFAULT NULL,\n"
                                                        + "\tPRIMARY KEY (\"id\")\n" + ");";
    private static final String INSERT_CUSTOMER_PATTERN = "INSERT INTO " + CUSTOMER_TABLE + " ( " + CUSTOMER_COLUMNS
                                                          + " ) VALUES ( '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s', '%s', %s );";

    private static final int FIRST_PRODUCT_ID = 1000;
    private static final int NUM_PRODUCTS = 150;
    private static final int LAST_PRODUCT_ID = ( FIRST_PRODUCT_ID + NUM_PRODUCTS - 1 );
    private static final String PRODUCT_TABLE = "\"Product\"";
    private static final String PRODUCT_COLUMNS = "\"productCode\", " + "\"productName\", " + "\"productSize\", "
                                                  + "\"productVendor\", " + "\"productDescription\", "
                                                  + "\"quantityInStock\", " + "\"buyPrice\", " + "\"MSRP\", "
                                                  + "\"departmentCode\"";
    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE " + PRODUCT_TABLE + " ( \n"
                                                       + "\t\"productCode\" varchar NOT NULL,\n"
                                                       + "\t\"productName\" varchar NOT NULL,\n"
                                                       + "\t\"productSize\" varchar NOT NULL,\n"
                                                       + "\t\"productVendor\" varchar NOT NULL,\n"
                                                       + "\t\"productDescription\" text NOT NULL,\n"
                                                       + "\t\"quantityInStock\" integer NOT NULL,\n"
                                                       + "\t\"buyPrice\" numeric(6,2) NOT NULL,\n"
                                                       + "\t\"MSRP\" numeric(6,2) NOT NULL,\n"
                                                       + "\t\"departmentCode\" varchar NOT NULL,\n"
                                                       + "\tPRIMARY KEY (\"productCode\")\n" + ");";
    private static final String INSERT_PRODUCT_PATTERN = "INSERT INTO " + PRODUCT_TABLE + " ( " + PRODUCT_COLUMNS
                                                         + " ) VALUES ( %s, '%s', '%s', '%s', '%s', %s, '%s', '%s', %s );";

    private static final int FIRST_ORDER_ID = 1;
    private static final int NUM_ORDERS = 1000;
    private static final String ORDER_TABLE = "\"Order\"";
    private static final String ORDER_COLUMNS = "\"orderNumber\", " + "\"orderDate\", " + "\"customerNumber\"";
    private static final String CREATE_ORDER_TABLE = "CREATE TABLE " + ORDER_TABLE + " ( \n"
                                                     + "\t\"orderNumber\" integer NOT NULL,\n"
                                                     + "\t\"orderDate\" date NOT NULL,\n" + "\t\"comments\" text,\n"
                                                     + "\t\"customerNumber\" integer NOT NULL,\n"
                                                     + "\tPRIMARY KEY (\"orderNumber\"),\n"
                                                     + "\tCONSTRAINT \"orders_ibfk_1\" FOREIGN KEY (\"customerNumber\") REFERENCES \"Customer\" (\"id\")\n"
                                                     + ");";
    private static final String INSERT_ORDER_PATTERN = "INSERT INTO " + ORDER_TABLE + " ( " + ORDER_COLUMNS
                                                       + " ) VALUES ( %s, '%s', %s );";

    private static final int NUM_ORDER_DETAILS = 2000;
    private static final String ORDER_DETAIL_TABLE = "\"OrderDetail\"";
    private static final String ORDER_DETAIL_COLUMNS = "\"orderNumber\", " + "\"productCode\", "
                                                       + "\"quantityOrdered\", " + "\"msrp\", " + "\"discount\"";
    private static final String CREATE_ORDER_DETAIL_TABLE = "CREATE TABLE " + ORDER_DETAIL_TABLE + " ( \n"
                                                            + "\t\"orderNumber\" integer NOT NULL,\n"
                                                            + "\t\"productCode\" varchar(15) NOT NULL,\n"
                                                            + "\t\"quantityOrdered\" integer NOT NULL,\n"
                                                            + "\t\"msrp\" numeric(6,2) NOT NULL,\n"
                                                            + "\t\"discount\" integer NOT NULL,\n"
                                                            + "\tPRIMARY KEY (\"orderNumber\",\"productCode\"),\n"
                                                            + "\tCONSTRAINT \"orderdetails_ibfk_2\" FOREIGN KEY (\"productCode\") REFERENCES \"Product\" (\"productCode\"),\n"
                                                            + "\tCONSTRAINT \"orderdetails_ibfk_1\" FOREIGN KEY (\"orderNumber\") REFERENCES \"Order\" (\"orderNumber\")\n"
                                                            + ");";
    private static final String INSERT_ORDER_DETAIL_PATTERN = "INSERT INTO " + ORDER_DETAIL_TABLE + " ( "
                                                              + ORDER_DETAIL_COLUMNS
                                                              + " ) VALUES ( %s, %s, %s, %s, %s );";

    private static final int FIRST_PROMOTION_ID = 1;
    private static final int NUM_PROMOTIONS = 150;
    private static final String PROMOTION_TABLE = "\"Promotion\"";
    private static final String PROMOTION_COLUMNS = "\"id\", " + "\"productCode\", " + "\"discount\"";
    private static final String CREATE_PROMOTION_TABLE = "CREATE TABLE " + PROMOTION_TABLE + " ( \n"
                                                         + "\t\"id\" integer NOT NULL,\n"
                                                         + "\t\"productCode\" varchar(15) NOT NULL,\n"
                                                         + "\t\"discount\" decimal(4,2) NOT NULL,\n"
                                                         + "\tPRIMARY KEY (\"id\",\"productCode\"),\n"
                                                         + "\tCONSTRAINT \"orderdetails_ibfk_2\" FOREIGN KEY (\"productCode\") REFERENCES \"Product\" (\"productCode\")\n"
                                                         + ");";
    private static final String INSERT_PROMOTION_PATTERN = "INSERT INTO " + PROMOTION_TABLE + " ( " + PROMOTION_COLUMNS
                                                           + " ) VALUES ( %s, '%s', %s );";

    private static final String[] TABLES = new String[] { CUSTOMER_TABLE,
                                                          PRODUCT_TABLE,
                                                          ORDER_TABLE,
                                                          ORDER_DETAIL_TABLE,
                                                          PROMOTION_TABLE };
    private static final String[] CREATE_TABLES = new String[] { CREATE_CUSTOMER_TABLE,
                                                                 CREATE_PRODUCT_TABLE,
                                                                 CREATE_ORDER_TABLE,
                                                                 CREATE_ORDER_DETAIL_TABLE,
                                                                 CREATE_PROMOTION_TABLE };

    public static void main( String[] args ) {
        try {
            final long start = System.currentTimeMillis();
            final IotPostgresDdlGenerator generator = new IotPostgresDdlGenerator();
            final String ddl = generator.generate();

            // write file out
            final Path output = Paths.get( OUTPUT_FILE );
            Files.write( output, ddl.toString().getBytes() );
            System.out.println( "Finished DDL generation in " + ( System.currentTimeMillis() - start ) + "ms" );
        } catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    private final RandomGenerator random;

    IotPostgresDdlGenerator() throws Exception {
        this.random = new RandomGenerator();
    }

    private String generate() throws Exception {
        final StringBuilder ddl = new StringBuilder();

        System.out.print( "Dropping tables ... " );
        writeDropTables( ddl );
        System.out.println( "done." );

        System.out.print( "Creating tables ... " );
        writeCreateTables( ddl );
        System.out.println( "done." );

        System.out.print( "Generating insert customers DDL ... " );
        generateCustomers( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of customers = " + NUM_CUSTOMERS );

        System.out.print( "Generating insert products DDL ... " );
        generateProducts( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of products = " + NUM_PRODUCTS );

        System.out.print( "Generating insert promotions DDL ... " );
        generatePromotions( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of promotions = " + NUM_PROMOTIONS );

        System.out.print( "Generating insert orders DDL ... " );
        generateOrders( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of orders = " + NUM_ORDERS );

        System.out.print( "Generating insert order details DDL ... " );
        generateOrderDetails( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of order details = " + NUM_ORDER_DETAILS );

        ddl.append( "\n\ncommit;\n\n" );

        return ddl.toString();
    }

    private void generateCustomers( final StringBuilder ddl ) throws Exception {
        ddl.append( "--" ).append( CUSTOMER_TABLE.substring( 1, ( CUSTOMER_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_CUSTOMER_ID, limit = ( FIRST_CUSTOMER_ID + NUM_CUSTOMERS ); i < limit; ++i ) {
            final City place = this.random.nextCity();
            final int id = i;
            final String name = this.random.nextName();
            final String addressLine1 = this.random.nextAddressLine1();
            final String city = place.getCity();
            final State state = place.getState();
            final String phone = this.random.nextPhoneNumber( state );
            final String postalCode = place.getPostalCode();
            final double creditLimit = this.random.next( 1000, 10000 );

            final String customerDdl = String.format( INSERT_CUSTOMER_PATTERN, id, name, phone, addressLine1, null,
                                                      city, state.getName(), postalCode, "USA", creditLimit );
            ddl.append( customerDdl ).append( '\n' );
        }
    }

    private String generateDescription( final String name,
                                        final String vendor ) {
        return ( "A " + name + " by manufacturer " + vendor );
    }

    private void generateOrderDetails( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( ORDER_DETAIL_TABLE.substring( 1, ( ORDER_DETAIL_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        for ( int i = FIRST_ORDER_ID, limit = ( FIRST_ORDER_ID + NUM_ORDERS ); i < limit; ++i ) {
            final List< Integer > prodIds = new ArrayList<>();

            // create details for each order
            for ( int j = 0, numDetails = this.random.next( 1, MAX_ORDER_DETAILS ); j < numDetails; ++j ) {
                final int orderNumber = i;
                int productCode = this.random.next( FIRST_PRODUCT_ID, LAST_PRODUCT_ID );

                while ( prodIds.contains( productCode ) ) {
                    productCode = this.random.next( FIRST_PRODUCT_ID, LAST_PRODUCT_ID );
                }

                prodIds.add( productCode );

                final int quantity = this.random.next( 1, MAX_QUANTITY );
                final float msrp = this.random.nextPrice( MIN_PRICE, MAX_PRICE );
                final int discount = ( int ) this.random.next( MIN_DISCOUNT, MAX_DISCOUNT );

                final String detailsDdl = String.format( INSERT_ORDER_DETAIL_PATTERN, orderNumber, productCode,
                                                         quantity, msrp, discount );
                ddl.append( detailsDdl ).append( '\n' );
            }
        }
    }

    private void generateOrders( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( ORDER_TABLE.substring( 1, ( ORDER_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_ORDER_ID, limit = ( FIRST_ORDER_ID + NUM_ORDERS ); i < limit; ++i ) {
            final int id = i;
            final Timestamp orderDate = this.random.next( FIRST_ORDER_DATE, LAST_ORDER_DATE );
            final int customerId = this.random.next( FIRST_CUSTOMER_ID, LAST_CUSTOMER_ID );

            final String orderDdl = String.format( INSERT_ORDER_PATTERN, id, DATE_FORMATTER.format( orderDate ),
                                                   customerId );
            ddl.append( orderDdl ).append( '\n' );
        }
    }

    private void generateProducts( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( PRODUCT_TABLE.substring( 1, ( PRODUCT_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_PRODUCT_ID, limit = ( FIRST_PRODUCT_ID + NUM_PRODUCTS ); i < limit; ++i ) {
            final int id = i;
            final String name = this.random.nextProduct();
            final String size = this.random.nextSize();
            final String vendor = this.random.nextVendor();
            final String description = generateDescription( name, vendor );
            final int quantityInStock = this.random.next( 1, MAX_QUANTITY );
            final String buyPrice = nextPrice();
            final String msrp = nextPrice();
            final int departmentCode = this.random.next( FIRST_DEPT, LAST_DEPT );

            final String productDdl = String.format( INSERT_PRODUCT_PATTERN, id, name, size, vendor, description,
                                                     quantityInStock, buyPrice, msrp, departmentCode );
            ddl.append( productDdl ).append( '\n' );
        }
    }

    private void generatePromotions( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( PROMOTION_TABLE.substring( 1, ( PROMOTION_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        for ( int i = FIRST_PROMOTION_ID, limit = ( FIRST_PROMOTION_ID + NUM_PROMOTIONS ); i < limit; ++i ) {
            final int id = i;
            final int productCode = this.random.next( FIRST_PRODUCT_ID, LAST_PRODUCT_ID );
            final float discount = this.random.nextPrice( MIN_DISCOUNT, MAX_DISCOUNT );

            final String productDdl = String.format( INSERT_PROMOTION_PATTERN, id, productCode, discount );
            ddl.append( productDdl ).append( '\n' );
        }
    }

    private String nextPrice() {
        final float price = this.random.nextPrice( MIN_PRICE, MAX_PRICE );
        return String.format( "%.2f", price );
    }

    private void writeCreateTables( final StringBuilder builder ) {
        for ( final String table : CREATE_TABLES ) {
            builder.append( table );
            builder.append( "\n\n" );
        }
    }

    private void writeDropTables( final StringBuilder builder ) {
        for ( final String table : TABLES ) {
            builder.append( String.format( DROP_TABLE_PATTERN, table ) );
            builder.append( "\n\n" );
        }
    }

}
