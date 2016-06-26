package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Store} class.
 */
public final class StoreTest {

    private static final String ADDRESS_LINE1 = "addressLine1";
    private static final String ADDRESS_LINE2 = "addressLine2";
    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final int ID = 1;
    private static final String PHONE = "phone";
    private static final String POSTAL_CODE = "postalCode";
    private static final String STATE = "state";

    private Store get() {
        return new Store( ID,
                          ADDRESS_LINE1,
                          ADDRESS_LINE2,
                          CITY,
                          STATE,
                          POSTAL_CODE,
                          COUNTRY,
                          PHONE );
    }

    @Test
    public void shouldBeEqual() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( thatStore ) );
        assertThat( thisStore.hashCode(), is( thatStore.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfAddressLine1IsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           ( thisStore.getAddressLine1() + "blah" ),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfAddressLine2IsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           ( thisStore.getAddressLine2() + "blah" ),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCityIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           ( thisStore.getCity() + "blah" ),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCountryIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           ( thisStore.getCountry() + "blah" ),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( ( thisStore.getId() + 1 ),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPhoneIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           ( thisStore.getPhone() + "blah" ) );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPostalCodeIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           thisStore.getState(),
                                           ( thisStore.getPostalCode() + "blah" ),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfStateIsDifferent() {
        final Store thisStore = get();
        final Store thatStore = new Store( thisStore.getId(),
                                           thisStore.getAddressLine1(),
                                           thisStore.getAddressLine2(),
                                           thisStore.getCity(),
                                           ( thisStore.getState() + "blah" ),
                                           thisStore.getPostalCode(),
                                           thisStore.getCountry(),
                                           thisStore.getPhone() );
        assertThat( thisStore, is( not( thatStore ) ) );
        assertThat( thisStore.hashCode(), is( not( thatStore.hashCode() ) ) );
    }

    @Test
    public void shouldSetFieldsAtConstruction() {
        final Store store = get();
        assertThat( store.getId(), is( ID ) );
        assertThat( store.getAddressLine1(), is( ADDRESS_LINE1 ) );
        assertThat( store.getAddressLine2(), is( ADDRESS_LINE2 ) );
        assertThat( store.getCity(), is( CITY ) );
        assertThat( store.getState(), is( STATE ) );
        assertThat( store.getPostalCode(), is( POSTAL_CODE ) );
        assertThat( store.getCountry(), is( COUNTRY ) );
        assertThat( store.getPhone(), is( PHONE ) );
    }

}
