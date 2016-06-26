package com.redhat.iot.concurrent;

import com.redhat.iot.domain.OrderDetail;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.OrderDetailMarshaller;

/**
 * Callback for working with {@link OrderDetail} results.
 */
class OrderDetailCallback extends IotCallback< OrderDetail > {

    @Override
    public IotMarshaller< OrderDetail > getMarshaller() {
        return OrderDetailMarshaller.get();
    }

}
