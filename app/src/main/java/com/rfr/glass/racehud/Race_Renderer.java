package com.rfr.glass.racehud;

/** Created by game1_000 on 1/27/2015. */

import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.glass.timeline.DirectRenderingCallback;

import java.util.concurrent.TimeUnit;

public class Race_Renderer implements DirectRenderingCallback {

    private static final String LIVE_CARD_TAG = "Racev3_Renderer";

    /** The refresh rate, in frames per second, of the speedometer. */
    private static final int REFRESH_RATE_FPS = 45;

    /** The duration, in milliseconds, of one frame. */
    private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

    private SurfaceHolder mHolder;
    private RenderThread mRenderThread;
    private final GPSManager mGPSManager;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private boolean mRenderingPaused;

    private final FrameLayout mLayout;
    private final Race_View mRaceView;

    private final GPSManager.OnChangedListener mRaceListener =
            new GPSManager.OnChangedListener()
            {
                @Override
                public void onLocationChanged(GPSManager gpsManager)
                {
                    Location currentLocation = gpsManager.getLocation();
                    Double currentSpeed = gpsManager.getSpeed();

                    /** Find Current speed in MPH */
                    double CurrentSpeedMPH = currentSpeed * 2.23694;

                    if (currentLocation == null)
                    {
                        /** If GPS not connected display default value */
                        mRaceView.setSpeed(-1);
                        mRaceView.setAngle(0);
                        //Log.d(LIVE_CARD_TAG, "-1");
                    }

                    else
                    {
                        mRaceView.setSpeed(CurrentSpeedMPH);
                        mRaceView.setAngle(CurrentSpeedMPH);
                        //Log.d(LIVE_CARD_TAG, String.valueOf(CurrentSpeedMPH));
                    }

                }
            };

    /** Creates a new instance of the {@code Racev3_Renderer}. */
    public Race_Renderer(Context context, GPSManager GPSManager)
    {
        LayoutInflater inflater = LayoutInflater.from(context);

        mLayout = (FrameLayout) inflater.inflate(R.layout.race_layout, null);
        mLayout.setWillNotDraw(false);
        mLayout.setKeepScreenOn(true);

        mRaceView = (Race_View) mLayout.findViewById(R.id.speedometer);
        mRaceView.setKeepScreenOn(true);

        mGPSManager = GPSManager;

        mRaceView.setGPSManager(mGPSManager);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        doLayout();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mRenderingPaused = false;
        mHolder = holder;
        updateRenderingState();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        mRenderingPaused = false;
        mHolder = null;
        updateRenderingState();
    }

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean paused)
    {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    /** Starts or stops rendering according to the {@link com.google.android.glass.timeline.LiveCard}'s state. */
    private void updateRenderingState()
    {
        boolean shouldRender = (mHolder != null) && !mRenderingPaused;
        boolean isRendering = (mRenderThread != null);

        if (shouldRender != isRendering)
        {
            if (shouldRender)
            {

                mGPSManager.addOnChangedListener(mRaceListener);
                mGPSManager.start();

                /**
                if (mGPSManager.hasLocation())
                {
                    Location location = mGPSManager.getLocation();
                    Double speed = mGPSManager.getSpeed();
                }
                 */

                mRenderThread = new RenderThread();
                mRenderThread.start();

            }
            else
            {
                mRenderThread.quit();
                mRenderThread = null;

                mGPSManager.removeOnChangedListener(mRaceListener);
                mGPSManager.stop();
            }
        }
    }

    /**
     * Requests that the views redo their layout. This must be called manually every time the
     * tips view's text is updated because this layout doesn't exist in a GUI thread where those
     * requests will be enqueued automatically.
     */
    private void doLayout()
    {
        /** Measure and update the layout so that it will take up the entire surface space
            when it is drawn. */
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth,
                View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight,
                View.MeasureSpec.EXACTLY);

        mLayout.measure(measuredWidth, measuredHeight);
        mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
    }

    /** Repaints the Live Card. */
    private synchronized void repaint()
    {
        Canvas canvas = null;

        try
        {
            canvas = mHolder.lockCanvas();
        }
        catch (RuntimeException e)
        {
            Log.d(LIVE_CARD_TAG, "lockCanvas failed", e);
        }

        if (canvas != null)
        {
            doLayout();
            mLayout.draw(canvas);

            try
            {
                mHolder.unlockCanvasAndPost(canvas);
            }
            catch (RuntimeException e)
            {
                Log.d(LIVE_CARD_TAG, "unlockCanvasAndPost failed", e);
            }
        }
    }

    /** Redraws the Live Card in the background. */
    private class RenderThread extends Thread
    {
        private boolean mShouldRun;

        /** Initializes the background rendering thread. */
        public RenderThread() {
            mShouldRun = true;
        }

        /** Returns true if the rendering thread should continue to run. */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /** Requests that the rendering thread exit at the next opportunity. */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run()
        {
            while (shouldRun())
            {
                long frameStart = SystemClock.elapsedRealtime();
                repaint();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0)
                {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }
}
