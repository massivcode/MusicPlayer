package com.massivcode.androidmusicplayer.managers;

import android.support.v4.app.Fragment;

import com.massivcode.androidmusicplayer.fragments.ArtistFragment;
import com.massivcode.androidmusicplayer.fragments.PlayerFragment;
import com.massivcode.androidmusicplayer.fragments.PlaylistFragment;
import com.massivcode.androidmusicplayer.fragments.SongsFragment;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class Manager {

    public static final Class[] FRAGMENTS = new Class[] {
            PlayerFragment.class,
            PlaylistFragment.class,
            ArtistFragment.class,
            SongsFragment.class
    };

    private Manager() {}

    public static Fragment getInstance(int position) {
        try {
            return (Fragment)FRAGMENTS[position].newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}