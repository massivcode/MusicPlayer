package com.massivcode.androidmusicplayer.events;

/**
 * Created by Ray Choe on 2015-11-24.
 */
public class PlayBack implements Event {

    private boolean mPlaying;
    private int mCurrentTime;

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
