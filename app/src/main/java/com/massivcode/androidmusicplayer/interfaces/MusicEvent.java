package com.massivcode.androidmusicplayer.interfaces;

import com.massivcode.androidmusicplayer.model.MusicInfo;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class MusicEvent implements Event {

   private MusicInfo mMusicInfo;
    private boolean mPlaying;
    private int mCurrentTime;

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
    }

    public void setMusicInfo(MusicInfo mMusicInfo) {
        this.mMusicInfo = mMusicInfo;
    }


    public boolean isPlaying() {
        return mPlaying;
    }

    public void setPlaying(boolean mPlaying) {
        this.mPlaying = mPlaying;
    }

    public int getCurrentTime() {
        return mCurrentTime;
    }

    public void setCurrentTime(int mCurrentTime) {
        this.mCurrentTime = mCurrentTime;
    }
}
