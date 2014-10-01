package com.example.demo_dv_sap.data.json.objects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.demo_dv_sap.data.json.JSONParser;
import com.example.demo_dv_sap.model.Flight;

public class FlightSearchResults {

	// url to make request
	private String url = null; 
	
	// JSON Node names
	private static final String TAG_AIRLINE_NAME = "airlineName";
	private static final String TAG_DEPART_TIME = "departTime";
	//private static final String TAG_ACTUAL_DEPART_TIME = "actualDepartTime";
	private static final String TAG_ARRIVE_TIME = "arriveTime";
	//private static final String TAG_ACTUAL_ARRIVE_TIME = "actualArriveTime";
	private static final String TAG_AIRLINE_IATA = "airline_iata";
	private static final String TAG_AIRPORT_FROM = "flightDetails_airportFrom";
	private static final String TAG_AIRPORT_TO = "toAirport";
	//private static final String TAG_FLIGHT_DATE = "flightDate";
	private static final String TAG_FLIGHT_NUMBER = "flightNo";
	private static final String TAG_TERMINAL = "terminal";
	private static final String TAG_FLIGHTDATE = "flightDate";
	private static final String TAG_STATUS = "status";
	private static final String TAG_GATE = "gate";

	// results JSONArray
	static JSONArray results = null;

	List<Flight> flights = new ArrayList<Flight>();

	public List<Flight> getFlights() {
		return flights;
	}

	public void getResults(String pFlightDate, String pFromAirport, String pToAirport ) {
		
		url = String.format("http://10.0.2.2:8080/odata/flight/AllFlightDataModel.AllFlightDataTable?$filter=flightDate eq '%s' and fromAirport eq '%s' and toAirport eq '%s'&$format=JSON", pFlightDate, pFromAirport, pToAirport);
	
		url = url.replaceAll(" ", "%20");
		
		// Creating JSON Parser instance
		JSONParser jParser = new JSONParser();

		// getting JSON string from URL
		JSONObject json = jParser.getJSONFromUrl(url, true);

		try {

			// Getting Result
			
			results = json.getJSONObject("d").getJSONArray("results");
			
			for (int i=0; i<results.length(); i++ ){
				// Storing each json item in variable
				JSONObject result = results.getJSONObject(i);
				String airlineName = result.getString(TAG_AIRLINE_NAME);
				String departTimeXsdDuration = jParser.parseTime(result.getString(TAG_DEPART_TIME));
				String arriveTimeXsdDuration = jParser.parseTime(result.getString(TAG_ARRIVE_TIME));
				String airline_iata = result.getString(TAG_AIRLINE_IATA);
				String airportFrom = result.getString(TAG_AIRPORT_FROM);
				String airportTo = result.getString(TAG_AIRPORT_TO);
				String flightNumber = result.getString(TAG_FLIGHT_NUMBER);
				String terminal = result.getString(TAG_TERMINAL);
				String flightDate = result.getString(TAG_FLIGHTDATE);
				String status = result.getString(TAG_STATUS);
				String gate = result.getString(TAG_GATE);
				flights.add(new Flight(airlineName, airline_iata, flightNumber, departTimeXsdDuration, departTimeXsdDuration, airportFrom, arriveTimeXsdDuration, arriveTimeXsdDuration, airportTo, terminal, flightDate, gate, status ));
				
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
