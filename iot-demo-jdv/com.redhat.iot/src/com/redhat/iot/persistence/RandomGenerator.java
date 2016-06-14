package com.redhat.iot.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

public final class RandomGenerator {

    private static final List< City > CITIES = new ArrayList<>( 43563 );
    private static final List< String > FEMALE_NAMES = new ArrayList<>( 4275 );
    private static final List< String > LAST_NAMES = new ArrayList<>( 88799 );
    private static final List< String > MALE_NAMES = new ArrayList<>( 1219 );

    // key is the state abbreviation
    private static final Map< String, State > STATES = new HashMap<>( 51 );
    private static final List< State > STATES_AS_LIST = new ArrayList<>( 51 );

    private static final String CITIES_FILE = "resources/us_postal_codes.csv";
    private static final String FEMALE_FIRST_NAMES_FILE = "resources/dist.female.first.txt";
    private static final String LAST_NAMES_FILE = "resources/dist.all.last.txt";
    private static final String MALE_FIRST_NAMES_FILE = "resources/dist.male.first.txt";
    private static final String[] PRODUCTS = new String[] { "Flannel shirt",
                                                            "Bucket hat",
                                                            "Overalls",
                                                            "Jeans",
                                                            "Sweatshirt",
                                                            "Bike short",
                                                            "Ballet skirt",
                                                            "Tank top",
                                                            "Wool hat",
                                                            "Rain jacket",
                                                            "Suspenders",
                                                            "Backpacks",
                                                            "Denim cut-offs",
                                                            "Hawaiian shirt",
                                                            "Cotton oxford",
                                                            "Swim trunk",
                                                            "Cargo short",
                                                            "Short sleeve Henley",
                                                            "Short sleeve polo",
                                                            "Bathing suit",
                                                            "Dress socks",
                                                            "Sport coat",
                                                            "Sport briefs",
                                                            "Skinny jean",
                                                            "Onesy",
                                                            "Bathrobe",
                                                            "Hoodie",
                                                            "V-neck t-shirt",
                                                            "Pajama pants",
                                                            "Sweat pants",
                                                            "Dress pants",
                                                            "Tuxedo",
                                                            "Yoga skort",
                                                            "Romper",
                                                            "Vest top",
                                                            "Beach sling", };
    private static final String[] SIZES = new String[] { "Kids", "Petite", "Small", "Medium", "Large", "X-Large" };
    private static final String[] STREETS = new String[] { "1st",
                                                           "2nd",
                                                           "3rd",
                                                           "4th",
                                                           "5th",
                                                           "6th",
                                                           "7th",
                                                           "8th",
                                                           "9th",
                                                           "10th",
                                                           "11th",
                                                           "13th",
                                                           "12th",
                                                           "14th",
                                                           "15th",
                                                           "Adams",
                                                           "Ash",
                                                           "Birch",
                                                           "Broadway",
                                                           "Cedar",
                                                           "Center",
                                                           "Central",
                                                           "Cherry",
                                                           "Chestnut",
                                                           "Church",
                                                           "College",
                                                           "Davis",
                                                           "Dogwood",
                                                           "East",
                                                           "Elm",
                                                           "Forest",
                                                           "Franklin",
                                                           "Green",
                                                           "Hickory",
                                                           "Highland",
                                                           "Hill",
                                                           "Hillcrest",
                                                           "Jackson",
                                                           "Jefferson",
                                                           "Johnson",
                                                           "Lake",
                                                           "Lakeview",
                                                           "Laurel",
                                                           "Lee",
                                                           "Lincoln",
                                                           "Locust",
                                                           "Madison",
                                                           "Main",
                                                           "Maple",
                                                           "Meadow",
                                                           "Mill",
                                                           "Miller",
                                                           "North",
                                                           "Oak",
                                                           "Park",
                                                           "Pine",
                                                           "Poplar",
                                                           "Railroad",
                                                           "Ridge",
                                                           "River",
                                                           "Smith",
                                                           "South",
                                                           "Spring",
                                                           "Spruce",
                                                           "Sunset",
                                                           "Sycamore",
                                                           "Taylor",
                                                           "Valley",
                                                           "View",
                                                           "Walnut",
                                                           "Washington",
                                                           "West",
                                                           "Williams",
                                                           "Willow",
                                                           "Wilson",
                                                           "Woodland", };
    private static final String[] STREET_SUFFIXES = new String[] { "Avenue",
                                                                   "Circle",
                                                                   "Court",
                                                                   "Drive",
                                                                   "Lane",
                                                                   "Place",
                                                                   "Road",
                                                                   "Terrace",
                                                                   "Trail",
                                                                   "Way" };
    private static final String[] VENDORS = new String[] { "Levi’s",
                                                           "Acrylick",
                                                           "Calvin Klein",
                                                           "Armani",
                                                           "Diesel",
                                                           "CLSC",
                                                           "Fred Perry",
                                                           "J.Crew",
                                                           "Hugo Boss",
                                                           "Carhartt",
                                                           "Dior",
                                                           "Guess",
                                                           "ZARA",
                                                           "H & M",
                                                           "Versace",
                                                           "Chanel",
                                                           "Prada",
                                                           "Gucci",
                                                           "Nununu",
                                                           "TinyCottons",
                                                           "Gymboree",
                                                           "Izod",
                                                           "Gap",
                                                           "Nike",
                                                           "Polo",
                                                           "Adidas",
                                                           "Aeropostale",
                                                           "Lacoste",
                                                           "Puma",
                                                           "Bellerose",
                                                           "Converse", };

