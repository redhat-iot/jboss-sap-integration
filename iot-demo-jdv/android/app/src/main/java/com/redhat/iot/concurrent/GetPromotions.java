package com.redhat.iot.concurrent;

import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Promotion;

/**
 * Task to retrieve {@link Promotion}s.
 */
public class GetPromotions extends GetData< Promotion > {

    /**
     * The OData URL used to obtain {@link Promotion}s.
     */
    private static final String URL =
        ( String.format( GetData.URL_PATTERN, "PostgreSQL_Sales_Promotions.Promotion" ) + GetData.JSONS_FORMAT );

    /**
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetPromotions( final PromotionCallback callback ) {
        super( URL, callback, Promotion.class, string.load_promotions );
    }

    @Override
    protected String getTestData() {
        return TestData.PROMOTIONS_JSON;
    }

}
