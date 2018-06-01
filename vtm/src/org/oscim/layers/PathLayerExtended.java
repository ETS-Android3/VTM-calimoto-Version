/*
 * Copyright 2017 Luca Osten
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
package org.oscim.layers;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.map.Map;
import org.oscim.theme.styles.LineStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * This class draws a path line in given color or texture.
 */
public class PathLayerExtended extends PathLayer {

    private String uniqueId;

    public PathLayerExtended(Map map, LineStyle style) {
        super(map, style);
    }

    public void setPoint(int index, GeoPoint pt) {
        synchronized (mPoints) {
            mPoints.set(index, pt);
        }
        updatePoints();
    }

    public void addPoint(int index, GeoPoint pt) {
        synchronized (mPoints) {
            mPoints.add(index, pt);
        }
        updatePoints();
    }

    public void removePoint(int index) {
        synchronized (mPoints) {
            mPoints.remove(index);
        }
        updatePoints();
    }

    public GeoPoint getPoint(int index) {
        return mPoints.get(index);
    }

    public List<GeoPoint> getPointsCopy() {
        return new ArrayList<GeoPoint>(mPoints);
    }

    public int size() {
        return mPoints.size();
    }

    public BoundingBox getBounds() {
        return new BoundingBox(mPoints);
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

}
