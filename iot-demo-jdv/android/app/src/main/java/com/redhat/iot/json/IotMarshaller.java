package com.redhat.iot.json;

import com.redhat.iot.IotException;
import com.redhat.iot.domain.IotObject;

import org.json.JSONArray;

/**
 * Converts to/from a {@link IotObject} and a JSON string.
 *
 * @param <T> the {@link IotObject} type
 */
public interface IotMarshaller< T extends IotObject > {

    /**
     * @param json the JSON string being parsed (cannot be empty)
     * @return aarray of {@link org.json.JSONObject}s the represent {@link IotObject}s (never <code>null</code>)
     * @throws IotException if an error occurs
     */
    JSONArray parseJsonArray( final String json ) throws IotException;

    /**
     * @param json the JSON being converted into one {@link IotObject} (cannot be empty)
     * @return the {@link IotObject IoT object}
     * @throws IotException if an error occurs
     */
    T toIot( final String json ) throws IotException;

    /**
     * @param iotObj the {@link IotObject} whose JSON representation is being requested (cannot be <code>null</code>)
     * @return the JSON representation (never empty)
     * @throws IotException if an error occurs
     */
    String toJson( final T iotObj ) throws IotException;

}
