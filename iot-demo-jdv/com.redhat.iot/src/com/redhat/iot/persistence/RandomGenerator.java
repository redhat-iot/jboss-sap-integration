package com.redhat.iot.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public final class RandomGenerator {

    /**
     * Keep seed the same so that order is repeatable.
     */
    private static final long SEED = Timestamp.valueOf( LocalDateTime.of( 2010, 1, 1, 1, 1 ) ).getTime();

    private final Random random;

    /**
     * Constructs a random generator using an internal seed.
     */
    public RandomGenerator() {
        this( SEED );
    }

    /**
     * @param seed the seed used to construct the random generator
     */
    public RandomGenerator( final long seed ) {
        this.random = new Random( seed );
    }

    /**
     * @return a random <code>boolean</code> value
     */
    public boolean next() {
        return this.random.nextBoolean();
    }

    /**
     * @param lastPlusOne the number one greater than the maximum number that can be generated
     * @return a random <code>int</code> between zero (inclusive) and the input parameter
     *         (exclusive)
     */
    public int next( final int lastPlusOne ) {
        return this.random.nextInt( lastPlusOne );
    }

    /**
     * @param min the minimum value the generated number can be
     * @param max the maximum value the generated number can be
     * @return a random <code>int</code> value within the specified range
     */
    public int next( final int min,
                     final int max ) {
        return ( this.random.nextInt( ( max - min ) + 1 ) + min );
    }

    /**
     * @param min the minimum value the generated number can be
     * @param max the maximum value the generated number can be
     * @return a random float value within the specified range
     */
    public float next( final float min,
                       final float max ) {
        return ( min + this.random.nextFloat() * ( max - min ) );
    }

    /**
     * @param elements a collection of which one element is chosen randomly (cannot be
     *            <code>null</code> or empty)
     * @return a random element from the input collection (never <code>null</code>)
     */
    public < T > T next( final List< T > elements ) {
        final int index = next( elements.size() );
        return elements.get( index );
    }

    /**
     * @param elements an array of which one element is chosen randomly (cannot be <code>null</code>
     *            or empty)
     * @return a random element from the input array (never <code>null</code>)
     */
    public < T > T next( final T[] elements ) {
        final int index = next( elements.length );
        return elements[ index ];
    }

    /**
     * @param elements an array of enums of which one is chosen randomly (cannot be
     *            <code>null</code> or empty)
     * @return a random enum from the input array (never <code>null</code>)
     */
    public < T extends Enum< T > > T next( final T[] elements ) {
        final int index = next( elements.length );
        return elements[ index ];
    }

    /**
     * @param firstDate the first value the generated timestamp can be (cannot be <code>null</code>)
     * @param lastDate the last value the generated timestamp can be (cannot be <code>null</code>)
     * @return a random timestamp value within the specified range (never <code>null</code>)
     */
    public Timestamp next( final Timestamp firstDate,
                           final Timestamp lastDate ) {
        final long first = firstDate.getTime();
        final long last = lastDate.getTime();
        long diff;

        if ( first >= 0 ) {
            diff = ( ( last - first ) + 1 );
        } else if ( last >= 0 ) {
            diff = ( ( Math.abs( first ) + last ) + 1 );
        } else {
            diff = ( ( Math.abs( first ) - Math.abs( last ) ) + 1 );
        }

        return new Timestamp( first + ( long ) ( this.random.nextDouble() * diff ) );
    }

    /**
     * @param min the minimum value the generated number can be
     * @param max the maximum value the generated number can be
     * @return a random float value within the specified range rounded to 2 decimal digits
     */
    public float nextPrice( final float min,
                            final float max ) {
        return ( Math.round( next( min, max ) * 100 ) / 100 );
    }

}
