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

/**
 * An immutable terminal map business object.
 */
public final class TerminalMap {

    private final String subtitle;

    private final String title;

    private final String imageName;

    /**
     * @param mapImageName the ID of the map (cannot be <code>null</code> or empty)
     * @param mapTitle the localized map title (cannot be <code>null</code> or empty)
     * @param mapSubtitle the localized map subtitle (can be <code>null</code> or empty)
     */
    public TerminalMap( final String mapImageName,
                        final String mapTitle,
                        final String mapSubtitle ) {
        this.imageName = mapImageName;
        this.title = mapTitle;
        this.subtitle = ((mapSubtitle == null) ? "" : mapSubtitle); //$NON-NLS-1$
    }

    /**
     * @return the map subtitle (never <code>null</code> but can be empty)
     */
    public String getSubtitle() {
        return this.subtitle;
    }

    /**
     * @return the map title (never <code>null</code> or empty)
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return the map resource id (never <code>null</code> or empty)
     */
    public String getImageName() {
        return this.imageName;
    }

}
