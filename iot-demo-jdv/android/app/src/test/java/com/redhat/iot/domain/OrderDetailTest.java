package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link OrderDetail} class.
 */
public final class OrderDetailTest {

    private static final int DISCOUNT = 10;
    private static final double MSRP = 5.99;
    private static final int ORDER_ID = 1;
    private static final int PRODUCT_ID = 1000;
    private static final int QUANTITY = 3;

    static OrderDetail get() {
        return new OrderDetail( ORDER_ID, PRODUCT_ID, QUANTITY, MSRP, DISCOUNT );
    }

    @Test
    public void shouldBeEqual() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( thisOrderDetail.getOrderId(),
                                                             thisOrderDetail.getProductId(),
                                                             thisOrderDetail.getQuantity(),
                                                             thisOrderDetail.getMsrp(),
                                                             thisOrderDetail.getDiscount() );
        assertThat( thisOrderDetail, is( thatOrderDetail ) );
        assertThat( thisOrderDetail.hashCode(), is( thatOrderDetail.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfDiscountIsDifferent() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( thisOrderDetail.getOrderId(),
                                                             thisOrderDetail.getProductId(),
                                                             thisOrderDetail.getQuantity(),
                                                             thisOrderDetail.getMsrp(),
                                                             ( thisOrderDetail.getDiscount() + 1 ) );
        assertThat( thisOrderDetail, is( not( thatOrderDetail ) ) );
        assertThat( thisOrderDetail.hashCode(), is( not( thatOrderDetail.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfMsrpIsDifferent() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( thisOrderDetail.getOrderId(),
                                                             thisOrderDetail.getProductId(),
                                                             thisOrderDetail.getQuantity(),
                                                             ( thisOrderDetail.getMsrp() + 1 ),
                                                             thisOrderDetail.getDiscount() );
        assertThat( thisOrderDetail, is( not( thatOrderDetail ) ) );
        assertThat( thisOrderDetail.hashCode(), is( not( thatOrderDetail.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfOrderIdIsDifferent() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( ( thisOrderDetail.getOrderId() + 1 ),
                                                             thisOrderDetail.getProductId(),
                                                             thisOrderDetail.getQuantity(),
                                                             thisOrderDetail.getMsrp(),
                                                             thisOrderDetail.getDiscount() );
        assertThat( thisOrderDetail, is( not( thatOrderDetail ) ) );
        assertThat( thisOrderDetail.hashCode(), is( not( thatOrderDetail.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfProductIdIsDifferent() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( thisOrderDetail.getOrderId(),
                                                             ( thisOrderDetail.getProductId() + 1 ),
                                                             thisOrderDetail.getQuantity(),
                                                             thisOrderDetail.getMsrp(),
                                                             thisOrderDetail.getDiscount() );
        assertThat( thisOrderDetail, is( not( thatOrderDetail ) ) );
        assertThat( thisOrderDetail.hashCode(), is( not( thatOrderDetail.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfQuantityIsDifferent() {
        final OrderDetail thisOrderDetail = get();
        final OrderDetail thatOrderDetail = new OrderDetail( thisOrderDetail.getOrderId(),
                                                             thisOrderDetail.getProductId(),
                                                             ( thisOrderDetail.getQuantity() + 1 ),
                                                             thisOrderDetail.getMsrp(),
                                                             thisOrderDetail.getDiscount() );
        assertThat( thisOrderDetail, is( not( thatOrderDetail ) ) );
        assertThat( thisOrderDetail.hashCode(), is( not( thatOrderDetail.hashCode() ) ) );
    }

}
