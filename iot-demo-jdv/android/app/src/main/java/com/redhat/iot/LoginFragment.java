package com.redhat.iot;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.R.string;
import com.redhat.iot.concurrent.CustomerCallback;
import com.redhat.iot.domain.Customer;

/**
 * A login screen.
 */
public class LoginFragment extends Fragment implements OnClickListener, OnSharedPreferenceChangeListener {

    private Button btnSignIn;
    private TextView txtCurrUser;
    private EditText txtUserId;
    private TextView txtUserName;

    private final CustomerCallback callback = new CustomerCallback() {

        @Override
        public void onSuccess( final Customer[] results ) {
            final Customer customer = ( ( ( results == null ) || ( results.length != 1 ) ) ? null : results[ 0 ] );
            updateCustomerName( customer );
        }
    };

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick( final View btn ) {
        final String idTxt = this.txtUserId.getText().toString();
        final int userId = Integer.parseInt( idTxt );

        DataProvider.get().findCustomer( userId, new CustomerCallback() {

            @Override
            public void onSuccess( final Customer[] results ) {
                final Customer customer = ( ( ( results == null ) || ( results.length != 1 ) ) ? null : results[ 0 ] );
                saveNewLogin( customer );
            }
        } );
    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup container,
                              final Bundle savedInstanceState ) {
        final View view = inflater.inflate( layout.login, container, false );

        // click listener for the sign in button
        this.btnSignIn = ( Button )view.findViewById( id.loginSignIn );
        this.btnSignIn.setOnClickListener( this );

        // get reference to user name text view
        this.txtCurrUser = ( TextView )view.findViewById( id.loginCurrentUser );

        // get reference to user name text view
        this.txtUserName = ( TextView )view.findViewById( id.loginUserName );

        //setup key listener for the user ID textfield and set to current user's ID if necessary
        this.txtUserId = ( EditText )view.findViewById( id.loginUserId );
        this.txtUserId.addTextChangedListener( new TextWatcher() {

            @Override
            public void afterTextChanged( final Editable s ) {
                userIdChanged();
            }

            @Override
            public void beforeTextChanged( final CharSequence s,
                                           final int start,
                                           final int count,
                                           final int after ) {
                // nothing to do
            }

            @Override
            public void onTextChanged( final CharSequence s,
                                       final int start,
                                       final int before,
                                       final int count ) {
                // nothing to do
            }

        } );

        final int loggedInUser = IotApp.getCustomerId();

        if ( loggedInUser == Customer.UNKNOWN_USER ) {
            this.txtCurrUser.setText( string.login_not_logged_in );
        } else {
            DataProvider.get().findCustomer( loggedInUser, new CustomerCallback() {

                @Override
                public void onSuccess( final Customer[] results ) {
                    final Customer customer = ( ( ( results == null ) || ( results.length != 1 ) ) ? null : results[ 0 ] );
                    setDataOnCreateView( customer );
                }
            } );
        }

        // register to receive preference changes
        IotApp.getPrefs().registerOnSharedPreferenceChangeListener( this );
        return view;
    }

    @Override
    public void onSharedPreferenceChanged( final SharedPreferences sharedPreferences,
                                           final String key ) {
        if ( Prefs.CUSTOMER_ID.equals( key ) ) {
            final int custId = IotApp.getCustomerId();

            if ( custId == Customer.UNKNOWN_USER ) {
                this.txtCurrUser.setText( string.login_not_logged_in );
            } else {
                DataProvider.get().findCustomer( custId, new CustomerCallback() {

                    @Override
                    public void onSuccess( final Customer[] results ) {
                        final Customer customer = ( ( ( results == null ) || ( results.length != 1 ) ) ? null : results[ 0 ] );
                        setCurrentUserName( customer );
                    }
                } );
            }
        }
    }

    private void saveNewLogin( final Customer customer ) {
        if ( customer == null ) {
            Toast.makeText( getActivity(),
                            getActivity().getString( string.login_unknown_user ),
                            Toast.LENGTH_SHORT ).show();
        } else {
            final int custId = customer.getId();

            if ( IotApp.getPrefs().getInt( Prefs.CUSTOMER_ID, Customer.UNKNOWN_USER ) != custId ) {
                // save customer ID to prefs
                final Editor editor = IotApp.getPrefs().edit();
                IotApp.logDebug( LoginFragment.class, "saveNewLogin", "Changing pref " + Prefs.CUSTOMER_ID + " to " + custId );
                editor.putInt( Prefs.CUSTOMER_ID, custId );
                editor.apply();

                Toast.makeText( getActivity(),
                                getActivity().getString( string.login_success ),
                                Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void setCurrentUserName( final Customer customer ) {
        final String name = ( ( customer == null ) ? "" : customer.getName() );
        this.txtCurrUser.setText( name );

        // disable sign in button if current ID is the same as the current user
        if ( customer != null ) {
            final String currId = Integer.toString( customer.getId() );

            if ( currId.equals( this.txtUserId.getText().toString() ) && this.btnSignIn.isEnabled() ) {
                this.btnSignIn.setEnabled( false );
            }
        }
    }

    private void setDataOnCreateView( final Customer customer ) {
        if ( customer == null ) {
            this.txtCurrUser.setText( string.login_not_logged_in );
        } else {
            this.txtCurrUser.setText( customer.getName() ); // must set this before txtUserId so that but enablement is correct
            this.txtUserId.setText( String.format( "%d", customer.getId() ) );
            this.txtUserId.setSelection( this.txtUserId.getText().length() );
        }
    }

    private void updateCustomerName( final Customer customer ) {
        boolean enable = ( customer != null );
        this.txtUserName.setText( enable ? customer.getName() : "" );

        // don't enable if currently logged in customer
        if ( enable && customer.getName().equals( this.txtCurrUser.getText() ) ) {
            enable = false;
        }

        if ( this.btnSignIn.isEnabled() != enable ) {
            this.btnSignIn.setEnabled( enable );
        }
    }

    private void userIdChanged() {
        final String idString = this.txtUserId.getText().toString();
        boolean enable = !idString.isEmpty();

        if ( enable ) {
            final int userId = Integer.parseInt( idString );
            DataProvider.get().findCustomer( userId, this.callback );
        } else if ( this.btnSignIn.isEnabled() ) {
            this.btnSignIn.setEnabled( false );
        }
    }

}
