package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.OrderDetail;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link OrderDetail} object.
 */
public class OrderDetailMarshaller implements IotMarshaller< OrderDetail > {

    private static OrderDetailMarshaller _shared;

    /**
     * @return the shared {@link OrderDetail} marshaller (never <code>null</code>)
     */
    public static OrderDetailMarshaller get() {
        if ( _shared == null ) {
            _shared = new OrderDetailMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private OrderDetailMarshaller() {
        // nothing to do
    }

    @Override
    public JSONArray parseJsonArray( final String json ) throws IotException {
        try {
            final JSONObject jobj = new JSONObject( json );
            final JSONObject d = jobj.getJSONObject( JsonUtils.RESULTS_ARRAY_PARENT );
            return d.getJSONArray( JsonUtils.RESULTS_ARRAY );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public OrderDetail toIot( final String json ) throws IotException {
        try {
            final JSONObject orderDetail = new JSONObject( json );

            // required
            final int orderId = orderDetail.getInt( "orderId" ); // must have an order ID
            final int productId = orderDetail.getInt( "productId" ); // must have a product ID
            final double msrp = orderDetail.getDouble( "msrp" );
            final int discount = orderDetail.getInt( "discount" );

            // optional
            final int quantity = ( orderDetail.has( "quantityOrdered" ) ? orderDetail.getInt( "quantityOrdered" ) : 1 );

            return new OrderDetail( orderId, productId, quantity, msrp, discount );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final OrderDetail detail ) throws IotException {
        return null;
    }
}
