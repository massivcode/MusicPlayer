package com.massivcode.androidmusicplayer.interfaces;

import java.util.ArrayList;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class LastPlayedSongs implements Event {

    private ArrayList<Long> currentPlayingList;
    private int currentPlayingPosition;

    public int getCurrentPlayingPosition() {
        return currentPlayingPosition;
    }

    public void setCurrentPlayingPosition(int currentPlayingPosition) {
        this.currentPlayingPosition = currentPlayingPosition;
    }

    public ArrayList<Long> getCurrentPlayingList() {
        return currentPlayingList;
    }

    public void setCurrentPlayingList(ArrayList<Long> currentPlayingList) {
        this.currentPlayingList = currentPlayingList;
    }
}
