package org.oscim.tiling.source.weather;

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

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Tile;
import org.oscim.debug.Logger;
import org.oscim.tiling.ITileDataSink;
import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.source.ITileDecoder;
import org.oscim.tiling.source.UrlTileDataSource;
import org.oscim.tiling.source.UrlTileSource;

import java.io.IOException;
import java.io.InputStream;

public class WeatherTileSource extends UrlTileSource {
    private static final Logger log = new Logger(WeatherTileSource.class);

    public static class Builder<T extends WeatherTileSource.Builder<T>> extends UrlTileSource.Builder<T> {

        public Builder() {
            super((String) null, "/{Z}/{X}/{Y}.png");
        }

        @Override
        public WeatherTileSource build() {
            return new WeatherTileSource(this);
        }
    }

    private WeatherTileSource(WeatherTileSource.Builder<?> builder) {
        super(builder);
    }

    @SuppressWarnings("rawtypes")
    public static WeatherTileSource.Builder<?> builder() {
        return new WeatherTileSource.Builder();
    }

    /**
     * Create BitmapTileSource for 'url'
     * <p/>
     * By default path will be formatted as: url/z/x/y.png
     * Use e.g. setExtension(".jpg") to overide ending or
     * implement getUrlString() for custom formatting.
     */
    public WeatherTileSource(String url, int zoomMin, int zoomMax) {
        this(url, "/{Z}/{X}/{Y}.png", zoomMin, zoomMax);
    }

    public WeatherTileSource(String url, int zoomMin, int zoomMax, String extension) {
        this(url, "/{Z}/{X}/{Y}" + extension, zoomMin, zoomMax);
    }

    public WeatherTileSource(String url, String tilePath, int zoomMin, int zoomMax) {
        super(builder()
                .url(url)
                .tilePath(tilePath)
                .zoomMin(zoomMin)
                .zoomMax(zoomMax));
    }

    @Override
    public ITileDataSource getDataSource() {
        return new UrlTileDataSource(this, new WeatherTileSource.WeatherBitmapTileDecoder(), getHttpEngine(), true);
    }

    private static class WeatherBitmapTileDecoder implements ITileDecoder {
        @Override
        public boolean decode(Tile tile, ITileDataSink sink, InputStream is)
                throws IOException {

            Bitmap bitmap = CanvasAdapter.decodeBitmap(is);
            if (!bitmap.isValid()) {
                log.debug("{} invalid bitmap", tile);
                return false;
            }
            sink.setTileImage(bitmap);
            return true;
        }
    }
}
