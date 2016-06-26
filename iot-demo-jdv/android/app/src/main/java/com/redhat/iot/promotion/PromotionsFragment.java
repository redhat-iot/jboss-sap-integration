package com.redhat.iot.promotion;

import android.R.attr;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;

import com.redhat.iot.DataProvider;
import com.redhat.iot.IotApp;
import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.concurrent.DepartmentCallback;
import com.redhat.iot.domain.Department;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A screen showing the current promotions.
 */
public class PromotionsFragment extends Fragment implements OnClickListener {

    private Activity activity;
    private PromotionAdapter adapter;
    private final Collection< CheckBox > chkDepts = new ArrayList<>();

    public PromotionsFragment() {
        // nothing to do
    }

    private void loadPromotionsData( final LayoutInflater inflater,
                                     final View promotionsView,
                                     final Department[] departments ) {
        // get promo dept ID preference
        final Set< String > deptIdsPref = IotApp.getPrefs().getStringSet( Prefs.PROMO_DEPT_IDS, null );
        final boolean showAll = ( ( deptIdsPref == null ) || deptIdsPref.isEmpty() );
        final List< Long > filter = new ArrayList<>(); // the departments of the promotions that will be shown

        final TableLayout table = ( TableLayout )promotionsView.findViewById( id.promoDepartments );
        TableRow row = null;
        int i = 0;
        int childIndex = 0;

        for ( final Department dept : departments ) {
            if ( i % 3 == 0 ) {
                childIndex = 0;
                row = new TableRow( this.activity );
                final LayoutParams params = new LayoutParams( LayoutParams.WRAP_CONTENT,
                                                              LayoutParams.WRAP_CONTENT );
                row.setLayoutParams( params );
                table.addView( row );
            }

            // create dept checkbox
            inflater.inflate( layout.promo_dept_chk, row, true ); // adds to row
            final CheckBox chk = ( CheckBox )row.getChildAt( childIndex++ );
            chk.setText( dept.getName() );

            // color background of button
            final int[][] states = {
                { attr.state_enabled },
            };

            final int[] colors = {
                DataProvider.get().getDepartmentColor( dept ),
            };

            ColorStateList colorStateList = new ColorStateList( states, colors );
            chk.setButtonTintList( colorStateList );
            chk.setButtonTintMode( Mode.DST_OVER );

            boolean check = false;

            // check if in preference
            if ( showAll || deptIdsPref.contains( Long.toString( dept.getId() ) ) ) {
                filter.add( dept.getId() );
                check = true;
            }

            chk.setChecked( check );
            chk.setTag( dept.getId() );
            chk.setOnClickListener( this );

            this.chkDepts.add( chk );

            ++i;
        }

        this.adapter = new PromotionAdapter( this.activity );
        this.adapter.setFilter( filter.toArray( new Long[ filter.size() ] ) );
        final RecyclerView rview = ( RecyclerView )promotionsView.findViewById( id.gridDeals );
        rview.setAdapter( this.adapter );
        rview.setLayoutManager( new GridLayoutManager( this.activity, 1 ) );
    }

    @Override
    public void onClick( final View view ) {
        final List< Long > selected = new ArrayList<>( this.chkDepts.size() );
        final Set< String > prefValue = new HashSet<>();

        for ( final CheckBox chk : this.chkDepts ) {
            if ( chk.isChecked() ) {
                selected.add( ( long )chk.getTag() );
                prefValue.add( chk.getTag().toString() );
            }
        }

        // save selected dept IDs in preference
        final Editor editor = IotApp.getPrefs().edit();
        editor.putStringSet( Prefs.PROMO_DEPT_IDS, prefValue );
        editor.apply();

        // reload requested promotions
        this.adapter.setFilter( selected.toArray( new Long[ selected.size() ] ) );
    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup parent,
                              final Bundle savedInstanceState ) {
        this.activity = getActivity();
        final View promotionsView = inflater.inflate( layout.promotions, parent, false );

        // create department checkboxes on callback
        DataProvider.get().getDepartments( new DepartmentCallback() {

            @Override
            public void onSuccess( final Department[] results ) {
                loadPromotionsData( inflater, promotionsView, results );
            }
        } );

        return promotionsView;
    }

}
