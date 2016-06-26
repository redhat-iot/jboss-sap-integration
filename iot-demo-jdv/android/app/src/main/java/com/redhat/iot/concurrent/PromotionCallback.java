package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Promotion;
import com.redhat.iot.json.IotMarshaller;
import com.redhat.iot.json.PromotionMarshaller;

/**
 * Callback for working with {@link Promotion} results.
 */
public class PromotionCallback extends IotCallback< Promotion > {

    @Override
    public IotMarshaller< Promotion > getMarshaller() {
        return PromotionMarshaller.get();
    }

}
