/*
 * Copyright 2013 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.example.demo_dv_sap.model;

import java.util.Comparator;

/**
 * An immutable airport business object.
 */
public class Airport {

    private static final double[] DEFAULT_LAT_LONG = new double[] {29.2, 81.0333}; // TODO remove this

    /**
     * Sorts airports by their IATA code.
     */
    public static final Comparator<Airport> IATA_SORTER = new Comparator<Airport>() {

        /**
         * @param thisAirport the first airport being checked (cannot be <code>null</code>)
         * @param thatAirport the other airport being checked (cannot be <code>null</code>)
         * @return the compare result
         */
        @Override
        public int compare( final Airport thisAirport,
                            final Airport thatAirport ) {
            return thisAirport.getIata().compareTo(thatAirport.getIata());
        }
    };

    /**
     * Sorts airports by country, state, city, then IATA code.
     */
    public static final Comparator<Airport> LOCATION_SORTER = new Comparator<Airport>() {

        /**
         * @param thisAirport the first airport being checked (cannot be <code>null</code>)
         * @param thatAirport the other airport being checked (cannot be <code>null</code>)
         * @return the compare result
         */
        @Override
        public int compare( final Airport thisAirport,
                            final Airport thatAirport ) {
            int result = thisAirport.getCountry().compareTo(thatAirport.getCountry());

            if (result == 0) {
                result = thisAirport.getState().compareTo(thatAirport.getState());

                if (result == 0) {
                    result = thisAirport.getCity().compareTo(thatAirport.getCity());

                    if (result == 0) {
                        result = thisAirport.getIata().compareTo(thatAirport.getIata());
                    }
                }
            }

            return result;
        }
    };

    private final String city;

    private double[] coordinates = DEFAULT_LAT_LONG;

    private final String country;

    private final String iata;

    private final String state;

    /**
     * @param airportCity the city (cannot be <code>null</code> or empty)
     * @param airportState the state (cannot be <code>null</code> or empty)
     * @param airportCountry the country (cannot be <code>null</code> or empty)
     * @param airportIata the IATA code (cannot be <code>null</code> or empty)
     */
    public Airport( final String airportCity,
                    final String airportState,
                    final String airportCountry,
                    final String airportIata ) {
        // TODO remove this constructor when all the airports have a lat/long
        this(airportCity, airportState, airportCountry, airportIata, DEFAULT_LAT_LONG[0], DEFAULT_LAT_LONG[1]);
    }

    /**
     * @param airportCity the city (cannot be <code>null</code> or empty)
     * @param airportState the state (cannot be <code>null</code> or empty)
     * @param airportCountry the country (cannot be <code>null</code> or empty)
     * @param airportIata the IATA code (cannot be <code>null</code> or empty)
     * @param airportLat the airport's latitide coordinate
     * @param airportLong the airport's longitude coordinate
     */
    public Airport( final String airportCity,
                    final String airportState,
                    final String airportCountry,
                    final String airportIata,
                    final double airportLat,
                    final double airportLong) {
        this.city = airportCity;
        this.state = airportState;
        this.country = airportCountry;
        this.iata = airportIata;
        this.coordinates = new double[] {airportLat, airportLong};
    }

    /**
     * @return the city (never <code>null</code> or empty)
     */
    public String getCity() {
        return this.city;
    }

    /**
     * @return the airport's lat/long coordinates
     */
    public double[] getCoordinates() {
        return this.coordinates;
    }

    /**
     * @return the country (never <code>null</code> or empty)
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * @return the IATA code (never <code>null</code> or empty)
     */
    public String getIata() {
        return this.iata;
    }

    /**
     * @return the state (never <code>null</code> or empty)
     */
    public String getState() {
        return this.state;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO find a way to use the strings.xml for this
        return ('(' + this.iata + ") " + this.city + ", " + this.state + ", " + this.country); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
