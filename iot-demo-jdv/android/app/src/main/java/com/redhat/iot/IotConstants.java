package com.redhat.iot;

import com.redhat.iot.R.string;

import java.text.SimpleDateFormat;

/**
 * Constants used in the IoT mobile app.
 */
public interface IotConstants {

    /**
     * Constants related to preferences.
     */
    interface Prefs {

        /**
         * The name of the <code>int</code> preference that contains the customer ID.
         */
        String CUSTOMER_ID = "customer_id";

        /**
         * The default enable notifications preference value. Value is {@value}.
         */
        boolean DEFAULT_ENABLE_NOTIFICATIONS = true;

        /**
         * The default interval length in milliseconds preference value. Value is {@value}.
         */
        int DEFAULT_NOTIFICATION_INTERVAL = 60000;

        /**
         * The name of the <code>boolean</code> preference that indicates if notifications are enabled.
         */
        String ENABLE_NOTIFICATIONS = "enabled_notifications";

        /**
         * The name of the time interval in milliseconds the app checks to see if a notification is available to send to the user.
         */
        String NOTIFICATION_INTERVAL = "notification_interval";

        /**
         * The name of the preference store.
         */
        String PREFS_NAME = "IoTPrefs";

        /**
         * The name of the preference holding the department IDs of the last viewed promotions.
         */
        String PROMO_DEPT_IDS = "IoTPrefs";

        /**
         * The name of the <code>int</code> preference that contains the ID of the customer's chosen {@link
         * com.redhat.iot.domain.Store}}.
         */
        String STORE_ID = "store_id";

    }

    /**
     * The date format used in the app.
     */
    String DATE_FORMAT = "MMM dd, yyyy";

    /**
     * The shared date formatter.
     */
    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( DATE_FORMAT );

    /**
     * The first customer ID in the actual demo data.
     */
    int FIRST_CUST_ID = 10000;

    /**
     * The HANA IP address.
     */
    String HANA_IP_ADDRESS = "54.187.27.202";

    /**
     * Tag to use when logging messages.
     */
    String LOG_TAG = "IoT_App";

    /**
     * The group name that all IoT notifications are sent under.
     */
    String NOTIFIER_GROUP = IotApp.getContext().getString( string.app_name );

    interface TestData {

        String NOTIFICATIONS = "{ \"d\" : [ { \"id\" : 1 } ] }";

        String STORE_9001_JSON =
            "{ \"id\" : 9001, \"addressLine1\" : \"1 Lennon Lane\", \"city\" : \"Lennon\", \"state\" : \"Michigan\", " +
                "\"postalCode\" : \"48449\", \"phone\": \"(800)101-5645\" }";

        String STORE_9002_JSON =
            "{ \"id\" : 9002, \"addressLine1\" : \"2 McCartney Way\", \"city\" : \"McCammon\", \"state\" : \"Idaho\", " +
                "\"postalCode\" : \"83250\", \"phone\": \"(800)202-5645\" }";

        String STORE_9003_JSON =
            "{ \"id\" : 9003, \"addressLine1\" : \"3 Harrison Road\", \"city\" : \"Harrison\", \"state\" : \"Maine\", " +
                "\"postalCode\" : \"04040\", \"phone\": \"(800)303-5645\" }";

        String STORE_9004_JSON =
            "{ \"id\" : 9004, \"addressLine1\" : \"4 Starkey Street\", \"city\" : \"Starr\", \"state\" : \"South Carolina\", " +
                "\"postalCode\" : \"29684\", \"phone\": \"(800)404-5645\" }";

        String STORE_9005_JSON =
            "{ \"id\" : 9005, \"addressLine1\" : \"5 Best Place\", \"city\" : \"Veribest\", \"state\" : \"Texas\", " +
                "\"postalCode\" : \"76886\", \"phone\": \"(800)505-5645\" }";

        String STORES_JSON = "{ d: { \"results\": [ "
            + STORE_9001_JSON + ','
            + STORE_9002_JSON + ','
            + STORE_9003_JSON + ','
            + STORE_9004_JSON + ','
            + STORE_9005_JSON
            + " ] } }";

        int DEPT_1_ID = 1000;
        String DEPT_1_JSON =
            "{ \"departmentCode\" : \"" + DEPT_1_ID + "\", \"departmentName\" : \"Womans\", \"departmentDescription\" : \"Womans " +
                "clothing and assessories.\" }";

        int DEPT_2_ID = 1001;
        String DEPT_2_JSON =
            "{ \"departmentCode\" : \"" + DEPT_2_ID + "\", \"departmentName\" : \"Boys\", \"departmentDescription\" : \"Boys " +
                "clothing and assessories.\" }";

