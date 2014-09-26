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
package com.example.demo_dv_sap.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A representation of a {@link TerminalMap} that can be passed into an intent extra.
 */
public final class TerminalMapParcelable implements Parcelable {

    /**
     * Used to un-marshal or de-serialize a terminal map from a parcel.
     */
    public static final Creator<TerminalMapParcelable> CREATOR = new Creator<TerminalMapParcelable>() {

        /**
         * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
         */
        @Override
        public TerminalMapParcelable createFromParcel( final Parcel in ) {
            return new TerminalMapParcelable(in);
        }

        /**
         * @see android.os.Parcelable.Creator#newArray(int)
         */
        @Override
        public TerminalMapParcelable[] newArray( final int size ) {
            return new TerminalMapParcelable[size];
        }
    };

    /**
     * The parcelabale identifier for the terminal map array from the details tab.
     */
    public static final String TERMINAL_MAPS = "terminal_maps"; //$NON-NLS-1$

    private final TerminalMap terminalMap;

    TerminalMapParcelable( final Parcel in ) {
        final String uri = in.readString();
        final String title = in.readString();
        final String subtitle = in.readString();

        this.terminalMap = new TerminalMap(uri, title, subtitle);
    }

    /**
     * @param mapModel the terminal map model (cannot be <code>null</code>)
     */
    public TerminalMapParcelable( final TerminalMap mapModel ) {
        this.terminalMap = mapModel;
    }

    /**
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * @return the terminal map (never <code>null</code>)
     */
    public TerminalMap getTerminalMap() {
        return this.terminalMap;
    }

    /**
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel( final Parcel dest,
                               final int flags ) {
        dest.writeString(this.terminalMap.getImageName());
        dest.writeString(this.terminalMap.getTitle());

        String subtitle = this.terminalMap.getSubtitle();

        if ((subtitle == null) || subtitle.isEmpty()) {
            subtitle = ""; //$NON-NLS-1$
        }

        dest.writeString(subtitle);
    }

}
