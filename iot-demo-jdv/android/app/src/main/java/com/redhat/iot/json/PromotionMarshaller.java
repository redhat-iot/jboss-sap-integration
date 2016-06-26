package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Promotion;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link Promotion} object.
 */
public class PromotionMarshaller implements IotMarshaller< Promotion > {

    private static PromotionMarshaller _shared;

    /**
     * @return the shared {@link com.redhat.iot.domain.Product} marshaller (never <code>null</code>)
     */
    public static PromotionMarshaller get() {
        if ( _shared == null ) {
            _shared = new PromotionMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private PromotionMarshaller() {
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
    public Promotion toIot( final String json ) throws IotException {
        try {
            final JSONObject cust = new JSONObject( json );

            // required
            final int id = cust.getInt( "id" ); // must have an ID
            final int productId = cust.getInt( "productId" ); // must have a product ID
            final double discount = cust.getDouble( "discount" ); // must have a discount

            return new Promotion( id, productId, discount );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Promotion promotion ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
