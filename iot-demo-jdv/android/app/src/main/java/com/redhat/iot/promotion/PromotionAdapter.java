package com.redhat.iot.promotion;

import android.content.Context;
import android.support.v7.widget.CardView;
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
import com.redhat.iot.concurrent.DepartmentCallback;
import com.redhat.iot.concurrent.ProductCallback;
import com.redhat.iot.concurrent.PromotionCallback;
import com.redhat.iot.domain.Department;
import com.redhat.iot.domain.Product;
import com.redhat.iot.domain.Promotion;

import java.util.Arrays;

/**
 * An adapter for displaying collections of {@link Promotion}s.
 */
class PromotionAdapter extends Adapter {

    private final Context context;
    private final LayoutInflater inflater;
    private Promotion[] promotions;
    private RecyclerView recyclerView;

    /**
     * @param c a context for use within this adapter (cannot be <code>null</code>)
     */
    public PromotionAdapter( final Context c ) {
        this.context = c;
        this.inflater = LayoutInflater.from( this.context );
        this.promotions = Promotion.NO_PROMOTIONS;
    }

    @Override
    public int getItemCount() {
        return this.promotions.length;
    }

    @Override
    public long getItemId( final int position ) {
        return position;
    }

    private Promotion getPromotion( final int index ) {
        return this.promotions[ index ];
    }

    private void handlePromotionClicked( final View promotionView ) {
        final int index = this.recyclerView.getChildLayoutPosition( promotionView );
        final Promotion promotion = getPromotion( index );
        Toast.makeText( this.context, "Promotion: " + promotion.getId(), Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onAttachedToRecyclerView( final RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder( final ViewHolder promotionHolder,
                                  final int position ) {
        final PromotionViewHolder holder = ( PromotionViewHolder )promotionHolder;
        final Promotion promotion = getPromotion( position );
        DataProvider.get().findProduct( promotion.getProductId(), new ProductCallback() {

            @Override
            public void onSuccess( final Product[] results ) {
                final Product product = ( ( ( results == null ) || results.length != 1 ) ? null : results[ 0 ] );
                setDataOnBind( holder, product, promotion );
            }
        } );
    }

    @Override
    public ViewHolder onCreateViewHolder( final ViewGroup parent,
                                          final int viewType ) {
        final View promotionView = this.inflater.inflate( layout.promotion, parent, false );
        return new PromotionViewHolder( promotionView );
    }

    private void refreshPromotions( final Promotion[] newPromotions ) {
        this.promotions = ( ( newPromotions == null ) ? Promotion.NO_PROMOTIONS : newPromotions );
        Arrays.sort( this.promotions, Promotion.DEPT__NAME_SORTER );
        notifyDataSetChanged();
    }

    private void setDataOnBind( final PromotionViewHolder holder,
                                final Department department ) {
        holder.tvDept.setText( department.getName() );
    }

    private void setDataOnBind( final PromotionViewHolder holder,
                                final Product product,
                                final Promotion promotion ) {
        if ( product == null ) {
            Log.e( IotConstants.LOG_TAG,
                   "Product " + promotion.getProductId() + " was not found for promotion " + promotion.getId() );
        } else {
            // set card background color for the product department
            holder.view.setCardBackgroundColor( DataProvider.get().getDepartmentColor( product.getDepartmentId() ) );

            // set product image
            holder.ivItem.setImageResource( product.getImageId() );

            // set product department
            DataProvider.get().findDepartment( product.getDepartmentId(), new DepartmentCallback() {

                @Override
                public void onSuccess( final Department[] results ) {
                    setDataOnBind( holder, results[ 0 ] );
                }
            } );

            // set product sale price
            final double discount = ( product.getMsrp() * ( promotion.getDiscount() / 100 ) );
            final double salePrice = ( product.getMsrp() - discount );
            holder.tvSalePrice.setText( this.context.getString( string.deal_sale_price, salePrice ) );

            // set product original price
            holder.tvOriginalPrice.setText( this.context.getString( string.deal_original_price, product.getMsrp() ) );

            // set product description
            holder.tvDescription.setText( product.getDescription() );
        }
    }

    /**
     * @param departmentIds the IDs of the departments whose promotions should be shown (can be <code>null</code>)
     */
    void setFilter( final Long... departmentIds ) {
        DataProvider.get().findPromotions( new PromotionCallback() {

            @Override
            public void onSuccess( final Promotion[] results ) {
                refreshPromotions( results );
            }
        }, departmentIds );
    }

    private class PromotionViewHolder extends ViewHolder {

        private final ImageView ivItem;
        private final TextView tvDept;
        private final TextView tvDescription;
        private final TextView tvSalePrice;
        private final TextView tvOriginalPrice;
        private final CardView view;

        public PromotionViewHolder( final View promotionlView ) {
            super( promotionlView );

            this.view = ( CardView )promotionlView;
            this.ivItem = ( ImageView )promotionlView.findViewById( id.dealImage );
            this.tvDept = ( TextView )promotionlView.findViewById( id.dealDept );
            this.tvDescription = ( TextView )promotionlView.findViewById( id.dealDescription );
            this.tvSalePrice = ( TextView )promotionlView.findViewById( id.dealSalePrice );
            this.tvOriginalPrice = ( TextView )promotionlView.findViewById( id.dealOriginalPrice );

            promotionlView.setOnClickListener( new OnClickListener() {

                @Override
                public void onClick( final View promotionView ) {
                    handlePromotionClicked( promotionView );
                }

            } );
        }

    }

}
