package com.redhat.iot.concurrent;

import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.OrderDetail;

/**
 * Task to retrieve the {@link OrderDetail}s of an {@link com.redhat.iot.domain.Order}.
 */
public class GetOrderDetails extends GetData< OrderDetail > {

    /**
     * The OData URL used to obtain {@link OrderDetail}s.
     */
    private static final String URL =
        ( String.format( GetData.URL_PATTERN, "PostgreSQL_Sales_Promotions.Order(%s)/OrderDetail" ) + GetData.JSONS_FORMAT );

    private final int orderId;

    /**
     * @param orderId  the ID of the {@link com.redhat.iot.domain.Order} whose {@link OrderDetail}s are being requested
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetOrderDetails( final int orderId,
                            final OrderDetailCallback callback ) {
        super( String.format( URL, orderId ), callback, OrderDetail.class, string.load_order_details );
        this.orderId = orderId;
    }

    @Override
    protected String getTestData() {
        switch ( this.orderId ) {
            case TestData.ORDER_1010_ID:
                return TestData.ORDER_1010_DETAILS_JSON;
            case TestData.ORDER_2020_ID:
                return TestData.ORDER_2020_DETAILS_JSON;
            case TestData.ORDER_3030_ID:
                return TestData.ORDER_3030_DETAILS_JSON;
            case TestData.ORDER_4040_ID:
                return TestData.ORDER_4040_DETAILS_JSON;
            case TestData.ORDER_5050_ID:
                return TestData.ORDER_5050_DETAILS_JSON;
            case TestData.ORDER_6060_ID:
                return TestData.ORDER_6060__DETAILS_JSON;
            default:
                return null;
        }
    }

}
