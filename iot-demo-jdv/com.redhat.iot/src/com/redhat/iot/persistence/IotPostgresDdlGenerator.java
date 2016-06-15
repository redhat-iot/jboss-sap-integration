package com.redhat.iot.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.iot.persistence.DataProvider.City;
import com.redhat.iot.persistence.DataProvider.State;

public final class IotPostgresDdlGenerator {

    private static final String OUTPUT_FILE = "../persistance/postgres.sql";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private static final String DROP_TABLE_PATTERN = "DROP TABLE IF EXISTS %s CASCADE;";

    private static final int FIRST_DEPT = 1000;
    private static final int LAST_DEPT = 1006;

    private static final Timestamp FIRST_ORDER_DATE = Timestamp.valueOf( LocalDateTime.of( 2010, 1, 1, 1, 1 ) );
    private static final Timestamp LAST_ORDER_DATE = new Timestamp( Instant.now().toEpochMilli() );

    private static final int MIN_DISCOUNT = 10;
    private static final int MAX_DISCOUNT = 50;

    private static final float MIN_PRICE = 1F;
    private static final float MAX_PRICE = 100.0F;

    private static final int MAX_ORDER_DETAILS = 7;
    private static final int MAX_QUANTITY = 5;

    private static final int FIRST_STORE_ID = 9000;
    private static final int NUM_STORES = 20;
    private static final String STORE_TABLE = "\"Store\"";
    private static final String STORE_COLUMNS = ( "\"id\", " + "\"phone\", " + "\"addressLine1\", " + "\"city\", "
                                                  + "\"state\", " + "\"postalCode\"" );
    private static final String CREATE_STORE_TABLE = "CREATE TABLE " + STORE_TABLE + " (\n"
                                                     + "\t\"id\" integer NOT NULL,\n" + "\t\"phone\" text NOT NULL,\n"
                                                     + "\t\"addressLine1\" text NOT NULL,\n"
                                                     + "\t\"addressLine2\" text DEFAULT NULL,\n"
                                                     + "\t\"city\" text NOT NULL,\n" + "\t\"state\" text NOT NULL,\n"
                                                     + "\t\"postalCode\" text NOT NULL,\n"
                                                     + "\t\"country\" text DEFAULT 'USA',\n"
                                                     + "\tPRIMARY KEY ( \"id\" )\n" + " );";
    private static final String INSERT_STORE_PATTERN = "INSERT INTO " + STORE_TABLE + " ( " + STORE_COLUMNS
                                                       + ") VALUES ( %s, '%s', '%s', '%s', '%s', '%s' );";

    private static final int FIRST_CUSTOMER_ID = 10000;
    private static final int NUM_CUSTOMERS = 5000;
    private static final int LAST_CUSTOMER_ID = ( ( FIRST_CUSTOMER_ID + NUM_CUSTOMERS ) - 1 );
    private static final String CUSTOMER_TABLE = "\"Customer\"";
    private static final String CUSTOMER_COLUMNS = "\"id\", " + "\"name\", " + "\"phone\", " + "\"addressLine1\", "
                                                   + "\"city\", " + "\"state\", " + "\"postalCode\", "
                                                   + "\"creditLimit\"";
    private static final String CREATE_CUSTOMER_TABLE = "CREATE TABLE " + CUSTOMER_TABLE + " (\n"
                                                        + "\t\"id\" integer NOT NULL,\n" + "\t\"name\" text NOT NULL,\n"
                                                        + "\t\"phone\" text NOT NULL,\n"
                                                        + "\t\"addressLine1\" text NOT NULL,\n"
                                                        + "\t\"addressLine2\" text DEFAULT NULL,\n"
                                                        + "\t\"city\" text NOT NULL,\n" + "\t\"state\" text NOT NULL,\n"
                                                        + "\t\"postalCode\" text NOT NULL,\n"
                                                        + "\t\"country\" text DEFAULT 'USA',\n"
                                                        + "\t\"creditLimit\" integer DEFAULT 0,\n"
                                                        + "\t\"storeId\" integer DEFAULT NULL,\n"
                                                        + "\tPRIMARY KEY ( \"id\" ),\n"
                                                        + "\tCONSTRAINT \"customer_store_fk\" FOREIGN KEY ( \"storeId\" ) REFERENCES \"Store\" ( \"id\" )\n"
                                                        + ");";
    private static final String INSERT_CUSTOMER_PATTERN = "INSERT INTO " + CUSTOMER_TABLE + " ( " + CUSTOMER_COLUMNS
                                                          + " ) VALUES ( %s, '%s', '%s', '%s', '%s', '%s', '%s', %s );";

