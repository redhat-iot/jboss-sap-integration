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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.example.demo_dv_sap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

/**
 * A screen that displays a Google map.
 */
public class MapScreen extends Activity implements LocationListener {

    private GoogleMap map;

    public MapScreen() {
        super();
    }

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
        this.map = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapView)).getMap();
        this.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status == ConnectionResult.SUCCESS) { // Google Play Services is available
            // Enabling MyLocation Layer of Google Map
            this.map.setMyLocationEnabled(true);

            // Getting LocationManager object from System Service LOCATION_SERVICE
            final LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            final Criteria criteria = new Criteria();

            // Getting the name of the best provider
            final String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            final Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
//            } else {
//                final String coordinates[] = {"1.352566007", "103.78921587"};
//                final double lat = Double.parseDouble(coordinates[0]);
//                final double lng = Double.parseDouble(coordinates[1]);
//
//                // Creating a LatLng object for the current location
//                LatLng latLng = new LatLng(lat, lng);
//
//                // Showing the current location in Google Map
//                this.map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                this.map.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
            boolean enabledGPS = locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean enabledWiFi = locationManager
                            .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    // Check if enabled and if not send user to the GSP settings
                    // Better solution would be to display a dialog and suggesting to 
                    // go to the settings
                    if (!enabledGPS || !enabledWiFi) {
                        Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
        } else { // Google Play Services are not available
            final int requestCode = 10;
            final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }
    }

    /**
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged( final Location newLocation ) {
        // nothing to do
    }

    /**
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled( final String newProvider ) {
        // nothing to do
    }

    /**
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled( final String newProvider ) {
        // nothing to do
    }

    /**
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged( final String newProvider,
                                 final int newStatus,
                                 final Bundle newExtras ) {
        // nothing to do
    }

}