    static {
        { // load name files
            try {
                System.out.print( "Loading female first names ... " );
                loadNameFile( FEMALE_FIRST_NAMES_FILE, FEMALE_NAMES );
                System.out.println( "done." );
                System.out.println( "\tNumber of female first names = " + FEMALE_NAMES.size() );

                System.out.print( "Loading male first names ... " );
                loadNameFile( MALE_FIRST_NAMES_FILE, MALE_NAMES );
                System.out.println( "done." );
                System.out.println( "\tNumber of male first names = " + MALE_NAMES.size() );

                System.out.print( "Loading last names ... " );
                loadNameFile( LAST_NAMES_FILE, LAST_NAMES );
                System.out.println( "done." );
                System.out.println( "\tNumber of last names = " + LAST_NAMES.size() );
            } catch ( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        { // load cities
            try {
                System.out.print( "Loading cities ... " );
                loadCities();
                System.out.println( "done." );
                System.out.println( "\tNumber of cities = " + CITIES.size() );
            } catch ( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        { // load area codes after cities
            for ( final Entry< String, State > entry : STATES.entrySet() ) {
                final State state = entry.getValue();

                switch ( entry.getKey() ) {
                    case "AL": // ALABAMA
                        state.setAreaCodes( new Integer[] { 205, 251, 256, 334, 938 } );
                        break;
                    case "AK": // ALASKA
                        state.setAreaCodes( new Integer[] { 907 } );
                        break;
                    case "AZ": // ARIZONA
                        state.setAreaCodes( new Integer[] { 480, 520, 602, 623, 928 } );
                        break;
                    case "AR": // ARKANSAS
                        state.setAreaCodes( new Integer[] { 327, 479, 501, 870 } );
                        break;
                    case "CA": // CALIFORNIA
                        state.setAreaCodes( new Integer[] { 209,
                                                            213,
                                                            310,
                                                            323,
                                                            408,
                                                            415,
                                                            424,
                                                            442,
                                                            510,
                                                            530,
                                                            559,
                                                            562,
                                                            619,
                                                            626,
                                                            628,
                                                            650,
                                                            657,
                                                            661,
                                                            669,
                                                            707,
                                                            714,
                                                            747,
                                                            760,
                                                            805,
                                                            818,
                                                            831,
                                                            858,
                                                            909,
                                                            916,
                                                            925,
                                                            949,
                                                            951 } );
                        break;
                    case "CO": // COLORADO
                        state.setAreaCodes( new Integer[] { 303, 719, 720, 970 } );
                        break;
                    case "CT": // CONNECTICUT
                        state.setAreaCodes( new Integer[] { 203, 475, 860, 959 } );
                        break;
                    case "DC": // DISTRICT OF COLUMBIA
                        state.setAreaCodes( new Integer[] { 202 } );
                    case "DE": // DELAWARE
                        state.setAreaCodes( new Integer[] { 302 } );
                        break;
                    case "FL": // FLORIDA
                        state.setAreaCodes( new Integer[] { 239,
                                                            305,
                                                            321,
                                                            352,
                                                            386,
                                                            407,
                                                            561,
                                                            727,
                                                            754,
                                                            772,
                                                            786,
                                                            813,
                                                            850,
                                                            863,
                                                            904,
                                                            941,
                                                            954 } );
                        break;
                    case "GA": // GEORGIA
                        state.setAreaCodes( new Integer[] { 229, 404, 470, 478, 678, 706, 762, 770, 912 } );
                        break;
                    case "HI": // HAWAII
                        state.setAreaCodes( new Integer[] { 808 } );
                        break;
                    case "ID": // IDAHO
                        state.setAreaCodes( new Integer[] { 208, 986 } );
                        break;
                    case "IL": // ILLINOIS
                        state.setAreaCodes( new Integer[] { 217,
                                                            224,
                                                            309,
                                                            312,
                                                            331,
                                                            447,
                                                            464,
                                                            618,
                                                            630,
                                                            708,
                                                            730,
                                                            773,
                                                            779,
                                                            815,
                                                            847,
                                                            872 } );
                        break;
                    case "IN": // INDIANA
                        state.setAreaCodes( new Integer[] { 219, 260, 317, 463, 574, 765, 812, 930 } );
                        break;
                    case "IA": // IOWA
                        state.setAreaCodes( new Integer[] { 319, 515, 563, 641, 712 } );
                        break;
                    case "KS": // KANSAS
                        state.setAreaCodes( new Integer[] { 316, 620, 785, 913 } );
                        break;
                    case "KY": // KENTUCKY
                        state.setAreaCodes( new Integer[] { 270, 364, 502, 606, 859 } );
                        break;
                    case "LA": // LOUISIANA
                        state.setAreaCodes( new Integer[] { 225, 318, 337, 504, 985 } );
                        break;
                    case "ME": // MAINE
                        state.setAreaCodes( new Integer[] { 207 } );
                        break;
                    case "MD": // MARYLAND
                        state.setAreaCodes( new Integer[] { 227, 240, 301, 410, 443, 667 } );
                        break;
                    case "MA": // MASSACHUSETTS
                        state.setAreaCodes( new Integer[] { 339, 351, 413, 508, 617, 774, 781, 857, 978 } );
                        break;
                    case "MI": // MICHIGAN
                        state.setAreaCodes( new Integer[] { 231,
                                                            248,
                                                            269,
                                                            313,
                                                            517,
                                                            586,
                                                            616,
                                                            734,
                                                            810,
                                                            906,
                                                            947,
                                                            989 } );
                        break;
                    case "MN": // MINNESOTA
                        state.setAreaCodes( new Integer[] { 218, 320, 507, 612, 651, 763, 952 } );
                        break;
                    case "MS": // MISSISSIPPI
                        state.setAreaCodes( new Integer[] { 228, 601, 662, 769 } );
                        break;
                    case "MO": // MISSOURI
                        state.setAreaCodes( new Integer[] { 314, 417, 573, 636, 660, 816, 975 } );
                        break;
                    case "MT": // MONTANA
                        state.setAreaCodes( new Integer[] { 406 } );
                        break;
                    case "NE": // NEBRASKA
                        state.setAreaCodes( new Integer[] { 308, 402, 531 } );
                        break;
                    case "NV": // NEVADA
                        state.setAreaCodes( new Integer[] { 702, 725, 775 } );
                        break;
                    case "NH": // NEW HAMPSHIRE
                        state.setAreaCodes( new Integer[] { 603 } );
                        break;
                    case "NJ": // NEW JERSEY
                        state.setAreaCodes( new Integer[] { 201, 551, 609, 732, 848, 856, 862, 908, 973 } );
                        break;
                    case "NM": // NEW MEXICO
                        state.setAreaCodes( new Integer[] { 505, 575 } );
                        break;
                    case "NY": // NEW YORK
                        state.setAreaCodes( new Integer[] { 212,
                                                            315,
                                                            332,
                                                            347,
                                                            516,
                                                            518,
                                                            585,
                                                            607,
                                                            631,
                                                            646,
                                                            680,
                                                            716,
                                                            718,
                                                            845,
                                                            914,
                                                            917,
                                                            929,
                                                            934 } );
                        break;
                    case "NC": // NORTH CAROLINA
                        state.setAreaCodes( new Integer[] { 252, 336, 704, 743, 828, 910, 919, 980, 984 } );
                        break;
                    case "ND": // NORTH DAKOTA
                        state.setAreaCodes( new Integer[] { 701 } );
                        break;
                    case "OH": // OHIO
                        state.setAreaCodes( new Integer[] { 216,
                                                            220,
                                                            234,
                                                            283,
                                                            330,
                                                            380,
                                                            419,
                                                            440,
                                                            513,
                                                            567,
                                                            614,
                                                            740,
                                                            937 } );
                        break;
                    case "OK": // OKLAHOMA
                        state.setAreaCodes( new Integer[] { 405, 539, 580, 918 } );
                        break;
                    case "OR": // OREGON
                        state.setAreaCodes( new Integer[] { 458, 503, 541, 971 } );
                        break;
                    case "PA": // PENNSYLVANIA
                        state.setAreaCodes( new Integer[] { 215, 267, 272, 412, 484, 570, 610, 717, 724, 814, 878 } );
                        break;
                    case "RI": // RHODE ISLAND
                        state.setAreaCodes( new Integer[] { 401 } );
                        break;
                    case "SC": // SOUTH CAROLINA
                        state.setAreaCodes( new Integer[] { 803, 843, 854, 864 } );
                        break;
                    case "SD": // SOUTH DAKOTA
                        state.setAreaCodes( new Integer[] { 605 } );
                        break;
                    case "TN": // TENNESSEE
                        state.setAreaCodes( new Integer[] { 423, 615, 629, 731, 865, 901, 931 } );
                        break;
                    case "TX": // TEXAS
                        state.setAreaCodes( new Integer[] { 210,
                                                            214,
                                                            254,
                                                            281,
                                                            325,
                                                            346,
                                                            361,
                                                            409,
                                                            430,
                                                            432,
                                                            469,
                                                            512,
                                                            682,
                                                            713,
                                                            737,
                                                            806,
                                                            817,
                                                            830,
                                                            832,
                                                            903,
                                                            915,
                                                            936,
                                                            940,
                                                            956,
                                                            972,
                                                            979 } );
                        break;
                    case "UT": // UTAH
                        state.setAreaCodes( new Integer[] { 385, 435, 801 } );
                        break;
                    case "VT": // VERMONT
                        state.setAreaCodes( new Integer[] { 802 } );
                        break;
                    case "VA": // VIRGINIA
                        state.setAreaCodes( new Integer[] { 276, 434, 540, 571, 703, 757, 804 } );
                        break;
                    case "WA": // WASHINGTON
                        state.setAreaCodes( new Integer[] { 206, 253, 360, 425, 509, 564 } );
                        break;
                    case "WV": // WEST VIRGINIA
                        state.setAreaCodes( new Integer[] { 304, 681 } );
                        break;
                    case "WI": // WISCONSIN
                        state.setAreaCodes( new Integer[] { 262, 274, 414, 534, 608, 715, 920 } );
                        break;
                    case "WY": // WYOMING"
                        state.setAreaCodes( new Integer[] { 307 } );
                        break;
                    default:
                        throw new RuntimeException( "Unexpected state abbreviation of " + state.getAbbreviation() );
                }
            }
        }
    }

    private static void loadCities() throws Exception {
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

            State state = STATES.get( stateAbbreviation );

            // load state if necessary
            if ( state == null ) {
                state = new State( stateName,
                                   stateAbbreviation );
                STATES.put( stateAbbreviation, state );
                STATES_AS_LIST.add( state );
            }

            final City place = new City( postalCode,
                                         city,
                                         state,
                                         county,
                                         latitude,
                                         longitude );
            CITIES.add( place );
        }
    }

    private static void loadNameFile( final String fileName,
                                      final List< String > names ) throws Exception {
        final Path input = Paths.get( fileName );
        final String content = new String( Files.readAllBytes( input ) );

        for ( final String line : content.split( "\n" ) ) {
            final int index = line.indexOf( ' ' );
            names.add( line.substring( 0, index ) );
        }
    }

    private final Map< String, Void > names = new HashMap<>();
    private final Random random;

    public RandomGenerator() throws Exception {
        this.random = new Random();
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

    public String nextAddressLine1() {
        final int number = next( 1, 900 );
        final String street = nextStreet();
        final String suffix = next( STREET_SUFFIXES );
        return ( number + " " + street + ' ' + suffix );
    }

    public int nextAreaCode( final State state ) {
        return next( state.getAreaCodes() );
    }

    public City nextCity() throws Exception {
        return next( CITIES );
    }

    public String nextName() throws Exception {
        final boolean female = next();
        String first = ( female ? next( FEMALE_NAMES ) : next( MALE_NAMES ) );
        String last = next( LAST_NAMES );
        String name = ( first + ' ' + last );

        while ( this.names.containsKey( name ) ) {
            first = ( female ? next( FEMALE_NAMES ) : next( MALE_NAMES ) );
            last = next( LAST_NAMES );
            name = ( first + ' ' + last );
        }

        this.names.put( name, null );
        return name;
    }

    public String nextPhoneNumber( final State state ) {
        final StringBuilder builder = new StringBuilder();
        builder.append( '(' ).append( nextAreaCode( state ) ).append( ')' );

        for ( int i = 0; i < 3; ++i ) {
            builder.append( next( 0, 9 ) );
        }

        builder.append( '-' );

        for ( int i = 0; i < 4; ++i ) {
            builder.append( next( 0, 9 ) );
        }

        return builder.toString();
    }

    public float nextPrice( final float min,
                            final float max ) {
        return ( Math.round( next( min, max ) * 100 ) / 100 );
    }

    public String nextProduct() {
        return next( PRODUCTS );
    }

    public String nextSize() {
        return next( SIZES );
    }

    public State nextState() {
        return next( STATES_AS_LIST );
    }

    public String nextStreet() {
        return next( STREETS );
    }

    public String nextVendor() {
        return next( VENDORS );
    }

    public static class State {

        private final String abbreviation;
        private Integer[] areaCodes;
        private final String name;

        public State( final String name,
                      final String abbreviation ) {
            this.name = name;
            this.abbreviation = abbreviation;
        }

        @Override
        public boolean equals( final Object obj ) {
            if ( ( obj == null ) || !getClass().equals( obj.getClass() ) ) {
                return false;
            }

            final State that = ( State ) obj;
            return ( Objects.equals( this.abbreviation, that.abbreviation ) && Objects.equals( this.name, that.name )
                     && Arrays.equals( this.areaCodes, that.areaCodes ) );
        }

        public String getAbbreviation() {
            return this.abbreviation;
        }

        public Integer[] getAreaCodes() {
            return this.areaCodes;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return Objects.hash( this.abbreviation, this.name, this.areaCodes );
        }

        public void setAreaCodes( final Integer[] areaCodes ) {
            this.areaCodes = areaCodes;
        }

        @Override
        public String toString() {
            return ( "State: name = " + this.name + ", abbreviation = " + this.abbreviation );
        }

    }

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

}
