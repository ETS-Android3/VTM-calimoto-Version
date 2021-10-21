package org.oscim.layers.tile.weather;

/*
 * Copyright 2021 calimoto GmbH (Robert Schierz)
 * Copyright 2021 calimoto GmbH (Luca Osten)
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

import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.layers.tile.*;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.map.Map;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.tiling.TileSource;
import org.oscim.utils.FastMath;

public class WeatherTileLayer extends TileLayer {
    private static final int CACHE_LIMIT = 40;

    private final TileSource mTileSource;

    private final String layerDateAndTime;

    /**
     * Bitmap alpha in range 0 to 1.
     */
    private float mBitmapAlpha = 1.0f;

    public WeatherTileLayer(Map map, TileSource tileSource, String layerDateAndTime) {
        this(map, tileSource, CACHE_LIMIT, layerDateAndTime);
    }

    public WeatherTileLayer(Map map, TileSource tileSource, float bitmapAlpha, String layerDateAndTime) {
        this(map, tileSource, CACHE_LIMIT, bitmapAlpha, layerDateAndTime);
    }

    public WeatherTileLayer(Map map, TileSource tileSource, int cacheLimit, String layerDateAndTime) {
        this(map, tileSource, cacheLimit, tileSource.getAlpha(), layerDateAndTime);
    }

    public WeatherTileLayer(Map map, TileSource tileSource, int cacheLimit, float bitmapAlpha, String layerDateAndTime) {
        super(map,
                new TileManager(map, cacheLimit),
                new VectorTileRenderer());

        mTileManager.setZoomLevel(tileSource.getZoomLevelMin(),
                tileSource.getZoomLevelMax());
        this.layerDateAndTime = layerDateAndTime;
        mTileSource = tileSource;
        setBitmapAlpha(bitmapAlpha, false);
        initLoader(getNumLoaders());
        setFade(map.getMapPosition());
    }

    public void setBitmapAlpha(float bitmapAlpha, boolean redraw) {
        mBitmapAlpha = FastMath.clamp(bitmapAlpha, 0f, 1f);
        tileRenderer().setBitmapAlpha(mBitmapAlpha);
        if (redraw)
            map().updateMap(true);
    }

    @Override
    public void onMapEvent(Event event, MapPosition pos) {
        super.onMapEvent(event, pos);
        if (event != Map.CLEAR_EVENT && event != Map.POSITION_EVENT) {
            if (mTileManager.update(pos)) {
                notifyLoaders();
            }
        }
        setFade(pos);
    }

    private void setFade(MapPosition pos) {
        BitmapTileLayer.FadeStep[] fade = mTileSource.getFadeSteps();

        if (fade == null) {
            return;
        }

        float alpha = mBitmapAlpha;
        for (BitmapTileLayer.FadeStep f : fade) {
            if (pos.scale < f.scaleStart || pos.scale > f.scaleEnd)
                continue;

            if (f.alphaStart == f.alphaEnd) {
                alpha = f.alphaStart;
                break;
            }
            // interpolate alpha between start and end
            alpha = (float) (f.alphaStart + (pos.getZoom() - f.zoomStart) * (f.alphaEnd - f.alphaStart) / (f.zoomEnd - f.zoomStart));
            break;
        }

        alpha = FastMath.clamp(alpha, 0f, 1f) * mBitmapAlpha;
        tileRenderer().setBitmapAlpha(alpha);
    }

    @Override
    protected TileLoader createLoader() {
        return new WeatherTileLoader(this, mTileSource);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pool.clear();
    }

    public void disposeTileCache() {
        mTileSource.tileCache.dispose();
    }

    private static final int POOL_FILL = 20;

    /**
     * pool shared by TextLayers
     */
    final TextureItem.TexturePool pool = new TextureItem.TexturePool(POOL_FILL) {

    };

    public String getLayerDateAndTime() {
        return layerDateAndTime;
    }

}
