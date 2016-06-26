package com.redhat.iot;

/**
 * An IoT error.
 */
public class IotException extends Exception {

    /**
     * @param detailMessage an error message (should not be empty)
     */
    public IotException( final String detailMessage ) {
        super( detailMessage );
    }

    /**
     * @param error an error (cannot be <code>null</code>)
     */
    public IotException( final Throwable error ) {
        super( error );
    }
}
