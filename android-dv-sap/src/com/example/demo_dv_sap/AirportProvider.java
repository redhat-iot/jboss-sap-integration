/*
 * Copyright 2013 JBoss Inc
 * 
 * Licensed under the Apache License","Version 2.0","the"License")); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing","software distributed under the License is distributed on
 * an"AS IS" BASIS","WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND","either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.example.demo_dv_sap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.demo_dv_sap.model.Airport;

/**
 * Provides the available airports.
 */
public final class AirportProvider {

    /**
     * All the available airports.
     */
    public static final List<Airport> AIRPORTS;

    static {
        AIRPORTS = new ArrayList<Airport>();
        AIRPORTS.add(new Airport("Allentown", "PA", "USA", "ABE")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Abilene", "TX", "USA", "ABI")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Albuquerque", "NM", "USA", "ABQ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Augusta", "ME", "USA", "AUG")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Austin", "TX", "USA", "AUS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Huntsville", "AL", "USA", "HSV")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Huntington", "WV", "USA", "HTS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Jonesboro", "AR", "USA", "JBR")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("New York", "NY", "USA", "JFK")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Kapalua, Maui", "HI", "USA", "JHM")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Rutland", "VT", "USA", "RUT")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("San Francisco", "CA", "USA", "SFO")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Fort Walton Beach", "FL", "USA", "VPS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Springfield", "VT", "USA", "VSF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Washington", "DC", "USA", "WAS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Enid", "OK", "USA", "WDG")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Wrangell", "AK", "USA", "WRG")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Worland", "WY", "USA", "WRL")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("West Yellowstone", "MT", "USA", "WYS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        AIRPORTS.add(new Airport("Fayetteville", "AR", "USA", "XNA")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        Collections.sort(AIRPORTS, Airport.IATA_SORTER);
    }

}
