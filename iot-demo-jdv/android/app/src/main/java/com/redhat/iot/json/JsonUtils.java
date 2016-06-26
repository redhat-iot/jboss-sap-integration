package com.redhat.iot.json;

import java.util.Calendar;
import java.util.Date;

/**
 * Utilities used during marshalling of JSON strings to/from {@link com.redhat.iot.domain.IotObject}s.
 */
class JsonUtils {

    /**
     * The name of the JSON array that will be converted into {@link com.redhat.iot.domain.IotObject}s.
     */
    static final String RESULTS_ARRAY = "results";

    /**
     * The name of the parent JSON object of the result array.
     */
    static final String RESULTS_ARRAY_PARENT = "d";

    /**
     * @param dateString the JSON representation of a date (cannot be <code>null</code>)
     * @return the {@link Calendar} representation (never <code>null</code>)
     */
    static Calendar parseDate( final String dateString ) {
        // need to strip off "/Date(" from beginning and ")/" from end
        final String temp = dateString.substring( 6, dateString.length() - 2 );
        final long orderDate = Long.parseLong( temp );
        final Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( orderDate ) );
        return cal;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private JsonUtils() {
        // nothing to do
    }

}
