package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Product} class.
 */
public final class ProductTest {

    private static final double BUY_PRICE = 5.99;
    private static final long DEPARTMENT_ID = 1000;
    private static final String DESCRIPTION = "description";
    private static final int ID = 1;
    private static final double MSRP = 9.99;
    private static final String NAME = "name";
    private static final String SIZE = "size";
    private static final String VENDOR = "vendor";

    private Product get() {
        return new Product( ID,
                            DEPARTMENT_ID,
                            DESCRIPTION,
                            MSRP,
                            BUY_PRICE,
                            SIZE,
                            NAME,
                            VENDOR );
    }

    @Test
    public void shouldBeEqual() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( thatProduct ) );
        assertThat( thisProduct.hashCode(), is( thatProduct.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfBuyPriceIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 ( thisProduct.getBuyPrice() + 1 ),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfDepartmentIdIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 ( thisProduct.getDepartmentId() + 1 ),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfDescriptionIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 ( thisProduct.getDescription() + "blah" ),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( ( thisProduct.getId() + 1 ),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfMsrpIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 ( thisProduct.getMsrp() + 1 ),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfNameIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 ( thisProduct.getName() + "blah" ),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfSizeIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 ( thisProduct.getSize() + "blah" ),
                                                 thisProduct.getName(),
                                                 thisProduct.getVendor() );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfVendorIsDifferent() {
        final Product thisProduct = get();
        final Product thatProduct = new Product( thisProduct.getId(),
                                                 thisProduct.getDepartmentId(),
                                                 thisProduct.getDescription(),
                                                 thisProduct.getMsrp(),
                                                 thisProduct.getBuyPrice(),
                                                 thisProduct.getSize(),
                                                 thisProduct.getName(),
                                                 ( thisProduct.getVendor() + "blah" ) );
        assertThat( thisProduct, is( not( thatProduct ) ) );
        assertThat( thisProduct.hashCode(), is( not( thatProduct.hashCode() ) ) );
    }

}
