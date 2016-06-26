package com.redhat.iot.concurrent;

import com.redhat.iot.domain.Department;
import com.redhat.iot.json.DepartmentMarshaller;
import com.redhat.iot.json.IotMarshaller;

/**
 * Callback for working with {@link Department} results.
 */
public class DepartmentCallback extends IotCallback< Department > {

    @Override
    public IotMarshaller< Department > getMarshaller() {
        return DepartmentMarshaller.get();
    }

}
