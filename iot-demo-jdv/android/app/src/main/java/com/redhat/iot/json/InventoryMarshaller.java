package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Inventory;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and an {@link com.redhat.iot.domain.Inventory} object.
 */
public class InventoryMarshaller implements IotMarshaller< Inventory > {

    private static InventoryMarshaller _shared;

    /**
     * @return the shared {@link com.redhat.iot.domain.Inventory} marshaller (never <code>null</code>)
     */
    public static InventoryMarshaller get() {
        if ( _shared == null ) {
            _shared = new InventoryMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private InventoryMarshaller() {
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
    public Inventory toIot( final String json ) throws IotException {
        try {
            final JSONObject cust = new JSONObject( json );

            // required
            final int storeId = cust.getInt( "storeId" ); // must have a store ID
            final int productId = cust.getInt( "productId" ); // must have a product ID
            final int quantity = cust.getInt( "quantity" ); // must have a quantity

            return new Inventory( storeId, productId, quantity );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Inventory promotion ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
