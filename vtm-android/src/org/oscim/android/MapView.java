/*
 * Copyright 2012 Hannes Janetzek
 * Copyright 2016-2017 devemux86
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
package org.oscim.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.android.gl.AndroidGL;
import org.oscim.android.gl.GlConfigChooser;
import org.oscim.android.input.AndroidMotionEvent;
import org.oscim.android.input.GestureHandler;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.core.Tile;
import org.oscim.map.Map;
import org.oscim.utils.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The MapView,
 * <p/>
 * add it your App, have a map!
 * <p/>
 * Dont forget to call onPause / onResume!
 */
public class MapView extends GLSurfaceView {

    static final Logger log = LoggerFactory.getLogger(MapView.class);

    private static void init() {
        System.loadLibrary("vtm-jni");
    }

    protected AndroidMap mMap;
    protected GestureDetector mGestureDetector;
    protected AndroidMotionEvent mMotionEvent;
    private GLRenderer renderer;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        if (isInEditMode())
            return;

        init();

        /* Not sure if this makes sense */
        this.setWillNotDraw(true);
        this.setClickable(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        /* Setup android backend */
        AndroidGraphics.init();
        AndroidAssets.init(context);
        GLAdapter.init(new AndroidGL());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        CanvasAdapter.dpi = (int) (metrics.scaledDensity * CanvasAdapter.DEFAULT_DPI);
        if (!Parameters.CUSTOM_TILE_SIZE)
            Tile.SIZE = Tile.calculateTileSize();

        /* Initialize the Map */
        mMap = new AndroidMap(this);

        /* Initialize Renderer */
        setEGLConfigChooser(new GlConfigChooser());
        setEGLContextClientVersion(2);

        if (GLAdapter.debug)
            setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR
                    | GLSurfaceView.DEBUG_LOG_GL_CALLS);

        renderer = new GLRenderer(mMap);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mMap.clearMap();
        mMap.updateMap(false);

        if (!Parameters.MAP_EVENT_LAYER2) {
            GestureHandler gestureHandler = new GestureHandler(mMap);
            mGestureDetector = new GestureDetector(context, gestureHandler);
            mGestureDetector.setOnDoubleTapListener(gestureHandler);
        }

        mMotionEvent = new AndroidMotionEvent();
    }

    public void onDestroy() {
        mMap.destroy();
    }

    public void onPause() {
        mMap.pause(true);
    }

    public void onResume() {
        mMap.pause(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(android.view.MotionEvent motionEvent) {
        if (!isClickable())
            return false;

        if (mGestureDetector != null) {
            if (mGestureDetector.onTouchEvent(motionEvent))
                return true;
        }

        mMap.input.fire(null, mMotionEvent.wrap(motionEvent));
        mMotionEvent.recycle();
        return true;
    }

    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {

        super.onSizeChanged(width, height, oldWidth, oldHeight);

        if (!isInEditMode()) {
            if (width > 0 && height > 0)
                mMap.viewport().setScreenSize(width, height);
        }
    }

    public Map map() {
        return mMap;
    }

    public Bitmap getBitmapScreenshot() {
        return renderer.getBitmapScreenshot();
    }

    public void enableScreenshots(boolean createScreenshots) {
        renderer.createScreenshots = createScreenshots;
        renderer.bitmapScreenshot = null;
    }

    public void setCompletedOnDrawFrame(Runnable completedOnDrawFrame) {
        renderer.mCompletedOnDrawFrame = completedOnDrawFrame;
    }

    static class AndroidMap extends Map {

        private final MapView mMapView;

        private boolean mRenderRequest;
        private boolean mRenderWait;
        private boolean mPausing;

        public AndroidMap(MapView mapView) {
            super();
            mMapView = mapView;
        }

        @Override
        public int getWidth() {
            return mMapView.getWidth();
        }

        @Override
        public int getHeight() {
            return mMapView.getHeight();
        }

        private final Runnable mRedrawCb = new Runnable() {
            @Override
            public void run() {
                prepareFrame();
                mMapView.requestRender();
            }
        };

        @Override
        public void updateMap(boolean redraw) {
            synchronized (mRedrawCb) {
                if (mPausing)
                    return;

                if (!mRenderRequest) {
                    mRenderRequest = true;
                    mMapView.post(mRedrawCb);
                } else {
                    mRenderWait = true;
                }
            }
        }

        @Override
        public void render() {
            if (mPausing)
                return;

            /** TODO should not need to call prepareFrame in mRedrawCb */
            updateMap(false);
        }

        @Override
        public void beginFrame() {
        }

        @Override
        public void doneFrame(boolean animate) {
            synchronized (mRedrawCb) {
                mRenderRequest = false;
                if (animate || mRenderWait) {
                    mRenderWait = false;
                    render();
                }
            }
        }

        @Override
        public boolean post(Runnable runnable) {
            return mMapView.post(runnable);
        }

        @Override
        public boolean postDelayed(Runnable action, long delay) {
            return mMapView.postDelayed(action, delay);
        }

        public void pause(boolean pause) {
            log.debug("pause... {}", pause);
            mPausing = pause;
        }
    }

    static class GLRenderer extends org.oscim.renderer.MapRenderer
            implements GLSurfaceView.Renderer {
        private Bitmap bitmapScreenshot = null;
        private Runnable mCompletedOnDrawFrame = null;
        private boolean createScreenshots = false;

        public GLRenderer(Map map) {
            super(map);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            super.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceChanged(width, height);

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            super.onDrawFrame();
            if (mCompletedOnDrawFrame != null) {
                mCompletedOnDrawFrame.run();
            }
            if (createScreenshots) {
                bitmapScreenshot = createBitmapFromGLSurface(0, 0, mMap.getWidth(), mMap.getHeight(), gl);
            }
        }

        /*
         * thanks to aaronvargas (http://stackoverflow.com/users/114549/aaronvargas)
         * http://stackoverflow.com/questions/5514149/capture-screen-of-glsurfaceview-to-bitmap
         */
        private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {
            int bitmapBuffer[] = new int[w * h];
            int bitmapSource[] = new int[w * h];
            IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
            intBuffer.position(0);

            try {
                gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
                int offset1, offset2;
                for (int i = 0; i < h; i++) {
                    offset1 = i * w;
                    offset2 = (h - i - 1) * w;
                    for (int j = 0; j < w; j++) {
                        int texturePixel = bitmapBuffer[offset1 + j];
                        int blue = (texturePixel >> 16) & 0xff;
                        int red = (texturePixel << 16) & 0x00ff0000;
                        int pixel = (texturePixel & 0xff00ff00) | red | blue;
                        bitmapSource[offset2 + j] = pixel;
                    }
                }
            } catch (GLException e) {
                return null;
            }

            return Bitmap.createBitmap(bitmapSource, w, h, Config.ARGB_8888);
        }

        public Bitmap getBitmapScreenshot() {
            return bitmapScreenshot;
        }
    }
}
