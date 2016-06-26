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

    private static final String POSTGRES_OUTPUT_FILE = "../persistance/postgres.sql";
    private static final String REMOTE_OUTPUT_FILE = "../persistance/remote.sql";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s CASCADE;";

    private static final Timestamp FIRST_ORDER_DATE = Timestamp.valueOf( LocalDateTime.of( 2010, 1, 1, 1, 1 ) );
    private static final Timestamp LAST_ORDER_DATE = new Timestamp( Instant.now().toEpochMilli() );

    private static final int MIN_DISCOUNT = 10;
    private static final int MAX_DISCOUNT = 50;

    private static final float MIN_PRICE = 1F;
    private static final float MAX_PRICE = 100.0F;

    private static final int MAX_ORDER_DETAILS = 7;
    private static final int MAX_QUANTITY = 5;

    private static final int FIRST_DEPT = 1000;
    private static final int LAST_DEPT = 1005;

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
    private static final String INSERT_STORE = "INSERT INTO " + STORE_TABLE + " ( " + STORE_COLUMNS
                                               + ") VALUES ( %s, '%s', '%s', '%s', '%s', '%s' );";

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
    private static final String INSERT_CUSTOMER = "INSERT INTO " + CUSTOMER_TABLE + " ( " + CUSTOMER_COLUMNS
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
    private static final String PG_INSERT_PRODUCT = "INSERT INTO " + PRODUCT_TABLE + " ( " + PRODUCT_COLUMNS
                                                    + " ) VALUES ( %s, '%s', '%s', '%s', '%s', %s, %s, %s );";
    private static final String REMOTE_INSERT_PRODUCT = "INSERT INTO Product ( "
                                                        + "\"productCode\", \"productName\", \"productSize\", \"productVendor\", "
                                                        + "\"productDescription\", \"buyPrice\", \"MSRP\", \"departmentCode\""
                                                        + ") VALUES ( "
                                                        + "%s, '%s', '%s', '%s', '%s', %s, %s, %s )\ngo";

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
    private static final String PG_INSERT_INVENTORY = "INSERT INTO " + INVENTORY_TABLE + " ( " + INVENTORY_COLUMNS
                                                      + " ) VALUES ( %s, %s, %s );";
    private static final String REMOTE_INSERT_INVENTORY = "INSERT INTO " + INVENTORY_TABLE + " ( " + INVENTORY_COLUMNS
                                                          + " ) VALUES ( %s, %s, %s )\ngo";

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
    private static final String INSERT_ORDER = "INSERT INTO " + ORDER_TABLE + " ( " + ORDER_COLUMNS
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
    private static final String INSERT_ORDER_DETAIL = "INSERT INTO " + ORDER_DETAIL_TABLE + " ( " + ORDER_DETAIL_COLUMNS
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
    private static final String INSERT_PROMOTION = "INSERT INTO " + PROMOTION_TABLE + " ( " + PROMOTION_COLUMNS
                                                   + " ) VALUES ( %s, %s, %s );";

    private static final String[] DROP_TABLES = new String[] { PROMOTION_TABLE,
                                                          ORDER_DETAIL_TABLE,
                                                          ORDER_TABLE,
                                                          INVENTORY_TABLE,
                                                          PRODUCT_TABLE,
                                                          CUSTOMER_TABLE,
                                                          STORE_TABLE };
    private static final String[] CREATE_TABLES = new String[] { CREATE_STORE_TABLE,
                                                                 CREATE_CUSTOMER_TABLE,
                                                                 CREATE_PRODUCT_TABLE,
                                                                 CREATE_INVENTORY_TABLE,
                                                                 CREATE_ORDER_TABLE,
                                                                 CREATE_ORDER_DETAIL_TABLE,
                                                                 CREATE_PROMOTION_TABLE };

    public static void main( final String[] args ) {
        final long start = System.currentTimeMillis();

        try {
            final IotPostgresDdlGenerator generator = new IotPostgresDdlGenerator();
            generator.generateDdl();

            {// write out Postgres DDL
                final Path output = Paths.get( POSTGRES_OUTPUT_FILE );
                Files.write( output, generator.getPostgresDdl().getBytes() );
            }

            { // write out remote.sql file
                final Path output = Paths.get( REMOTE_OUTPUT_FILE );
                Files.write( output, generator.getRemoteDdl().getBytes() );
            }

            System.out.println( "Finished generating and writing DDL in " + ( System.currentTimeMillis() - start )
                                + "ms" );
        } catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    private int firstCustId;
    private int lastCustId;
    private int lastProductId;
    private final RandomGenerator random;
    private final StringBuilder remoteDdl = new StringBuilder();
    private final StringBuilder postgresDdl = new StringBuilder();

    IotPostgresDdlGenerator() throws Exception {
        this.random = new RandomGenerator();
    }

    private void generateCustomers( final StringBuilder ddl ) throws Exception {
        ddl.append( "\n--" ).append( CUSTOMER_TABLE.substring( 1, ( CUSTOMER_TABLE.length() - 1 ) ) ).append( "\n\n" );

        // using customer ID and customer name from this file
        final Path input = Paths.get( "../redhatsapiot/customers.dat" );
        final String content = new String( Files.readAllBytes( input ) );
        int custId = -1;
        boolean firstTime = true;

        for ( final String line : content.split( "\n" ) ) {
            final String[] tokens = line.split( "\\|" );
            custId = Integer.parseInt( tokens[ 0 ] );
            final String name = tokens[ 1 ];
            final City place = nextCity();
            final String addressLine1 = nextAddressLine1();
            final String city = place.getCity();
            final State state = place.getState();
            final String phone = nextPhoneNumber( state );
            final String postalCode = place.getPostalCode();
            final double creditLimit = this.random.next( 1000, 10000 );

            final String customerDdl = String.format( INSERT_CUSTOMER, toDdl( custId ), toDdl( name ), toDdl( phone ),
                                                      toDdl( addressLine1 ), toDdl( city ), toDdl( state.getName() ),
                                                      toDdl( postalCode ), toDdl( creditLimit ) );
            ddl.append( customerDdl ).append( '\n' );

            if ( firstTime ) {
                this.firstCustId = custId;
                firstTime = false;
            }
        }

        this.lastCustId = custId;
    }

    private void generateDdl() throws Exception {
        System.out.print( "Generating hard-coded remote SQL DDL ... " );
        final Path input = Paths.get( "resources/remote.sql.txt" );
        final String content = new String( Files.readAllBytes( input ) );

        for ( final String line : content.split( "\n" ) ) {
            this.remoteDdl.append( line ).append( '\n' );
        }

        this.remoteDdl.append( '\n' );
        System.out.println( "done." );

        System.out.print( "Generating drop Postgres tables ... " );
        writeDropTables( this.postgresDdl );
        System.out.println( "done." );

        System.out.print( "Generating create Postgres tables ... " );
        writeCreateTables( this.postgresDdl );
        System.out.println( "done." );

        System.out.print( "Generating Postgres insert stores DDL ... " );
        generateStores( this.postgresDdl );
        System.out.println( "done." );
        System.out.println( "\tNumber of stores = " + NUM_STORES );

        System.out.print( "Generating insert Postgres customers DDL ... " );
        generateCustomers( this.postgresDdl );
        System.out.println( "done." );
        System.out.println( "\tNumber of customers = " + ( this.lastCustId - this.firstCustId + 1 ) );

        System.out.print( "Generating insert Postgres and remote products DDL ... " );
        generateProducts();
        System.out.println( "done." );
        System.out.println( "\tNumber of products = " + NUM_PRODUCTS );

        System.out.print( "Generating insert Posgres and remote inventory DDL ... " );
        generateInventory();
        System.out.println( "done." );
        System.out.println( "\tNumber of inventory = " + NUM_INVENTORY );

        System.out.print( "Generating insert Postgres promotions DDL ... " );
        generatePromotions( this.postgresDdl );
        System.out.println( "done." );
        System.out.println( "\tNumber of promotions = " + NUM_PROMOTIONS );

        System.out.print( "Generating insert Postgres orders DDL ... " );
        generateOrders( this.postgresDdl );
        System.out.println( "done." );
        System.out.println( "\tNumber of orders = " + NUM_ORDERS );

        System.out.print( "Generating insert Postgres order details DDL ... " );
        generateOrderDetails( this.postgresDdl );
        System.out.println( "done." );
        System.out.println( "\tNumber of order details = " + NUM_ORDER_DETAILS );

        System.out.print( "Generating commit statements for Postgres and remote ... " );
        this.postgresDdl.append( "\n\ncommit;\n\n" );
        this.remoteDdl.append( "\ncommit work\n" );
        this.remoteDdl.append( "go\n" );
        System.out.println( "done." );
    }

    private String generateDescription( final String name,
                                        final String vendor ) {
        return ( "A " + name + " by manufacturer " + vendor );
    }

    private void generateInventory() {
        this.postgresDdl.append( "\n--" ).append( INVENTORY_TABLE.substring( 1, ( INVENTORY_TABLE.length() - 1 ) ) )
                .append( "\n\n" );
        this.remoteDdl.append( "/* Data for Inventory table */\n\n" );

        // pick one store's inventory to also write to remote.sql
        final int remoteStore = this.random.next( FIRST_STORE_ID, ( ( FIRST_STORE_ID + NUM_STORES ) - 1 ) );

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
            final String pgInventoryDdl = String.format( PG_INSERT_INVENTORY, toDdl( storeId ), toDdl( productId ),
                                                         toDdl( quantity ) );
            this.postgresDdl.append( pgInventoryDdl ).append( '\n' );

            // write to remote.sql if necessary
            if ( remoteStore == storeId ) {
                final String remoteInventoryDdl = String.format( REMOTE_INSERT_INVENTORY, toDdl( storeId ),
                                                                 toDdl( productId ), toDdl( quantity ) );
                this.remoteDdl.append( remoteInventoryDdl ).append( '\n' );
            }
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

                final String detailsDdl = String.format( INSERT_ORDER_DETAIL, toDdl( orderId ), toDdl( productId ),
                                                         toDdl( quantity ), toDdl( msrp ), toDdl( discount ) );
                ddl.append( detailsDdl ).append( '\n' );
            }
        }
    }

    private void generateOrders( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( ORDER_TABLE.substring( 1, ( ORDER_TABLE.length() - 1 ) ) ).append( "\n\n" );

        for ( int i = FIRST_ORDER_ID, limit = ( FIRST_ORDER_ID + NUM_ORDERS ); i < limit; ++i ) {
            final int id = i;
            final Timestamp orderDate = this.random.next( FIRST_ORDER_DATE, LAST_ORDER_DATE );
            final int customerId = this.random.next( this.firstCustId, this.lastCustId );

            final String orderDdl = String.format( INSERT_ORDER, toDdl( id ), toDdl( customerId ),
                                                   toDdl( DATE_FORMATTER.format( orderDate ) ) );
            ddl.append( orderDdl ).append( '\n' );
        }
    }

    private void generateProducts() throws Exception {
        this.postgresDdl.append( "\n--" ).append( PRODUCT_TABLE.substring( 1, ( PRODUCT_TABLE.length() - 1 ) ) )
                .append( "\n\n" );
        this.remoteDdl.append( "/* Data for Product table */\n\n" );

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

                { // insert into postres DDL
                    final String pgProductDdl = String.format( PG_INSERT_PRODUCT, toDdl( this.lastProductId ),
                                                               toDdl( name ), toDdl( size ), toDdl( vendor ),
                                                               toDdl( description ), toDdl( buyPrice ), toDdl( msrp ),
                                                               toDdl( departmentCode ) );
                    this.postgresDdl.append( pgProductDdl ).append( '\n' );
                }

                { // insert into remote DDL
                    final String remoteProductDdl = String.format( REMOTE_INSERT_PRODUCT, toDdl( this.lastProductId ),
                                                                   toDdl( name ), toDdl( size ), toDdl( vendor ),
                                                                   toDdl( description ), toDdl( buyPrice ),
                                                                   toDdl( msrp ), toDdl( departmentCode ) );
                    this.remoteDdl.append( remoteProductDdl ).append( '\n' );
                }
            }
        }

        this.remoteDdl.append( '\n' );
    }

    private void generatePromotions( final StringBuilder ddl ) {
        ddl.append( "\n--" ).append( PROMOTION_TABLE.substring( 1, ( PROMOTION_TABLE.length() - 1 ) ) )
                .append( "\n\n" );

        for ( int i = FIRST_PROMOTION_ID, limit = ( FIRST_PROMOTION_ID + NUM_PROMOTIONS ); i < limit; ++i ) {
            final int id = i;
            final int productId = this.random.next( FIRST_PRODUCT_ID, this.lastProductId );
            final int discount = this.random.next( MIN_DISCOUNT, MAX_DISCOUNT );

            final String productDdl = String.format( INSERT_PROMOTION, toDdl( id ), toDdl( productId ),
                                                     toDdl( discount ) );
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

            final String storeDdl = String.format( INSERT_STORE, toDdl( id ), toDdl( phone ), toDdl( addressLine1 ),
                                                   toDdl( city ), toDdl( state.getName() ), toDdl( postalCode ) );
            ddl.append( storeDdl ).append( '\n' );
        }
    }

    public String getPostgresDdl() {
        return this.postgresDdl.toString();
    }

    public String getRemoteDdl() {
        return this.remoteDdl.toString();
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

    private Object toDdl( final Object value ) {
        if ( ( value == null ) || !( value instanceof String ) ) {
            return value;
        }

        return ( ( String ) value ).replace( "'", "''" ); // escape any single quotes
    }

    private void writeCreateTables( final StringBuilder builder ) {
        for ( final String table : CREATE_TABLES ) {
            builder.append( table );
            builder.append( "\n\n" );
        }
    }

    private void writeDropTables( final StringBuilder builder ) {
        for ( final String table : DROP_TABLES ) {
            builder.append( String.format( DROP_TABLE, table ) );
            builder.append( '\n' );
        }

        builder.append( '\n' );
    }

}
