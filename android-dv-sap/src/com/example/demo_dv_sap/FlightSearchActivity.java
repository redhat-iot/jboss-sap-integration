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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.example.demo_dv_sap.R;
import com.example.demo_dv_sap.data.json.objects.FlightDetail;
import com.example.demo_dv_sap.data.json.objects.FlightSearchResults;
import com.example.demo_dv_sap.model.Airport;
import com.example.demo_dv_sap.model.Flight;
import com.example.demo_dv_sap.model.FlightParcelable;

/**
 * The application main screen.
 */
public final class FlightSearchActivity extends Activity implements DialogInterface.OnCancelListener {

    /**
     * A constant for no airport selected.
     */
    private static Airport _noAirport;

    private Airport arrivalAirport;

    private ArrayAdapter<Airport> arrivalAirportsAdapter;

    private final List<Flight> availableFlights = new ArrayList<Flight>();

    private AlertDialog dateDialog;

    private final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        /**
         * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
         */
        @Override
        public void onDateSet( final DatePicker view,
                               final int selectedYear,
                               final int selectedMonth,
                               final int selectedDay ) {
            handleTravelDateChanged(selectedYear, selectedMonth, selectedDay);
        }
    };

    private int day = -1;

    private Airport departureAirport;

    private ArrayAdapter<Airport> departureAirportsAdapter;

    private int month = -1;

    private TableLayout tableLayout;

    private TextView txtFlightsTableTitle;

    private TextView txtTravelDate;

    private int year = -1;

    FlightSearchActivity accessThis() {
        return this;
    }

    private void clearFlights() {
        this.tableLayout.removeAllViews();
        this.availableFlights.clear();
    }

    private void createArrivalAirportsChooser() {
        this.arrivalAirportsAdapter = new ArrayAdapter<Airport>(this, android.R.layout.simple_spinner_item,
                                                                new ArrayList<Airport>(AirportProvider.AIRPORTS)) {

            /**
             * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
             */
            @Override
            public View getView( final int position,
                                 final View view,
                                 final ViewGroup parent ) {
                final TextView textView = (TextView)super.getDropDownView(position, view, parent);

                if ((position == 0) && getDisplayNoAirportSelectedText().equals(textView.getText())) {
                    textView.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
                }

                return textView;
            }
        };
        this.arrivalAirportsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner arrivalAirportChooser = (Spinner)findViewById(R.id.arrival_airports);
        arrivalAirportChooser.setAdapter(this.arrivalAirportsAdapter);
        arrivalAirportChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView,
             *      android.view.View, int, long)
             */
            @Override
            public void onItemSelected( final AdapterView<?> spinner,
                                        final View dropDownView,
                                        final int position,
                                        final long id ) {
                handleArrivalAirportChanged(spinner);
            }

            /**
             * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
             */
            @Override
            public void onNothingSelected( final AdapterView<?> spinner ) {
                // nothing to do
            }
        });

        this.arrivalAirportsAdapter.insert(_noAirport, 0);
    }

    private void createDepartureAirportsChooser() {
        this.departureAirportsAdapter = new ArrayAdapter<Airport>(this, android.R.layout.simple_spinner_item,
                                                                  new ArrayList<Airport>(AirportProvider.AIRPORTS)) {

            /**
             * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
             */
            @Override
            public View getView( final int position,
                                 final View view,
                                 final ViewGroup parent ) {
                final TextView textView = (TextView)super.getDropDownView(position, view, parent);

                if ((position == 0) && getDisplayNoAirportSelectedText().equals(textView.getText())) {
                    textView.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
                }

                return textView;
            }

        };
        this.departureAirportsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner departureAirportChooser = (Spinner)findViewById(R.id.departure_airports);
        departureAirportChooser.setAdapter(this.departureAirportsAdapter);
        departureAirportChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView,
             *      android.view.View, int, long)
             */
            @Override
            public void onItemSelected( final AdapterView<?> spinner,
                                        final View dropDownView,
                                        final int position,
                                        final long id ) {
                handleDepartureAirportChanged(spinner);
            }

            /**
             * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
             */
            @Override
            public void onNothingSelected( final AdapterView<?> spinner ) {
                // nothing to do
            }
        });

        this.departureAirportsAdapter.insert(_noAirport, 0);
    }

    String getDisplayNoAirportSelectedText() {
        return getString(R.string.no_selection);
    }

    void handleArrivalAirportChanged( final AdapterView<?> spinner ) {
        this.arrivalAirport = (Airport)spinner.getSelectedItem();

        // remove temporary first item if still there
        if (!this.arrivalAirport.equals(_noAirport) && _noAirport.equals(this.arrivalAirportsAdapter.getItem(0))) {
            final int index = spinner.getSelectedItemPosition();
            this.arrivalAirportsAdapter.remove(this.arrivalAirportsAdapter.getItem(0));
            spinner.setSelection(index - 1);
        }

        updateEnablements();
    }

    void handleDepartureAirportChanged( final AdapterView<?> spinner ) {
        this.departureAirport = (Airport)spinner.getSelectedItem();

        // remove temporary first item if still there
        if (!this.departureAirport.equals(_noAirport) && _noAirport.equals(this.departureAirportsAdapter.getItem(0))) {
            final int index = spinner.getSelectedItemPosition();
            this.departureAirportsAdapter.remove(this.departureAirportsAdapter.getItem(0));
            spinner.setSelection(index - 1);
        }

        updateEnablements();
    }

    void handleFlightClicked( final int index ) {
        final Parcelable data = new FlightParcelable(this.availableFlights.get(index));

        final Intent intent = new Intent(this, DetailsScreen.class);
        intent.putExtra(FlightParcelable.SELECTED_FLIGHT, data);

        this.startActivity(intent);
    }

    void handleTravelDateChanged( final int newYear,
                                  final int newMonth,
                                  final int newDay ) {
        if (this.dateDialog != null) {
            this.dateDialog.dismiss();
        }

        if ((this.year != newYear) || (this.month != newMonth) || (this.day != newDay)) {
            this.year = newYear;
            this.month = newMonth;
            this.day = newDay;

            // set selected date into textview
            final Calendar date = Calendar.getInstance();

            if (this.year != -1) {
                date.set(Calendar.YEAR, this.year);
            }

            if (this.month != -1) {
                date.set(Calendar.MONTH, this.month);
            }

            if (this.day != -1) {
                date.set(Calendar.DATE, this.day);
            }

            final Date travelDate = date.getTime();
            this.txtTravelDate.setText(DateFormat.getDateInstance().format(travelDate));
            updateEnablements();
        }
    }

    private void loadFlights(final TextView txtTravelDate, final Airport departureAirport, final Airport arrivalAirport) {
        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            private ProgressDialog dialog;
            private List<Flight> flights;
            private TextView travelDate = txtTravelDate;
            private Airport departAirport = departureAirport;
            private Airport arriveAirport = arrivalAirport;

            /**
             * @see android.os.AsyncTask#doInBackground(Void[])
             */
            @Override
            protected Void doInBackground( final Void... args ) {
                // TODO get real data here
                // TODO set argument types
                this.flights = new ArrayList<Flight>();

                try {
                    if (isCancelled()) {
                        throw new InterruptedException(); // TODO add message
                    }

                    FlightSearchResults flightSearchResults = new FlightSearchResults();
                	
                	String date = this.travelDate.getText().toString();
                	
                	Date dateObj = new Date(date);
                	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.s");
                	String s = df.format(dateObj);

                	flightSearchResults.getResults(s, this.departAirport.getIata(), this.arriveAirport.getIata());
                    this.flights=flightSearchResults.getFlights();
                	
                	
//                    this.flights.add(new Flight("American Airlines", "AA", "100", "11:00 AM", "PT11H00M00S", "JFK", "2:00 PM", "PT14H01M00S", "SFO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
//                                                "1", "2014-11-26T00:00.0", "A10", "DELAYED")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//                    this.flights.add(new Flight("United", "UA", "400", "12:00 PM", "PT12H00M00S", "JFK", "3:00 PM", "PT15H00M00S","SFO", "1", "2014-11-26T00:00.0", "A10", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
//                                                "ON TIME")); //$NON-NLS-1$
                    Thread.sleep(2000);
                } catch (final Exception e) {
                    // TODO handle this
                    this.flights = Collections.emptyList();
                }
            	
                return null;
            }

            /**
             * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
             */
            @Override
            protected void onPostExecute( final Void result ) {
                if (this.dialog != null) {
                    this.dialog.dismiss();
                }

                if (!isCancelled()) {
                    setFlights(this.flights);
                }
            }

            /**
             * @see android.os.AsyncTask#onPreExecute()
             */
            @Override
            protected void onPreExecute() {
                this.dialog = ProgressDialog.show(accessThis(),
                                                  getString(R.string.available_flights_progress_dialog_title),
                                                  getString(R.string.available_flights_progress_dialog_message), true,
                                                  true, accessThis());
            }

        };

        task.execute((Void[])null);
    }

    /**
     * @param progressDialog the progress dialog displayed when obtaining flights
     */
    @Override
    public void onCancel( final DialogInterface progressDialog ) {
        clearFlights();
    }

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate( final Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flight_search_activity_main);

        if (_noAirport == null) {
            _noAirport = new Airport(null, null, null, null) {

                /**
                 * @see com.example.demo_dv_sap.model.Airport#toString()
                 */
                @Override
                public String toString() {
                    return getDisplayNoAirportSelectedText();
                }
            };
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        this.arrivalAirport = _noAirport;
        this.departureAirport = _noAirport;

        createDepartureAirportsChooser();
        createArrivalAirportsChooser();

        final TableLayout table = (TableLayout)findViewById(R.id.flights_preview_table);
        final ScrollView scroller = (ScrollView)table.findViewById(R.id.flights_preview_table_scroller);
        this.tableLayout = (TableLayout)scroller.findViewById(R.id.flights_preview_table_data);
        this.txtTravelDate = (TextView)findViewById(R.id.travel_date);
        this.txtFlightsTableTitle = (TextView)findViewById(R.id.flights_preview_table_title);
    }

    void setFlights( final List<Flight> flights ) {
        if (flights == null || flights.isEmpty()) {
            this.txtFlightsTableTitle.setText(R.string.no_available_flights);
        } else {
            this.txtFlightsTableTitle.setText(R.string.available_flights);
            int i = 0;

            for (final Flight flight : flights) {
                this.availableFlights.add(flight);
                final TableRow row = new TableRow(this);
                row.setClickable(true);
                row.setFocusableInTouchMode(true);
                row.setBackgroundResource(android.R.drawable.list_selector_background);
                row.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                     android.R.attr.listPreferredItemHeight));

                { // departure time
                    final TextView textView = new TextView(this);
                    textView.setPadding(10, 4, 4, 10);
                    textView.setText(flight.getDepartureTime());

                    final TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.gravity = Gravity.RIGHT;
                    params.weight = 0.2f;
                    textView.setLayoutParams(params);

                    row.addView(textView);
                }

                { // arrival time
                    final TextView textView = new TextView(this);
                    textView.setPadding(10, 4, 4, 10);
                    textView.setText(flight.getArrivalTime());

                    final TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.gravity = Gravity.RIGHT;
                    params.weight = 0.2f;
                    textView.setLayoutParams(params);

                    row.addView(textView);
                }

                { // carrier
                    final TextView textView = new TextView(this);
                    textView.setPadding(10, 4, 4, 10);
                    textView.setText(flight.getCarrier());

                    final TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.gravity = Gravity.LEFT;
                    params.weight = 0.5f;
                    textView.setLayoutParams(params);

                    row.addView(textView);
                }

                { // right arrow
                    final ImageView imageView = new ImageView(this);
                    imageView.setPadding(10, 4, 4, 10);
                    imageView.setImageResource(R.drawable.list_item_arrow);

                    final TableRow.LayoutParams params = new TableRow.LayoutParams();
                    params.weight = 0.1f;
                    params.gravity = Gravity.RIGHT;
                    imageView.setLayoutParams(params);

                    row.addView(imageView);
                }

                final int index = i;
                row.setOnClickListener(new View.OnClickListener() {

                    /**
                     * @see android.view.View.OnClickListener#onClick(android.view.View)
                     */
                    @Override
                    public void onClick( final View view ) {
                        handleFlightClicked(index);
                    }
                });

                this.tableLayout.setColumnStretchable(3, true);
                this.tableLayout.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                           ViewGroup.LayoutParams.WRAP_CONTENT));
                ++i;
            }
        }
    }

    /**
     * @param view the show date picker button (never <code>null</code>)
     */
    public void showDatePickerDialog( final View view ) {
        // initialize travel date fields to current date if necessary
        final Calendar currentDay = Calendar.getInstance();
        final int displayYear = ((this.year == -1) ? currentDay.get(Calendar.YEAR) : this.year);
        final int displayMonth = ((this.month == -1) ? currentDay.get(Calendar.MONTH) : this.month);
        final int displayDay = ((this.day == -1) ? currentDay.get(Calendar.DATE) : this.day);

        this.dateDialog = new DatePickerDialog(this, this.datePickerListener, displayYear, displayMonth, displayDay);
        this.dateDialog.show();
    }

    void updateEnablements() {
        clearFlights();

        final boolean hasDate = ((this.year != -1) && (this.month != -1) && (this.day != -1));
        final boolean enable = ((this.arrivalAirport != _noAirport) && (this.departureAirport != _noAirport) && hasDate);

        if (enable) {
            loadFlights(this.txtTravelDate, this.departureAirport, this.arrivalAirport);
        }
    }

}
