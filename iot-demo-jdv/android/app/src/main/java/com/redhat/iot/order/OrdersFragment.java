package com.redhat.iot.order;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.redhat.iot.DataProvider;
import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.concurrent.OrderCallback;
import com.redhat.iot.domain.Customer;
import com.redhat.iot.domain.Order;

/**
 * A screen for displaying the {@link Order} history of the logged in {@link Customer}.
 */
public class OrdersFragment extends Fragment {

    private Activity activity;
    private TextView emptyView;
    private RecyclerView ordersView;

    /**
     * Constructs an {@link Order}s screen.
     */
    public OrdersFragment() {
        // nothing to do
    }

    @Override
    public void onActivityCreated( final Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );

        // look up currently logged in customer
        final SharedPreferences settings = this.activity.getSharedPreferences( Prefs.PREFS_NAME, 0 );
        final int customerId = settings.getInt( Prefs.CUSTOMER_ID, Customer.UNKNOWN_USER );

        if ( customerId == Customer.UNKNOWN_USER ) {
            setDataOnCreated( Order.NO_ORDERS );
        } else {
            // obtain customer orders and create adapter
            DataProvider.get().getOrders( customerId, new OrderCallback() {

                @Override
                public void onSuccess( final Order[] results ) {
                    setDataOnCreated( results );
                }
            } );
        }
    }

    @Override
    public View onCreateView( final LayoutInflater inflater,
                              final ViewGroup parent,
                              final Bundle savedInstanceState ) {
        this.activity = getActivity();

        final View view = inflater.inflate( layout.orders, parent, false );
        this.emptyView = ( TextView )view.findViewById( id.tv_no_orders );
        this.ordersView = ( RecyclerView )view.findViewById( id.orderHistory );

        final OrderAdapter adapter = new OrderAdapter( this.activity, Order.NO_ORDERS );
        this.ordersView.setAdapter( adapter );
        this.ordersView.setLayoutManager( new GridLayoutManager( this.activity, 1 ) );

        return view;
    }

    private void setDataOnCreated( final Order[] orders ) {
        final boolean noOrders = ( orders.length == 0 );
        this.ordersView.setVisibility( noOrders ? View.GONE : View.VISIBLE );
        this.emptyView.setVisibility( noOrders ? View.VISIBLE : View.GONE );

        final OrderAdapter adapter = new OrderAdapter( this.activity, orders );
        this.ordersView.setAdapter( adapter );
        adapter.notifyDataSetChanged();
    }

}
