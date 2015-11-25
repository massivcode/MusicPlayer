package com.massivcode.androidmusicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.Playback;
import com.massivcode.androidmusicplayer.model.MusicInfo;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = MusicService.class.getSimpleName();

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT";
    public static final String ACTION_PLAY_PREVIOUS = "ACTION_PLAY_PREVIOUS";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";

    private boolean isReady = false;

    private class UIRefresher extends Thread {
        @Override
        public void run() {
            super.run();
            while(true) {
                if(mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "UIRefresher is running");
                    Playback playback = new Playback();
                    playback.setPlaying(mMediaPlayer.isPlaying());
                    playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
                    EventBus.getDefault().post(playback);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "UIRefresher is stopped");
                    Playback playback = new Playback();
                    playback.setPlaying(mMediaPlayer.isPlaying());
                    playback.setCurrentTime(mMediaPlayer.getCurrentPosition());
                    EventBus.getDefault().post(playback);
                    break;
                }
            }
        }
    }


    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCompletion(MediaPlayer mp) {

        // 현재 노래 재생이 끝났을 경우 호출되는 리스너

        int lastPosition = mp.getCurrentPosition();
        int duration = mp.getDuration();
        Log.d(TAG, "더 빨리 호출됨: " + (duration - lastPosition));

        mp.pause();
        mp.reset();

        try {
            if(mCurrentPosition < getCurrentPlaylistSize()) {
                mCurrentPosition += 1;
                mp.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(mCurrentPosition)));
            } else {
                mp.setDataSource(getApplicationContext(), switchIdToUri(mCurrentPlaylist.get(0)));
            }


            mp.prepare();
            isReady = true;
            mp.start();
            // TODO 프래그먼트들에 메세지 보내기
            sendMusicEvent();
        } catch (IOException e) {
            e.printStackTrace();
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
    private int mCurrentPosition;
    private MusicInfo mCurrentMusicInfo;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "MusicService started");

        // EventBus 등록이 되어서 모든 이벤트를 수신 가능
        EventBus.getDefault().register(this);

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mAction = intent.getAction();

        switch (mAction) {
            case ACTION_PLAY:
                mCurrentPlaylist = (ArrayList<Long>) intent.getSerializableExtra("list");
                mCurrentPosition = intent.getIntExtra("position", 0);
                mCurrentMusicInfo = MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
                break;
            case ACTION_PLAY_NEXT:
            case ACTION_PLAY_PREVIOUS:
                mCurrentPosition = intent.getIntExtra("position", 0);
                break;
        }


        switch (mAction) {
            case ACTION_PLAY:
                if(mMediaPlayer.isPlaying()) {
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
                    }
                }
                break;
            case ACTION_PAUSE:
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    sendAllEvent();
                } else {
                    mMediaPlayer.start();
                    sendAllEvent();
                }
                break;
            case ACTION_PLAY_NEXT:
                if(mMediaPlayer.isPlaying()) {

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
                    }
                }

                break;
            case ACTION_PLAY_PREVIOUS:
                if(mMediaPlayer.isPlaying()) {

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
                    }
                }
                break;
        }




        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;

        // 해제 꼭 해주세요
        EventBus.getDefault().unregister(this);

    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {
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
        if(mMediaPlayer != null) {
            return mMediaPlayer;
        } else {
            return null;
        }
    }

    public MusicInfo getCurrentInfo() {
        if(mCurrentPlaylist != null) {
            Log.d(TAG, "case1");
            return  MusicInfoUtil.getSelectedMusicInfo(getApplicationContext(), mCurrentPlaylist.get(mCurrentPosition));
        } else {
            Log.d(TAG, "case2");
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
        return mCurrentPlaylist.size()-1;
    }

    private Uri switchIdToUri(long id) {
        return Uri.parse("content://media/external/audio/media/" + id);
    }

    private void sendMusicEvent()  {
        MusicEvent musicEvent = new MusicEvent();
        musicEvent.setMusicInfo(getCurrentInfo());
        EventBus.getDefault().post(musicEvent);
    }

    private void sendAllEvent() {
        sendMusicEvent();

        UIRefresher uiRefresher = new UIRefresher();
        uiRefresher.start();
    }
}