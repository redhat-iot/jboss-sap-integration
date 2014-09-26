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
package com.example.demo_dv_sap;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.demo_dv_sap.R;
import com.example.demo_dv_sap.data.json.objects.NextFlightDetails;
import com.example.demo_dv_sap.model.Flight;
import com.example.demo_dv_sap.model.FlightParcelable;

/**
 * The available flights tab UI.
 */
public final class FlightsTab extends Fragment {

	static final String ID = FlightsTab.class.getSimpleName();

	private List<Flight> flights;

	private LayoutInflater inflater;

	public List<Flight> getFlights() {
		return flights;
	}

	LayoutInflater accessInflator() {
		return this.inflater;
	}

	void handleFlightClicked(final int flightIndex) {
		// TODO make sure Book It button is enabled
	}

	private List<Flight> loadFlights() {
		// final ArrayList<FlightParcelable> data = getActivity().getIntent()
		// .getParcelableArrayListExtra(FlightParcelable.ALTERNATIVE_FLIGHTS);
		//
		// if ((data == null) || data.isEmpty()) {
		// return Collections.emptyList();
		// }

		final FlightParcelable parcelable = getActivity().getIntent()
				.getExtras().getParcelable(FlightParcelable.SELECTED_FLIGHT);
		Flight selectedFlight = parcelable.getFlight();

		this.flights = new NextFlightDetails().getResults(selectedFlight
				.getFlightDate());

		return flights;
	}

	/**
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(final Bundle newSavedInstanceState) {
		super.onActivityCreated(newSavedInstanceState);
		setRetainInstance(true);
	}

	/**
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(final LayoutInflater layoutInflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		this.inflater = layoutInflater;
		final View flightsTab = this.inflater.inflate(R.layout.flights_tab,
				container, false);

		final ListView listView = (ListView) flightsTab
				.findViewById(R.id.flight_details_list);
		final FlightsAdapter adapter = new FlightsAdapter(getActivity(),
				R.layout.flight_details_row, loadFlights());
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			/**
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
			 *      android.view.View, int, long)
			 */
			@Override
			public void onItemClick(final AdapterView<?> adapterView,
					final View parent, final int position, final long id) {
				handleFlightClicked(position);
			}
		});

		return flightsTab;
	}

	static class FlightHolder {

		TextView txtArrivalAirport;
		TextView txtArrivalTime;
		TextView txtCarrier;
		TextView txtDepartureAirport;
		TextView txtDepartureIata;
		TextView txtDepartureTime;
		TextView txtFlightNumber;
		TextView txtGate;
		TextView txtStatus;
		TextView txtTerminal;

	}

	class FlightsAdapter extends ArrayAdapter<Flight> {

		final Context activity;
		final List<Flight> flights;

		FlightsAdapter(final Context currentContext, final int resourceId,
				final List<Flight> alternateFlights) {
			super(currentContext, resourceId, alternateFlights);
			this.activity = currentContext;
			this.flights = alternateFlights;
		}

		/**
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View row = convertView;
			FlightHolder holder = null;

			if (row == null) {
				row = accessInflator().inflate(R.layout.flight_details_row,
						parent, false);

				holder = new FlightHolder();
				holder.txtDepartureIata = (TextView) row
						.findViewById(R.id.departure_iata);
				holder.txtCarrier = (TextView) row
						.findViewById(R.id.departure_airlines);
				holder.txtFlightNumber = (TextView) row
						.findViewById(R.id.departure_flight_number);
				holder.txtDepartureTime = (TextView) row
						.findViewById(R.id.departure_time);
				holder.txtDepartureAirport = (TextView) row
						.findViewById(R.id.departure_airport_code);
				holder.txtArrivalTime = (TextView) row
						.findViewById(R.id.arrival_time);
				holder.txtArrivalAirport = (TextView) row
						.findViewById(R.id.arrival_airport_code);
				holder.txtGate = (TextView) row.findViewById(R.id.arrival_gate);
				holder.txtTerminal = (TextView) row
						.findViewById(R.id.arrival_terminal);
				holder.txtStatus = (TextView) row
						.findViewById(R.id.departure_status);

				row.setTag(holder);
			} else {
				holder = (FlightHolder) row.getTag();
			}

			final Flight flight = this.flights.get(position);

			holder.txtDepartureIata.setText(flight.getIata());
			holder.txtCarrier.setText(flight.getCarrier());
			holder.txtFlightNumber.setText(flight.getFlightNumber());
			holder.txtDepartureTime.setText(flight.getDepartureTime());
			holder.txtDepartureAirport
					.setText(flight.getDepartureAirportCode());
			holder.txtArrivalTime.setText(flight.getArrivalTime());
			holder.txtArrivalAirport.setText(flight.getArrivalAirportCode());
			holder.txtGate.setText(flight.getArrivalGate());
			holder.txtTerminal.setText(flight.getArrivalTerminal());
			holder.txtStatus.setText(flight.getStatus());

			return row;
		}

	}

}
