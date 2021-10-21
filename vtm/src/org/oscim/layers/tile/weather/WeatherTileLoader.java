package org.oscim.layers.tile.weather;

/*
 * Copyright 2021 calimoto GmbH (Robert Schierz)
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

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Tile;
import org.oscim.debug.Logger;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.TileLoader;
import org.oscim.renderer.bucket.BitmapBucket;
import org.oscim.renderer.bucket.RenderBuckets;
import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.QueryResult;
import org.oscim.tiling.TileSource;

import static org.oscim.layers.tile.MapTile.State.LOADING;

public class WeatherTileLoader extends TileLoader {
    private static final Logger log = new Logger(WeatherTileLoader.class);

    private final ITileDataSource mTileDataSource;
    private final WeatherTileLayer mLayer;

    public WeatherTileLoader(WeatherTileLayer tileLayer, TileSource tileSource) {
        super(tileLayer.getManager());

        mTileDataSource = tileSource.getDataSource();
        mLayer = tileLayer;
    }

    @Override
    protected boolean loadTile(MapTile tile) {
        try {
            mTileDataSource.query(tile, this);
        } catch (Exception e) {
            log.debug("{} {}", tile, e);
            return false;
        }
        return true;
    }

    @Override
    public void setTileImage(Bitmap bitmap) {
        if (isCanceled() || !mTile.state(LOADING)) {
            bitmap.recycle();
            return;
        }

        BitmapBucket l = new BitmapBucket(false);
        l.setBitmap(bitmap, Tile.SIZE, Tile.SIZE, mLayer.pool);

        RenderBuckets buckets = new RenderBuckets();
        buckets.set(l);
        mTile.data = buckets;
    }

    @Override
    public void completed(QueryResult result) {
        super.completed(result);
    }

    @Override
    public void dispose() {
        mTileDataSource.cancel();
    }

    @Override
    public void cancel() {
        mTileDataSource.cancel();
    }
}
