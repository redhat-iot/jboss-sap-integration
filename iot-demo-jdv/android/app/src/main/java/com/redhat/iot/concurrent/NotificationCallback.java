package com.redhat.iot.concurrent;

import com.redhat.iot.domain.IotNotification;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.NotificationMarshaller;

/**
 * Callback for working with {@link IotNotification} results.
 */
public class NotificationCallback extends IotCallback< IotNotification > {

    @Override
    public IotMarshaller< IotNotification > getMarshaller() {
        return NotificationMarshaller.get();
    }

}
