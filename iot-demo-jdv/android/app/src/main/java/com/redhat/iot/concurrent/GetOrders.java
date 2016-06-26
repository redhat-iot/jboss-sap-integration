package com.redhat.iot.concurrent;

import com.redhat.iot.IotApp;
import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Order;
import com.redhat.iot.domain.OrderDetail;

/**
 * Task to retrieve past {@link Order}s of a {@link com.redhat.iot.domain.Customer}.
 */
public class GetOrders extends GetData< Order > {

    /**
     * The OData URL used to obtain {@link Order}s.
     */
    private static final String URL =
        ( String.format( GetData.URL_PATTERN, "PostgreSQL_Sales_Promotions.Customer(%s)/Order" ) + GetData.JSONS_FORMAT );

    private final int customerId;

    /**
     * @param customerId the ID of the {@link com.redhat.iot.domain.Customer} whose {@link Order}s are being requested
     * @param callback   the callback (cannot be <code>null</code>)
     */
    public GetOrders( final int customerId,
                      final OrderCallback callback ) {
        super( String.format( URL, customerId ), callback, Order.class, string.load_orders );
        this.customerId = customerId;
    }

    @Override
    protected String getTestData() {
        switch ( this.customerId ) {
            case TestData.ELVIS_ID:
                return TestData.ELVIS_ORDERS_JSON;
            case TestData.RINGO_ID:
                return TestData.RINGO_ORDERS_JSON;
            case TestData.SLEDGE_ID:
                return TestData.SLEDGE_ORDERS_JSON;
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute( final Order[] orders ) {
        if ( orders.length != 0 ) {
            try {
                // for each order add in the order details
                for ( final Order order : orders ) {
                    final OrderDetail[] details =
                        new GetOrderDetails( order.getId(), new OrderDetailCallback() ).execute().get(); // block
                    order.setDetails( details );
                }
            } catch ( final Exception e ) {
                IotApp.logError( GetOrders.class, "doInBackground", "", e );
                this.error = e;
            }
        }

        super.onPostExecute( orders );
    }

}
