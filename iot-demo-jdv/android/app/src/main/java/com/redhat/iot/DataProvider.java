package com.redhat.iot;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

import com.redhat.iot.R.array;
import com.redhat.iot.concurrent.CustomerCallback;
import com.redhat.iot.concurrent.DepartmentCallback;
import com.redhat.iot.concurrent.GetCustomers;
import com.redhat.iot.concurrent.GetDepartments;
import com.redhat.iot.concurrent.GetNotifications;
import com.redhat.iot.concurrent.GetOrders;
import com.redhat.iot.concurrent.GetProducts;
import com.redhat.iot.concurrent.GetPromotions;
import com.redhat.iot.concurrent.GetStores;
import com.redhat.iot.concurrent.NotificationCallback;
import com.redhat.iot.concurrent.OrderCallback;
import com.redhat.iot.concurrent.ProductCallback;
import com.redhat.iot.concurrent.PromotionCallback;
import com.redhat.iot.concurrent.StoreCallback;
import com.redhat.iot.domain.Customer;
import com.redhat.iot.domain.Department;
import com.redhat.iot.domain.Order;
import com.redhat.iot.domain.Product;
import com.redhat.iot.domain.Promotion;
import com.redhat.iot.domain.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider for domain objects.
 */
public class DataProvider {

    private static DataProvider _shared;

    /**
     * @return the shared provider (never <code>null</code>)
     */
    public static DataProvider get() {
        if ( _shared == null ) {
            _shared = new DataProvider();
        }

        return _shared;
    }

    private final Map< Integer, Customer > customers = new HashMap<>();
    private final Map< Long, Department > departments = new HashMap<>();
    private final Map< Long, Integer > deptColors = new HashMap<>();
    private final Map< Integer, Product > products = new HashMap<>();
    private final Map< Integer, Promotion > promotions = new HashMap<>();
    private final Map< Integer, Store > stores = new HashMap<>();

    /**
     * Don't allow construction outside of this class.
     */
    private DataProvider() {
        // nothing to do
    }

    private void cacheCustomers( final Customer[] customers ) {
        Log.d( IotConstants.LOG_TAG, "clearing customer cache" );
        this.customers.clear();

        Log.d( IotConstants.LOG_TAG, "Adding " + customers.length + " customers to the cache" );
        for ( final Customer cust : customers ) {
            this.customers.put( cust.getId(), cust );
        }
    }

    private void cacheDepartments( final Department[] departments ) {
        Log.d( IotConstants.LOG_TAG, "clearing department cache" );
        this.departments.clear();
        this.deptColors.clear();

        Log.d( IotConstants.LOG_TAG, "Adding " + departments.length + " departments to the cache" );
        final Resources res = IotApp.getContext().getResources();
        final TypedArray deptColors = res.obtainTypedArray( array.dept_colors );
        int i = 0;

        for ( final Department dept : departments ) {
            this.departments.put( dept.getId(), dept );
            final int colorId = deptColors.getColor( i++, 0 );
            this.deptColors.put( dept.getId(), colorId );
        }

        deptColors.recycle(); // call after done with TypeArray
    }

    private void cacheProducts( final Product[] products ) {
        Log.d( IotConstants.LOG_TAG, "clearing product cache" );
        this.products.clear();

        Log.d( IotConstants.LOG_TAG, "Adding " + products.length + " products to the cache" );
        for ( final Product product : products ) {
            this.products.put( product.getId(), product );
        }
    }

    private void cachePromotions( final Promotion[] promotions ) {
        Log.d( IotConstants.LOG_TAG, "clearing promotion cache" );
        this.promotions.clear();

        Log.d( IotConstants.LOG_TAG, "Adding " + promotions.length + " promotions to the cache" );
        for ( final Promotion promotion : promotions ) {
            this.promotions.put( promotion.getId(), promotion );
        }
    }

    private void cacheStores( final Store[] stores ) {
        Log.d( IotConstants.LOG_TAG, "clearing store cache" );
        this.stores.clear();

        Log.d( IotConstants.LOG_TAG, "Adding " + stores.length + " stores to the cache" );
        for ( final Store store : stores ) {
            this.stores.put( store.getId(), store );
        }
    }

