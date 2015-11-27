package com.massivcode.androidmusicplayer.interfaces;

import com.massivcode.androidmusicplayer.models.MusicInfo;

import java.util.ArrayList;

/**
 * Created by Ray Choe on 2015-11-27.
 */
public class LastPlayedSongs implements Event {

    private MusicInfo currentPlayingMusicInfo;
    private ArrayList<Long> currentPlayingList;

    public MusicInfo getCurrentPlayingMusicInfo() {
        return currentPlayingMusicInfo;
    }

    public void setCurrentPlayingMusicInfo(MusicInfo currentPlayingMusicInfo) {
        this.currentPlayingMusicInfo = currentPlayingMusicInfo;
    }

    public ArrayList<Long> getCurrentPlayingList() {
        return currentPlayingList;
    }

    public void setCurrentPlayingList(ArrayList<Long> currentPlayingList) {
        this.currentPlayingList = currentPlayingList;
    }
}
