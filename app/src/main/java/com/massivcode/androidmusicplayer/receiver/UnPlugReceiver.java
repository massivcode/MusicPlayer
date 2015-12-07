/*
 * Copyright 2015. Pureum Choe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.massivcode.androidmusicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.massivcode.androidmusicplayer.services.MusicService;


public class UnPlugReceiver extends BroadcastReceiver{

    private static final String TAG = UnPlugReceiver.class.getSimpleName();
    public int mHeadSetState;

    @Override
    public void onReceive(Context context, Intent intent) {
//        intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)
        if(Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            if(intent.hasExtra("state")) {
                mHeadSetState = intent.getIntExtra("state", -1);
                // 헤드셋 플러그가 빠짐
                if(mHeadSetState == 0) {
                    Log.d(TAG, "헤드셋 플러그가 빠짐");
                    Intent startServiceIntent = new Intent(context, MusicService.class);
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
