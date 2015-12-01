package com.massivcode.androidmusicplayer;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Ray Choe on 2015-12-01.
 */
public class DebugApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