    private static final int FIRST_PRODUCT_ID = 1000;
    private static final int NUM_PRODUCTS = 1000;
    private static final String PRODUCT_TABLE = "\"Product\"";
    private static final String PRODUCT_COLUMNS = ( "\"id\", " + "\"productName\", " + "\"productSize\", "
                                                    + "\"productVendor\", " + "\"productDescription\", "
                                                    + "\"buyPrice\", " + "\"msrp\", " + "\"departmentCode\"" );
    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE " + PRODUCT_TABLE + " (\n"
                                                       + "\t\"id\" integer NOT NULL,\n"
                                                       + "\t\"productName\" text NOT NULL,\n"
                                                       + "\t\"productSize\" text NOT NULL,\n"
                                                       + "\t\"productVendor\" text NOT NULL,\n"
                                                       + "\t\"productDescription\" text NOT NULL,\n"
                                                       + "\t\"buyPrice\" numeric(6,2) NOT NULL,\n"
                                                       + "\t\"msrp\" numeric(6,2) NOT NULL,\n"
                                                       + "\t\"departmentCode\" integer NOT NULL,\n"
                                                       + "\tPRIMARY KEY ( \"id\" )\n" + ");";
    private static final String INSERT_PRODUCT_PATTERN = "INSERT INTO " + PRODUCT_TABLE + " ( " + PRODUCT_COLUMNS
                                                         + " ) VALUES ( %s, '%s', '%s', '%s', '%s', '%s', '%s', %s );";

    private static final int MAX_STOCK = 2000;
    private static final int NUM_INVENTORY = 5000;
    private static final String INVENTORY_TABLE = "\"Inventory\"";
    private static final String INVENTORY_COLUMNS = ( "\"storeId\", " + "\"productId\", " + "\"quantity\"" );
    private static final String CREATE_INVENTORY_TABLE = "CREATE TABLE " + INVENTORY_TABLE + " (\n"
                                                         + "\t\"storeId\" integer NOT NULL,\n"
                                                         + "\t\"productId\" integer NOT NULL,\n"
                                                         + "\t\"quantity\" integer NOT NULL,\n"
                                                         + "\tPRIMARY KEY ( \"storeId\", \"productId\" ),\n"
                                                         + "\tCONSTRAINT \"inventory_store_fk\" FOREIGN KEY ( \"storeId\" ) REFERENCES \"Store\" ( \"id\" ),\n"
                                                         + "\tCONSTRAINT \"inventory_product_fk\" FOREIGN KEY ( \"productId\" ) REFERENCES \"Product\" ( \"id\" )\n"
                                                         + ");";
    private static final String INSERT_INVENTORY_PATTERN = "INSERT INTO " + INVENTORY_TABLE + " ( " + INVENTORY_COLUMNS
                                                           + " ) VALUES ( %s, %s, %s );";

    private static final int FIRST_ORDER_ID = 1;
    private static final int NUM_ORDERS = 1000;
    private static final String ORDER_TABLE = "\"Order\"";
    private static final String ORDER_COLUMNS = ( "\"id\", " + "\"customerId\", " + "\"orderDate\"" );
    private static final String CREATE_ORDER_TABLE = "CREATE TABLE " + ORDER_TABLE + " (\n"
                                                     + "\t\"id\" integer NOT NULL,\n"
                                                     + "\t\"customerId\" integer NOT NULL,\n"
                                                     + "\t\"orderDate\" date NOT NULL,\n"
                                                     + "\t\"comments\" text DEFAULT NULL,\n"
                                                     + "\tPRIMARY KEY ( \"id\" ),\n"
                                                     + "\tCONSTRAINT \"orders_customer_fk\" FOREIGN KEY ( \"customerId\" ) REFERENCES \"Customer\" ( \"id\" )\n"
                                                     + ");";
    private static final String INSERT_ORDER_PATTERN = "INSERT INTO " + ORDER_TABLE + " ( " + ORDER_COLUMNS
                                                       + " ) VALUES ( %s, %s, '%s' );";

    private static final int NUM_ORDER_DETAILS = 2000;
    private static final String ORDER_DETAIL_TABLE = "\"OrderDetail\"";
    private static final String ORDER_DETAIL_COLUMNS = ( "\"orderId\", " + "\"productId\", " + "\"quantityOrdered\", "
                                                         + "\"msrp\", " + "\"discount\"" );
    private static final String CREATE_ORDER_DETAIL_TABLE = "CREATE TABLE " + ORDER_DETAIL_TABLE + " (\n"
                                                            + "\t\"orderId\" integer NOT NULL,\n"
                                                            + "\t\"productId\" integer NOT NULL,\n"
                                                            + "\t\"quantityOrdered\" integer NOT NULL,\n"
                                                            + "\t\"msrp\" numeric(6,2) NOT NULL,\n"
                                                            + "\t\"discount\" integer NOT NULL,\n"
                                                            + "\tPRIMARY KEY ( \"orderId\", \"productId\" ),\n"
                                                            + "\tCONSTRAINT \"orderdetails_product_fk\" FOREIGN KEY ( \"productId\" ) REFERENCES \"Product\" ( \"id\" ),\n"
                                                            + "\tCONSTRAINT \"orderdetails_order_fk\" FOREIGN KEY ( \"orderId\" ) REFERENCES \"Order\" ( \"id\" )\n"
                                                            + ");";
    private static final String INSERT_ORDER_DETAIL_PATTERN = "INSERT INTO " + ORDER_DETAIL_TABLE + " ( "
                                                              + ORDER_DETAIL_COLUMNS
                                                              + " ) VALUES ( %s, %s, %s, %s, %s );";

    private static final int FIRST_PROMOTION_ID = 1;
    private static final int NUM_PROMOTIONS = 150;
    private static final String PROMOTION_TABLE = "\"Promotion\"";
    private static final String PROMOTION_COLUMNS = "\"id\", " + "\"productId\", " + "\"discount\"";
    private static final String CREATE_PROMOTION_TABLE = "CREATE TABLE " + PROMOTION_TABLE + " (\n"
                                                         + "\t\"id\" integer NOT NULL,\n"
                                                         + "\t\"productId\" integer NOT NULL,\n"
                                                         + "\t\"discount\" integer NOT NULL,\n"
                                                         + "\tPRIMARY KEY ( \"id\", \"productId\" ),\n"
                                                         + "\tCONSTRAINT \"orderdetails_product_fk\" FOREIGN KEY ( \"productId\" ) REFERENCES \"Product\" ( \"id\" )\n"
                                                         + ");";
    private static final String INSERT_PROMOTION_PATTERN = "INSERT INTO " + PROMOTION_TABLE + " ( " + PROMOTION_COLUMNS
                                                           + " ) VALUES ( %s, %s, %s );";

    private static final String[] TABLES = new String[] { STORE_TABLE,
                                                          CUSTOMER_TABLE,
                                                          PRODUCT_TABLE,
                                                          INVENTORY_TABLE,
                                                          ORDER_TABLE,
                                                          ORDER_DETAIL_TABLE,
                                                          PROMOTION_TABLE };
    private static final String[] CREATE_TABLES = new String[] { CREATE_STORE_TABLE,
                                                                 CREATE_CUSTOMER_TABLE,
                                                                 CREATE_PRODUCT_TABLE,
                                                                 CREATE_INVENTORY_TABLE,
                                                                 CREATE_ORDER_TABLE,
                                                                 CREATE_ORDER_DETAIL_TABLE,
                                                                 CREATE_PROMOTION_TABLE };

    public static void main( final String[] args ) {
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

    private int lastProductId;
    private final Map< String, Void > names = new HashMap<>();
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

        System.out.print( "Generating insert stores DDL ... " );
        generateStores( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of stores = " + NUM_STORES );

        System.out.print( "Generating insert customers DDL ... " );
        generateCustomers( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of customers = " + NUM_CUSTOMERS );

        System.out.print( "Generating insert products DDL ... " );
        generateProducts( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of products = " + NUM_PRODUCTS );

        System.out.print( "Generating insert inventory DDL ... " );
        generateInventory( ddl );
        System.out.println( "done." );
        System.out.println( "\tNumber of inventory = " + NUM_INVENTORY );

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
        ddl.append( "\n--" ).append( CUSTOMER_TABLE.substring( 1, ( CUSTOMER_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_CUSTOMER_ID, limit = ( FIRST_CUSTOMER_ID + NUM_CUSTOMERS ); i < limit; ++i ) {
            final City place = nextCity();
            final int id = i;
            final String name = nextName();
            final String addressLine1 = nextAddressLine1();
            final String city = place.getCity();
            final State state = place.getState();
            final String phone = nextPhoneNumber( state );
            final String postalCode = place.getPostalCode();
            final double creditLimit = this.random.next( 1000, 10000 );

            final String customerDdl = String.format( INSERT_CUSTOMER_PATTERN, id, name, phone, addressLine1, city,
                                                      state.getName(), postalCode, creditLimit );
            ddl.append( customerDdl ).append( '\n' );
        }
    }

    private String generateDescription( final String name,
                                        final String vendor ) {
        return ( "A " + name + " by manufacturer " + vendor );
    }

    private void generateInventory( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( INVENTORY_TABLE.substring( 1, ( INVENTORY_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        // make sure the same store and product are not already used
        final Map< Integer, List< Integer > > storeProducts = new HashMap<>();

        for ( int i = 0; i < NUM_INVENTORY; ++i ) {
            int storeId = -1;
            int productId = -1;

            do {
                storeId = this.random.next( FIRST_STORE_ID, ( ( FIRST_STORE_ID + NUM_STORES ) - 1 ) );
                productId = this.random.next( FIRST_PRODUCT_ID, this.lastProductId );
            } while ( storeProducts.containsKey( storeId ) && storeProducts.get( storeId ).contains( productId ) );

            List< Integer > products = storeProducts.get( storeId );

            if ( products == null ) {
                products = new ArrayList<>();
            }

            products.add( productId );
            storeProducts.put( storeId, products );

            final int quantity = this.random.next( 1, MAX_STOCK );

            final String inventoryDdl = String.format( INSERT_INVENTORY_PATTERN, storeId, productId, quantity );
            ddl.append( inventoryDdl ).append( '\n' );
        }
    }

    private void generateOrderDetails( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( ORDER_DETAIL_TABLE.substring( 1, ( ORDER_DETAIL_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        for ( int i = FIRST_ORDER_ID, limit = ( FIRST_ORDER_ID + NUM_ORDERS ); i < limit; ++i ) {
            final List< Integer > prodIds = new ArrayList<>();

            // create details for each order
            for ( int j = 0, numDetails = this.random.next( 1, MAX_ORDER_DETAILS ); j < numDetails; ++j ) {
                final int orderId = i;
                int productId = this.random.next( FIRST_PRODUCT_ID, this.lastProductId );

                while ( prodIds.contains( productId ) ) {
                    productId = this.random.next( FIRST_PRODUCT_ID, this.lastProductId );
                }

                prodIds.add( productId );

                final int quantity = this.random.next( 1, MAX_QUANTITY );
                final float msrp = this.random.nextPrice( MIN_PRICE, MAX_PRICE );
                final int discount = this.random.next( MIN_DISCOUNT, MAX_DISCOUNT );

                final String detailsDdl = String.format( INSERT_ORDER_DETAIL_PATTERN, orderId, productId, quantity,
                                                         msrp, discount );
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

            final String orderDdl = String.format( INSERT_ORDER_PATTERN, id, customerId,
                                                   DATE_FORMATTER.format( orderDate ) );
            ddl.append( orderDdl ).append( '\n' );
        }
    }

    private void generateProducts( final StringBuilder ddl ) throws Exception {
        ddl.append( "\n--" ).append( PRODUCT_TABLE.substring( 1, ( PRODUCT_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_PRODUCT_ID, limit = ( FIRST_PRODUCT_ID + NUM_PRODUCTS ); i < limit; i += DataProvider
                .getSizes().size() ) {
            final String name = nextProduct();
            final String vendor = nextVendor();
            final String description = generateDescription( name, vendor );
            final String buyPrice = nextPrice();
            final String msrp = nextPrice();
            final int departmentCode = this.random.next( FIRST_DEPT, LAST_DEPT );
            int j = 0;

            // create a product for each size
            for ( final String size : DataProvider.getSizes() ) {
                this.lastProductId = ( i + j++ );
                final String productDdl = String.format( INSERT_PRODUCT_PATTERN, this.lastProductId, name, size, vendor,
                                                         description, buyPrice, msrp, departmentCode );
                ddl.append( productDdl ).append( '\n' );
            }
        }
    }

    private void generatePromotions( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( PROMOTION_TABLE.substring( 1, ( PROMOTION_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        for ( int i = FIRST_PROMOTION_ID, limit = ( FIRST_PROMOTION_ID + NUM_PROMOTIONS ); i < limit; ++i ) {
            final int id = i;
            final int productId = this.random.next( FIRST_PRODUCT_ID, this.lastProductId );
            final int discount = this.random.next( MIN_DISCOUNT, MAX_DISCOUNT );

            final String productDdl = String.format( INSERT_PROMOTION_PATTERN, id, productId, discount );
            ddl.append( productDdl ).append( '\n' );
        }
    }

    private void generateStores( final StringBuilder ddl ) throws Exception {
        ddl.append( "\n--" ).append( STORE_TABLE.substring( 1, ( STORE_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_STORE_ID, limit = ( FIRST_STORE_ID + NUM_STORES ); i < limit; ++i ) {
            final City place = nextCity();
            final int id = i;
            final String addressLine1 = nextAddressLine1();
            final String city = place.getCity();
            final State state = place.getState();
            final String phone = nextPhoneNumber( state );
            final String postalCode = place.getPostalCode();

            final String storeDdl = String.format( INSERT_STORE_PATTERN, id, phone, addressLine1, city, state.getName(),
                                                   postalCode );
            ddl.append( storeDdl ).append( '\n' );
        }
    }

    private String nextAddressLine1() throws Exception {
        final int number = this.random.next( 1, 900 );
        final String street = nextStreet();
        final String suffix = this.random.next( DataProvider.getStreetSuffixes() );
        return ( number + " " + street + ' ' + suffix );
    }

    private int nextAreaCode( final State state ) throws Exception {
        return this.random.next( DataProvider.getAreaCodes( state.getAbbreviation() ) );
    }

    private City nextCity() throws Exception {
        return this.random.next( DataProvider.getCities() );
    }

    private String nextName() throws Exception {
        final boolean female = this.random.next();
        String first = ( female ? this.random.next( DataProvider.getFemaleNames() )
                                : this.random.next( DataProvider.getMaleNames() ) );
        String last = this.random.next( DataProvider.getLastNames() );
        String name = ( first + ' ' + last );

        while ( this.names.containsKey( name ) ) {
            first = ( female ? this.random.next( DataProvider.getFemaleNames() )
                             : this.random.next( DataProvider.getMaleNames() ) );
            last = this.random.next( DataProvider.getLastNames() );
            name = ( first + ' ' + last );
        }

        this.names.put( name, null );
        return name;
    }

    private String nextPhoneNumber( final State state ) throws Exception {
        final StringBuilder builder = new StringBuilder();
        builder.append( '(' ).append( nextAreaCode( state ) ).append( ')' );

        for ( int i = 0; i < 3; ++i ) {
            builder.append( this.random.next( 0, 9 ) );
        }

        builder.append( '-' );

        for ( int i = 0; i < 4; ++i ) {
            builder.append( this.random.next( 0, 9 ) );
        }

        return builder.toString();
    }

    private String nextPrice() {
        final float price = this.random.nextPrice( MIN_PRICE, MAX_PRICE );
        return String.format( "%.2f", price );
    }

    private String nextProduct() throws Exception {
        return this.random.next( DataProvider.getProducts() );
    }

    private String nextStreet() throws Exception {
        return this.random.next( DataProvider.getStreets() );
    }

    private String nextVendor() throws Exception {
        return this.random.next( DataProvider.getVendors() );
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
            builder.append( '\n' );
        }

        builder.append( '\n' );
    }

}
