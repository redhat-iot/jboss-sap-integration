package com.redhat.iot.concurrent;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.redhat.iot.IotApp;
import com.redhat.iot.IotConstants;
import com.redhat.iot.IotException;
import com.redhat.iot.R.string;
import com.redhat.iot.domain.IotObject;
import com.redhat.iot.json.IotMarshaller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task whose result is a collection of {@link IotObject}s.
 */
abstract class GetData< T extends IotObject > extends AsyncTask< Void, Void, T[] > {

    private static final boolean USE_REAL_DATA = true;
    private static final boolean I_AM_TED = true;
    private static final String HOST = "10.0.2.2"; // when DV is running locally (use localhost in browser)
    private static final String PORT = ( I_AM_TED ? "8081" : "8080" );
    private static final String PSWD = ( I_AM_TED ? "TbJ01221991$" : "4teiid$admin" );
    private static final String USER = "teiidUser";

    protected static final String JSONS_FORMAT = "?$format=json";
    protected static final String URL_PATTERN = new StringBuilder( "http://" ).append( HOST )
        .append( ':' )
        .append( PORT )
        .append( "/odata/customer_iot/%s" )
        .toString();

    private final IotCallback< T > callback;
    private final Class< T > clazz;
    private ProgressDialog dialog;
    protected Exception error;
    private String errorMsg;
    private final String pswd;
    private final String urlAsString;
    private final String user;

    /**
     * @param url                     the string representation of the {@link URL} being used to fetch data (cannot be empty)
     * @param callback                the callback that is notified after task is finished  (cannot be <code>null</code>)
     * @param clazz                   the {@link Class} of the result objects (cannot be <code>null</code>)
     * @param progressDialogMessageId the resource ID of the progress dialog message or -1 if no progress dialog should be shown
     */
    protected GetData( final String url,
                       final IotCallback< T > callback,
                       final Class< T > clazz,
                       final int progressDialogMessageId ) {
        this.urlAsString = url;
        this.user = USER;
        this.pswd = PSWD;
        this.callback = callback;
        this.clazz = clazz;

        if ( progressDialogMessageId != -1 ) {
            this.dialog = new ProgressDialog( IotApp.getContext() );
            this.dialog.setTitle( string.app_load_data_progress_title );
            this.dialog.setMessage( IotApp.getContext().getString( progressDialogMessageId ) );
        }
    }

    private void dismissProgressDialog() {
        if ( ( this.dialog != null ) && this.dialog.isShowing() ) {
            this.dialog.dismiss();
        }
    }

    @Override
    protected T[] doInBackground( final Void... params ) {
        try {
            return executeHttpGet( this.urlAsString, this.user, this.pswd );
        } catch ( final Exception e ) {
            IotApp.logError( GetData.class, "doInBackground", "url = '" + this.urlAsString + '\'', e );
            this.error = e;
            return null;
        }
    }

    private T[] executeHttpGet( final String urlAsString,
                                final String user,
                                final String pswd ) {
        boolean ok;
        String json;
        HttpURLConnection urlConnection = null;

        try {
            if ( isUsingRealData() ) {
                final URL url = new URL( urlAsString );
                final String userCredentials = ( user + ':' + pswd );
                final String encoding =
                    new String( Base64.encode( userCredentials.getBytes(), Base64.DEFAULT ) ).replaceAll( "\\s+", "" );

                urlConnection = ( HttpURLConnection )url.openConnection();
                urlConnection.setRequestProperty( "Authorization", "Basic " + encoding );
                urlConnection.setRequestMethod( "GET" );
                urlConnection.setRequestProperty( "ACCEPT-LANGUAGE", "en-US,en;0.5" );

                final int code = urlConnection.getResponseCode();
                ok = ( code == HttpURLConnection.HTTP_OK );
                InputStream is;

                if ( ok ) {
                    Log.d( IotConstants.LOG_TAG, ( "HTTP GET SUCCESS for URL: " + urlAsString ) );
                    is = urlConnection.getInputStream();
                } else {
                    is = urlConnection.getErrorStream();
                }

                final BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
                final StringBuilder builder = new StringBuilder();
                String line;

                while ( ( line = reader.readLine() ) != null ) {
                    builder.append( line ).append( "\n" );
                }

                reader.close();
                json = builder.toString();
            } else {
                ok = true;
                json = getTestData();
            }

            if ( ok ) {
                T[] iotObjs;

                if ( ( json == null ) || json.isEmpty() ) {
                    iotObjs = ( T[] )Array.newInstance( this.clazz, 0 );
                } else {
                    final IotMarshaller< T > marshaller = this.callback.getMarshaller();
                    final JSONArray jarray = marshaller.parseJsonArray( json );

                    final List< T > result = new ArrayList<>( jarray.length() );

                    for ( int i = 0;
                          i < jarray.length();
                          ++i ) {
                        final JSONObject jIot = jarray.getJSONObject( i );
                        final T iot = marshaller.toIot( jIot.toString() );
                        result.add( iot );
                    }

                    iotObjs = ( T[] )Array.newInstance( this.clazz, result.size() );

                    for ( int i = 0;
                          i < result.size();
                          ++i ) {
                        iotObjs[ i ] = result.get( i );
                    }
                }

                return iotObjs;
            }

            // Not HTTP OK
            this.errorMsg = json;
        } catch ( final Exception e ) {
            IotApp.logError( GetData.class, "executeHttpGet", "url = '" + urlAsString + '\'', e );
            this.error = e;
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * @return the error that occurred during processing (can be <code>null</code>)
     */
    public Exception getError() {
        return this.error;
    }

    /**
     * @return an error message (can be <code>null</code>)
     */
    public String getErrorMessage() {
        return this.errorMsg;
    }

    /**
     * @return a JSON string of objects (cannot be empty)
     * @throws IotException if an error occurs
     */
    protected abstract String getTestData() throws IotException;

    /**
     * @return <code>true</code> if real data should be used
     */
    protected boolean isUsingRealData() {
        return USE_REAL_DATA;
    }

    @Override
    protected void onCancelled( final T[] ts ) {
        dismissProgressDialog();
    }

    @Override
    protected void onPreExecute() {
        if ( this.dialog != null ) {
            this.dialog.show();
        }
    }

    @Override
    protected void onPostExecute( final T[] results ) {
        dismissProgressDialog();

        if ( this.error != null ) {
            this.callback.onFailure( this.error );
        } else if ( this.errorMsg != null ) {
            this.callback.onFailure( this.errorMsg );
        } else {
            this.callback.onSuccess( results );
        }
    }

}
