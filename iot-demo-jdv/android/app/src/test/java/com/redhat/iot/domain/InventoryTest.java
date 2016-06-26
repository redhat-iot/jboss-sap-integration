package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Inventory} class.
 */
public final class InventoryTest {

    private static final int PRODUCT_ID = 1000;
    private static final int STORE_ID = 1;
    private static final int QUANTITY = 10;

    private Inventory get() {
        return new Inventory( STORE_ID, PRODUCT_ID, QUANTITY );
    }

    @Test
    public void shouldBeEqual() {
        final Inventory thisInventory = get();
        final Inventory thatInventory = new Inventory( thisInventory.getStoreId(),
                                                       thisInventory.getProductId(),
                                                       thisInventory.getQuantity() );
        assertThat( thisInventory, is( thatInventory ) );
        assertThat( thisInventory.hashCode(), is( thatInventory.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfProductIdIsDifferent() {
        final Inventory thisInventory = get();
        final Inventory thatInventory = new Inventory( thisInventory.getStoreId(),
                                                       ( thisInventory.getProductId() + 1 ),
                                                       thisInventory.getQuantity() );
        assertThat( thisInventory, is( not( thatInventory ) ) );
        assertThat( thisInventory.hashCode(), is( not( thatInventory.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfQuantityIsDifferent() {
        final Inventory thisInventory = get();
        final Inventory thatInventory = new Inventory( thisInventory.getStoreId(),
                                                       thisInventory.getProductId(),
                                                       ( thisInventory.getQuantity() + 1 ) );
        assertThat( thisInventory, is( not( thatInventory ) ) );
        assertThat( thisInventory.hashCode(), is( not( thatInventory.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfStoreIdIsDifferent() {
        final Inventory thisInventory = get();
        final Inventory thatInventory = new Inventory( ( thisInventory.getStoreId() + 1 ),
                                                       thisInventory.getProductId(),
                                                       thisInventory.getQuantity() );
        assertThat( thisInventory, is( not( thatInventory ) ) );
        assertThat( thisInventory.hashCode(), is( not( thatInventory.hashCode() ) ) );
    }

}
