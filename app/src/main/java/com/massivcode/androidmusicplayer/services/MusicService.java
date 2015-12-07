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

package com.massivcode.androidmusicplayer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.activities.MainActivity;
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.FinishActivity;
import com.massivcode.androidmusicplayer.events.InitEvent;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.events.RequestEvent;
import com.massivcode.androidmusicplayer.events.RequestMusicEvent;
import com.massivcode.androidmusicplayer.events.Restore;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.receiver.UnPlugReceiver;
import com.massivcode.androidmusicplayer.utils.DataBackupUtil;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import de.greenrobot.event.EventBus;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = MusicService.class.getSimpleName();


    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT";
    public static final String ACTION_PLAY_PREVIOUS = "ACTION_PLAY_PREVIOUS";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PAUSE_UNPLUGGED = "ACTION_PAUSE_UNPLUGGED";
    public static final String ACTION_PLAY_SELECTED = "ACTION_PLAY_SELECTED";
    public static final String ACTION_FINISH = "ACTION_FINISH";

    private boolean isReady = false;
    private MediaSessionCompat mSession;
    private MediaMetadataCompat mMetadata;

    private SaveState mSaveState;


    // 수행하는 곳
    private static class UiRefresher extends Handler {
        private final WeakReference<MusicService> mService;

        UiRefresher(MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mService.get();
            if (service != null) {
                service.sendEvents();
            }
        }
    }


    UiRefresher mHandler = new UiRefresher(this);


    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCompletion(MediaPlayer mp) {

        // 현재 노래 재생이 끝났을 경우 호출되는 리스너

        mp.pause();
        mp.reset();

        try {
            // 다음 곡의 포지션에 영향을 준다.
            boolean isShuffle = DataBackupUtil.getInstance(getApplicationContext()).loadIsShuffle();
            // 전부 재생하고 동작.
            boolean isRepeat = DataBackupUtil.getInstance(getApplicationContext()).loadIsRepeat();
            Log.d(TAG, "컴플리션 -> 셔플 : " + isShuffle + " 반복 : " + isRepeat);

            if (mCurrentPlaylist != null) {
                setNextMusicInfo(mp, isShuffle, isRepeat);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mCurrentMusicInfo != null) {
                showNotification();
                sendMusicEvent();
            }
        }

    }

    private void setNextMusicInfo(MediaPlayer mp, boolean isShuffle, boolean isRepeat) throws IOException {

        if (isShuffle) {
            mCurrentPosition = shuffle(mCurrentPlaylist.size());
        } else {
            if (mCurrentPosition < getCurrentPlaylistSize()) {
                mCurrentPosition++;
            } else {
                mCurrentPosition = 0;
            }
        }

        mp.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
        mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));

        if (isShuffle) {
            mp.prepare();
            mp.start();
            isReady = true;
        } else {
            if (isRepeat) {
                mp.prepare();
                mp.start();
                isReady = true;
            } else {
                if (mCurrentPosition != 0) {
                    mp.prepare();
                    mp.start();
                    isReady = true;
                } else {
                    mp.stop();
                    mp.reset();
                    isReady = false;
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    private MediaPlayer mMediaPlayer;
    private String mAction = null;
    private ArrayList<Long> mCurrentPlaylist;
    private int mCurrentPosition = -1;
    private MusicInfo mCurrentMusicInfo;
    private UnPlugReceiver mUnPlugReceiver;

    private HashMap<Long, MusicInfo> mAllMusicData = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "MusicService.onCreate()");
        Log.d(TAG, "셔플 : " + DataBackupUtil.getInstance(getApplicationContext()).loadIsShuffle() +
                " 반복 : " + DataBackupUtil.getInstance(getApplicationContext()).loadIsRepeat());

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(this);

        mUnPlugReceiver = new UnPlugReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mUnPlugReceiver, filter);

        if (Build.VERSION.SDK_INT < 23) {
            if (mAllMusicData == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "InitEvent @ service");
                        mAllMusicData = MusicInfoLoadUtil.getAllMusicInfo(getApplicationContext());
                    }
                }).start();
            }
        }

        int loadedPosition = DataBackupUtil.getInstance(getApplicationContext()).loadCurrentPlayingMusicPosition();
        if (loadedPosition != -1) {
            mCurrentPosition = loadedPosition;
            Log.d(TAG, "로딩한 플레이 위치 : " + loadedPosition);
        }

        ArrayList<Long> loadedPlaylist = DataBackupUtil.getInstance(getApplicationContext()).loadLastPlayedSongs();
        if (loadedPlaylist != null && loadedPlaylist.size() != 0 ) {
            mCurrentPlaylist = loadedPlaylist;
            mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
            try {
                mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isReady = true;
            // TODO 프래그먼트들에 메세지 보내기
            sendAllEvent();
            EventBus.getDefault().post(new RequestEvent());
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            mAction = intent.getAction();
        }

        if (mAction == null) {
            EventBus.getDefault().post(new FinishActivity());
            stopForeground(true);
            stopService(new Intent(getApplicationContext(), MusicService.class));
            return START_NOT_STICKY;
        }

        switch (mAction) {
            case ACTION_PLAY_SELECTED:
                mCurrentPosition = intent != null ? intent.getIntExtra("position", 0) : 0;
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_PLAY:
                mCurrentPlaylist = (ArrayList<Long>) (intent != null ? intent.getSerializableExtra("list") : null);
                mCurrentPosition = intent.getIntExtra("position", 0);
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_PLAY_NEXT:
            case ACTION_PLAY_PREVIOUS:
                boolean isShuffle = DataBackupUtil.getInstance(getApplicationContext()).loadIsShuffle();
                if (isShuffle) {
                    mCurrentPosition = shuffle(mCurrentPlaylist.size());
                } else {
                    mCurrentPosition = intent != null ? intent.getIntExtra("position", 0) : 0;
                }
                mCurrentMusicInfo = MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_FINISH:

                if (mMediaPlayer.isPlaying()) {
                    mHandler.removeMessages(0);
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                } else {
                    mHandler.removeMessages(0);
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                EventBus.getDefault().post(new FinishActivity());
                stopForeground(true);
                stopService(new Intent(getApplicationContext(), MusicService.class));
                break;
            case ACTION_PAUSE_UNPLUGGED:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    if (mCurrentMusicInfo != null && mCurrentPlaylist != null) {
                        sendAllEvent();
                        showNotification();
                    }
                }
                break;
        }


        switch (mAction) {

            case ACTION_PLAY_SELECTED:


                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.reset();
                    try {

                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;


                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                }
                break;
            case ACTION_PLAY:

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;

                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                }
                break;
            case ACTION_PAUSE:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
                if (mCurrentMusicInfo != null && mCurrentPlaylist != null) {
                    sendAllEvent();
                    showNotification();
                }
                break;
            case ACTION_PLAY_NEXT:
//                Log.d(TAG, "ACTION_PLAY_NEXT");
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();

                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                }

                break;
            case ACTION_PLAY_PREVIOUS:
                if (mMediaPlayer.isPlaying()) {

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }

                } else {
                    mMediaPlayer.reset();
                    try {
                        mMediaPlayer.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
                        mMediaPlayer.prepare();
                        isReady = true;
                        // TODO 프래그먼트들에 메세지 보내기
                        sendAllEvent();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        showNotification();
                    }
                }
                break;
        }


        return START_STICKY;
    }

    private void setMetaData() {
        if (mCurrentMusicInfo != null) {
            Bitmap bitmap = MusicInfoLoadUtil.getBitmap(getApplicationContext(), mCurrentMusicInfo.getUri(), 4);
            mMetadata = new MediaMetadataCompat.Builder().putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, mCurrentMusicInfo.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentMusicInfo.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentMusicInfo.getTitle())
                    .build();
            if (mSession == null) {
                mSession = new MediaSessionCompat(this, "tag", null, null);
                mSession.setMetadata(mMetadata);
                mSession.setActive(true);
                mSession.setCallback(new MediaSessionCompat.Callback() {
                    @Override
                    public void onPlay() {
                        super.onPlay();
                        Toast.makeText(MusicService.this, "onPlay", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

        unregisterReceiver(mUnPlugReceiver);

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);

        if (mCurrentPlaylist != null && mCurrentPosition != -1) {
            DataBackupUtil.getInstance(getApplicationContext()).saveCurrentPlayingMusicPosition(mCurrentPosition);
            DataBackupUtil.getInstance(getApplicationContext()).saveCurrentPlaylist(mCurrentPlaylist);
        }

        Log.d(TAG, "MusicService.onDestroy()");

    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
        if (event instanceof RequestEvent) {
            sendMusicEvent();
            sendPlayback();
        } else if (event instanceof SaveState) {
            mSaveState = new SaveState();
            MusicInfo musicInfo = getCurrentInfo();
            ArrayList<Long> currentPlaylist = getCurrentPlaylist();
            int currentPlayPosition = getCurrentPosition();
            mSaveState.setMusicInfo(musicInfo);
            mSaveState.setCurrentPlaylist(currentPlaylist);
            mSaveState.setCurrentPositionAtPlaylist(currentPlayPosition);
            mSaveState.setCurrentPlayTime(mMediaPlayer.getCurrentPosition());
        } else if (event instanceof Restore) {
            if (mSaveState != null) {
                EventBus.getDefault().post(mSaveState);
            }
        } else if (event instanceof InitEvent) {
            if (mAllMusicData == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "InitEvent @ service");
                        mAllMusicData = MusicInfoLoadUtil.getAllMusicInfo(getApplicationContext());
                    }
                }).start();
            }

        } else if (event instanceof RequestMusicEvent) {
            sendMusicEvent();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public MediaPlayer getMediaPlayer() {
        if (mMediaPlayer != null) {
            return mMediaPlayer;
        } else {
            return null;
        }
    }

    public MusicInfo getCurrentInfo() {
        if (mCurrentPlaylist != null) {
            return MusicInfoLoadUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
        } else {
            return null;
        }

    }


    public boolean isReady() {
        return isReady;
    }


    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public int getCurrentPlaylistSize() {
//        할당할 곳 = 비교문? 참일때값 : 거짓일때 값
        return mCurrentPlaylist != null ? (mCurrentPlaylist.size() - 1) : 0;
    }

    private Uri switchIdToUri(long id) {
        return Uri.parse("content://media/external/audio/media/" + id);
    }

    private void sendPlayback() {
        PlayBack playback = new PlayBack();
        playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
        playback.setPlaying(mMediaPlayer.isPlaying());
        EventBus.getDefault().post(playback);
    }

    private void sendMusicEvent() {
        MusicEvent musicEvent = new MusicEvent();
        musicEvent.setMediaPlayer(getMediaPlayer());
        musicEvent.setMusicInfo(getCurrentInfo());
        EventBus.getDefault().post(musicEvent);
    }

    private void sendAllEvent() {
        sendMusicEvent();
        mHandler.sendEmptyMessage(0);
    }

    public ArrayList<Long> getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    public HashMap<Long, MusicInfo> getAllMusicData() {
        return mAllMusicData;
    }

    private void sendEvents() {
        if (mMediaPlayer.isPlaying()) {
//            Log.d(TAG, "UIRefresher is running");
            PlayBack playback = new PlayBack();
            playback.setPlaying(mMediaPlayer.isPlaying());
            playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
            EventBus.getDefault().post(playback);

            mHandler.sendEmptyMessageDelayed(0, 1000);

        } else {
//            Log.d(TAG, "UIRefresher is stopped");
            PlayBack playback = new PlayBack();
            playback.setPlaying(mMediaPlayer.isPlaying());
            playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
            EventBus.getDefault().post(playback);
        }
    }

    private void showNotificationUpperKitKat(MediaMetadataCompat metadata) {
        // Notification 작성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        // Hide the timestamp
        builder.setShowWhen(false);
        // Set the Notification style
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        // Set the Notification color
        builder.setColor(Color.parseColor("#2196F3"));
        // Set the large and small icons
        Bitmap bitmap = MusicInfoLoadUtil.getBitmap(getApplicationContext(), mCurrentMusicInfo.getUri(), 4);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_image);
        }
        builder.setLargeIcon(bitmap);
        builder.setSmallIcon(R.mipmap.ic_no_image);
        builder.setContentTitle(mCurrentMusicInfo.getTitle());
        builder.setContentText(mCurrentMusicInfo.getArtist());


        int icon;
        if (mMediaPlayer.isPlaying()) {
            Log.d(TAG, "is playing");
            icon = android.R.drawable.ic_media_pause;
        } else {
            Log.d(TAG, "is not playing");
            icon = android.R.drawable.ic_media_play;
        }

        // 이전 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================


        Intent musicPreviousIntent = new Intent(getApplicationContext(), MusicService.class);
        musicPreviousIntent.putExtra("metadata", metadata);
        musicPreviousIntent.setAction(ACTION_PLAY_PREVIOUS);
        musicPreviousIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_PREVIOUS));

        PendingIntent musicPreviousPendingIntent = PendingIntent.getService(getApplicationContext(),
                0, musicPreviousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_media_previous, "prev", musicPreviousPendingIntent);
        // =========================================================================================

        // 재생 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicStopIntent = new Intent(getApplicationContext(), MusicService.class);
        musicStopIntent.putExtra("metadata", metadata);
        musicStopIntent.setAction(ACTION_PAUSE);

        PendingIntent musicStopPendingIntent = PendingIntent.getService(getApplicationContext(),
                1,
                musicStopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(icon, "pause", musicStopPendingIntent);
        // =========================================================================================

        // 다음 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicNextIntent = new Intent(getApplicationContext(), MusicService.class);
        musicNextIntent.putExtra("metadata", metadata);
        musicNextIntent.setAction(ACTION_PLAY_NEXT);
        musicNextIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_NEXT));

        PendingIntent musicNextPendingIntent = PendingIntent.getService(getApplicationContext(),
                2, musicNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(android.R.drawable.ic_media_next, "next", musicNextPendingIntent);
        // =========================================================================================

        // 종료 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent closeIntent = new Intent(getApplicationContext(), MusicService.class);
        closeIntent.setAction(ACTION_FINISH);

        PendingIntent closePendingIntent = PendingIntent.getService(getApplicationContext(), 3, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.arrow_up_float, "close", closePendingIntent);
        // =========================================================================================

        // Notification 터치 했을 때 실행할 PendingIntent 지정
        // =========================================================================================
        Intent activityStartIntent = new Intent(MainActivity.ACTION_NAME);
        activityStartIntent.putExtra("restore", getCurrentInfo());
        PendingIntent activityStartPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                1,
                activityStartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(activityStartPendingIntent);
        // =========================================================================================


        Notification notification = builder.setAutoCancel(true).build();

        startForeground(1, notification);


    }

    public int getPositionAtPreviousOrNext(String flag) {
        int position = getCurrentPosition();
        switch (flag) {
            case ACTION_PLAY_NEXT:
                if (position < getCurrentPlaylistSize()) {
                    position += 1;
                } else {
                    position = 0;
                }
                break;
            case ACTION_PLAY_PREVIOUS:
                if (position > 0) {
                    position -= 1;
                } else {
                    position = getCurrentPlaylistSize();
                }
                break;
        }
        return position;
    }

    /**
     * random.nextInt(range) -> 0 ~ range-1
     *
     * @param range
     * @return
     */
    public int shuffle(int range) {
        int result;

        if (range == 1) {
            return 0;
        }

        Random random = new Random();
        result = random.nextInt(range);

        if (result == mCurrentPosition) {
            result = shuffle(range);
        }

        return result;
    }

    private void showNotificationUnderLollipop() {
        Notification.Builder builder = new Notification.Builder(this);
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);

        // 이전 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================


        Intent musicPreviousIntent = new Intent(getApplicationContext(), MusicService.class);
        musicPreviousIntent.setAction(ACTION_PLAY_PREVIOUS);
        musicPreviousIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_PREVIOUS));

        PendingIntent musicPreviousPendingIntent = PendingIntent.getService(getApplicationContext(),
                0, musicPreviousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_previous_ib, musicPreviousPendingIntent);
        // =========================================================================================

        // 재생 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicStopIntent = new Intent(getApplicationContext(), MusicService.class);
        musicStopIntent.setAction(ACTION_PAUSE);

        PendingIntent musicStopPendingIntent = PendingIntent.getService(getApplicationContext(),
                1,
                musicStopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_play_ib, musicStopPendingIntent);
        // =========================================================================================

        // 다음 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent musicNextIntent = new Intent(getApplicationContext(), MusicService.class);
        musicNextIntent.setAction(ACTION_PLAY_NEXT);
        musicNextIntent.putExtra("position", getPositionAtPreviousOrNext(ACTION_PLAY_NEXT));

        PendingIntent musicNextPendingIntent = PendingIntent.getService(getApplicationContext(),
                2, musicNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_next_ib, musicNextPendingIntent);
        // =========================================================================================

        // 종료 버튼을 눌렀을 때 실행하는 작업
        // =========================================================================================
        Intent closeIntent = new Intent(getApplicationContext(), MusicService.class);
        closeIntent.setAction(ACTION_FINISH);

        PendingIntent closePendingIntent = PendingIntent.getService(getApplicationContext(), 3, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_close_ib, closePendingIntent);
        // =========================================================================================

        // Notification 터치 했을 때 실행할 PendingIntent 지정
        // =========================================================================================
        Intent activityStartIntent = new Intent(MainActivity.ACTION_NAME);
        activityStartIntent.putExtra("restore", getCurrentInfo());
        PendingIntent activityStartPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                1,
                activityStartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(activityStartPendingIntent);
        // =========================================================================================

        Bitmap bitmap = null;
        if(mCurrentMusicInfo != null) {
            if(mCurrentMusicInfo.getUri() != null) {
                bitmap = MusicInfoLoadUtil.getBitmap(getApplicationContext(), mCurrentMusicInfo.getUri(), 4);
            }
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_no_image);
        }

        remoteViews.setImageViewBitmap(R.id.noti_album_art_iv, bitmap);
        if(mCurrentMusicInfo != null) {
            remoteViews.setTextViewText(R.id.noti_artist_tv, mCurrentMusicInfo.getArtist());
            remoteViews.setTextViewText(R.id.noti_title_tv, mCurrentMusicInfo.getTitle());
        }

        builder.setContent(remoteViews);
        builder.setLargeIcon(bitmap);
        builder.setSmallIcon(R.mipmap.ic_no_image);

        if(mMediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.noti_play_ib, android.R.drawable.ic_media_pause);
        } else {
            remoteViews.setImageViewResource(R.id.noti_play_ib, android.R.drawable.ic_media_play);
        }

        builder.setAutoCancel(false);

        Notification notification = builder.build();

        startForeground(1, notification);
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT < 21) {
            showNotificationUnderLollipop();
        } else {
            setMetaData();
            showNotificationUpperKitKat(mMetadata);
        }
    }


}
