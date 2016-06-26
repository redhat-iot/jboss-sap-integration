package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Promotion} class.
 */
public final class PromotionTest {

    private static final double DISCOUNT = 10.0;
    private static final int ID = 1;
    private static final int PRODUCT_ID = 1000;

    private Promotion get() {
        return new Promotion( ID, PRODUCT_ID, DISCOUNT );
    }

    @Test
    public void shouldBeEqual() {
        final Promotion thisPromotion = get();
        final Promotion thatPromotion = new Promotion( thisPromotion.getId(),
                                                       thisPromotion.getProductId(),
                                                       thisPromotion.getDiscount() );
        assertThat( thisPromotion, is( thatPromotion ) );
        assertThat( thisPromotion.hashCode(), is( thatPromotion.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfDiscountIsDifferent() {
        final Promotion thisPromotion = get();
        final Promotion thatPromotion = new Promotion( thisPromotion.getId(),
                                                       thisPromotion.getProductId(),
                                                       ( thisPromotion.getDiscount() + 1 ) );
        assertThat( thisPromotion, is( not( thatPromotion ) ) );
        assertThat( thisPromotion.hashCode(), is( not( thatPromotion.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Promotion thisPromotion = get();
        final Promotion thatPromotion = new Promotion( ( thisPromotion.getId() + 1 ),
                                                       thisPromotion.getProductId(),
                                                       thisPromotion.getDiscount() );
        assertThat( thisPromotion, is( not( thatPromotion ) ) );
        assertThat( thisPromotion.hashCode(), is( not( thatPromotion.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfProductIdIsDifferent() {
        final Promotion thisPromotion = get();
        final Promotion thatPromotion = new Promotion( thisPromotion.getId(),
                                                       ( thisPromotion.getProductId() + 1 ),
                                                       thisPromotion.getDiscount() );
        assertThat( thisPromotion, is( not( thatPromotion ) ) );
        assertThat( thisPromotion.hashCode(), is( not( thatPromotion.hashCode() ) ) );
    }

}
