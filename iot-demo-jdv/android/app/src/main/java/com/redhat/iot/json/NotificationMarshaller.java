package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.IotNotification;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Converts to/from a JSON string and a {@link IotNotification} object.
 */
public class NotificationMarshaller implements IotMarshaller< IotNotification > {

    private static NotificationMarshaller _shared;

    /**
     * @return the shared {@link IotNotification} marshaller (never <code>null</code>)
     */
    public static NotificationMarshaller get() {
        if ( _shared == null ) {
            _shared = new NotificationMarshaller();
        }

        return _shared;
    }

    /**
     * Don't allow construction outside of this class.
     */
    private NotificationMarshaller() {
        // nothing to do
    }

    @Override
    public JSONArray parseJsonArray( final String json ) throws IotException {
        try {
            final JSONObject jobj = new JSONObject( json );
            return jobj.getJSONArray( JsonUtils.RESULTS_ARRAY_PARENT );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public IotNotification toIot( final String json ) throws IotException {
        try {
            final JSONObject jnotification = new JSONObject( json );
            final int promoId = jnotification.getInt( "id" );
            return new IotNotification( promoId );
        } catch ( final Exception e ) {
            throw new IotException( e );
        }
    }

    @Override
    public String toJson( final IotNotification notification ) throws IotException {
        // TODO implement toJson
        return null;
    }

}
