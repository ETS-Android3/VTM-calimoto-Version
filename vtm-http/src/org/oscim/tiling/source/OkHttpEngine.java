/*
 * Copyright 2014 Charles Greb
 * Copyright 2014 Hannes Janetzek
 * Copyright 2017 devemux86
 * Copyright 2017 Mathieu De Brito
 * Copyright 2019 calimoto GmbH (Mareike Wendtland)
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
package org.oscim.tiling.source;

import org.oscim.core.Tile;
import org.oscim.utils.IOUtils;
import org.oscim.debug.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpEngine implements HttpEngine {
    
    private static final Logger log = new Logger(OkHttpEngine.class);

    private static final int TIMEOUT_CONNECT_IN_SEC = 3;

    private final OkHttpClient mClient;
    private final UrlTileSource mTileSource;

    private InputStream mInputStream;
    private byte[] mCachedData;
    private int rerequestIntervalInSec = 0;

    public static class OkHttpFactory implements HttpEngine.Factory {
        private final OkHttpClient.Builder mClientBuilder;

        public OkHttpFactory() {
            // decrease connect timeout manually (default 10 sec.) that map tiles can initially 
            // be loaded as fast as possible (otherwise problems with wifi connection)
            mClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECT_IN_SEC, TimeUnit.SECONDS);
        }

        public OkHttpFactory(OkHttpClient.Builder clientBuilder) {
            mClientBuilder = clientBuilder;
        }

        @Override
        public HttpEngine create(UrlTileSource tileSource) {
            return new OkHttpEngine(mClientBuilder.build(), tileSource);
        }
    }

    public OkHttpEngine(OkHttpClient client, UrlTileSource tileSource) {
        mClient = client;
        mTileSource = tileSource;
    }

    @Override
    public InputStream read() throws IOException {
        return mInputStream;
    }

    @Override
    public void sendRequest(Tile tile) throws IOException {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null.");
        }
        URL url = new URL(mTileSource.getTileUrl(tile, mTileSource.getUseFallbackUrl()));
        Request.Builder builder = new Request.Builder().url(url);
        for (Entry<String, String> opt : mTileSource.getRequestHeader().entrySet())
        {
            builder.addHeader(opt.getKey(), opt.getValue());
        }
        Request request = builder.build();
        Response response;
        try
        {
            response = mClient.newCall(request).execute();
        }
        catch (IOException e)
        {
            // increment interval before starting a new request
            if (rerequestIntervalInSec < 5)
            {
                rerequestIntervalInSec++;
            }
            else
            {
                // cancel further fallback requests
                // (this is necessary to avoid endless loop of retrying)
                return;
            }
            try
            {
                // wait
                Thread.sleep(1000L * rerequestIntervalInSec);
            }
            catch (InterruptedException ie)
            {
                /*
                 * interrupt exception most likely occurs because thread is being shut down
                 * e.g. user switches from online map to offline map and thread pool is shut down
                 * since the interrupt exception was thrown the thread is no longer interrupted
                 * we now need to interrupt the thread again, so it can shut down as desired
                 */
                try
                {
                    // interrupt thread again so it can shut down
                    if (!Thread.currentThread().isInterrupted())
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                catch (Throwable error)
                {
                    log.error(error);
                }
                // cancel further code as thread is being shut down
                return;
            }

            // log the problem
            log.warn("Error on loading tiles - " + Thread.currentThread().getName());

            // switch to fallback url for all further requests in this session
            if (!mTileSource.getUseFallbackUrl())
            {
                // only once
                mTileSource.setUseFallbackUrl(true);
                log.warn("Fallback server " + mTileSource.getFallbackUrl() +
                        " will be used for requesting tiles instead of " + mTileSource.getUrl());
            }

            // load current tile again with new fallback url
            sendRequest(tile);

            // forward exception to be handled later
            throw e;
        }

        if (mTileSource.tileCache != null)
        {
            mCachedData = response.body().bytes();
            mInputStream = new ByteArrayInputStream(mCachedData);
        }
        else
        {
            mInputStream = response.body().byteStream();
        }
        rerequestIntervalInSec = 0;
    }

    @Override
    public void close() {
        if (mInputStream == null)
            return;

        IOUtils.closeQuietly(mInputStream);
        mInputStream = null;
    }

    @Override
    public void setCache(OutputStream os) {
        if (mTileSource.tileCache != null) {
            try {
                os.write(mCachedData);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean requestCompleted(boolean success) {
        IOUtils.closeQuietly(mInputStream);
        mInputStream = null;

        return success;
    }
}