        int DEPT_3_ID = 1002;
        String DEPT_3_JSON =
            "{ \"departmentCode\" : \"" + DEPT_3_ID + "\", \"departmentName\" : \"Girls\", \"departmentDescription\" : \"Girls " +
                "clothing and assessories.\" }";

        int DEPT_4_ID = 1003;
        String DEPT_4_JSON =
            "{ \"departmentCode\" : \"" + DEPT_4_ID + "\", \"departmentName\" : \"Mens\", \"departmentDescription\" : \"Mens " +
                "clothing and assessories.\" }";

        int DEPT_5_ID = 1004;
        String DEPT_5_JSON =
            "{ \"departmentCode\" : \"" + DEPT_5_ID + "\", \"departmentName\" : \"Formal\", \"departmentDescription\" : \"Formal " +
                "wear for men and women.\" }";

        int DEPT_6_ID = 1005;
        String DEPT_6_JSON =
            "{ \"departmentCode\" : \"" + DEPT_6_ID + "\", \"departmentName\" : \"Sport\", \"departmentDescription\" : \"Sports " +
                "wear for men and women.\" }";

        String DEPARTMENTS_JSON = "{ d: { \"results\": [ "
            + DEPT_1_JSON + ','
            + DEPT_2_JSON + ','
            + DEPT_3_JSON + ','
            + DEPT_4_JSON + ','
            + DEPT_5_JSON + ','
            + DEPT_6_JSON
            + " ] } }";

        int ELVIS_ID = 10000;
        int RINGO_ID = 10001;
        int SLEDGE_ID = 10002;

        String PRODUCT_100_JSON = "{\"id\" : 100, "
            + "\"departmentCode\" : " + DEPT_1_ID + ", "
            + "\"productDescription\" : \"Boy's socks, Ages 1-3\", "
            + "\"productName\" : \"Socks\", "
            + "\"msrp\" : 1.99, "
            + "\"buyPrice\" : 1.00 }";

