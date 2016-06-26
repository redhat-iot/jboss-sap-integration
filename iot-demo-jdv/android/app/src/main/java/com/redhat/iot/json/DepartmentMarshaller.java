package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.Department;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link Department} object.
 */
public class DepartmentMarshaller implements IotMarshaller< Department > {

    private static DepartmentMarshaller _shared;

    /**
     * @return the shared {@link Department} marshaller (never <code>null</code>)
     */
    public static DepartmentMarshaller get() {
        if ( _shared == null ) {
            _shared = new DepartmentMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private DepartmentMarshaller() {
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
    public Department toIot( final String json ) throws IotException {
        try {
            final JSONObject dept = new JSONObject( json );

            // required
            final long id = dept.getLong( "departmentCode" ); // must have an ID
            final String name = dept.getString( "departmentName" ); // must have a name

            // optional
            final String description = ( dept.has( "departmentDescription" ) ? dept.getString( "departmentDescription" ) : "" );

            // TODO what about dimension

            return new Department( id, name, description );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final Department department ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
