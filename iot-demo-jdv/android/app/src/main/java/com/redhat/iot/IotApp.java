package com.redhat.iot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.domain.Customer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The IoT Mobile App.
 */
public class IotApp extends Application {

    private static final String LOG_MSG = ( "%s: %s: %s" );

    private static final AtomicInteger IMAGE_COUNT = new AtomicInteger( 1 );
    private static final Map< Long, Integer > IMAGE_MAP = new HashMap<>();
    private static final int NUM_IMAGES = 50;

    private static Context _context;

    /**
     * @return the app context (never <code>null</code>)
     */
    public static Context getContext() {
        return _context;
    }

    /**
     * @return the ID of the logged in customer or {@link Customer#UNKNOWN_USER} if no one is logged in
     */
    public static int getCustomerId() {
        final SharedPreferences prefs = getPrefs();
        return prefs.getInt( Prefs.CUSTOMER_ID, Customer.UNKNOWN_USER );
    }

    /**
     * @param o the object whose image resource identifier is being requested (cannot be <code>null</code>)
     * @return the image resource ID
     */
    public static int getImageId( final Object o ) {
        final long id = o.hashCode();
        Integer imageId = IMAGE_MAP.get( id );

        if ( imageId == null ) {
            final Resources resources = getContext().getResources();
            imageId = resources.getIdentifier( "com.redhat.iot:drawable/item_" + IMAGE_COUNT.get(), null, null );
            IMAGE_MAP.put( id, imageId );

            if ( IMAGE_COUNT.intValue() > NUM_IMAGES ) {
                IMAGE_COUNT.set( 1 );
            } else {
                IMAGE_COUNT.incrementAndGet();
            }
        }

        return imageId;
    }

    /**
     * @return the app preferences (never <code>null</code>)
     */
    public static SharedPreferences getPrefs() {
        return _context.getSharedPreferences( Prefs.PREFS_NAME, 0 );
    }

    /**
     * @param clazz         the class logging the debug message (cannot be <code>null</code>)
     * @param methodContext the name of the method where the error is being logged (cannot be empty)
     * @param msg           the debug message (cannot be empty)
     */
    public static void logDebug( final Class< ? > clazz,
                                 final String methodContext,
                                 final String msg ) {
        Log.d( IotConstants.LOG_TAG, String.format( LOG_MSG, clazz.getSimpleName(), methodContext, msg ) );
    }

    /**
     * @param clazz         the class logging the error message (cannot be <code>null</code>)
     * @param methodContext the name of the method where the error is being logged (cannot be empty)
     * @param msg           the error message (can be empty)
     * @param e             the error (can be <code>null</code>)
     */
    public static void logError( final Class< ? > clazz,
                                 final String methodContext,
                                 final String msg,
                                 final Throwable e ) {
        final String errorMsg = ( ( msg == null ) ? "" : msg );
        Log.e( IotConstants.LOG_TAG, String.format( LOG_MSG, clazz.getSimpleName(), methodContext, errorMsg ), e );
    }

    /**
     * @param hostIpAddress the IP address of the host that is being checked (cannot be empty)
     * @return <code>true</code> if host is reachable
     */
    public static boolean ping( final String hostIpAddress ) {
        boolean reachable = false;

        try {
            String cmd;

            if ( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {
                cmd = ( "ping -n 1 " + hostIpAddress );
            } else {
                cmd = ( "ping -c 1 " + hostIpAddress );
            }

            final Process myProcess = Runtime.getRuntime().exec( cmd );
            myProcess.waitFor();

            if ( myProcess.exitValue() == 0 ) {
                reachable = true;
            }
        } catch ( final Exception e ) {
            reachable = false;
        }

        return reachable;
    }

    /**
     * Must be called once at startup from the main activity.
     *
     * @param mainActivity the shared context used by the app (cannot be <code>null</code>)
     */
    public static void setContext( final Context mainActivity ) {
        if ( _context == null ) {
            _context = mainActivity;
        } else {
            logError( IotApp.class, "setContext", "setting context more than once", null );
        }
    }

    /**
     * @param userId the ID of the logged in user or {@link Customer#UNKNOWN_USER} if no one is logged in
     */
    public static void setUserId( final int userId ) {
        final Editor editor = getPrefs().edit();
        editor.putInt( Prefs.CUSTOMER_ID, userId );
        editor.apply();
    }

}
