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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A representation of an {@link Airport} that can be passed into an intent extra.
 */
public class AirportParcelable implements Parcelable {

    /**
     * Used to un-marshal or de-serialize an airport from a parcel.
     */
    public static final Creator<AirportParcelable> CREATOR = new Creator<AirportParcelable>() {

        /**
         * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
         */
        @Override
        public AirportParcelable createFromParcel( final Parcel in ) {
            return new AirportParcelable(in);
        }

        /**
         * @see android.os.Parcelable.Creator#newArray(int)
         */
        @Override
        public AirportParcelable[] newArray( final int size ) {
            return new AirportParcelable[size];
        }

    };

    /**
     * The parcelabale identifier for the selected airport from the detail screen.
     */
    public static final String SELECTED_AIRPORT = "selected_airport"; //$NON-NLS-1$

    private final Airport airport;

    /**
     * @param airportModel the airport model (cannot be <code>null</code>)
     */
    public AirportParcelable( final Airport airportModel ) {
        this.airport = airportModel;
    }

    AirportParcelable( final Parcel in ) {
        final String iata = in.readString();
        final String city = in.readString();
        final String state = in.readString();
        final String country = in.readString();
        final double[] latLong = new double[2];
        in.readDoubleArray(latLong);

        this.airport = new Airport(city, state, country, iata, latLong[0], latLong[1]);
    }

    /**
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * @return the airport model object (never <code>null</code>)
     */
    public Airport getAirport() {
        return this.airport;
    }

    /**
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel( final Parcel dest,
                               final int flags ) {
        dest.writeString(this.airport.getCity());
        dest.writeString(this.airport.getState());
        dest.writeString(this.airport.getCountry());
        dest.writeString(this.airport.getIata());
        dest.writeDoubleArray(this.airport.getCoordinates());
    }

}
