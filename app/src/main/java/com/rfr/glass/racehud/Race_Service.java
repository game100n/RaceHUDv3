package com.rfr.glass.racehud;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

/**
 * A {@link android.app.Service} that publishes a {@link com.google.android.glass.timeline.LiveCard} in the timeline.
 */
public class Race_Service extends Service
{
    private static final String LIVE_CARD_TAG = "Racev3_Service";

    private GPSManager mGPSManager;

    private LiveCard mLiveCard;
    private Race_Renderer mRenderer;

    @Override
    public void onCreate()
    {
        super.onCreate();

        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGPSManager = new GPSManager(locationManager);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null)
        {
            Log.d(LIVE_CARD_TAG, "Publishing LiveCard");
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            mRenderer = new Race_Renderer(this, mGPSManager);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer);

            /** Display the options menu when the live card is tapped. */
            Intent menuIntent = new Intent(this, Race_MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);
            Log.d(LIVE_CARD_TAG, "Done publishing LiveCard");
        }
        else
        {
            mLiveCard.navigate();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        if (mLiveCard != null && mLiveCard.isPublished())
        {
            Log.d(LIVE_CARD_TAG, "Unpublishing LiveCard");
            mLiveCard.unpublish();
            mLiveCard = null;
        }

        mGPSManager = null;
        super.onDestroy();
    }
}
