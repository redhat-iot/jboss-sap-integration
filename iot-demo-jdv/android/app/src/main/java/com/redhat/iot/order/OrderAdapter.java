package com.redhat.iot.order;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.redhat.iot.DataProvider;
import com.redhat.iot.IotConstants;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.R.string;
import com.redhat.iot.concurrent.ProductCallback;
import com.redhat.iot.domain.Order;
import com.redhat.iot.domain.OrderDetail;
import com.redhat.iot.domain.Product;

import java.util.Calendar;

/**
 * An adapter for displaying collections of {@link Order}s.
 */
class OrderAdapter extends Adapter {

    private final Context context;
    private final LayoutInflater inflater;
    private final Order[] orders;
    private RecyclerView recyclerView;

    public OrderAdapter( final Context c,
                         final Order[] orders ) {
        this.context = c;
        this.inflater = LayoutInflater.from( this.context );
        this.orders = orders;
    }

    @Override
    public int getItemCount() {
        return this.orders.length;
    }

    @Override
    public long getItemId( final int position ) {
        return position;
    }

    private void handleOrderClicked( final View orderView ) {
        final int index = this.recyclerView.getChildLayoutPosition( orderView );
        final Order order = this.orders[ index ];
        Toast.makeText( this.context, "Order: " + order.getId(), Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onAttachedToRecyclerView( final RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder( final ViewHolder viewHolder,
                                  final int position ) {
        final OrderViewHolder holder = ( OrderViewHolder )viewHolder;
        final Order order = this.orders[ position ];

        // set order ID
        holder.tvId.setText( this.context.getString( string.order_id, order.getId() ) );

        // set order date
        final Calendar calendar = order.getOrderDate();
        IotConstants.DATE_FORMATTER.setCalendar( calendar );
        final String formatted = IotConstants.DATE_FORMATTER.format( calendar.getTime() );
        holder.tvDate.setText( formatted );

        // details
        final OrderDetail[] details = order.getDetails();

        if ( details.length == 0 ) {
            Log.e( IotConstants.LOG_TAG,
                   "Order " + order.getId() + " does not have any order details" );
            holder.ivOrder.setImageResource( 0 );
            holder.tvDescription.setText( "" );
        } else {
            final int productId = details[ 0 ].getProductId();
            DataProvider.get().findProduct( productId, new ProductCallback() {

                @Override
                public void onSuccess( final Product[] results ) {
                    setDataOnBind( holder, results[ 0 ] );
                }
            } );
        }

        // set number of items in order
        if ( details.length > 1 ) {
            holder.tvNumItems.setText( this.context.getString( string.order_num_additional, ( details.length - 1 ) ) );
        } else {
            holder.tvNumItems.setText( "" );
        }

        // set order price
        holder.tvPrice.setText( this.context.getString( string.order_price, order.getPrice() ) );
    }

    @Override
    public ViewHolder onCreateViewHolder( final ViewGroup parent,
                                          final int viewType ) {
        final View view = this.inflater.inflate( layout.order, parent, false );
        return new OrderViewHolder( view );
    }

    private void setDataOnBind( final OrderViewHolder holder,
                                final Product firstProduct ) {
        // set order image based on first item
        holder.ivOrder.setImageResource( firstProduct.getImageId() );

        // set order description based on first item
        holder.tvDescription.setText( firstProduct.getDescription() );
    }

    private class OrderViewHolder extends ViewHolder {

        private final ImageView ivOrder;
        private final TextView tvDate;
        private final TextView tvDescription;
        private final TextView tvId;
        private final TextView tvNumItems;
        private final TextView tvPrice;

        public OrderViewHolder( final View orderView ) {
            super( orderView );

            this.ivOrder = ( ImageView )orderView.findViewById( id.orderImage );
            this.tvDate = ( TextView )orderView.findViewById( id.orderDate );
            this.tvDescription = ( TextView )orderView.findViewById( id.orderFirstItem );
            this.tvId = ( TextView )orderView.findViewById( id.orderId );
            this.tvNumItems = ( TextView )orderView.findViewById( id.orderNumItems );
            this.tvPrice = ( TextView )orderView.findViewById( id.orderPrice );

            orderView.setOnClickListener( new OnClickListener() {

                @Override
                public void onClick( final View orderView ) {
                    handleOrderClicked( orderView );
                }

            } );
        }

    }

}