    /**
     * Result will be an array with zero or one {@link Customer}.
     *
     * @param custId   the ID of the customer being requested
     * @param callback the callback receiving the results (cannot be <code>null</code>)
     */
    public void findCustomer( final int custId,
                              final CustomerCallback callback ) {
        if ( this.customers.isEmpty() ) {
            getCustomers( new CustomerCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Customer[] results ) {
                    callback.onSuccess( getCustomer( custId ) );
                }
            } );
        } else {
            callback.onSuccess( getCustomer( custId ) );
        }
    }

    /**
     * Result will be an array with zero or one {@link Department}.
     *
     * @param deptId   the ID of the department being requested
     * @param callback the callback receiving the results (cannot be <code>null</code>)
     */
    public void findDepartment( final long deptId,
                                final DepartmentCallback callback ) {
        if ( this.departments.isEmpty() ) {
            getDepartments( new DepartmentCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Department[] results ) {
                    callback.onSuccess( getDepartment( deptId ) );
                }
            } );
        } else {
            callback.onSuccess( getDepartment( deptId ) );
        }
    }

    /**
     * Result will be an array with zero or one {@link Product}.
     *
     * @param productId the ID of the product being requested
     * @param callback  the callback receiving the results (cannot be <code>null</code>)
     */
    public void findProduct( final int productId,
                             final ProductCallback callback ) {
        if ( this.products.isEmpty() ) {
            getProducts( new ProductCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Product[] results ) {
                    callback.onSuccess( getProduct( productId ) );
                }
            } );
        } else {
            callback.onSuccess( getProduct( productId ) );
        }
    }

    /**
     * Result will be an array with zero or one {@link Promotion}.
     *
     * @param promoId  the ID of the promotion being requested
     * @param callback the callback receiving the results (cannot be <code>null</code>)
     */
    public void findPromotion( final int promoId,
                               final PromotionCallback callback ) {
        if ( this.promotions.isEmpty() ) {
            getPromotions( new PromotionCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Promotion[] results ) {
                    callback.onSuccess( getPromotion( promoId ) );
                }
            } );
        } else {
            callback.onSuccess( getPromotion( promoId ) );
        }
    }

    /**
     * @param callback the result handler (cannot be <code>null</code>)
     * @param deptIds  the IDs of the departments whose promotions are being requested
     */
    public void findPromotions( final PromotionCallback callback,
                                final Long... deptIds ) {
        if ( ( deptIds == null ) || ( deptIds.length == 0 ) ) {
            callback.onSuccess( Promotion.NO_PROMOTIONS );
            return;
        }

        // make sure products are loaded since product department is needed
        if ( this.products.isEmpty() ) {
            getProducts( new ProductCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Product[] results ) {
                    if ( DataProvider.this.promotions.isEmpty() ) {
                        getPromotions( new PromotionCallback() {

                            @Override
                            public void onFailure( final Exception error ) {
                                callback.onFailure( error );
                            }

                            @Override
                            public void onFailure( final String errorMsg ) {
                                callback.onFailure( errorMsg );
                            }

                            @Override
                            public void onSuccess( final Promotion[] results ) {
                                callback.onSuccess( getPromotions( deptIds ) );
                            }
                        } );
                    } else {
                        callback.onSuccess( getPromotions( deptIds ) );
                    }
                }
            } );
        } else {
            // products are loaded so make sure promotions are
            if ( DataProvider.this.promotions.isEmpty() ) {
                getPromotions( new PromotionCallback() {

                    @Override
                    public void onFailure( final Exception error ) {
                        callback.onFailure( error );
                    }

                    @Override
                    public void onFailure( final String errorMsg ) {
                        callback.onFailure( errorMsg );
                    }

                    @Override
                    public void onSuccess( final Promotion[] results ) {
                        callback.onSuccess( getPromotions( deptIds ) );
                    }
                } );
            } else {
                callback.onSuccess( getPromotions( deptIds ) );
            }
        }
    }

    private Customer[] getCustomer( final int custId ) {
        final Customer customer = this.customers.get( custId );
        return ( ( customer == null ) ? Customer.NO_CUSTOMERS : new Customer[]{ customer } );
    }

    /**
     * @param callback the handler of the {@link Customer} results (cannot be <code>null</code>)
     */
    private void getCustomers( final CustomerCallback callback ) {
        if ( this.customers.isEmpty() ) {
            new GetCustomers( new CustomerCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Customer[] results ) {
                    cacheCustomers( results );
                    callback.onSuccess( results );
                }
            } ).execute();
        } else {
            callback.onSuccess( this.customers.values().toArray( new Customer[ this.customers.size() ] ) );
        }
    }

    private Department[] getDepartment( final long deptId ) {
        final Department department = this.departments.get( deptId );
        return ( ( department == null ) ? Department.NO_DEPARTMENTS : new Department[]{ department } );
    }

    /**
     * Assumes departments have been loaded since you have an ID.
     *
     * @param deptId the ID of the department whose color is being requested (cannot be <code>null</code>)
     * @return the ID of the color
     */
    public int getDepartmentColor( final long deptId ) {
        final Department dept = this.departments.get( deptId );

        if ( dept != null ) {
            return this.deptColors.get( deptId );
        }

        IotApp.logError( DataProvider.class, "getDepartmentColor", "No department found for deptId '" + deptId + '\'', null );
        return -1;
    }

    /**
     * Assumes products and departments have already been loaded.
     *
     * @param productId the ID of the {@link Product} whose {@link Department} name is being requested
     * @return the department name or <code>null</code> if the product or department is not found
     */
    public String getDepartmentName( final int productId ) {
        final Product[] products = getProduct( productId );

        if ( products.length == 1 ) {
            final Department[] departments = getDepartment( products[ 0 ].getDepartmentId() );

            if ( departments.length == 1 ) {
                return departments[ 0 ].getName();
            }
        }

        return null;
    }

    /**
     * @param dept the department whose color is being requested (cannot be <code>null</code>)
     * @return the ID of the color
     */
    public int getDepartmentColor( final Department dept ) {
        return getDepartmentColor( dept.getId() );
    }

    /**
     * @param callback the handler of the {@link Department} results (cannot be <code>null</code>)
     */
    public void getDepartments( final DepartmentCallback callback ) {
        if ( this.departments.isEmpty() ) {
            new GetDepartments( new DepartmentCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Department[] results ) {
                    Arrays.sort( results, Department.NAME_SORTER );
                    cacheDepartments( results );
                    callback.onSuccess( results );
                }
            } ).execute();
        } else {
            final Department[] result = this.departments.values().toArray( new Department[ this.departments.size() ] );
            Arrays.sort( result, Department.NAME_SORTER );
            callback.onSuccess( result );
        }
    }

    /**
     * @param callback the handler for processing new notifications for the logged in customer (cannot be <code>null</code>)
     */
    public void getNotifications( final int customerID, final NotificationCallback callback ) {
        new GetNotifications( callback, customerID ).execute();
    }

    /**
     * @param customerId the ID of the customer whose orders are being requested
     * @param callback   the handler of the {@link Order} results (cannot be <code>null</code>)
     */
    public void getOrders( final int customerId,
                           final OrderCallback callback ) {
        new GetOrders( customerId, callback ).execute();
    }

    private Product[] getProduct( final int productId ) {
        final Product product = this.products.get( productId );
        return ( ( product == null ) ? Product.NO_PRODUCTS : new Product[]{ product } );
    }

    /**
     * @param callback the handler of the {@link Product} results (cannot be <code>null</code>)
     */
    private void getProducts( final ProductCallback callback ) {
        if ( this.products.isEmpty() ) {
            new GetProducts( new ProductCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Product[] results ) {
                    cacheProducts( results );
                    callback.onSuccess( results );
                }
            } ).execute();
        } else {
            callback.onSuccess( this.products.values().toArray( new Product[ this.products.size() ] ) );
        }
    }

    private Promotion[] getPromotion( final int promoId ) {
        final Promotion promotion = this.promotions.get( promoId );
        return ( ( promotion == null ) ? Promotion.NO_PROMOTIONS : new Promotion[]{ promotion } );
    }

    private Promotion[] getPromotions( final Long... deptIds ) {
        final List< Long > requestedDepts = Arrays.asList( deptIds );
        final List< Promotion > result = new ArrayList<>();

        for ( final Promotion promo : this.promotions.values() ) {
            final int productId = promo.getProductId();
            final Product[] product = getProduct( productId );

            if ( ( product == null ) || ( product.length == 0 ) ) {
                IotApp.logError( DataProvider.class, "getPromotions", "product '" + productId + "' was not found", null );
            } else if ( requestedDepts.contains( product[ 0 ].getDepartmentId() ) ) {
                result.add( promo );
            }
        }

        return result.toArray( new Promotion[ result.size() ] );
    }

    /**
     * @param callback the handler of the {@link Promotion} results (cannot be <code>null</code>)
     */
    private void getPromotions( final PromotionCallback callback ) {
        if ( this.promotions.isEmpty() ) {
            new GetPromotions( new PromotionCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Promotion[] results ) {
                    cachePromotions( results );
                    Arrays.sort( results, Promotion.DEPT__NAME_SORTER );
                    callback.onSuccess( results );
                }
            } ).execute();
        } else {
            final Promotion[] results = this.promotions.values().toArray( new Promotion[ this.promotions.size() ] );
            Arrays.sort( results, Promotion.DEPT__NAME_SORTER );
            callback.onSuccess( results );
        }
    }

    /**
     * @param callback the handler of the {@link com.redhat.iot.domain.Store} results (cannot be <code>null</code>)
     */
    public void getStores( final StoreCallback callback ) {
        if ( this.stores.isEmpty() ) {
            new GetStores( new StoreCallback() {

                @Override
                public void onFailure( final Exception error ) {
                    callback.onFailure( error );
                }

                @Override
                public void onFailure( final String errorMsg ) {
                    callback.onFailure( errorMsg );
                }

                @Override
                public void onSuccess( final Store[] results ) {
                    cacheStores( results );
                    callback.onSuccess( results );
                }
            } ).execute();
        } else {
            callback.onSuccess( this.stores.values().toArray( new Store[ this.stores.size() ] ) );
        }
    }

}
