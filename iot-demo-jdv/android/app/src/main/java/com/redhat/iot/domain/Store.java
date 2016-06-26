package com.redhat.iot.domain;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a store.
 */
public class Store implements IotObject {

    /**
     * The ID of the store when one has not been chosen.
     */
    public static final int NOT_IDENTIFIED = -1;

    /**
     * Sorts {@link Store stores} by state and then by name.
     */
    public static final Comparator< Store > SORTER = new Comparator< Store >() {

        @Override
        public int compare( final Store thisStore,
                            final Store thatStore ) {
            final int result = thisStore.getState().compareTo( thatStore.getState() );

            if ( result == 0 ) {
                return thisStore.getCity().compareTo( thatStore.getCity() );
            }

            return result;
        }
    };

    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String country;
    private final int id;
    private final String phone;
    private final String postalCode;
    private final String state;

    /**
     * @param id           the unique ID of the store
     * @param addressLine1 the first line of the store address (can be empty)
     * @param addressLine2 the second line of the store address (can be empty)
     * @param city         the city of the store address (can be empty)
     * @param state        the state of the store address (can be empty)
     * @param postalCode   the zipcode of the store address (can be empty)
     * @param country      the country of the store address (can be empty)
     * @param phone        the store phone number (can be empty)
     */
    public Store( final int id,
                  final String addressLine1,
                  final String addressLine2,
                  final String city,
                  final String state,
                  final String postalCode,
                  final String country,
                  final String phone ) {
        this.id = id;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.phone = phone;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Store that = ( Store )o;
        return ( ( this.id == that.id )
            && Objects.equals( this.addressLine1, that.addressLine1 )
            && Objects.equals( this.addressLine2, that.addressLine2 )
            && Objects.equals( this.city, that.city )
            && Objects.equals( this.country, that.country )
            && Objects.equals( this.phone, that.phone )
            && Objects.equals( this.postalCode, that.postalCode )
            && Objects.equals( this.state, that.state ) );
    }

    /**
     * @return the address line 1 (can be empty)
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    /**
     * @return the address line 2 (can be empty)
     */
    public String getAddressLine2() {
        return this.addressLine2;
    }

    /**
     * @return the address city (can be empty)
     */
    public String getCity() {
        return this.city;
    }

    /**
     * @return the address country (can be empty)
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * @return the store ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the store phone number (can be empty)
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * @return the address postal code (can be empty)
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    /**
     * @return the address state (can be empty)
     */
    public String getState() {
        return this.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.addressLine1,
                             this.addressLine2,
                             this.city,
                             this.country,
                             this.id,
                             this.phone,
                             this.postalCode,
                             this.state );
    }

    @Override
    public String toString() {
        return ( "Store: id = " + this.id );
    }

}
