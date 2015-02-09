package com.rfr.glass.racehud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * A transparent {@link android.app.Activity} displaying a "Stop" options menu to remove the {@link com.google.android.glass.timeline.LiveCard}.
 */
public class Race_MenuActivity extends Activity
{
    /** For logging. */
    private static final String LIVE_CARD_TAG = "Racev3_Menu";

    private View content = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.race_layout);
        content = findViewById(android.R.id.content);
        Log.d(LIVE_CARD_TAG, "View Created");
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        /** Open the options menu right away. */
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(LIVE_CARD_TAG, "Menu Created");
        getMenuInflater().inflate(R.menu.race_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_stop:
                /** Stop the service which will unpublish the live card. */
                stopService(new Intent(this, Race_Service.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu)
    {
        super.onOptionsMenuClosed(menu);
        /** Nothing else to do, finish the Activity. */
        finish();
    }
}
