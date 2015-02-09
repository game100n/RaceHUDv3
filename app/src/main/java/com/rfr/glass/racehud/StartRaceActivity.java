package com.rfr.glass.racehud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Created by game1_000 on 1/31/2015. */

/**
 * Having an activity that starts the service allows one to attach with the
 * interactive debugger more predictably
 */
public class StartRaceActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        startService(new Intent(this, Race_Service.class));
        finish();
    }
}
