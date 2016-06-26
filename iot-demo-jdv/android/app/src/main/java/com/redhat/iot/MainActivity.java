package com.redhat.iot;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.redhat.iot.IotConstants.Prefs;
import com.redhat.iot.R.drawable;
import com.redhat.iot.R.id;
import com.redhat.iot.R.layout;
import com.redhat.iot.R.menu;
import com.redhat.iot.R.string;
import com.redhat.iot.concurrent.DepartmentCallback;
import com.redhat.iot.concurrent.NotificationCallback;
import com.redhat.iot.concurrent.ProductCallback;
import com.redhat.iot.concurrent.PromotionCallback;
import com.redhat.iot.domain.Customer;
import com.redhat.iot.domain.Department;
import com.redhat.iot.domain.IotNotification;
import com.redhat.iot.domain.Product;
import com.redhat.iot.domain.Promotion;
import com.redhat.iot.order.OrdersFragment;
import com.redhat.iot.promotion.PromotionsFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    private static final int ICON_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final Integer[][] DRAWER_CONFIG = {
        new Integer[]{ drawable.ic_user, string.title_login_fragment },
        new Integer[]{ drawable.ic_home, string.title_home_fragment },
        new Integer[]{ drawable.ic_price_tags, string.title_deals_fragment },
        new Integer[]{ drawable.ic_coin_dollar, string.title_orders_fragment },
        new Integer[]{ drawable.ic_credit_card, string.title_billing_fragment },
        new Integer[]{ drawable.ic_cog, string.title_settings_fragment },
        new Integer[]{ drawable.ic_mail, string.title_contact_fragment },
        new Integer[]{ drawable.ic_info, string.title_about_fragment },
    };

    private static final int LOGIN_SCREEN_INDEX = 1;
    private static final int HOME_SCREEN_INDEX = 2;
    static final int PROMOTIOHS_SCREEN_INDEX = 3;
    private static final int ORDERS_SCREEN_INDEX = 4;
    private static final int BILLING_SCREEN_INDEX = 5;
    private static final int SETTINGS_SCREEN_INDEX = 6;
    private static final int CONTACT_SCREEN_INDEX = 7;
    private static final int ABOUT_SCREEN_INDEX = 8;

    private Timer notifierTimer;
    private final AtomicInteger notificationId = new AtomicInteger();

    // customer should get notified once for each promotion
    private final Map< Integer, List< Integer > > notifications = new HashMap<>(); // key=customer ID, value=list of promo IDs

    private void handleFoundPromotion( final Promotion promotion ) {
        DataProvider.get().findProduct( promotion.getProductId(), new ProductCallback() {

            @Override
            public void onSuccess( final Product[] products ) {
                handleFoundProduct( promotion, products[ 0 ] );
            }
        } );
    }

    private void handleFoundProduct( final Promotion promotion,
                                     final Product product ) {
        DataProvider.get().findDepartment( product.getDepartmentId(), new DepartmentCallback() {

            @Override
            public void onSuccess( final Department[] results ) {
                publishNotification( promotion, product, results[ 0 ] );
            }
        } );
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = ( DrawerLayout )findViewById( id.drawer_layout );

        if ( drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        IotApp.setContext( this );
        setContentView( layout.activity_main );

        final Toolbar toolbar = ( Toolbar )findViewById( id.toolbar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowHomeEnabled( true );

        final RecyclerView drawerRecyclerView = ( RecyclerView )findViewById( id.drawer_view );
        drawerRecyclerView.setHasFixedSize( true );

        final Adapter drawerAdapter = new DrawerAdapter( this );
        drawerRecyclerView.setAdapter( drawerAdapter );

        final LayoutManager drawerLayoutMgr = new LinearLayoutManager( this );
        drawerRecyclerView.setLayoutManager( drawerLayoutMgr );

        final DrawerLayout drawerLayout = ( DrawerLayout )findViewById( id.drawer_layout );
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle( this,
                                                                              drawerLayout,
                                                                              toolbar,
                                                                              string.navigation_drawer_open,
                                                                              string.navigation_drawer_close );
        drawerLayout.addDrawerListener( drawerToggle );
        drawerToggle.syncState();
//
//        final NavigationView navigationView = ( NavigationView )findViewById( R.id.nav_view );
//        navigationView.setNavigationItemSelectedListener( this );

        // set home as first fragment
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
            .replace( id.content_frame, new HomeFragment() )
            .commit();

//        this.drawerLayoutMgr.scrollToPosition( 2 );
//        this.drawerRecyclerView.setCheckedItem( R.id.nav_home );

        // register to receive preference changes
        IotApp.getPrefs().registerOnSharedPreferenceChangeListener( this );
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu optionsMenu ) {
        getMenuInflater().inflate( menu.main, optionsMenu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item ) {
        int itemId = item.getItemId();

        // log user out
        if ( itemId == id.action_sign_out ) {
            IotApp.setUserId( Customer.UNKNOWN_USER );
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onPause() {
        Log.d( IotConstants.LOG_TAG, "onPause" );
        stopNotificationThread();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        Log.d( IotConstants.LOG_TAG, "onRestart" );
        super.onRestart();

        // start notifications if pref is set
        onSharedPreferenceChanged( IotApp.getPrefs(), Prefs.ENABLE_NOTIFICATIONS );
    }

    @Override
    public void onSharedPreferenceChanged( final SharedPreferences sharedPreferences,
                                           final String key ) {
        if ( Prefs.ENABLE_NOTIFICATIONS.equals( key ) ) {
            Log.d( IotConstants.LOG_TAG, ( "Enable notifications preference changed" ) );
            final boolean enable = IotApp.getPrefs().getBoolean( Prefs.ENABLE_NOTIFICATIONS,
                                                                 Prefs.DEFAULT_ENABLE_NOTIFICATIONS );

            if ( enable ) {
                startNotificationThread();
            } else {
                stopNotificationThread();
            }
        } else if ( Prefs.NOTIFICATION_INTERVAL.equals( key ) ) {
            Log.d( IotConstants.LOG_TAG, ( "Notifications interval preference changed" ) );
            stopNotificationThread();
            startNotificationThread();
        }
    }

    @Override
    protected void onStart() {
        Log.d( IotConstants.LOG_TAG, "onStart" );
        super.onStart();

        // make sure we have a user logged in at startup
        final int userId = IotApp.getCustomerId();

        if ( userId == Customer.UNKNOWN_USER ) {
            IotApp.setUserId( IotConstants.FIRST_CUST_ID );
        }

        // start notifications if pref is set
        onSharedPreferenceChanged( IotApp.getPrefs(), Prefs.ENABLE_NOTIFICATIONS );
    }

    @Override
    protected void onStop() {
        Log.d( IotConstants.LOG_TAG, "onStop" );
        stopNotificationThread();
        super.onStop();
    }

    private void publishNotification( final Promotion promotion,
                                      final Product product,
                                      final Department department ) {
        // don't send if a customer is not logged in
        final int custId = IotApp.getCustomerId();

        if ( custId == Customer.UNKNOWN_USER ) {
            IotApp.logError( MainActivity.class, "sendNotifications", "No customer logged in", null );
            return;
        }

        // create
        final Builder builder = new Builder( this );
        builder.setSmallIcon( product.getImageId() );
        builder.setContentTitle( getString( string.notification_title, department.getName() ) );
        builder.setContentText( getString( string.notification_message, product.getName(), promotion.getDiscount() ) );
        builder.setGroup( IotConstants.NOTIFIER_GROUP );
        final Notification alert = builder.build();

        // send
        final NotificationManager mgr = ( NotificationManager )getSystemService( Context.NOTIFICATION_SERVICE );
        final int alertId = this.notificationId.getAndIncrement();
        Log.d( IotConstants.LOG_TAG,
               MessageFormat.format( "Sending IoT Notification {0} to customer {1} for promotion {2}",
                                     alertId,
                                     custId,
                                     promotion.getId() ) );
        mgr.notify( alertId, alert );

        // remember the promotions each customer received
        List< Integer > received = this.notifications.get( custId );

        if ( received == null ) {
            received = new ArrayList<>();
            this.notifications.put( custId, received );
        }

        received.add( promotion.getId() );
    }

    private void sendNotifications( final IotNotification[] results ) {
        if ( ( results == null ) || ( results.length == 0 ) ) {
            return;
        }

        // just looking at first
        final int promoId = results[ 0 ].getPromoId();

        // don't send if already received the promotion
        final int custId = IotApp.getCustomerId();
        final List< Integer > received = this.notifications.get( custId );
        final boolean send = ( ( received == null ) || !received.contains( promoId ) );

        if ( send ) {
            // need to find the promotion, product, and product department to send the notification
            DataProvider.get().findPromotion( promoId, new PromotionCallback() {

                @Override
                public void onSuccess( final Promotion[] results ) {
                    handleFoundPromotion( results[ 0 ] );
                }
            } );
        } else {
            Log.d( IotConstants.LOG_TAG, "Not sending IoT Notification for promotion " + promoId
                + " because customer " + custId + " has already received it" );
        }
    }

    void showScreen( final int index ) {
        Fragment fragment;
        int titleId = -1;

        switch ( index ) {
            case LOGIN_SCREEN_INDEX:
                fragment = new LoginFragment();
                titleId = string.title_login_fragment;
                break;
            case HOME_SCREEN_INDEX:
                fragment = new HomeFragment();
                break;
            case PROMOTIOHS_SCREEN_INDEX:
                fragment = new PromotionsFragment();
                titleId = string.title_deals_fragment;
                break;
            case ORDERS_SCREEN_INDEX:
                fragment = new OrdersFragment();
                titleId = string.title_orders_fragment;
                break;
            case BILLING_SCREEN_INDEX:
                fragment = new BillingFragment();
                titleId = string.title_billing_fragment;
                break;
            case SETTINGS_SCREEN_INDEX:
                fragment = new SettingsFragment();
                titleId = string.title_settings_fragment;
                break;
            case CONTACT_SCREEN_INDEX:
                fragment = new ContactFragment();
                titleId = string.title_contact_fragment;
                break;
            case ABOUT_SCREEN_INDEX:
                fragment = new AboutFragment();
                titleId = string.title_about_fragment;
                break;
            default:
                Log.e( IotConstants.LOG_TAG, "index " + index + " does not create a fragment" );
                fragment = new HomeFragment();
                break;
        }

        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace( id.content_frame, fragment ).commit();

        final DrawerLayout drawer = ( DrawerLayout )findViewById( id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );

        if ( getSupportActionBar() != null ) {
            if ( titleId == -1 ) {
                getSupportActionBar().setSubtitle( "" );
            } else {
                getSupportActionBar().setSubtitle( getString( titleId ) );
            }
        }
    }

    private void startNotificationThread() {
        stopNotificationThread();
        Log.d( IotConstants.LOG_TAG, "starting notification timer" );
        final int interval = IotApp.getPrefs().getInt( Prefs.NOTIFICATION_INTERVAL,
                                                       Prefs.DEFAULT_NOTIFICATION_INTERVAL );
        this.notifierTimer = new Timer( true );
        this.notifierTimer.schedule( new NotificationTask(), 0, interval );
    }

    private void stopNotificationThread() {
        if ( this.notifierTimer != null ) {
            Log.d( IotConstants.LOG_TAG, "stopping notification timer" );
            this.notifierTimer.cancel();
        }
    }

    /**
     * The entire path to DrawerAdapter.Holder needs to be here in order for the build to work correctly!
     */
    class DrawerAdapter extends Adapter< com.redhat.iot.MainActivity.DrawerAdapter.Holder > {

        private final Context context;
        private final LayoutInflater inflater;

        DrawerAdapter( final Context c ) {
            this.context = c;
            this.inflater = LayoutInflater.from( this.context );
        }

        @Override
        public int getItemCount() {
            return ( DRAWER_CONFIG.length + 1 );
        }

        @Override
        public long getItemId( final int position ) {
            return position;
        }

        @Override
        public int getItemViewType( final int position ) {
            return ( ( position == 0 ) ? 0 : 1 );
        }

        @Override
        public void onBindViewHolder( final Holder holder,
                                      final int position ) {
            if ( position != 0 ) {
                holder.ivIcon.setImageResource( DRAWER_CONFIG[ position - 1 ][ ICON_INDEX ] );
                holder.tvName.setText( DRAWER_CONFIG[ position - 1 ][ NAME_INDEX ] );
            }
        }

        @Override
        public Holder onCreateViewHolder( final ViewGroup parent,
                                          final int viewType ) {
            // item
            if ( viewType == 1 ) {
                final View view = this.inflater.inflate( layout.drawer_item, parent, false );
                return new Holder( view, viewType );
            }

            // header
            final View view = this.inflater.inflate( layout.nav_header_main, parent, false );
            return new Holder( view, viewType );
        }

        class Holder extends ViewHolder implements OnClickListener {

            private final ImageView ivIcon;
            private final TextView tvName;

            public Holder( final View itemView,
                           final int itemType ) {
                super( itemView );

                if ( itemType == 1 ) {
                    itemView.setClickable( true );
                    itemView.setOnClickListener( this );

                    this.ivIcon = ( ImageView )itemView.findViewById( id.drawerItemIcon );
                    this.tvName = ( TextView )itemView.findViewById( id.drawerItemName );
                } else {
                    this.ivIcon = null;
                    this.tvName = null;
                }
            }

            @Override
            public void onClick( final View view ) {
                showScreen( getAdapterPosition() );
            }

        }

    }

    class NotificationHandler extends NotificationCallback {

        @Override
        public void onSuccess( final IotNotification[] results ) {
            if ( results.length != 0 ) {
                sendNotifications( results );
            }
        }

    }

    /**
     * A task to check for notifications.
     */
    private class NotificationTask extends TimerTask {

        @Override
        public void run() {
            int customerID = IotApp.getCustomerId();
            if (customerID!=Customer.UNKNOWN_USER)
                DataProvider.get().getNotifications( customerID, new NotificationHandler());}
        }

}
