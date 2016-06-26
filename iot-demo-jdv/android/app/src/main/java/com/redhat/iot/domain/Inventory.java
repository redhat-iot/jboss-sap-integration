package com.redhat.iot.domain;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents the quantity of a specific product at a specific store.
 */
public class Inventory implements IotObject {

    /**
     * An empty collection of {@link Inventory}s.
     */
    public static final Inventory[] NO_INVENTORIES = new Inventory[ 0 ];

    /**
     * Sorts an {@link Inventory inventory} by {@link Product product ID} and then by {@link Store store ID}.
     */
    public static final Comparator< Inventory > PRODUCT_SORTER = new Comparator< Inventory >() {

        @Override
        public int compare( final Inventory thisInventory,
                            final Inventory thatInventory ) {
            final int result = Integer.compare( thisInventory.getProductId(), thatInventory.getProductId() );

            if ( result == 0 ) {
                return Integer.compare( thisInventory.getStoreId(), thatInventory.getStoreId() );
            }

            return result;
        }
    };

    /**
     * Sorts an {@link Inventory inventory} by {@link Store store ID} and then by {@link Product product ID}.
     */
    public static final Comparator< Inventory > STORE_SORTER = new Comparator< Inventory >() {

        @Override
        public int compare( final Inventory thisInventory,
                            final Inventory thatInventory ) {
            final int result = Integer.compare( thisInventory.getStoreId(), thatInventory.getStoreId() );

            if ( result == 0 ) {
                return Integer.compare( thisInventory.getProductId(), thatInventory.getProductId() );
            }

            return result;
        }
    };

    private final int productId;
    private final int quantity;
    private final int storeId;

    /**
     * @param storeId   the unique ID of this promotion
     * @param productId the ID of the product that is on sale
     * @param quantity  the quantity on hand
     */
    public Inventory( final int storeId,
                      final int productId,
                      final int quantity ) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Inventory that = ( Inventory )o;
        return ( ( this.productId == that.productId ) && ( this.storeId == that.storeId ) && ( this.quantity == that.quantity ) );

    }

    /**
     * @return the ID of the product which is inventoried
     */
    public int getProductId() {
        return this.productId;
    }

    /**
     * @return the quantity on hand
     */
    public int getQuantity() {
        return this.quantity;
    }

    /**
     * @return the store ID of the inventory
     */
    public int getStoreId() {
        return this.storeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.quantity, this.storeId, this.productId );
    }

    @Override
    public String toString() {
        return ( "Inventory: storeId = " + this.storeId + ", productId = " + this.productId + ", quantity = " + this.quantity );
    }

}
