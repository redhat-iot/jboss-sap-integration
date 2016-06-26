package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Customer;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link Customer} object.
 */
public class CustomerMarshaller implements IotMarshaller< Customer > {

    private static CustomerMarshaller _shared;

    /**
     * @return the shared {@link Customer} marshaller (never <code>null</code>)
     */
    public static CustomerMarshaller get() {
        if ( _shared == null ) {
            _shared = new CustomerMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private CustomerMarshaller() {
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
    public Customer toIot( final String json ) throws IotException {
        try {
            final JSONObject cust = new JSONObject( json );

            // required
            final int id = cust.getInt( "id" ); // must have an ID
            final String name = cust.getString( "name" ); // must have a name

            // optional
            final String addressLine1 = ( cust.has( "addressLine1" ) ? cust.getString( "addressLine1" ) : "" );
            final String addressLine2 = ( cust.has( "addressLine2" ) ? cust.getString( "addressLine2" ) : "" );
            final String city = ( cust.has( "city" ) ? cust.getString( "city" ) : "" );
            final String country = ( cust.has( "country" ) ? cust.getString( "country" ) : "" );
            final int creditLimit = ( cust.has( "creditLimit" ) ? cust.getInt( "creditLimit" ) : -1 );
            final String email = ( cust.has( "email" ) ? cust.getString( "email" ) : "" );
            final String phone = ( cust.has( "phone" ) ? cust.getString( "phone" ) : "" );
            final String postalCode = ( cust.has( "postalCode" ) ? cust.getString( "postalCode" ) : "" );
            final String pswd = ( cust.has( "pswd" ) ? cust.getString( "pswd" ) : "" );
            final String state = ( cust.has( "state" ) ? cust.getString( "state" ) : "" );

            return new Customer( id,
                                 email,
                                 pswd,
                                 name,
                                 addressLine1,
                                 addressLine2,
                                 city,
                                 state,
                                 postalCode,
                                 country,
                                 phone,
                                 creditLimit );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Customer customer ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
