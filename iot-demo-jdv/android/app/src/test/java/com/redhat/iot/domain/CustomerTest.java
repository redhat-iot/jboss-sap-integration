package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Customer} class.
 */
public final class CustomerTest {

    private static final String ADDRESS_LINE1 = "addressLine1";
    private static final String ADDRESS_LINE2 = "addressLine2";
    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final int CREDIT_LIMIT = 1000;
    private static final String EMAIL = "email";
    private static final int ID = 1;
    private static final String NAME = "name";
    private static final String PHONE = "phone";
    private static final String POSTAL_CODE = "postalCode";
    private static final String PASSWORD = "pswd";
    private static final String STATE = "state";

    private Customer get() {
        return new Customer( ID,
                             EMAIL,
                             PASSWORD,
                             NAME,
                             ADDRESS_LINE1,
                             ADDRESS_LINE2,
                             CITY,
                             STATE,
                             POSTAL_CODE,
                             COUNTRY,
                             PHONE,
                             CREDIT_LIMIT );
    }

    @Test
    public void shouldBeEqual() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( thatCustomer ) );
        assertThat( thisCustomer.hashCode(), is( thatCustomer.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfAddressLine1IsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    ( thisCustomer.getAddressLine1() + "blah" ),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfAddressLine2IsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    ( thisCustomer.getAddressLine2() + "blah" ),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCityIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    ( thisCustomer.getCity() + "blah" ),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCountryIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    ( thisCustomer.getCountry() + "blah" ),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfCreditLimitIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    ( thisCustomer.getCreditLimit() + 1 ) );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfEmailIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    ( thisCustomer.getEmail() + "blah" ),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( ( thisCustomer.getId() + 1 ),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfNameIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    ( thisCustomer.getName() + "blah" ),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPhoneIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    ( thisCustomer.getPhone() + "blah" ),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPostalCodeIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    ( thisCustomer.getPostalCode() + "blah" ),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfPasswordIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    ( thisCustomer.getPswd() + "blah" ),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    thisCustomer.getState(),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfStateIsDifferent() {
        final Customer thisCustomer = get();
        final Customer thatCustomer = new Customer( thisCustomer.getId(),
                                                    thisCustomer.getEmail(),
                                                    thisCustomer.getPswd(),
                                                    thisCustomer.getName(),
                                                    thisCustomer.getAddressLine1(),
                                                    thisCustomer.getAddressLine2(),
                                                    thisCustomer.getCity(),
                                                    ( thisCustomer.getState() + "blah" ),
                                                    thisCustomer.getPostalCode(),
                                                    thisCustomer.getCountry(),
                                                    thisCustomer.getPhone(),
                                                    thisCustomer.getCreditLimit() );
        assertThat( thisCustomer, is( not( thatCustomer ) ) );
        assertThat( thisCustomer.hashCode(), is( not( thatCustomer.hashCode() ) ) );
    }

    @Test
    public void shouldSetFieldsAtConstruction() {
        final Customer customer = get();
        assertThat( customer.getId(), is( ID ) );
        assertThat( customer.getEmail(), is( EMAIL ) );
        assertThat( customer.getPswd(), is( PASSWORD ) );
        assertThat( customer.getName(), is( NAME ) );
        assertThat( customer.getAddressLine1(), is( ADDRESS_LINE1 ) );
        assertThat( customer.getAddressLine2(), is( ADDRESS_LINE2 ) );
        assertThat( customer.getCity(), is( CITY ) );
        assertThat( customer.getState(), is( STATE ) );
        assertThat( customer.getPostalCode(), is( POSTAL_CODE ) );
        assertThat( customer.getCountry(), is( COUNTRY ) );
        assertThat( customer.getPhone(), is( PHONE ) );
        assertThat( customer.getCreditLimit(), is( CREDIT_LIMIT ) );
        assertThat( customer.getStoreId(), is( Store.NOT_IDENTIFIED ) );
    }

    @Test
    public void shouldSetStoreId() {
        final Customer customer = get();
        final int newStoreId = ( customer.getStoreId() + 1 );
        customer.setStoreId( newStoreId );
        assertThat( customer.getStoreId(), is( newStoreId ) );
    }

}
