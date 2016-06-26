package com.redhat.iot.concurrent;

import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.IotException;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Store;

/**
 * Task to retrieve {@link Store}s.
 */
public class GetStores extends GetData< Store > {

    /**
     * The OData URL used to obtain {@link Store}s.
     */
    private static final String URL = ( String.format( GetData.URL_PATTERN, "Store" ) + GetData.JSONS_FORMAT );

    /**
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetStores( final StoreCallback callback ) {
        super( URL, callback, Store.class, string.load_stores );
    }

    @Override
    protected String getTestData() throws IotException {
        return TestData.STORES_JSON;
    }

}
