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

package com.massivcode.androidmusicplayer.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
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
import com.massivcode.androidmusicplayer.events.Event;
import com.massivcode.androidmusicplayer.events.InitEvent;
import com.massivcode.androidmusicplayer.events.MusicEvent;
import com.massivcode.androidmusicplayer.events.PlayBack;
import com.massivcode.androidmusicplayer.events.Restore;
import com.massivcode.androidmusicplayer.events.SaveState;
import com.massivcode.androidmusicplayer.models.MusicInfo;
import com.massivcode.androidmusicplayer.utils.MusicInfoLoadUtil;

import de.greenrobot.event.EventBus;


public class SongsFragment extends Fragment {

    private static final String TAG = SongsFragment.class.getSimpleName();
    private ListView mListView;
    private SongAdapter mAdapter;

    private SaveState mSaveState;
    private PlayBack mPlayBack;

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

        View header = getActivity().getLayoutInflater().inflate(R.layout.header, null, false);
        header.findViewById(R.id.songs_playAll_btn).setOnClickListener((View.OnClickListener) getActivity());
        mListView.addHeaderView(header);

        if(Build.VERSION.SDK_INT < 23) {
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicInfoLoadUtil.projection, MediaStore.Audio.Media.ARTIST + " != ? AND " + MediaStore.Audio.Media.TITLE + " NOT LIKE '%" + "hangout" + "%'" , new String[]{MediaStore.UNKNOWN_STRING}, null);
            mAdapter = new SongAdapter(getActivity().getApplicationContext(), cursor, true);
            mListView.setAdapter(mAdapter);
        } else {
            if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if(mAdapter == null) {
                    EventBus.getDefault().post(new InitEvent());
                }
            }
        }






        mListView.setOnItemClickListener((AdapterView.OnItemClickListener) getActivity());

        if(mSaveState != null) {
            MusicInfo musicInfo = mSaveState.getMusicInfo();
            MusicEvent musicEvent = new MusicEvent();
            musicEvent.setMusicInfo(musicInfo);
            mAdapter.swapMusicEvent(musicEvent);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new Restore());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    // EventBus 용 이벤트 수신
    public void onEvent(Event event) {

        if (event instanceof MusicEvent) {
//            Log.d(TAG, "노래에서 뮤직이벤트를 받았습니다.");
            mAdapter.swapMusicEvent((MusicEvent) event);
            mAdapter.notifyDataSetChanged();
        } else if(event instanceof PlayBack) {
//            Log.d(TAG, "노래에서 플레이백이벤트를 받았습니다.");

            final PlayBack playback = (PlayBack) event;

            if (mPlayBack == null || mPlayBack.isPlaying() != playback.isPlaying()) {
                mPlayBack = playback;
                mAdapter.swapPlayback((PlayBack) event);
                mAdapter.notifyDataSetChanged();
            }

        } else if(event instanceof SaveState) {
            if(((SaveState) event).getMusicInfo() != null) {
                mSaveState = (SaveState) event;
            }
        } else if(event instanceof InitEvent) {
            Log.d(TAG, "initEvent : SongsFragment");
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicInfoLoadUtil.projection, MediaStore.Audio.Media.ARTIST + " != ? ", new String[]{MediaStore.UNKNOWN_STRING}, null);
            mAdapter = new SongAdapter(getActivity().getApplicationContext(), cursor, true);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }


    }


}
