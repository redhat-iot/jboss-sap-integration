package com.redhat.iot.domain;

import com.redhat.iot.IotApp;

import java.util.Objects;

/**
 * Represents a product from the store.
 */
public class Product implements IotObject {

    public static final Product[] NO_PRODUCTS = new Product[ 0 ];

    private final double buyPrice;
    private final long departmentId;
    private final String description;
    private final int id;
    private final double msrp;
    private final String name;
    private final String size;
    private final String vendor;

    /**
     * @param id           the product's unique ID
     * @param departmentId the ID of the product's department
     * @param description  the description (cannot be empty)
     * @param msrp         the product MSRP
     * @param buyPrice     the product buy price
     * @param size         the product size
     * @param name         the product name
     * @param vendor       the product vendor
     */
    public Product( final int id,
                    final long departmentId,
                    final String description,
                    final double msrp,
                    final double buyPrice,
                    final String size,
                    final String name,
                    final String vendor ) {
        this.id = id;
        this.departmentId = departmentId;
        this.description = description;
        this.msrp = msrp;
        this.buyPrice = buyPrice;
        this.size = size;
        this.name = name;
        this.vendor = vendor;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Product that = ( Product )o;
        return ( ( Double.compare( that.buyPrice, this.buyPrice ) == 0 )
            && ( this.departmentId == that.departmentId )
            && ( this.id == that.id )
            && ( Double.compare( that.msrp, this.msrp ) == 0 )
            && Objects.equals( this.description, that.description )
            && Objects.equals( this.size, that.size )
            && Objects.equals( this.name, that.name )
            && Objects.equals( this.vendor, that.vendor ) );
    }

    /**
     * @return the buy price
     */
    public double getBuyPrice() {
        return this.buyPrice;
    }

    /**
     * @return the product's department identifier
     */
    public long getDepartmentId() {
        return this.departmentId;
    }

    /**
     * @return the product description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the product identifier
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the product's image identifier
     */
    public int getImageId() {
        return IotApp.getImageId( this );
    }

    /**
     * @return the product msrp
     */
    public double getMsrp() {
        return this.msrp;
    }

    /**
     * @return the product name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the product size
     */
    public String getSize() {
        return this.size;
    }

    /**
     * @return the product vendor
     */
    public String getVendor() {
        return this.vendor;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.buyPrice,
                             this.departmentId,
                             this.description,
                             this.id,
                             this.msrp,
                             this.name,
                             this.size,
                             this.vendor );
    }

    @Override
    public String toString() {
        return ( "Product: id = " + this.id + ", name = " + this.name );
    }

}
