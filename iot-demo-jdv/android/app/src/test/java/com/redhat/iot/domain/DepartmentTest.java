package com.redhat.iot.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * A test class for the {@link Department} class.
 */
public final class DepartmentTest {

    private static final String DESCRIPTION = "description";
    private static final int ID = 1;
    private static final String NAME = "name";

    private Department get() {
        return new Department( ID, NAME, DESCRIPTION );
    }

    @Test
    public void shouldBeEqual() {
        final Department thisDepartment = get();
        final Department thatDepartment = new Department( thisDepartment.getId(),
                                                          thisDepartment.getName(),
                                                          thisDepartment.getDescription() );
        assertThat( thisDepartment, is( thatDepartment ) );
        assertThat( thisDepartment.hashCode(), is( thatDepartment.hashCode() ) );
    }

    @Test
    public void shouldNotBeEqualIfDescriptionIsDifferent() {
        final Department thisDepartment = get();
        final Department thatDepartment = new Department( thisDepartment.getId(),
                                                          thisDepartment.getName(),
                                                          ( thisDepartment.getDescription() + "blah" ) );
        assertThat( thisDepartment, is( not( thatDepartment ) ) );
        assertThat( thisDepartment.hashCode(), is( not( thatDepartment.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfIdIsDifferent() {
        final Department thisDepartment = get();
        final Department thatDepartment = new Department( ( thisDepartment.getId() + 1 ),
                                                          thisDepartment.getName(),
                                                          thisDepartment.getDescription() );
        assertThat( thisDepartment, is( not( thatDepartment ) ) );
        assertThat( thisDepartment.hashCode(), is( not( thatDepartment.hashCode() ) ) );
    }

    @Test
    public void shouldNotBeEqualIfNameIsDifferent() {
        final Department thisDepartment = get();
        final Department thatDepartment = new Department( thisDepartment.getId(),
                                                          ( thisDepartment.getName() + "blah" ),
                                                          thisDepartment.getDescription() );
        assertThat( thisDepartment, is( not( thatDepartment ) ) );
        assertThat( thisDepartment.hashCode(), is( not( thatDepartment.hashCode() ) ) );
    }

}
