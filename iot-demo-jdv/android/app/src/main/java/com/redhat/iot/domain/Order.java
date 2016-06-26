package com.redhat.iot.domain;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

/**
 * Represents an order.
 */
public class Order implements IotObject {

    /**
     * An empty collection of {@link Order}s.
     */
    public static final Order[] NO_ORDERS = new Order[ 0 ];

    private final String comments;
    private final int customerId;
    private OrderDetail[] details = OrderDetail.NO_DETAILS;
    private final int id;
    private final Calendar orderDate;
    private double price;
    private final Calendar requiredDate;
    private final Calendar shippedDate;
    private final String status;

    /**
     * @param id           the unique ID of this order
     * @param comments     the order comments (can be empty)
     * @param custId       the ID of the customer that this order applies
     * @param orderDate    the date of the order (can be <code>null</code>)
     * @param requiredDate the required delivery date of this order (can be <code>null</code>)
     * @param shippedDate  the shipped date of the order (can be <code>null</code>)
     * @param status       the order status (can be empty)
     */
    public Order( final int id,
                  final String comments,
                  final int custId,
                  final Calendar orderDate,
                  final Calendar requiredDate,
                  final Calendar shippedDate,
                  final String status
                ) {
        this.id = id;
        this.comments = comments;
        this.customerId = custId;
        this.orderDate = orderDate;
        this.requiredDate = requiredDate;
        this.shippedDate = shippedDate;
        this.status = status;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( o == null || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Order that = ( Order )o;
        return ( ( this.customerId == that.customerId )
            && ( this.id == that.id )
            && ( Double.compare( that.price, this.price ) == 0 )
            && Objects.equals( this.comments, that.comments )
            && Arrays.equals( this.details, that.details )
            && Objects.equals( this.orderDate, that.orderDate )
            && Objects.equals( this.requiredDate, that.requiredDate )
            && Objects.equals( this.shippedDate, that.shippedDate )
            && Objects.equals( this.status, that.status ) );
    }

    /**
     * @return the order comments (can be empty)
     */
    public String getComments() {
        return this.comments;
    }

    /**
     * @return the customer ID
     */
    public int getCustomerId() {
        return this.customerId;
    }

    /**
     * @return the order details (never <code>null</code> but can be empty)
     */
    public OrderDetail[] getDetails() {
        return this.details;
    }

    /**
     * @return the order ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the date of the order (never <code>null</code>)
     */
    public Calendar getOrderDate() {
        return this.orderDate;
    }

    /**
     * @return the order price
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * @return the required date of the order (never <code>null</code>)
     */
    public Calendar getRequiredDate() {
        return this.requiredDate;
    }

    /**
     * @return the shipped date of the order (never <code>null</code>)
     */
    public Calendar getShippedDate() {
        return this.shippedDate;
    }

    /**
     * @return the order status (never empty)
     */
    public String getStatus() {
        return this.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.comments,
                             this.customerId,
                             this.details,
                             this.id,
                             this.orderDate,
                             this.price,
                             this.requiredDate,
                             this.shippedDate,
                             this.status );
    }

    /**
     * @param details the order details (can be <code>null</code>) sorted by line number
     */
    public void setDetails( final OrderDetail[] details ) {
        this.details = ( ( details == null ) ? OrderDetail.NO_DETAILS : details );
        this.price = 0;

        if ( this.details.length != 0 ) {
            Arrays.sort( this.details, OrderDetail.SORTER );

            for ( final OrderDetail detail : this.details ) {
                this.price += ( ( detail.getMsrp() - ( detail.getMsrp() * detail.getDiscount() / 100 ) ) * detail.getQuantity() );
            }
        }
    }

    @Override
    public String toString() {
        return ( "Order: id = " + this.customerId + ", custId = " + this.customerId );
    }

}
