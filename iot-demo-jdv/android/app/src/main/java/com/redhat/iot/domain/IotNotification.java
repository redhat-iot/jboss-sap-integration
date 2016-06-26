package com.redhat.iot.domain;

import com.redhat.iot.IotConstants;

import java.util.Objects;

/**
 * Represents a notification produced by the IoT app.
 */
public class IotNotification implements IotObject {

    private final int promoId;
    private final long timestamp;

    /**
     * @param promoId the ID of the {@link Promotion} involved in the notification
     */
    public IotNotification( final int promoId ) {
        this.promoId = promoId;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final IotNotification that = ( IotNotification )o;
        return ( ( this.promoId == that.promoId ) && ( this.timestamp == that.timestamp ) );

    }

    /**
     * @return the ID of the {@link Promotion} involved in the notification
     */
    public int getPromoId() {
        return this.promoId;
    }

    /**
     * @return the time this notification was created
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.promoId, this.timestamp );
    }

    @Override
    public String toString() {
        return ( "IotNotification: promoId = " + this.promoId + ", timestamp = " + IotConstants.DATE_FORMATTER.format( this.timestamp ) );
    }

}
