package com.massivcode.androidmusicplayer.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.SongAdapter;
import com.massivcode.androidmusicplayer.interfaces.Event;
import com.massivcode.androidmusicplayer.interfaces.MusicEvent;
import com.massivcode.androidmusicplayer.interfaces.Playback;
import com.massivcode.androidmusicplayer.utils.MusicInfoUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class SongsFragment extends Fragment {

    private static final String TAG = SongsFragment.class.getSimpleName();
    private ListView mListView;
    private SongAdapter mAdapter;

    public SongsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        mListView = (ListView) view.findViewById(R.id.songs_listView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicInfoUtil.projection, MediaStore.Audio.Media.ARTIST + " != ? ", new String[]{MediaStore.UNKNOWN_STRING}, null);
        mAdapter = new SongAdapter(getActivity().getApplicationContext(), cursor, true);
        View header = getActivity().getLayoutInflater().inflate(R.layout.header, null, false);
        header.findViewById(R.id.songs_playAll_btn).setOnClickListener((View.OnClickListener) getActivity());
        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((AdapterView.OnItemClickListener) getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {

        if (event instanceof MusicEvent) {
            Log.d(TAG, "노래에서 뮤직이벤트를 받았습니다.");
            mAdapter.swapMusicEvent((MusicEvent) event);
            mAdapter.notifyDataSetChanged();
        } else if(event instanceof Playback) {
            Log.d(TAG, "노래에서 플레이백이벤트를 받았습니다.");
            mAdapter.swapPlayback((Playback) event);
            mAdapter.notifyDataSetChanged();
        }

        mListView.setSelection(mAdapter.getCurrentPlayingPosition());

    }
}