        String PRODUCT_101_JSON = "{\"id\" : 101, "
            + "\"departmentCode\" : " + DEPT_1_ID + ", "
            + "\"productDescription\" : \"Boy's shirt, Size 12\", "
            + "\"productName\" : \"Dress Shirt\", "
            + "\"msrp\" : 10.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_102_JSON = "{\"id\" : 102, "
            + "\"departmentCode\" : " + DEPT_1_ID + ", "
            + "\"productDescription\" : \"Boy's shoes, Size 11\", "
            + "\"productName\" : \"Tennis Shoes\", "
            + "\"msrp\" : 11.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_200_JSON = "{\"id\" : 200, "
            + "\"departmentCode\" : " + DEPT_2_ID + ", "
            + "\"productDescription\" : \"Gold cuff links\", "
            + "\"productName\" : \"Cuff links\", "
            + "\"msrp\" : 5.99, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_201_JSON = "{\"id\" : 201, "
            + "\"departmentCode\" : " + DEPT_2_ID + ", "
            + "\"productDescription\" : \"Black tuxedo pants, Size 32W 32L\", "
            + "\"productName\" : \"Tuxedo Pants\", "
            + "\"msrp\" : 50.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_202_JSON = "{\"id\" : 202, "
            + "\"departmentCode\" : " + DEPT_2_ID + ", "
            + "\"productDescription\" : \"White tuxedo shirt, Size 15\", "
            + "\"productName\" : \"Tuxedo Shirt\", "
            + "\"msrp\" : 55.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_300_JSON = "{\"id\" : 300, "
            + "\"departmentCode\" : " + DEPT_3_ID + ", "
            + "\"productDescription\" : \"Girl's socks, Ages 5-10\", "
            + "\"productName\" : \"Socks\", "
            + "\"msrp\" : 3.99, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_301_JSON = "{\"id\" : " + 301 + ", "
            + "\"departmentCode\" : " + DEPT_3_ID + ", "
            + "\"productDescription\" : \"Girl's dress, White, Full length\", "
            + "\"productName\" : \"Summer Dress\", "
            + "\"msrp\" : 30.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_302_JSON = "{\"id\" : 302, "
            + "\"departmentCode\" : " + DEPT_3_ID + ", "
            + "\"productDescription\" : \"Girl's outfit, Shirt and Slacks\", "
            + "\"productName\" : \"Outfit\", "
            + "\"msrp\" : 33.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_400_JSON = "{\"id\" : 400, "
            + "\"departmentCode\" : " + DEPT_4_ID + ", "
            + "\"productDescription\" : \"Men's tie, Black, Thin\", "
            + "\"productName\" : \"Tie\", "
            + "\"msrp\" : 4.99, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_401_JSON = "{\"id\" : 401, "
            + "\"departmentCode\" : " + DEPT_4_ID + ", "
            + "\"productDescription\" : \"Men's dress pants, Size 34W 33L\", "
            + "\"productName\" : \"Dress Pants\", "
            + "\"msrp\" : 40.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_402_JSON = "{\"id\" : 402, "
            + "\"departmentCode\" : " + DEPT_4_ID + ", "
            + "\"productDescription\" : \"Men's sport coat, Winter, Wool3\", "
            + "\"productName\" : \"Sport Coat\", "
            + "\"msrp\" : 44.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_500_JSON = "{\"id\" : 500, "
            + "\"departmentCode\" : " + DEPT_5_ID + ", "
            + "\"productDescription\" : \"Swim suit, Women's, 2 piece\", "
            + "\"productName\" : \"Swim Suit\", "
            + "\"msrp\" : 5.99, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_501_JSON = "{\"id\" : 501, "
            + "\"departmentCode\" : " + DEPT_5_ID + ", "
            + "\"productDescription\" : \"Jogging pants, Cleveland Cavaliers\", "
            + "\"productName\" : \"Jogging Pants\", "
            + "\"msrp\" : 50.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_502_JSON = "{\"id\" : 502, "
            + "\"departmentCode\" : " + DEPT_5_ID + ", "
            + "\"productDescription\" : \"St Louis Blues Jersey, Wayne Gretsky\", "
            + "\"productName\" : \"Team Jersey\", "
            + "\"msrp\" : 55.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_600_JSON = "{\"id\" : 600, "
            + "\"departmentCode\" : " + DEPT_6_ID + ", "
            + "\"productDescription\" : \"Woman's scarf, Multi-colored\", "
            + "\"productName\" : \"Scarf\", "
            + "\"msrp\" : 6.99, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_601_JSON = "{\"id\" : 601, "
            + "\"departmentCode\" : " + DEPT_6_ID + ", "
            + "\"productDescription\" : \"Woman's summer dress, Peach\", "
            + "\"productName\" : \"Dress\", "
            + "\"msrp\" : 60.50, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCT_602_JSON = "{\"id\" : 602, "
            + "\"departmentCode\" : " + DEPT_6_ID + ", "
            + "\"productDescription\" : \"Woman's cowboy boots\", "
            + "\"productName\" : \"Boots\", "
            + "\"msrp\" : 66.00, "
            + "\"buyPrice\" : 1.00}";

        String PRODUCTS_JSON = "{ d: { \"results\": [ "
            + PRODUCT_100_JSON + ','
            + PRODUCT_101_JSON + ','
            + PRODUCT_102_JSON + ','
            + PRODUCT_200_JSON + ','
            + PRODUCT_201_JSON + ','
            + PRODUCT_202_JSON + ','
            + PRODUCT_300_JSON + ','
            + PRODUCT_301_JSON + ','
            + PRODUCT_302_JSON + ','
            + PRODUCT_400_JSON + ','
            + PRODUCT_401_JSON + ','
            + PRODUCT_402_JSON + ','
            + PRODUCT_500_JSON + ','
            + PRODUCT_501_JSON + ','
            + PRODUCT_502_JSON + ','
            + PRODUCT_600_JSON + ','
            + PRODUCT_601_JSON + ','
            + PRODUCT_602_JSON
            + " ] } }";

        String INVENTORY_9001_100_JSON = "{\"storeId\" : 9001, \"productId\" : \"100\", \"quantity\" : \"100\"}";
        String INVENTORY_9001_101_JSON = "{\"storeId\" : 9001, \"productId\" : \"101\", \"quantity\" : \"101\"}";
        String INVENTORY_9001_102_JSON = "{\"storeId\" : 9001, \"productId\" : \"102\", \"quantity\" : \"102\"}";
        String INVENTORY_9001_200_JSON = "{\"storeId\" : 9001, \"productId\" : \"200\", \"quantity\" : \"200\"}";
        String INVENTORY_9001_201_JSON = "{\"storeId\" : 9001, \"productId\" : \"201\", \"quantity\" : \"201\"}";
        String INVENTORY_9001_202_JSON = "{\"storeId\" : 9001, \"productId\" : \"202\", \"quantity\" : \"202\"}";
        String INVENTORY_9001_300_JSON = "{\"storeId\" : 9001, \"productId\" : \"200\", \"quantity\" : \"300\"}";
        String INVENTORY_9001_301_JSON = "{\"storeId\" : 9001, \"productId\" : \"201\", \"quantity\" : \"301\"}";
        String INVENTORY_9001_302_JSON = "{\"storeId\" : 9001, \"productId\" : \"202\", \"quantity\" : \"302\"}";
        String INVENTORY_9001_400_JSON = "{\"storeId\" : 9001, \"productId\" : \"200\", \"quantity\" : \"400\"}";
        String INVENTORY_9001_401_JSON = "{\"storeId\" : 9001, \"productId\" : \"201\", \"quantity\" : \"401\"}";
        String INVENTORY_9001_402_JSON = "{\"storeId\" : 9001, \"productId\" : \"202\", \"quantity\" : \"402\"}";
        String INVENTORY_9001_500_JSON = "{\"storeId\" : 9001, \"productId\" : \"200\", \"quantity\" : \"500\"}";
        String INVENTORY_9001_501_JSON = "{\"storeId\" : 9001, \"productId\" : \"201\", \"quantity\" : \"501\"}";
        String INVENTORY_9001_502_JSON = "{\"storeId\" : 9001, \"productId\" : \"202\", \"quantity\" : \"502\"}";
        String INVENTORY_9001_600_JSON = "{\"storeId\" : 9001, \"productId\" : \"200\", \"quantity\" : \"600\"}";
        String INVENTORY_9001_601_JSON = "{\"storeId\" : 9001, \"productId\" : \"201\", \"quantity\" : \"601\"}";
        String INVENTORY_9001_602_JSON = "{\"storeId\" : 9001, \"productId\" : \"202\", \"quantity\" : \"602\"}";

        String STORE_9001_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_100_JSON + ','
            + INVENTORY_9001_101_JSON + ','
            + INVENTORY_9001_102_JSON + ','
            + INVENTORY_9001_200_JSON + ','
            + INVENTORY_9001_201_JSON + ','
            + INVENTORY_9001_202_JSON + ','
            + INVENTORY_9001_300_JSON + ','
            + INVENTORY_9001_301_JSON + ','
            + INVENTORY_9001_302_JSON + ','
            + INVENTORY_9001_400_JSON + ','
            + INVENTORY_9001_401_JSON + ','
            + INVENTORY_9001_402_JSON + ','
            + INVENTORY_9001_500_JSON + ','
            + INVENTORY_9001_501_JSON + ','
            + INVENTORY_9001_502_JSON + ','
            + INVENTORY_9001_600_JSON + ','
            + INVENTORY_9001_601_JSON + ','
            + INVENTORY_9001_602_JSON
            + " ] } }";

        String INVENTORY_9002_100_JSON = "{\"storeId\" : 9002, \"productId\" : \"100\", \"quantity\" : \"1001\"}";
        String INVENTORY_9002_102_JSON = "{\"storeId\" : 9002, \"productId\" : \"102\", \"quantity\" : \"1022\"}";
        String INVENTORY_9002_201_JSON = "{\"storeId\" : 9002, \"productId\" : \"201\", \"quantity\" : \"2013\"}";
        String INVENTORY_9002_300_JSON = "{\"storeId\" : 9002, \"productId\" : \"200\", \"quantity\" : \"3004\"}";
        String INVENTORY_9002_302_JSON = "{\"storeId\" : 9002, \"productId\" : \"202\", \"quantity\" : \"3025\"}";
        String INVENTORY_9002_401_JSON = "{\"storeId\" : 9002, \"productId\" : \"201\", \"quantity\" : \"4016\"}";
        String INVENTORY_9002_500_JSON = "{\"storeId\" : 9002, \"productId\" : \"200\", \"quantity\" : \"5007\"}";
        String INVENTORY_9002_502_JSON = "{\"storeId\" : 9002, \"productId\" : \"202\", \"quantity\" : \"5028\"}";
        String INVENTORY_9002_601_JSON = "{\"storeId\" : 9002, \"productId\" : \"201\", \"quantity\" : \"6019\"}";

        String STORE_9002_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9002_100_JSON + ','
            + INVENTORY_9002_102_JSON + ','
            + INVENTORY_9002_201_JSON + ','
            + INVENTORY_9002_300_JSON + ','
            + INVENTORY_9002_302_JSON + ','
            + INVENTORY_9002_401_JSON + ','
            + INVENTORY_9002_500_JSON + ','
            + INVENTORY_9002_502_JSON + ','
            + INVENTORY_9002_601_JSON
            + " ] } }";

        String INVENTORY_9003_100_JSON = "{\"storeId\" : 9003, \"productId\" : \"100\", \"quantity\" : \"100\"}";
        String INVENTORY_9003_101_JSON = "{\"storeId\" : 9003, \"productId\" : \"101\", \"quantity\" : \"200\"}";
        String INVENTORY_9003_102_JSON = "{\"storeId\" : 9003, \"productId\" : \"102\", \"quantity\" : \"300\"}";
        String INVENTORY_9003_200_JSON = "{\"storeId\" : 9003, \"productId\" : \"200\", \"quantity\" : \"400\"}";
        String INVENTORY_9003_201_JSON = "{\"storeId\" : 9003, \"productId\" : \"201\", \"quantity\" : \"500\"}";
        String INVENTORY_9003_202_JSON = "{\"storeId\" : 9003, \"productId\" : \"202\", \"quantity\" : \"600\"}";
        String INVENTORY_9003_300_JSON = "{\"storeId\" : 9003, \"productId\" : \"200\", \"quantity\" : \"700\"}";
        String INVENTORY_9003_301_JSON = "{\"storeId\" : 9003, \"productId\" : \"201\", \"quantity\" : \"800\"}";
        String INVENTORY_9003_302_JSON = "{\"storeId\" : 9003, \"productId\" : \"202\", \"quantity\" : \"900\"}";
        String INVENTORY_9003_400_JSON = "{\"storeId\" : 9003, \"productId\" : \"200\", \"quantity\" : \"1000\"}";
        String INVENTORY_9003_401_JSON = "{\"storeId\" : 9003, \"productId\" : \"201\", \"quantity\" : \"1010\"}";
        String INVENTORY_9003_402_JSON = "{\"storeId\" : 9003, \"productId\" : \"202\", \"quantity\" : \"1020\"}";

        String STORE_9003_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9003_100_JSON + ','
            + INVENTORY_9003_101_JSON + ','
            + INVENTORY_9003_102_JSON + ','
            + INVENTORY_9003_200_JSON + ','
            + INVENTORY_9003_201_JSON + ','
            + INVENTORY_9003_202_JSON + ','
            + INVENTORY_9003_300_JSON + ','
            + INVENTORY_9003_301_JSON + ','
            + INVENTORY_9003_302_JSON + ','
            + INVENTORY_9003_400_JSON + ','
            + INVENTORY_9003_401_JSON + ','
            + INVENTORY_9003_402_JSON
            + " ] } }";

        String INVENTORY_9004_100_JSON = "{\"storeId\" : 9004, \"productId\" : \"100\", \"quantity\" : \"10\"}";
        String INVENTORY_9004_101_JSON = "{\"storeId\" : 9004, \"productId\" : \"101\", \"quantity\" : \"20\"}";
        String INVENTORY_9004_102_JSON = "{\"storeId\" : 9004, \"productId\" : \"102\", \"quantity\" : \"30\"}";
        String INVENTORY_9004_200_JSON = "{\"storeId\" : 9004, \"productId\" : \"200\", \"quantity\" : \"40\"}";
        String INVENTORY_9004_201_JSON = "{\"storeId\" : 9004, \"productId\" : \"201\", \"quantity\" : \"50\"}";
        String INVENTORY_9004_202_JSON = "{\"storeId\" : 9004, \"productId\" : \"202\", \"quantity\" : \"60\"}";
        String INVENTORY_9004_300_JSON = "{\"storeId\" : 9004, \"productId\" : \"200\", \"quantity\" : \"70\"}";
        String INVENTORY_9004_301_JSON = "{\"storeId\" : 9004, \"productId\" : \"201\", \"quantity\" : \"80\"}";
        String INVENTORY_9004_302_JSON = "{\"storeId\" : 9004, \"productId\" : \"202\", \"quantity\" : \"90\"}";

        String STORE_9004_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9004_100_JSON + ','
            + INVENTORY_9004_101_JSON + ','
            + INVENTORY_9004_102_JSON + ','
            + INVENTORY_9004_200_JSON + ','
            + INVENTORY_9004_201_JSON + ','
            + INVENTORY_9004_202_JSON + ','
            + INVENTORY_9004_300_JSON + ','
            + INVENTORY_9004_301_JSON + ','
            + INVENTORY_9004_302_JSON
            + " ] } }";

        String INVENTORY_9005_100_JSON = "{\"storeId\" : 9005, \"productId\" : \"100\", \"quantity\" : \"1\"}";
        String INVENTORY_9005_101_JSON = "{\"storeId\" : 9005, \"productId\" : \"101\", \"quantity\" : \"2\"}";
        String INVENTORY_9005_102_JSON = "{\"storeId\" : 9005, \"productId\" : \"102\", \"quantity\" : \"3\"}";

        String STORE_9005_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9005_100_JSON + ','
            + INVENTORY_9005_101_JSON + ','
            + INVENTORY_9005_102_JSON
            + " ] } }";

        String PRODUCT_100_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_100_JSON + ','
            + INVENTORY_9002_100_JSON + ','
            + INVENTORY_9003_100_JSON + ','
            + INVENTORY_9004_100_JSON + ','
            + INVENTORY_9005_100_JSON
            + " ] } }";

        String PRODUCT_101_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_101_JSON + ','
            + INVENTORY_9003_101_JSON + ','
            + INVENTORY_9004_101_JSON + ','
            + INVENTORY_9005_101_JSON
            + " ] } }";

        String PRODUCT_102_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_102_JSON + ','
            + INVENTORY_9002_102_JSON + ','
            + INVENTORY_9003_102_JSON + ','
            + INVENTORY_9004_102_JSON + ','
            + INVENTORY_9005_102_JSON
            + " ] } }";

        String PRODUCT_200_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_200_JSON + ','
            + INVENTORY_9003_200_JSON + ','
            + INVENTORY_9004_200_JSON
            + " ] } }";

        String PRODUCT_201_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_201_JSON + ','
            + INVENTORY_9002_201_JSON + ','
            + INVENTORY_9003_201_JSON + ','
            + INVENTORY_9004_201_JSON
            + " ] } }";

        String PRODUCT_202_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_202_JSON + ','
            + INVENTORY_9003_202_JSON + ','
            + INVENTORY_9004_202_JSON
            + " ] } }";

        String PRODUCT_300_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_300_JSON + ','
            + INVENTORY_9002_300_JSON + ','
            + INVENTORY_9003_300_JSON + ','
            + INVENTORY_9004_300_JSON
            + " ] } }";

        String PRODUCT_301_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_301_JSON + ','
            + INVENTORY_9003_301_JSON + ','
            + INVENTORY_9004_301_JSON
            + " ] } }";

        String PRODUCT_302_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_302_JSON + ','
            + INVENTORY_9002_302_JSON + ','
            + INVENTORY_9003_302_JSON + ','
            + INVENTORY_9004_302_JSON
            + " ] } }";

        String PRODUCT_400_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_400_JSON + ','
            + INVENTORY_9003_400_JSON
            + " ] } }";

        String PRODUCT_401_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_401_JSON + ','
            + INVENTORY_9002_401_JSON + ','
            + INVENTORY_9003_401_JSON
            + " ] } }";

        String PRODUCT_402_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_402_JSON + ','
            + INVENTORY_9003_402_JSON
            + " ] } }";

        String PRODUCT_500_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_500_JSON + ','
            + INVENTORY_9002_500_JSON
            + " ] } }";

        String PRODUCT_501_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_501_JSON + ','
            + " ] } }";

        String PRODUCT_502_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_502_JSON + ','
            + INVENTORY_9002_502_JSON
            + " ] } }";

        String PRODUCT_600_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_600_JSON + ','
            + " ] } }";

        String PRODUCT_601_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_601_JSON + ','
            + INVENTORY_9002_601_JSON
            + " ] } }";

        String PRODUCT_602_INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_602_JSON + ','
            + " ] } }";

        String INVENTORY_JSON = "{ d: { \"results\": [ "
            + INVENTORY_9001_100_JSON + ','
            + INVENTORY_9001_100_JSON + ','
            + INVENTORY_9001_101_JSON + ','
            + INVENTORY_9001_102_JSON + ','
            + INVENTORY_9001_200_JSON + ','
            + INVENTORY_9001_201_JSON + ','
            + INVENTORY_9001_202_JSON + ','
            + INVENTORY_9001_300_JSON + ','
            + INVENTORY_9001_301_JSON + ','
            + INVENTORY_9001_302_JSON + ','
            + INVENTORY_9001_400_JSON + ','
            + INVENTORY_9001_401_JSON + ','
            + INVENTORY_9001_402_JSON + ','
            + INVENTORY_9001_500_JSON + ','
            + INVENTORY_9001_501_JSON + ','
            + INVENTORY_9001_502_JSON + ','
            + INVENTORY_9001_600_JSON + ','
            + INVENTORY_9001_601_JSON + ','
            + INVENTORY_9001_602_JSON + ','
            + INVENTORY_9002_100_JSON + ','
            + INVENTORY_9002_102_JSON + ','
            + INVENTORY_9002_201_JSON + ','
            + INVENTORY_9002_300_JSON + ','
            + INVENTORY_9002_302_JSON + ','
            + INVENTORY_9002_401_JSON + ','
            + INVENTORY_9002_500_JSON + ','
            + INVENTORY_9002_502_JSON + ','
            + INVENTORY_9002_601_JSON + ','
            + INVENTORY_9003_100_JSON + ','
            + INVENTORY_9003_101_JSON + ','
            + INVENTORY_9003_102_JSON + ','
            + INVENTORY_9003_200_JSON + ','
            + INVENTORY_9003_201_JSON + ','
            + INVENTORY_9003_202_JSON + ','
            + INVENTORY_9003_300_JSON + ','
            + INVENTORY_9003_301_JSON + ','
            + INVENTORY_9003_302_JSON + ','
            + INVENTORY_9003_400_JSON + ','
            + INVENTORY_9003_401_JSON + ','
            + INVENTORY_9003_402_JSON + ','
            + INVENTORY_9004_100_JSON + ','
            + INVENTORY_9004_101_JSON + ','
            + INVENTORY_9004_102_JSON + ','
            + INVENTORY_9004_200_JSON + ','
            + INVENTORY_9004_201_JSON + ','
            + INVENTORY_9004_202_JSON + ','
            + INVENTORY_9004_300_JSON + ','
            + INVENTORY_9004_301_JSON + ','
            + INVENTORY_9004_302_JSON + ','
            + INVENTORY_9005_100_JSON + ','
            + INVENTORY_9005_101_JSON + ','
            + INVENTORY_9005_102_JSON
            + " ] } }";

        int ORDER_1010_ID = 1010;
        String DETAIL_1010_1_JSON =
            "{\"orderId\" : 1010, \"productId\" : 300, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_1010_2_JSON =
            "{\"orderId\" : 1010, \"productId\" : 301, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String DETAIL_1010_3_JSON =
            "{\"orderId\" : 1010, \"productId\" : 302, \"quantityOrdered\" : 2, \"msrp\" : 20.5, \"discount\" :" +
                " 30 }";

        String ORDER_1010_JSON = "{\"id\" : 1010, "
            + "\"customerId\" : " + ELVIS_ID + ", "
            + "\"orderDate\" : \"/Date(1463360426000)/\"}";

        int ORDER_2020_ID = 2020;
        String DETAIL_2020_1_JSON =
            "{\"orderId\" : 2020, \"productId\" : 100, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_2020_2_JSON =
            "{\"orderId\" : 2020, \"productId\" : 200, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String DETAIL_2020_3_JSON =
            "{\"orderId\" : 2020, \"productId\" : 602, \"quantityOrdered\" : 2, \"msrp\" : 20.5, \"discount\" :" +
                " 30 }";

        String DETAIL_2020_4_JSON =
            "{\"orderId\" : 2020, \"productId\" : 400, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "40 }";

        String DETAIL_2020_5_JSON =
            "{\"orderId\" : 2020, \"productId\" : 500, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 15 }";

        String DETAIL_2020_6_JSON =
            "{\"orderId\" : 2020, \"productId\" : 600, \"quantityOrdered\" : 2, \"msrp\" : 20.5, \"discount\" :" +
                " 25 }";

        String ORDER_2020_JSON = "{\"id\" : 2020, "
            + "\"customerId\" : " + RINGO_ID + ", "
            + "\"orderDate\" : \"/Date(1463360451000)/\"}";

        int ORDER_3030_ID = 3030;
        String DETAIL_3030_1_JSON =
            "{\"orderId\" : 3030, \"productId\" : 202, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_3030_2_JSON =
            "{\"orderId\" : 3030, \"productId\" : 102, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String ORDER_3030_JSON = "{\"id\" : 3030, "
            + "\"customerId\" : " + RINGO_ID + ", "
            + "\"orderDate\" : \"/Date(1419033600000)/\"}";

        int ORDER_4040_ID = 4040;
        String DETAIL_4040_1_JSON =
            "{\"orderId\" : 4040, \"productId\" : 101, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_4040_2_JSON =
            "{\"orderId\" : 4040, \"productId\" : 201, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String DETAIL_4040_3_JSON =
            "{\"orderId\" : 4040, \"productId\" : 402, \"quantityOrdered\" : 2, \"msrp\" : 20.5, \"discount\" :" +
                " 30 }";

        String ORDER_4040_JSON = "{\"id\" : 4040, "
            + "\"customerId\" : " + SLEDGE_ID + ", "
            + "\"orderDate\" : \"/Date(1419033600000)/\"}";

        int ORDER_5050_ID = 5050;
        String DETAIL_5050_1_JSON =
            "{\"orderId\" : 5050, \"productId\" : 301, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_5050_2_JSON =
            "{\"orderId\" : 5050, \"productId\" : 401, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String DETAIL_5050_3_JSON =
            "{\"orderId\" : 5050, \"productId\" : 501, \"quantityOrdered\" : 2, \"msrp\" : 20.5, \"discount\" :" +
                " 30 }";

        String ORDER_5050_JSON = "{\"id\" : 5050, "
            + "\"customerId\" : " + SLEDGE_ID + ", "
            + "\"orderDate\" : \"/Date(1419033600000)/\"}";

        int ORDER_6060_ID = 6060;
        String DETAIL_6060_1_JSON =
            "{\"orderId\" : 6060, \"productId\" : 100, \"quantityOrdered\" : 1, \"msrp\" : 5.5, \"discount\" : " +
                "10 }";

        String DETAIL_6060_2_JSON =
            "{\"orderId\" : 6060, \"productId\" : 502, \"quantityOrdered\" : 2, \"msrp\" : 10.0, \"discount\" :" +
                " 20 }";

        String ORDER_6060_JSON = "{\"id\" : 6060, "
            + "\"customerId\" : " + SLEDGE_ID + ", "
            + "\"orderDate\" : \"/Date(1419033600000)/\"}";

        String ELVIS_ORDERS_JSON = "{ d: { \"results\": [ "
            + ORDER_1010_JSON
            + " ] } }";

        String RINGO_ORDERS_JSON = "{ d: { \"results\": [ "
            + ORDER_2020_JSON + ','
            + ORDER_3030_JSON
            + " ] } }";

        String SLEDGE_ORDERS_JSON = "{ d: { \"results\": [ "
            + ORDER_4040_JSON + ','
            + ORDER_5050_JSON + ','
            + ORDER_6060_JSON
            + " ] } }";

        String ORDER_1010_DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_1010_1_JSON + ','
            + DETAIL_1010_2_JSON + ','
            + DETAIL_1010_3_JSON
            + " ] } }";

        String ORDER_2020_DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_2020_1_JSON + ','
            + DETAIL_2020_2_JSON + ','
            + DETAIL_2020_3_JSON + ','
            + DETAIL_2020_4_JSON + ','
            + DETAIL_2020_5_JSON + ','
            + DETAIL_2020_6_JSON
            + " ] } }";

        String ORDER_3030_DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_3030_1_JSON + ','
            + DETAIL_3030_2_JSON
            + " ] } }";

        String ORDER_4040_DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_4040_1_JSON + ','
            + DETAIL_4040_2_JSON + ','
            + DETAIL_4040_3_JSON
            + " ] } }";

        String ORDER_5050_DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_5050_1_JSON + ','
            + DETAIL_5050_2_JSON + ','
            + DETAIL_5050_3_JSON
            + " ] } }";

        String ORDER_6060__DETAILS_JSON = "{ d: { \"results\": [ "
            + DETAIL_6060_1_JSON + ','
            + DETAIL_6060_2_JSON
            + " ] } }";

        String PROMO_1_JSON = "{\"id\" : 1, \"productId\" : \"100\", \"discount\" : \"10.0\"}";
        String PROMO_2_JSON = "{\"id\" : 2, \"productId\" : \"101\", \"discount\" : \"20.0\"}";
        String PROMO_3_JSON = "{\"id\" : 3, \"productId\" : \"200\", \"discount\" : \"30.0\"}";
        String PROMO_4_JSON = "{\"id\" : 4, \"productId\" : \"201\", \"discount\" : \"40.0\"}";
        String PROMO_5_JSON = "{\"id\" : 5, \"productId\" : \"300\", \"discount\" : \"50.0\"}";
        String PROMO_6_JSON = "{\"id\" : 6, \"productId\" : \"301\", \"discount\" : \"10.0\"}";
        String PROMO_7_JSON = "{\"id\" : 7, \"productId\" : \"400\", \"discount\" : \"20.0\"}";
        String PROMO_8_JSON = "{\"id\" : 8, \"productId\" : \"401\", \"discount\" : \"30.0\"}";
        String PROMO_9_JSON = "{\"id\" : 9, \"productId\" : \"500\", \"discount\" : \"40.0\"}";
        String PROMO_10_JSON = "{\"id\" : 10, \"productId\" : \"501\", \"discount\" : \"50.0\"}";
        String PROMO_11_JSON = "{\"id\" : 11, \"productId\" : \"600\", \"discount\" : \"10.0\"}";
        String PROMO_12_JSON = "{\"id\" : 12, \"productId\" : \"601\", \"discount\" : \"20.0\"}";

        String PROMOTIONS_JSON = "{ d: { \"results\": [ "
            + PROMO_1_JSON + ','
            + PROMO_2_JSON + ','
            + PROMO_3_JSON + ','
            + PROMO_4_JSON + ','
            + PROMO_5_JSON + ','
            + PROMO_6_JSON + ','
            + PROMO_7_JSON + ','
            + PROMO_8_JSON + ','
            + PROMO_9_JSON + ','
            + PROMO_10_JSON + ','
            + PROMO_11_JSON + ','
            + PROMO_12_JSON
            + " ] } }";

    }

}
