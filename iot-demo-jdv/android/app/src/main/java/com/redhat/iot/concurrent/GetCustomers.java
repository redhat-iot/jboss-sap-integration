package com.redhat.iot.concurrent;

import com.redhat.iot.IotApp;
import com.redhat.iot.IotException;
import com.redhat.iot.R.raw;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Customer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Task to retrieve {@link Customer}s.
 */
public class GetCustomers extends GetData< Customer > {

    /**
     * The OData URL used to obtain {@link Customer}s.
     */
    private static final String URL = ( String.format( GetData.URL_PATTERN, "Customer" ) + GetData.JSONS_FORMAT );

    /**
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetCustomers( final CustomerCallback callback ) {
        super( URL, callback, Customer.class, string.load_customers );
    }

    @Override
    protected String getTestData() throws IotException {
        try {
            final InputStream is = IotApp.getContext().getResources().openRawResource( raw.customer );
            final BufferedReader streamReader = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
            final StringBuilder builder = new StringBuilder();
            String inputStr;

            while ( ( inputStr = streamReader.readLine() ) != null ) {
                builder.append( inputStr );
            }

            return builder.toString();
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

}
