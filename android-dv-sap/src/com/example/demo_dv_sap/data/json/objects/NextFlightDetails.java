package com.example.demo_dv_sap.data.json.objects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.demo_dv_sap.data.json.JSONParser;
import com.example.demo_dv_sap.model.Flight;

public class NextFlightDetails {

	// url to make request
	private String url = null;

	// JSON Node names
	private static final String TAG_RESULTS = "results";
	private static final String TAG_AIRLINE_NAME = "airlineName";
	private static final String TAG_DEPART_TIME = "departTime";
	// private static final String TAG_ACTUAL_DEPART_TIME = "actualDepartTime";
	private static final String TAG_ARRIVE_TIME = "arriveTime";
	// private static final String TAG_ACTUAL_ARRIVE_TIME = "actualArriveTime";
	private static final String TAG_AIRLINE_IATA = "airline_iata";
	private static final String TAG_AIRPORT_FROM = "flightDetails_airportFrom";
	private static final String TAG_AIRPORT_TO = "toAirport";
	private static final String TAG_FLIGHT_NUMBER = "flightNo";
	private static final String TAG_TERMINAL = "terminal";
	private static final String TAG_FLIGHTDATE = "flightDate";
	private static final String TAG_STATUS = "status";
	private static final String TAG_GATE = "gate";

	// results JSONObject
	static JSONArray results = null;

	Flight flight = null;
	
	List<Flight> flights = new ArrayList<Flight>();

	public List<Flight> getFlights() {
		return flights;
	}

	public List<Flight> getResults(String pFlightDate) {
		url = String
				.format("http://10.0.2.2:8080/odata/flight/AllFlightDataModel.AllFlightDataTable?$filter=flightDate eq '%s'&$format=JSON",
						pFlightDate);

		url = url.replaceAll(" ", "%20");
		// Creating JSON Parser instance
		JSONParser jParser = new JSONParser();

		// getting JSON string from URL
		JSONObject json = jParser.getJSONFromUrl(url, true);

		try {

			// Getting Array of Results
			results = json.getJSONObject("d").getJSONArray(TAG_RESULTS);

			// looping through All Results
			for (int i = 0; i < results.length(); i++) {
				JSONObject c = results.getJSONObject(i);

				// Storing each json item in variable
				String airlineName = c.getString(TAG_AIRLINE_NAME);
				String departTimeXsdDuration = c.getString(TAG_DEPART_TIME);
				// String actualDepartTime =
				// result.getString(TAG_ACTUAL_DEPART_TIME);
				String arriveTimeXsdDuration = c.getString(TAG_ARRIVE_TIME);
				// String actualArriveTime =
				// result.getString(TAG_ACTUAL_ARRIVE_TIME);
				String airline_iata = c.getString(TAG_AIRLINE_IATA);
				String airportFrom = c.getString(TAG_AIRPORT_FROM);
				String airportTo = c.getString(TAG_AIRPORT_TO);
				String flightNumber = c.getString(TAG_FLIGHT_NUMBER);
				String terminal = c.getString(TAG_TERMINAL);
				String flightDate = c.getString(TAG_FLIGHTDATE);
				String status = c.getString(TAG_STATUS);
				String gate = c.getString(TAG_GATE);
				flight = new Flight(airlineName, airline_iata, flightNumber,
						jParser.parseTime(departTimeXsdDuration), departTimeXsdDuration,
						airportFrom, jParser.parseTime(arriveTimeXsdDuration),
						arriveTimeXsdDuration, airportTo, terminal, flightDate,
						gate, status);
				flights.add(flight);
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return flights;
	}

}
