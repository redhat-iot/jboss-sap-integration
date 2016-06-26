package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Store;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.StoreMarshaller;

/**
 * Callback for working with {@link Store} results.
 */
public class StoreCallback extends IotCallback< Store > {

    @Override
    public IotMarshaller< Store > getMarshaller() {
        return StoreMarshaller.get();
    }

}
