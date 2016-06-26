package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Inventory;
import com.redhat.iot.json.InventoryMarshaller;
import com.redhat.iot.json.IotMarshaller;

/**
 * Callback for working with {@link Inventory} results.
 */
public class InventoryCallback extends IotCallback< Inventory > {

    @Override
    public IotMarshaller< Inventory > getMarshaller() {
        return InventoryMarshaller.get();
    }

}
