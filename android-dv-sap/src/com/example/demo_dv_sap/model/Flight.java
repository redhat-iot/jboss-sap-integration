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

/**
 * An immutable flight business object.
 */
public final class Flight { 

    private final String arrivalAirportCode;

    private final String arrivalGate;

    private final String arrivalTerminal;

    private final String arrivalTime;
    
    private final String arrivalTimeXsdDuration;

    private final String carrier;

	private final String departureAirportCode;

    private final String departureTime;
    
    private final String departureTimeXsdDuration;

    private final String flightNumber;
    
    private final String flightDate;

    private final String iata;

    private final String status;

    /**
     * @param theCarrier the carrier (cannot be <code>null</code> or empty)
     * @param theIata the carrier IATA code (cannot be <code>null</code> or empty)
     * @param theFlightNumber the carrier (cannot be <code>null</code> or empty)
     * @param theDepartureTime the carrier (cannot be <code>null</code> or empty)
     * @param theDepartureAirportCode the carrier (cannot be <code>null</code> or empty)
     * @param theArrivalTime the carrier (cannot be <code>null</code> or empty)
     * @param theArrivalAirportCode the carrier (cannot be <code>null</code> or empty)
     * @param theArrivalTerminal the carrier (cannot be <code>null</code> or empty)
     * @param theArrivalGate the carrier (cannot be <code>null</code> or empty)
     * @param theFlightStatus the flight status (cannot be <code>null</code> or empty)
     */
    public Flight( final String theCarrier,
                   final String theIata,
                   final String theFlightNumber,
                   final String theDepartureTime,
                   final String theDepartureTimeXsdDuration,
                   final String theDepartureAirportCode,
                   final String theArrivalTime,
                   final String theArrivalTimeXsdDuration,
                   final String theArrivalAirportCode,
                   final String theArrivalTerminal,
                   final String theFlightDate,
                   final String theArrivalGate,
                   final String theFlightStatus ) {
        this.carrier = theCarrier;
        this.iata = theIata;
        this.flightNumber = theFlightNumber;
        this.departureTime = theDepartureTime;
        this.departureTimeXsdDuration = theDepartureTimeXsdDuration;
        this.departureAirportCode = theDepartureAirportCode;
        this.arrivalTime = theArrivalTime;
        this.arrivalTimeXsdDuration = theArrivalTimeXsdDuration;
        this.arrivalAirportCode = theArrivalAirportCode;
        this.arrivalTerminal = theArrivalTerminal;
        this.flightDate = theFlightDate;
        this.arrivalGate = theArrivalGate;
        this.status = theFlightStatus;
    }

    /**
	 * @return the flightDate
	 */
	public String getFlightDate() {
		return flightDate;
	}

	/**
     * @return the airport code of the arrival location (never <code>null</code> or empty)
     */
    public String getArrivalAirportCode() {
        return this.arrivalAirportCode;
    }

    /**
     * @return the arrival gate (never <code>null</code> or empty)
     */
    public String getArrivalGate() {
        return this.arrivalGate;
    }

    /**
     * @return the arrival time (never <code>null</code> or empty)
     */
    public String getArrivalTerminal() {
        return this.arrivalTerminal;
    }

    /**
     * @return the time of arrival (never <code>null</code> or empty)
     */
    public String getArrivalTime() {
        return this.arrivalTime;
    }

    /**
     * @return the carrier (never <code>null</code> or empty)
     */
    public String getCarrier() {
        return this.carrier;
    }

    /**
     * @return the airport code of the departure location (never <code>null</code> or empty)
     */
    public String getDepartureAirportCode() {
        return this.departureAirportCode;
    }

    /**
     * @return the departure time (never <code>null</code> or empty)
     */
    public String getDepartureTime() {
        return this.departureTime;
    }

    /**
     * @return the flight number (never <code>null</code> or empty)
     */
    public String getFlightNumber() {
        return this.flightNumber;
    }

    /**
     * @return the carrier IATA code (never <code>null</code> or empty)
     */
    public String getIata() {
        return this.iata;
    }

    /**
     * @return the flight status (never <code>null</code> or empty)
     */
    public String getStatus() {
        return this.status;
    }
    
    /**
   	 * @return the arrivalTimeXsdDuration
   	 */
   	public String getArrivalTimeXsdDuration() {
   		return arrivalTimeXsdDuration;
   	}

   	/**
   	 * @return the departureTimeXsdDuration
   	 */
   	public String getDepartureTimeXsdDuration() {
   		return departureTimeXsdDuration;
   	}

}
