package com.redhat.iot.persistence;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class DataProvider {

    public static class City {

        private final String postalCode;
        private final String city;
        private final State state;
        private final String county;
        private final double latitude;
        private final double longitude;

        public City( final String postalCode,
                     final String city,
                     final State state,
                     final String county,
                     final double latitude,
                     final double longitude ) {
            this.postalCode = postalCode;
            this.city = city;
            this.state = state;
            this.county = county;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getCity() {
            return this.city;
        }

        public String getCounty() {
            return this.county;
        }

        public double getLatitude() {
            return this.latitude;
        }

        public double getLongitude() {
            return this.longitude;
        }

        public String getPostalCode() {
            return this.postalCode;
        }

        public State getState() {
            return this.state;
        }

    }

    public static class State implements Comparable< State > {

        private final String abbreviation;
        private final String name;

        public State( final String name,
                      final String abbreviation ) {
            this.name = name;
            this.abbreviation = abbreviation;
        }

        @Override
        public int compareTo( final State that ) {
            return this.abbreviation.compareTo( that.abbreviation );
        }

        @Override
        public boolean equals( final Object obj ) {
            if ( ( obj == null ) || !getClass().equals( obj.getClass() ) ) {
                return false;
            }

            final State that = ( State ) obj;
            return ( Objects.equals( this.abbreviation, that.abbreviation ) && Objects.equals( this.name, that.name ) );
        }

        public String getAbbreviation() {
            return this.abbreviation;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return Objects.hash( this.abbreviation, this.name );
        }

        @Override
        public String toString() {
            return ( "State: name = " + this.name + ", abbreviation = " + this.abbreviation );
        }

    }

    private static final int NUM_STATES = 51;

    private static final String AREA_CODES_FILE = "resources/area_codes.properties";
    private static final String CITIES_FILE = "resources/us_postal_codes.csv";
    private static final String FEMALE_NAMES_FILE = "female_names.txt";
    private static final String LAST_NAMES_FILE = "last_names.txt";
    private static final String MALE_NAMES_FILE = "male_names.txt";
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String SIZES_FILE = "sizes.txt";
    private static final String STREETS_FILE = "streets.txt";
    private static final String STREET_SUFFIXES_FILE = "street_suffixes.txt";
    private static final String VENDORS_FILE = "vendors.txt";

    /**
     * Key is the state abbreviation.
     */
    private static Map< String, List< Integer > > _areaCodes;
    private static List< City > _cities;
    private static List< String > _femaleNames;
    private static List< String > _lastNames;
    private static List< String > _maleNames;
    private static List< String > _products;
    private static List< String > _sizes;
    private static Map< String, State > _states;
    private static List< State > _statesAsList;
    private static List< String > _streets;
    private static List< String > _streetSuffixes;
    private static List< String > _vendors;

    public static List< Integer > getAreaCodes( final String stateAbbreviation ) throws Exception {
        if ( _areaCodes == null ) {
            final Map< String, List< Integer > > tempAreaCodes = new HashMap<>();
            final Path input = Paths.get( AREA_CODES_FILE );

            try ( final InputStream stream = Files.newInputStream( input ) ) {
                final Properties props = new Properties();
                props.load( stream );

                for ( final String key : props.stringPropertyNames() ) {
                    final String[] value = props.getProperty( key ).split( "," );
                    final List< Integer > areaCodes = new ArrayList<>( value.length );

                    for ( final String areaCodeAsString : value ) {
                        areaCodes.add( Integer.parseInt( areaCodeAsString ) );
                    }

                    tempAreaCodes.put( key, Collections.unmodifiableList( areaCodes ) );
                }

                _areaCodes = Collections.unmodifiableMap( tempAreaCodes );
            }
        }

        return _areaCodes.get( stateAbbreviation );
    }

    public static List< City > getCities() throws Exception {
        if ( _cities == null ) {
            final List< City > tempCities = new ArrayList<>( 43563 );
            final Map< String, State > tempStates = new HashMap<>( NUM_STATES );
            final List< State > tempStatesAsList = new ArrayList<>( NUM_STATES );
            final Path input = Paths.get( CITIES_FILE );
            final String content = new String( Files.readAllBytes( input ) );

            for ( final String line : content.split( "\r\n" ) ) {
                final String tokens[] = line.split( "," );

                if ( tokens.length == 0 ) {
                    continue;
                }

                final String postalCode = tokens[ 0 ];
                final String city = tokens[ 1 ];
                final String stateName = tokens[ 2 ];
                final String stateAbbreviation = tokens[ 3 ];
                final String county = tokens[ 4 ];
                final double latitude = Double.parseDouble( tokens[ 5 ] );
                final double longitude = Double.parseDouble( tokens[ 6 ] );

                State state = tempStates.get( stateAbbreviation );

                // load state if necessary
                if ( state == null ) {
                    state = new State( stateName,
                                       stateAbbreviation );
                    tempStates.put( stateAbbreviation, state );
                    tempStatesAsList.add( state );
                }

                final City place = new City( postalCode,
                                             city,
                                             state,
                                             county,
                                             latitude,
                                             longitude );
                tempCities.add( place );
            }

            _cities = Collections.unmodifiableList( tempCities );
            _states = Collections.unmodifiableMap( tempStates );

            Collections.sort( tempStatesAsList );
            _statesAsList = Collections.unmodifiableList( tempStatesAsList );
        }

        return _cities;
    }

    public static List< String > getFemaleNames() throws Exception {
        if ( _femaleNames == null ) {
            _femaleNames = load( FEMALE_NAMES_FILE, 4275 );
        }

        return _femaleNames;
    }

    public static List< String > getLastNames() throws Exception {
        if ( _lastNames == null ) {
            _lastNames = load( LAST_NAMES_FILE, 88799 );
        }

        return _lastNames;
    }

    public static List< String > getMaleNames() throws Exception {
        if ( _maleNames == null ) {
            _maleNames = load( MALE_NAMES_FILE, 1219 );
        }

        return _maleNames;
    }

    public static List< String > getProducts() throws Exception {
        if ( _products == null ) {
            _products = load( PRODUCTS_FILE, 36 );
        }

        return _products;
    }

    public static List< String > getSizes() throws Exception {
        if ( _sizes == null ) {
            _sizes = load( SIZES_FILE, 6 );
        }

        return _sizes;
    }

    public static State getState( final String stateAbbreviation ) throws Exception {
        if ( _states == null ) {
            getStates();
        }

        return _states.get( stateAbbreviation );
    }

    public static List< State > getStates() throws Exception {
        if ( _statesAsList == null ) {
            getCities();
        }

        return _statesAsList;
    }

    public static List< String > getStreets() throws Exception {
        if ( _streets == null ) {
            _streets = load( STREETS_FILE, 76 );
        }

        return _streets;
    }

    public static List< String > getStreetSuffixes() throws Exception {
        if ( _streetSuffixes == null ) {
            _streetSuffixes = load( STREET_SUFFIXES_FILE, 10 );
        }

        return _streetSuffixes;
    }

    public static List< String > getVendors() throws Exception {
        if ( _vendors == null ) {
            _vendors = load( VENDORS_FILE, 31 );
        }

        return _vendors;
    }

    private static List< String > load( final String fileName,
                                        final int numItems ) throws Exception {
        final String inputFileName = ( "resources/" + fileName );
        final List< String > temp = new ArrayList<>( numItems );
        final Path input = Paths.get( inputFileName );
        final String content = new String( Files.readAllBytes( input ) );

        for ( final String line : content.split( "\n" ) ) {
            temp.add( line );
        }

        return Collections.unmodifiableList( temp );
    }

    /**
     * Don't allow construction outside of this class.
     */
    private DataProvider() {
        // nothing to do
    }

}
