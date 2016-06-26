package com.redhat.iot;

import android.app.Fragment;
import android.content.SharedPreferences.Editor;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;

import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.concurrent.DepartmentCallback;
import com.redhat.iot.domain.Department;

import java.util.Collections;

/**
 * The home screen.
 */
public class HomeFragment extends Fragment implements OnClickListener {

    private MainActivity iotMain;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick( final View btn ) {
        final String deptId = btn.getTag().toString();

        // save selected dept IDs in preference
        final Editor editor = IotApp.getPrefs().edit();
        editor.putStringSet( Prefs.PROMO_DEPT_IDS, Collections.singleton( deptId ) );
        editor.apply();

        this.iotMain.showScreen( MainActivity.PROMOTIOHS_SCREEN_INDEX );
    }

    private void createDepartmentButtons( final LayoutInflater inflater,
                                          final View view,
                                          final Department[] departments ) {
        // create department buttons
        final TableLayout table = ( TableLayout )view.findViewById( id.homeDeptTable );
        TableRow row = null;
        int i = 0;
        int childIndex = 0;

        for ( final Department dept : departments ) {
            if ( i % 2 == 0 ) {
                childIndex = 0;
                row = new TableRow( this.iotMain );
                final LayoutParams params = new LayoutParams( LayoutParams.WRAP_CONTENT,
                                                              0,
                                                              1.0f );
                row.setLayoutParams( params );
                table.addView( row );
            }

            // dept button
            inflater.inflate( layout.home_dept_button, row, true ); // adds to row
            final Button btn = ( Button )row.getChildAt( childIndex++ );
            btn.setBackgroundColor( DataProvider.get().getDepartmentColor( dept ) );
            btn.setText( dept.getName() );
            btn.setOnClickListener( this );
            btn.setTag( dept.getId() );

            final ViewOutlineProvider provider = new ViewOutlineProvider() {

                @Override
                public void getOutline( final View view,
                                        final Outline outline ) {
                    outline.setRoundRect( 10, 10, btn.getWidth() - 10, btn.getHeight() - 10, 30.0f );
                }
            };
            btn.setOutlineProvider( provider );
            btn.setClipToOutline( true );

            ++i;
        }

    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup container,
                              final Bundle savedInstanceState ) {
        this.iotMain = ( MainActivity )getActivity();
        final View view = inflater.inflate( layout.home, container, false );

        // load customers
        DataProvider.get().getDepartments( new DepartmentCallback() {

            @Override
            public void onSuccess( final Department[] results ) {
                createDepartmentButtons( inflater, view, results );
            }
        } );

        return view;
    }

}
