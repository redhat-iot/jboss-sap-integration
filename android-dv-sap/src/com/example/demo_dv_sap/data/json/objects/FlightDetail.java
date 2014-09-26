package com.example.demo_dv_sap.data.json.objects;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.demo_dv_sap.data.json.JSONParser;
import com.example.demo_dv_sap.model.Flight;

public class FlightDetail {

	// url to make request
	private String url = null; 
	
	// JSON Node names
	private static final String TAG_AIRLINE_NAME = "airlineName";
	private static final String TAG_DEPART_TIME = "departTime";
	//private static final String TAG_ACTUAL_DEPART_TIME = "actualDepartTime";
	private static final String TAG_ARRIVE_TIME = "arriveTime";
	//private static final String TAG_ACTUAL_ARRIVE_TIME = "actualArriveTime";
	private static final String TAG_AIRLINE_IATA = "airline_iata";
	private static final String TAG_AIRPORT_FROM = "d:airportFrom";
	private static final String TAG_AIRPORT_TO = "toAirport";
	//private static final String TAG_FLIGHT_DATE = "flightDate";
	private static final String TAG_FLIGHT_NUMBER = "flightNo";
	private static final String TAG_TERMINAL = "terminal";
	private static final String TAG_FLIGHTDATE = "flightDate";
	private static final String TAG_STATUS = "status";
	private static final String TAG_GATE = "gate";

	// results JSONObject
	static JSONObject result = null;

	Flight flight = null;

	public Flight getFlight() {
		return flight;
	}

	public void getResults(String pIata, String pDepartTime, String pArriveTime, String pFlightDate, String pFromAirport, String pToAirport ) {
		
		url = String.format("http://10.0.2.2:8080/odata/flight/AllFlightDataModel.AllFlightDataTable(airline_iata='%s',arriveTime='%s',departTime='%s',flightDate='%s',fromAirport='%s',toAirport='%s')?$format=JSON", pIata, pArriveTime, pDepartTime, pFlightDate, pFromAirport, pToAirport);
	
		// Creating JSON Parser instance
		JSONParser jParser = new JSONParser();

		// getting JSON string from URL
		JSONObject json = jParser.getJSONFromUrl(url, true);

		try {

			// Getting Result
			result = json.getJSONObject("d");
			
			
			// Storing each json item in variable
			String airlineName = result.getString(TAG_AIRLINE_NAME);
			String departTimeXsdDuration = result.getString(TAG_DEPART_TIME);
		//	String actualDepartTime = result.getString(TAG_ACTUAL_DEPART_TIME);
			String arriveTimeXsdDuration = result.getString(TAG_ARRIVE_TIME);
		//	String actualArriveTime = result.getString(TAG_ACTUAL_ARRIVE_TIME);
			String airline_iata = result.getString(TAG_AIRLINE_IATA);
			String airportFrom = result.getString(TAG_AIRPORT_FROM);
			String airportTo = result.getString(TAG_AIRPORT_TO);
			String flightNumber = result.getString(TAG_FLIGHT_NUMBER);
			String terminal = result.getString(TAG_TERMINAL);
			String flightDate = result.getString(TAG_FLIGHTDATE);
			String status = result.getString(TAG_STATUS);
			String gate = result.getString(TAG_GATE);
			flight = new Flight(airlineName, airline_iata, flightNumber, departTimeXsdDuration, departTimeXsdDuration, airportFrom, arriveTimeXsdDuration, arriveTimeXsdDuration, airportTo, terminal, flightDate, gate, status );

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
