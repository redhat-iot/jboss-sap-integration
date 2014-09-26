package com.example.demo_dv_sap.data.json.objects;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.example.demo_dv_sap.data.json.JSONParser;
import com.example.demo_dv_sap.model.TerminalMap;

public class AirportMaps {
	
	// url to make request
	private static String url = "http://10.0.2.2:8080/odata/flight/myflight.airport_maps?$format=JSON";
	 
	// JSON Node names
	private static final String TAG_RESULTS = "results";
	private static final String TAG_IATA = "iata";
	private static final String TAG_TITLE = "image_title";
	private static final String TAG_SUBTITLE = "image_subtitle";
	private static final String TAG_IMAGE_NAME = "image_name";
	private static final String TAG_SEQUENCE = "sequence";
	 
	// results JSONArray
	static JSONArray results = null;
	
	Map<String, TerminalMap> map = new TreeMap<String,TerminalMap>();
	
	public Map<String, TerminalMap> getMap() {
		return map;
	}

	public void getResults(String AIRPORT_IATA) {
		// Creating JSON Parser instance
		JSONParser jParser = new JSONParser();
		
		// getting JSON string from URL
		JSONObject json = jParser.getJSONFromUrl(url, true);
		 
		try {
			
		    // Getting Array of Results
		    results = json.getJSONObject("d").getJSONArray(TAG_RESULTS);
		     
		    // looping through All Results
		    for(int i = 0; i < results.length(); i++){
		        JSONObject c = results.getJSONObject(i);
		         
		        // Storing each json item in variable
		        String iata = c.getString(TAG_IATA);
		        if (!AIRPORT_IATA.equals(iata)){
		        	continue;
		        }
		        String title = c.getString(TAG_TITLE);
		        String subtitle = c.getString(TAG_SUBTITLE);
		        String imageName = c.getString(TAG_IMAGE_NAME);
		        String sequence = c.getString(TAG_SEQUENCE);
		        map.put(sequence+iata,  new TerminalMap(imageName, title, subtitle));
		         
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	}
	
	public class JSONExecutor extends AsyncTask<String, AirportMaps, Object> {
		String url;
		
	    @Override
	    protected void onPreExecute() {
	    }
	    
	    @Override
		protected void onPostExecute(Object result) {
	    	AirportMaps maps = new AirportMaps();
	    	maps.getResults(url);
			super.onPostExecute(result);
		}
	    
		@Override
		protected String doInBackground(String... params) {
			url = (String)params[0];
			return null;
		}
	}
	
}
