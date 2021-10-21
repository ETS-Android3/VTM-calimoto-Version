/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014-2015 Ludwig M Brinckmann
 * Copyright 2018 Luca Osten
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
package org.oscim.utils;

import org.oscim.core.BoundingBox;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convenience class for tile functions.
 */
public final class TileUtil {
	
	public static List<Tile> getTiles(BoundingBox boundingBox, byte zoomLevel) {
		int tileLeft = MercatorProjection.longitudeToTileX(boundingBox.getMinLongitude(), zoomLevel);
		int tileTop = MercatorProjection.latitudeToTileY(boundingBox.getMaxLatitude(), zoomLevel);
		int tileRight = MercatorProjection.longitudeToTileX(boundingBox.getMaxLongitude(), zoomLevel);
		int tileBottom = MercatorProjection.latitudeToTileY(boundingBox.getMinLatitude(), zoomLevel);
		
		int initialCapacity = (tileRight - tileLeft + 1) * (tileBottom - tileTop + 1);
		List<Tile> tiles = new ArrayList<Tile>(initialCapacity);
		
		for (int tileY = tileTop; tileY <= tileBottom; ++tileY) {
			for (int tileX = tileLeft; tileX <= tileRight; ++tileX) {
				tiles.add(new Tile(tileX, tileY, zoomLevel));
			}
		}
		
		return tiles;
	}
	
	public static List<Tile> getTilesPlusBuffer(BoundingBox boundingBox, byte zoomLevel, int buffer) {
		int maxTileIndex = (2 << zoomLevel) - 1;
		int tileLeft = MercatorProjection.longitudeToTileX(boundingBox.getMinLongitude(), zoomLevel);
		tileLeft -= buffer;
		if (tileLeft < 0)
			tileLeft = 0;
		int tileTop = MercatorProjection.latitudeToTileY(boundingBox.getMaxLatitude(), zoomLevel);
		tileTop -= buffer;
		if (tileTop < 0)
			tileTop = 0;
		int tileRight = MercatorProjection.longitudeToTileX(boundingBox.getMaxLongitude(), zoomLevel);
		tileRight += buffer;
		if (tileRight > maxTileIndex)
			tileRight = maxTileIndex;
		int tileBottom = MercatorProjection.latitudeToTileY(boundingBox.getMinLatitude(), zoomLevel);
		tileBottom += buffer;
		if (tileBottom > maxTileIndex)
			tileBottom = maxTileIndex;
		
		int initialCapacity = (tileRight - tileLeft + 1) * (tileBottom - tileTop + 1);
		List<Tile> tiles = new ArrayList<Tile>(initialCapacity);
		
		for (int tileY = tileTop; tileY <= tileBottom; ++tileY) {
			for (int tileX = tileLeft; tileX <= tileRight; ++tileX) {
				tiles.add(new Tile(tileX, tileY, zoomLevel));
			}
		}
		
		return tiles;
	}
	
	/**
	 * Upper left tile for an area.
	 *
	 * @param boundingBox the area boundingBox
	 * @param zoomLevel   the zoom level.
	 * @return the tile at the upper left of the bbox.
	 */
	public static Tile getUpperLeft(BoundingBox boundingBox, byte zoomLevel) {
		int tileLeft = MercatorProjection.longitudeToTileX(boundingBox.getMinLongitude(), zoomLevel);
		int tileTop = MercatorProjection.latitudeToTileY(boundingBox.getMaxLatitude(), zoomLevel);
		return new Tile(tileLeft, tileTop, zoomLevel);
	}
	
	/**
	 * Lower right tile for an area.
	 *
	 * @param boundingBox the area boundingBox
	 * @param zoomLevel   the zoom level.
	 * @return the tile at the lower right of the bbox.
	 */
	public static Tile getLowerRight(BoundingBox boundingBox, byte zoomLevel) {
		int tileRight = MercatorProjection.longitudeToTileX(boundingBox.getMaxLongitude(), zoomLevel);
		int tileBottom = MercatorProjection.latitudeToTileY(boundingBox.getMinLatitude(), zoomLevel);
		return new Tile(tileRight, tileBottom, zoomLevel);
	}
	
	public static Set<Tile> getTiles(Tile upperLeft, Tile lowerRight) {
		Set<Tile> tiles = new HashSet<Tile>();
		for (int tileY = upperLeft.tileY; tileY <= lowerRight.tileY; ++tileY) {
			for (int tileX = upperLeft.tileX; tileX <= lowerRight.tileX; ++tileX) {
				tiles.add(new Tile(tileX, tileY, upperLeft.zoomLevel));
			}
		}
		return tiles;
	}
	
}
