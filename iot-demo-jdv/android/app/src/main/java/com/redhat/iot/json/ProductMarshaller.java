package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Product;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link Product} object.
 */
public class ProductMarshaller implements IotMarshaller< Product > {

    private static ProductMarshaller _shared;

    /**
     * @return the shared {@link Product} marshaller (never <code>null</code>)
     */
    public static ProductMarshaller get() {
        if ( _shared == null ) {
            _shared = new ProductMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private ProductMarshaller() {
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
    public Product toIot( final String json ) throws IotException {
        try {
            final JSONObject product = new JSONObject( json );

            // required
            final int id = product.getInt( "id" ); // must have an ID
            final int departmentId = product.getInt( "departmentCode" ); // must have a department ID

            // optional
            final String description = ( product.has( "productDescription" ) ? product.getString( "productDescription" ) : "" );
            final String size = ( product.has( "productSize" ) ? product.getString( "productSize" ) : "" );
            final String name = ( product.has( "productName" ) ? product.getString( "productName" ) : "" );
            final String vendor = ( product.has( "productVendor" ) ? product.getString( "productVendor" ) : "" );
            final double buyPrice = ( product.has( "buyPrice" ) ? product.getDouble( "buyPrice" ) : -1 );
            final double msrp = ( product.has( "msrp" ) ? product.getDouble( "msrp" ) : -1 );

            return new Product( id, departmentId, description, msrp, buyPrice, size, name, vendor );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Product product ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
