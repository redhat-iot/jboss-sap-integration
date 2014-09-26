/*
 * Copyright 2013 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.example.demo_dv_sap;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.example.demo_dv_sap.R;
import com.example.demo_dv_sap.model.Flight;
import com.example.demo_dv_sap.model.FlightParcelable;

/**
 * The airport details screen.
 */
public final class DetailsScreen extends Activity {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle newSavedInstanceState) {
		super.onCreate(newSavedInstanceState);
		setContentView(R.layout.details_screen);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE,
				ActionBar.DISPLAY_SHOW_TITLE);

		// details tab
		actionBar.addTab(actionBar
				.newTab()
				.setText(getString(R.string.details_tab_title))
				.setTabListener(
						new TabListener<DetailsTab>(this, DetailsTab.ID,
								DetailsTab.class)));

		// flights tab
		actionBar.addTab(actionBar
				.newTab()
				.setText(getString(R.string.flights_tab_title))
				.setTabListener(
						new TabListener<FlightsTab>(this, FlightsTab.ID,
								FlightsTab.class)));
	}

	class TabListener<T extends Fragment> implements ActionBar.TabListener {

		private final Activity mActivity;
		private final Bundle mArgs;
		private final Class<T> mClass;
		private Fragment mFragment;
		private final String mTag;

		public TabListener(final Activity activity, final String tag,
				final Class<T> clz) {
			this.mActivity = activity;
			this.mTag = tag;
			this.mClass = clz;
			this.mArgs = null;

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			this.mFragment = this.mActivity.getFragmentManager()
					.findFragmentByTag(this.mTag);

			if ((this.mFragment != null) && !this.mFragment.isDetached()) {
				final FragmentTransaction ft = this.mActivity
						.getFragmentManager().beginTransaction();
				ft.detach(this.mFragment);
				ft.commit();
			}
		}

		/**
		 * @see android.app.ActionBar.TabListener#onTabReselected(android.app.ActionBar.Tab,
		 *      android.app.FragmentTransaction)
		 */
		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
			// nothing to do
		}

		/**
		 * @see android.app.ActionBar.TabListener#onTabSelected(android.app.ActionBar.Tab,
		 *      android.app.FragmentTransaction)
		 */
		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
			if (this.mFragment == null) {
				this.mFragment = Fragment.instantiate(this.mActivity,
						this.mClass.getName(), this.mArgs);

//				if (FlightsTab.ID.equals(this.mTag)) {
//					// TODO Provide actual data here
//					final ArrayList<FlightParcelable> data = new ArrayList<FlightParcelable>(
//							5);
//					data.add(new FlightParcelable((new Flight("American Airlines", "AA", "100", "11:00 PM", "PT11H00M00S", "JFK", "2:00 PM", "PT14H00M00S", "SFO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
//                            "1", "2013-03-13T00:00:00", "A10", "DELAYED")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//				
//					this.mActivity.getIntent().putParcelableArrayListExtra(
//							FlightParcelable.ALTERNATIVE_FLIGHTS, data);
//				}

				ft.add(android.R.id.content, this.mFragment, this.mTag);
			} else {
				ft.attach(this.mFragment);
			}

			// set activity title
			this.mActivity
					.setTitle(getString(R.string.app_name) + " - " //$NON-NLS-1$
							+ (FlightsTab.ID.equals(this.mTag) ? getString(R.string.flights_tab_activity_title)
									: getString(R.string.details_tab_activity_title)));
		}

		/**
		 * @see android.app.ActionBar.TabListener#onTabUnselected(android.app.ActionBar.Tab,
		 *      android.app.FragmentTransaction)
		 */
		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
			if (this.mFragment != null) {
				ft.detach(this.mFragment);
			}
		}

	}

}
