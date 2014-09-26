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
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.demo_dv_sap.R;
import com.example.demo_dv_sap.model.TerminalMap;
import com.example.demo_dv_sap.model.TerminalMapParcelable;

/**
 * A screen that displays a set of maps by using swipe gestures.
 */
public final class MapGalleryScreen extends Activity {

    private LayoutInflater inflater;

    private ViewPager mapPager;

    private List<TerminalMap> maps = Collections.emptyList();

    private PagerAdapter pageAdapter;

    void goToMap( final int index ) {
        this.mapPager.setCurrentItem(index);
    }

    LayoutInflater inflater() {
        return this.inflater;
    }

    void loadMaps() {
        final ArrayList<TerminalMapParcelable> data = getIntent().getParcelableArrayListExtra(TerminalMapParcelable.TERMINAL_MAPS);

        if ((data == null) || data.isEmpty()) {
            this.maps = Collections.emptyList();
        } else {
            this.maps = new ArrayList<TerminalMap>(data.size());

            for (final TerminalMapParcelable mapParcelable : data) {
                this.maps.add(mapParcelable.getTerminalMap());
            }
        }

        this.pageAdapter.notifyDataSetChanged();
    }

    int mapCount() {
        return this.maps.size();
    }

    String mapSubtitle( final int index ) {
        return this.maps.get(index).getSubtitle();
    }

    String mapTitle( final int index ) {
        return this.maps.get(index).getTitle();
    }

    String imageName( final int index ) {
        return this.maps.get(index).getImageName();
    }

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_gallery);

        this.inflater = getLayoutInflater();
        this.pageAdapter = new ImagePageAdapter();
        this.mapPager = (ViewPager)findViewById(R.id.map_pager);
        this.mapPager.setAdapter(this.pageAdapter);

        loadMaps();
    }

    class ImagePageAdapter extends PagerAdapter {

        /**
         * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.View, int, java.lang.Object)
         */
        @Override
        public void destroyItem( final View container,
                                 final int position,
                                 final Object object ) {
            ((ViewPager)container).removeView((View)object);
        }

        /**
         * @see android.support.v4.view.PagerAdapter#getCount()
         */
        @Override
        public int getCount() {
            return mapCount();
        }

        /**
         * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
         */
        @Override
        public Object instantiateItem( final ViewGroup container,
                                       final int position ) {
        	Resources res = getResources();
            final View view = inflater().inflate(R.layout.map_gallery_page, null);

            { // title
                final TextView title = (TextView)view.findViewById(R.id.map_title);
                title.setText(mapTitle(position));
            }

            { // subtitle
                final TextView title = (TextView)view.findViewById(R.id.map_subtitle);
                title.setText(mapSubtitle(position));
            }

            { // load map image
                final ImageView map = (ImageView)view.findViewById(R.id.mapImage);
                String imageName = imageName(position);
                int resourceId = res.getIdentifier( 
                		imageName, "drawable", getPackageName() );
                map.setImageResource(resourceId);
            }

            { // hook up left button
                final Button btnLeft = (Button)view.findViewById(R.id.btnPrevious);

                if (position == 0) {
                    btnLeft.setEnabled(false);
                } else {
                    btnLeft.setOnClickListener(new OnClickListener() {

                        /**
                         * @see android.view.View.OnClickListener#onClick(android.view.View)
                         */
                        @Override
                        public void onClick( final View newV ) {
                            goToMap(position - 1);
                        }
                    });
                }
            }

            { // hook up right button
                final Button btnRight = (Button)view.findViewById(R.id.btnNext);

                if (position == (mapCount() - 1)) {
                    btnRight.setEnabled(false);
                } else {
                    btnRight.setOnClickListener(new OnClickListener() {

                        /**
                         * @see android.view.View.OnClickListener#onClick(android.view.View)
                         */
                        @Override
                        public void onClick( final View newV ) {
                            goToMap(position + 1);
                        }
                    });
                }
            }

            ((ViewPager)container).addView(view, 0);
            return view;

        }

        /**
         * @see android.support.v4.view.PagerAdapter#isViewFromObject(android.view.View, java.lang.Object)
         */
        @Override
        public boolean isViewFromObject( final View view,
                                         final Object object ) {
            return (view == object);
        }

    }

}
