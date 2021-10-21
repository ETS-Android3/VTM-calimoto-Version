/*
 * Copyright 2020 Luca Osten
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.core;

import com.calimoto.logic.CaloCoordinateFactory;

/**
 * {@link CaloCoordinateFactory} implementation for {@link GeoPoint}.
 */
public class GeoPointFactory implements CaloCoordinateFactory<GeoPoint> {
    /**
     * Singleton {@link GeoPointFactory} instance.
     */
    public static final GeoPointFactory INSTANCE = new GeoPointFactory();

    // private constructor for singleton instance
    private GeoPointFactory() {}

    @Override
    public GeoPoint create(double latitude, double longitude) {
        return new GeoPoint(latitude, longitude);
    }
}
