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

    public RandomGenerator() throws Exception {
        this.random = new Random( SEED );
    }

    public boolean next() {
        return this.random.nextBoolean();
    }

    public int next( final int lastPlusOne ) {
        return this.random.nextInt( lastPlusOne );
    }

    public int next( final int first,
                     final int last ) {
        return ( this.random.nextInt( ( last - first ) + 1 ) + first );
    }

    public float next( final float min,
                       final float max ) {
        return ( min + this.random.nextFloat() * ( max - min ) );
    }

    public < T > T next( final List< T > elements ) {
        final int index = next( elements.size() );
        return elements.get( index );
    }

    public < T > T next( final T[] elements ) {
        final int index = next( elements.length );
        return elements[ index ];
    }

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

    public float nextPrice( final float min,
                            final float max ) {
        return ( Math.round( next( min, max ) * 100 ) / 100 );
    }

}
