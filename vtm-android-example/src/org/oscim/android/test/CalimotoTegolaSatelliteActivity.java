/*
 * Copyright 2018 devemux86
 * Copyright 2021 calimoto GmbH (Robert Schierz)
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
package org.oscim.android.test;

import android.os.Bundle;

import org.oscim.GlobalConfig;
import org.oscim.android.cache.TileCache;
import org.oscim.layers.TileGridLayer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.mvt.CalimotoTegolaTilesMvtTileSource;

public class CalimotoTegolaSatelliteActivity extends MapActivity {
    private static final boolean USE_CACHE = false;

    private TileCache mCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BitmapTileSource backgroundTileSource = BitmapTileSource.builder()
                .httpFactory(new OkHttpEngine.OkHttpFactory())
                .url(GlobalConfig.SATELLITE_SERVER_DNS)
                .tilePath("/{Z}/{X}/{Y}.jpg")
                .build();

        mMap.layers().add(0, new BitmapTileLayer(mMap, backgroundTileSource));

        UrlTileSource tileSource = CalimotoTegolaTilesMvtTileSource.builder()
                .httpFactory(new OkHttpEngine.OkHttpFactory())
                //.locale("en")
                .build();

        if (USE_CACHE) {
            // Cache the tiles into a local SQLite database
            mCache = new TileCache(this, null, "tile.db");
            mCache.setCacheSize(512 * (1 << 10));
            tileSource.setCache(mCache);
        }

        VectorTileLayer l = mMap.setBaseMap(tileSource);
        mMap.setTheme(VtmThemes.CALIMOTO_ONLINE_SATELLITE);

        mMap.layers().add(new LabelLayer(mMap, l));

        TileGridLayer mGridLayer = new TileGridLayer(mMap);
        mMap.layers().add(mGridLayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCache != null)
            mCache.dispose();
    }
}
