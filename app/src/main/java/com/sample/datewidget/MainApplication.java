package com.sample.datewidget;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

/**
 * Created by priyabratapatnaik on 04/11/15.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        // Important to initialize to escape Resource not found: "org/joda/time/tz/data/ZoneInfoMap" IOException
        JodaTimeAndroid.init(getApplicationContext());
    }
}
