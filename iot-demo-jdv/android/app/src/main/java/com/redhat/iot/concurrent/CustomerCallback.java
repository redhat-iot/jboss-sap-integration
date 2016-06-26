package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Customer;
import com.redhat.iot.json.CustomerMarshaller;
import com.redhat.iot.json.IotMarshaller;

/**
 * Callback for working with {@link Customer} results.
 */
public class CustomerCallback extends IotCallback< Customer > {

    @Override
    public IotMarshaller< Customer > getMarshaller() {
        return CustomerMarshaller.get();
    }

}
