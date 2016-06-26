package com.redhat.iot.domain;

import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Order} class.
 */
public final class OrderTest {

    private static final String COMMENTS = "comments";
    private static final int CUSTOMER_ID = 1000;
    private static final int ID = 1;
    private static final Calendar ORDER_DATE = Calendar.getInstance();
    private static final Calendar REQUIRED_DATE = Calendar.getInstance();
    private static final Calendar SHIPPED_DATE = Calendar.getInstance();
    private static final String STATUS = "status";

    private Order get() {
        final Order order = new Order( ID, COMMENTS, CUSTOMER_ID, ORDER_DATE, REQUIRED_DATE, SHIPPED_DATE, STATUS );
        order.setDetails( new OrderDetail[]{ OrderDetailTest.get() } );
        return order;
    }

    @Test
    public void shouldBeEqual() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        thatOrder.setDetails( thisOrder.getDetails() );
        assertThat( thisOrder, is( thatOrder ) );
        assertThat( thisOrder.hashCode(), is( thatOrder.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfCommentsIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           ( thisOrder.getComments() + "blah" ),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCustomerIdIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           ( thisOrder.getCustomerId() + 1 ),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfDetailsIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        thatOrder.setDetails( OrderDetail.NO_DETAILS );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( ( thisOrder.getId() + 1 ),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfOrderDateIsDifferent() {
        final Order thisOrder = get();

        final Calendar orderDate = thisOrder.getOrderDate();
        orderDate.add( Calendar.YEAR, 1 );

        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           orderDate,
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPriceIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        thatOrder.setDetails( OrderDetail.NO_DETAILS );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfRequiredDateIsDifferent() {
        final Order thisOrder = get();

        final Calendar requiredDate = thisOrder.getOrderDate();
        requiredDate.add( Calendar.YEAR, 1 );

        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           requiredDate,
                                           thisOrder.getShippedDate(),
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfShippedDateIsDifferent() {
        final Order thisOrder = get();

        final Calendar shippedDate = thisOrder.getOrderDate();
        shippedDate.add( Calendar.YEAR, 1 );

        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           shippedDate,
                                           thisOrder.getStatus() );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfStatusIsDifferent() {
        final Order thisOrder = get();
        final Order thatOrder = new Order( thisOrder.getId(),
                                           thisOrder.getComments(),
                                           thisOrder.getCustomerId(),
                                           thisOrder.getOrderDate(),
                                           thisOrder.getRequiredDate(),
                                           thisOrder.getShippedDate(),
                                           ( thisOrder.getStatus() + "blah" ) );
        assertThat( thisOrder, is( not( thatOrder ) ) );
        assertThat( thisOrder.hashCode(), is( not( thatOrder.hashCode() ) ) );
    }

}
