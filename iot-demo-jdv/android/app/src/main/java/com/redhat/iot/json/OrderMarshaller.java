package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Converts to/from a JSON string and a {@link Order} object.
 */
public class OrderMarshaller implements IotMarshaller< Order > {

    private static OrderMarshaller _shared;

    /**
     * @return the shared {@link Order} marshaller (never <code>null</code>)
     */
    public static OrderMarshaller get() {
        if ( _shared == null ) {
            _shared = new OrderMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private OrderMarshaller() {
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
    public Order toIot( final String json ) throws IotException {
        try {
            final JSONObject order = new JSONObject( json );

            // required
            final int id = order.getInt( "id" ); // must have an ID
            final int customerId = order.getInt( "customerId" ); // must have a customer ID

            // optional
            final String comments = ( order.has( "comments" ) ? order.getString( "comments" ) : "" );
            final String status = ( order.has( "status" ) ? order.getString( "status" ) : "" );

            final Calendar orderDate;

            if ( order.has( "orderDate" ) ) {
                orderDate = JsonUtils.parseDate( order.getString( "orderDate" ) );
            } else {
                orderDate = null;
            }

            final Calendar requiredDate;

            if ( order.has( "requiredDate" ) ) {
                requiredDate = JsonUtils.parseDate( order.getString( "requiredDate" ) );
            } else {
                requiredDate = null;
            }

            final Calendar shippedDate;

            if ( order.has( "shippedDate" ) ) {
                shippedDate = JsonUtils.parseDate( order.getString( "shippedDate" ) );
            } else {
                shippedDate = null;
            }

            return new Order( id, comments, customerId, orderDate, requiredDate, shippedDate, status );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Order order ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
