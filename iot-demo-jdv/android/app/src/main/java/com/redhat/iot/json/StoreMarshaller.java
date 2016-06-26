package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Store;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link com.redhat.iot.domain.Store} object.
 */
public class StoreMarshaller implements IotMarshaller< Store > {

    private static StoreMarshaller _shared;

    /**
     * @return the shared {@link Store} marshaller (never <code>null</code>)
     */
    public static StoreMarshaller get() {
        if ( _shared == null ) {
            _shared = new StoreMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private StoreMarshaller() {
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
    public Store toIot( final String json ) throws IotException {
        try {
            final JSONObject store = new JSONObject( json );

            // required
            final int id = store.getInt( "id" ); // must have an ID

            // optional
            final String addressLine1 = ( store.has( "addressLine1" ) ? store.getString( "addressLine1" ) : "" );
            final String addressLine2 = ( store.has( "addressLine2" ) ? store.getString( "addressLine2" ) : "" );
            final String city = ( store.has( "city" ) ? store.getString( "city" ) : "" );
            final String country = ( store.has( "country" ) ? store.getString( "country" ) : "" );
            final String phone = ( store.has( "phone" ) ? store.getString( "phone" ) : "" );
            final String postalCode = ( store.has( "postalCode" ) ? store.getString( "postalCode" ) : "" );
            final String state = ( store.has( "state" ) ? store.getString( "state" ) : "" );

            return new Store( id,
                              addressLine1,
                              addressLine2,
                              city,
                              state,
                              postalCode,
                              country,
                              phone );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Store store ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
