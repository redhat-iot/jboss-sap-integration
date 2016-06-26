package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Product;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.ProductMarshaller;

/**
 * Callback for working with {@link Product} results.
 */
public class ProductCallback extends IotCallback< Product > {

    @Override
    public IotMarshaller< Product > getMarshaller() {
        return ProductMarshaller.get();
    }

}
