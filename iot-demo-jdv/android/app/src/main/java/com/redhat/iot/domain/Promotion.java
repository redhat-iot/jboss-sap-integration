package com.redhat.iot.domain;

import com.redhat.iot.DataProvider;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a promotion.
 */
public class Promotion implements IotObject {

    /**
     * An empty collection of {@link Promotion}s.
     */
    public static final Promotion[] NO_PROMOTIONS = new Promotion[ 0 ];

    /**
     * Sorts {@link Promotion promotions} be {@link Department department} name.
     */
    public static final Comparator< Promotion > DEPT__NAME_SORTER = new Comparator< Promotion >() {

        @Override
        public int compare( final Promotion thisPromo,
                            final Promotion thatPromo ) {
            final String thisDeptName = DataProvider.get().getDepartmentName( thisPromo.getProductId() );
            final String thatDeptName = DataProvider.get().getDepartmentName( thatPromo.getProductId() );

            if ( Objects.equals( thisDeptName, thatDeptName ) ) {
                return 0;
            }

            // should not happen
            if ( thisDeptName == null ) {
                return -1;
            }

            // should not happen
            if ( thatDeptName == null ) {
                return 1;
            }

            return thisDeptName.compareTo( thatDeptName );
        }
    };

    private final double discount;
    private final int id;
    private final int productId;

    /**
     * @param id        the unique ID of this promotion
     * @param productId the ID of the product that is on sale
     * @param discount  the percentage discount
     */
    public Promotion( final int id,
                      final int productId,
                      final double discount ) {
        this.id = id;
        this.productId = productId;
        this.discount = discount;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Promotion that = ( Promotion )o;
        return ( ( Double.compare( that.discount, this.discount ) == 0 )
            && ( this.productId == that.productId )
            && ( this.id == that.id ) );
    }

    /**
     * @return the promotional percentage discount
     */
    public double getDiscount() {
        return this.discount;
    }

    /**
     * @return the ID of the promotion
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the ID of the product which is being discounted
     */
    public int getProductId() {
        return this.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.discount, this.id, this.productId );
    }

    @Override
    public String toString() {
        return ( "Promotion: id = " + this.id + ", productId = " + this.productId );
    }

}
