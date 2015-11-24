package com.massivcode.androidmusicplayer.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.massivcode.androidmusicplayer.R;
import com.massivcode.androidmusicplayer.adapters.SongAdapter;
import com.massivcode.androidmusicplayer.util.MusicInfoUtil;

/**
 * Created by Ray Choe on 2015-11-23.
 */
public class SongsFragment extends Fragment {

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

        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicInfoUtil.projection, null, null, null);
        mAdapter = new SongAdapter(getActivity().getApplicationContext(), cursor, true);
        View header = getActivity().getLayoutInflater().inflate(R.layout.header, null, false);
        header.findViewById(R.id.songs_playAll_btn).setOnClickListener((View.OnClickListener) getActivity());
        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((AdapterView.OnItemClickListener) getActivity());
    }
}
