package com.redhat.iot.concurrent;

import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Product;

/**
 * Task to retrieve {@link Product}s.
 */
public class GetProducts extends GetData< Product > {

    /**
     * The OData URL used to obtain {@link Product}s.
     */
    private static final String URL =
        ( String.format( GetData.URL_PATTERN, "PostgreSQL_Sales_Promotions.Product" ) + GetData.JSONS_FORMAT );

    /**
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetProducts( final ProductCallback callback ) {
        super( URL, callback, Product.class, string.load_products );
    }

    @Override
    protected String getTestData() {
        return TestData.PRODUCTS_JSON;
    }

}
