package com.redhat.iot.concurrent;

import android.util.Log;

import com.redhat.iot.IotApp;
import com.redhat.iot.IotConstants;
import com.redhat.iot.IotConstants.TestData;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.Department;

/**
 * Task to retrieve {@link Department}s.
 */
public class GetDepartments extends GetData< Department > {

    /**
     * The OData URL used to obtain {@link Department}s.
     */
    private static final String URL = ( String.format( GetData.URL_PATTERN, "FUSE.Department" ) + GetData.JSONS_FORMAT );

    /**
     * @param callback the callback (cannot be <code>null</code>)
     */
    public GetDepartments( final DepartmentCallback callback ) {
        super( URL, callback, Department.class, string.load_departments );
    }

    @Override
    protected String getTestData() {
        return TestData.DEPARTMENTS_JSON;
    }

    private boolean isHanaRunning() {
        final boolean reachable = IotApp.ping( IotConstants.HANA_IP_ADDRESS );

        if ( reachable ) {
            Log.d( IotConstants.LOG_TAG, "Ping HANA (" + IotConstants.HANA_IP_ADDRESS + ") was successful" );
        } else {
            IotApp.logError( GetDepartments.class,
                             "isHanaRunning",
                             "Ping of HANA (" + IotConstants.HANA_IP_ADDRESS + ") *** FAILED ***",
                             null );
        }

        return reachable;
    }

    @Override
    protected boolean isUsingRealData() {
        if ( super.isUsingRealData() ) {
            return isHanaRunning(); // only if HANA is running
        }

        // use test data
        return false;
    }

}
