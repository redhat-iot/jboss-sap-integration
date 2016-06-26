package com.redhat.iot.concurrent;

import android.R.string;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.redhat.iot.IotApp;
import com.redhat.iot.R;
import com.redhat.iot.domain.IotObject;
import com.redhat.iot.json.IotMarshaller;

/**
 * Callback for IoT results.
 *
 * @param <T> the result type
 */
public abstract class IotCallback< T extends IotObject > implements OnClickListener {

    /**
     * @return the {@link IotMarshaller} who can convert to/from JSON strings and {@link IotObject}s (never <code>null</code>)
     */
    abstract IotMarshaller< T > getMarshaller();

    @Override
    public void onClick( final DialogInterface dialog,
                         final int which ) {
        // TODO quit app
    }

    /**
     * @param error the error that was caught running the task (never <code>null</code>)
     */
    public void onFailure( final Exception error ) {
        IotApp.logError( getClass(), "onFailure", null, error );
        showAlertDialog( IotApp.getContext().getString( R.string.app_error_dialog_title ), error.getLocalizedMessage() );
    }

    /**
     * This method is called when an error occurs but no {@link Exception} was caught.
     *
     * @param errorMsg the error message generated running the task (never empty)
     */
    public void onFailure( final String errorMsg ) {
        IotApp.logError( getClass(), "onFailure", errorMsg, null );
        showAlertDialog( IotApp.getContext().getString( R.string.app_error_dialog_title ), errorMsg );
    }

    /**
     * The task completed successfully.
     *
     * @param results the results (never <code>null</code>)
     */
    public void onSuccess( final T[] results ) {
        // nothing to do
    }

    private void showAlertDialog( final String title,
                                  final String message ) {
        final Context context = IotApp.getContext();
        final Builder builder = new Builder( context );
        builder.setTitle( title );
        builder.setMessage( message );

        final String positiveText = context.getString( string.ok );
        builder.setPositiveButton( positiveText, this );

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

}
