package com.redhat.iot;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R.array;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.R.string;
import com.redhat.iot.concurrent.CustomerCallback;
import com.redhat.iot.concurrent.StoreCallback;
import com.redhat.iot.domain.Customer;
import com.redhat.iot.domain.Store;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment
    implements OnCheckedChangeListener, OnItemSelectedListener {

    private static final Store NO_STORE = new Store( Store.NOT_IDENTIFIED, null, null, null, null, null, null, null );

    private StoreAdapter storeAdapter;
    private Spinner storeSpinner;
    private TextView txt;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCheckedChanged( final CompoundButton buttonView,
                                  final boolean isChecked ) {

        if ( IotApp.getPrefs().getBoolean( Prefs.ENABLE_NOTIFICATIONS, Prefs.DEFAULT_ENABLE_NOTIFICATIONS ) != isChecked ) {
            // save preference
            final Editor editor = IotApp.getPrefs().edit();
            editor.putBoolean( Prefs.ENABLE_NOTIFICATIONS, isChecked );
            IotApp.logDebug( SettingsFragment.class,
                             "onCheckedChanged",
                             "Changing pref " + Prefs.ENABLE_NOTIFICATIONS + " to " + isChecked );
            editor.apply();
        }
    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup container,
                              final Bundle savedInstanceState ) {
        final View view = inflater.inflate( layout.settings, container, false );

        { // user name
            this.txt = ( TextView )view.findViewById( id.settingsCurrentUser );
            final int userId = IotApp.getCustomerId();

            DataProvider.get().findCustomer( userId, new CustomerCallback() {

                @Override
                public void onSuccess( final Customer[] results ) {
                    final Customer customer = ( ( ( results == null ) || ( results.length != 1 ) ) ? null : results[ 0 ] );
                    setCurrentCustomerOnCreateView( customer );
                }
            } );
        }

        { // My Store
            this.storeSpinner = ( Spinner )view.findViewById( id.settingsStores );
            this.storeSpinner.setOnItemSelectedListener( this );

            this.storeAdapter = new StoreAdapter( getActivity() );
            this.storeSpinner.setAdapter( this.storeAdapter );

            DataProvider.get().getStores( new StoreCallback() {

                @Override
                public void onSuccess( final Store[] results ) {
                    setStoresOnCreateView( results );
                }
            } );
        }

        { // enable notifications
            final CheckBox chk = ( CheckBox )view.findViewById( id.settingsEnableNotifications );
            final boolean checked = ( IotApp.getPrefs()
                .getBoolean( Prefs.ENABLE_NOTIFICATIONS, Prefs.DEFAULT_ENABLE_NOTIFICATIONS ) );
            chk.setChecked( checked );
            chk.setOnCheckedChangeListener( this );
        }

        { // notification interval
            final Spinner spinner = ( Spinner )view.findViewById( id.settingsNotificationInterval );
            spinner.setOnItemSelectedListener( this );

            final ArrayAdapter< CharSequence > adapter = new ArrayAdapter<>( getActivity(),
                                                                             layout.spinner_item,
                                                                             getResources().getTextArray( array
                                                                                                              .notification_intervals

                                                                                                        ) );
            spinner.setAdapter( adapter );

            // set selection to value of preference
            final int interval = ( ( IotApp.getPrefs()
                .getInt( Prefs.NOTIFICATION_INTERVAL, Prefs.DEFAULT_NOTIFICATION_INTERVAL ) ) / 60000 );
            int index;

            switch ( interval ) {
                case 2:
                    index = 1;
                    break;
                case 3:
                    index = 2;
                    break;
                case 4:
                    index = 3;
                    break;
                case 5:
                    index = 4;
                    break;
                case 10:
                    index = 5;
                    break;
                default:
                    index = 0;
                    break;
            }

            spinner.setSelection( index );
        }

        return view;
    }

    @Override
    public void onItemSelected( final AdapterView< ? > parent,
                                final View view,
                                final int position,
                                final long id ) {
        final int parentId = parent.getId();

        if ( parentId == R.id.settingsStores ) {
            int storeId = Store.NOT_IDENTIFIED;

            if ( position != 0 ) {
                storeId = this.storeAdapter.getStores()[ position ].getId();
            }

            if ( IotApp.getPrefs().getInt( Prefs.STORE_ID, Store.NOT_IDENTIFIED ) != storeId ) {
                // save preference
                final Editor editor = IotApp.getPrefs().edit();
                IotApp.logDebug( SettingsFragment.class, "onItemSelected", "Changing pref " + Prefs.STORE_ID + " to " + storeId );
                editor.putInt( Prefs.STORE_ID, storeId );
                editor.apply();
            }
        } else if ( parentId == R.id.settingsNotificationInterval ) {
            int minutes;

            switch ( position ) {
                case 0:
                    minutes = 1;
                    break;
                case 1:
                    minutes = 2;
                    break;
                case 2:
                    minutes = 3;
                    break;
                case 3:
                    minutes = 4;
                    break;
                case 4:
                    minutes = 5;
                    break;
                case 5:
                    minutes = 10;
                    break;
                default:
                    minutes = 1;
                    break;
            }

            final int newValue = ( minutes * 60000 );

            if ( IotApp.getPrefs().getInt( Prefs.NOTIFICATION_INTERVAL, Prefs.DEFAULT_NOTIFICATION_INTERVAL ) != newValue ) {
                // save preference
                final Editor editor = IotApp.getPrefs().edit();
                editor.putInt( Prefs.NOTIFICATION_INTERVAL, newValue );
                IotApp.logDebug( SettingsFragment.class,
                                 "onItemSelected",
                                 "Changing pref " + Prefs.NOTIFICATION_INTERVAL + " to " + newValue );
                editor.apply();
            }
        }
    }

    @Override
    public void onNothingSelected( final AdapterView< ? > parent ) {
        // nothing to do
    }

    private void setCurrentCustomerOnCreateView( final Customer customer ) {
        final String name =
            ( ( customer == null ) ? getActivity().getString( string.settings_user_not_logged_in ) : customer.getName() );
        this.txt.setText( name );
    }

    private void setStoresOnCreateView( final Store[] stores ) {
        this.storeAdapter.setStores( stores );
        final int storeId = IotApp.getPrefs().getInt( Prefs.STORE_ID, Store.NOT_IDENTIFIED );

        if ( storeId != Store.NOT_IDENTIFIED ) {
            int index = 0;

            for ( final Store store : this.storeAdapter.getStores() ) {
                if ( store.getId() == storeId ) {
                    break;
                }

                ++index;
            }

            this.storeSpinner.setSelection( index );
        }
    }

    private class StoreAdapter extends ArrayAdapter< Store > {

        private Store[] stores;
        private final Activity activity;

        public StoreAdapter( final Activity activity ) {
            super( activity, android.R.layout.simple_list_item_1 );
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return ( ( this.stores == null ) ? 0 : ( this.stores.length + 1 ) );
        }

        @Override
        public View getDropDownView( final int position,
                                     final View convertView,
                                     final ViewGroup parent ) {
            TextView v = ( TextView )super.getView( ( position ), convertView, parent );

            if ( v == null ) {
                v = new TextView( this.activity );
            }

            v.setText( getText( position ) );
            return v;
        }

        @Override
        public Store getItem( int position ) {
            if ( position == 0 ) {
                return NO_STORE;
            }

            return this.stores[ position - 1 ];
        }

        Store[] getStores() {
            return this.stores;
        }

        private String getText( int position ) {
            if ( this.stores == null ) {
                return "Loading stores...";
            }

            if ( position == 0 ) {
                return this.activity.getString( string.store_not_selected );
            }

            return ( this.stores[ position - 1 ].getCity() + ", " + this.stores[ position - 1 ].getState() );
        }

        @Override
        public View getView( final int position,
                             final View convertView,
                             final ViewGroup parent ) {
            TextView tv = null;

            if ( convertView == null ) {
                final LayoutInflater inflater = this.activity.getLayoutInflater();
                tv = ( TextView )inflater.inflate( layout.spinner_item, null );
            } else {
                tv = ( TextView )convertView;
            }

            tv.setText( getText( position ) );
            return tv;
        }

        void setStores( final Store[] stores ) {
            this.stores = stores;
            Arrays.sort( this.stores, Store.SORTER );
            notifyDataSetChanged();
        }

    }

}
