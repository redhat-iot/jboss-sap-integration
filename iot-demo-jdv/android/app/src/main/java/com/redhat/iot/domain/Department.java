package com.redhat.iot.domain;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a store department.
 */
public class Department implements IotObject {

    /**
     * An empty collection of departments.
     */
    public static final Department[] NO_DEPARTMENTS = new Department[ 0 ];

    /**
     * Sorts {@link Department departments} by name.
     */
    public static final Comparator< Department > NAME_SORTER = new Comparator< Department >() {

        @Override
        public int compare( final Department thisDepartment,
                            final Department thatDepartment ) {
            return thisDepartment.getName().compareTo( thatDepartment.getName() );
        }
    };

    private final String description;
    private final long id;
    private final String name;

    /**
     * @param id          the unique ID of the department
     * @param name        the department name (cannot be empty)
     * @param description the department description (cannot be empty)
     */
    public Department( final long id,
                       final String name,
                       final String description ) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( ( o == null ) || ( getClass() != o.getClass() ) ) {
            return false;
        }

        final Department that = ( Department )o;
        return ( ( this.id == that.id )
            && Objects.equals( this.description, that.description )
            && Objects.equals( this.name, that.name ) );
    }

    /**
     * @return the department ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return the department description (never empty)
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the department name (never empty)
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash( this.description, this.id, this.name );
    }

    @Override
    public String toString() {
        return ( "Department: id = " + this.id + ", name = " + this.name );
    }

}
