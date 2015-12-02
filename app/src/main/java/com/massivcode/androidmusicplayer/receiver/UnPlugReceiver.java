package com.massivcode.androidmusicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.massivcode.androidmusicplayer.services.MusicService;

/**
 * Created by Ray Choe on 2015-11-28.
 */
public class UnPlugReceiver extends BroadcastReceiver{

    private static final String TAG = UnPlugReceiver.class.getSimpleName();
    public int mHeadSetState;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            if(intent.hasExtra("state")) {
                mHeadSetState = intent.getIntExtra("state", -1);
                // 헤드셋 플러그가 빠짐
                if(mHeadSetState == 0) {
                    Log.d(TAG, "헤드셋 플러그가 빠짐");
                    Intent startServiceIntent = new Intent(MusicService.ACTION_NAME);
                    startServiceIntent.setAction(MusicService.ACTION_PAUSE_UNPLUGGED);
                    context.startService(startServiceIntent);
                    // 헤드셋 플러그가 껴짐
                } else if(mHeadSetState == 1) {
                    Log.d(TAG, "헤드셋 플러그가 껴짐");
                }
            }
        }
    }
}
