package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Order;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.OrderMarshaller;

/**
 * Callback for working with {@link Order} results.
 */
public class OrderCallback extends IotCallback< Order > {

    @Override
    public IotMarshaller< Order > getMarshaller() {
        return OrderMarshaller.get();
    }

}
